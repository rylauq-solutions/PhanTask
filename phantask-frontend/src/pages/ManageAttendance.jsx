import { Scanner } from "@yudiel/react-qr-scanner";
import { useState, useRef } from "react";
import { apiService } from "../services/api";
import { toast } from "react-hot-toast";

export default function ManageAttendance() {
  const [message, setMessage] = useState("");
  const [state, setState] = useState("");
  const [isScannerActive, setIsScannerActive] = useState(false);
  const [processing, setProcessing] = useState(false);
  const lastScannedToken = useRef("");
  const lastScannedTime = useRef(0);

  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [userId, setUserId] = useState("");
  const [downloadError, setDownloadError] = useState("");
  const [downloading, setDownloading] = useState(false);

  // Success and error sound effects
  const playSuccessSound = () => {
    const audio = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmwhBSuByPDhimMQFWC36+yjTQwOVKrk7bBfHQU7k9byynssBS1+y/DdkD8KFF+16+upVRQKRp/g8r5sIAQpf8jx4YpiEBRctezsok0MDFSq5O2wXhwFO5PW8sp7KwUtesvw3Y9BCRRetuvqqVQUCkaf4PK+ax8EKn/I8eFKYQ8UXLXs7KFNCwxVquTtsF4cBTuT1vLKeSwGLHrL8N2PQQkUXrbs6qhUFApGn+Dyvmw');
    audio.volume = 0.4;
    audio.play().catch(e => console.log('Audio play failed:', e));
  };

  const playErrorSound = () => {
    const audio = new Audio('data:audio/wav;base64,UklGRjIAAABXQVZFZm10IBIAAAABAAEAQB8AAEAfAAABAAgAAABmYWN0BAAAAAAAAABkYXRhAAAAAA==');
    audio.volume = 0.5;
    audio.play().catch(e => console.log('Audio play failed:', e));
  };

  const handleScan = async (result) => {
    if (!result || result.length === 0 || !isScannerActive || processing) return;

    const decodedText = result[0].rawValue;
    const currentTime = Date.now();

    // Prevent duplicate scans within 2 seconds
    if (decodedText === lastScannedToken.current && currentTime - lastScannedTime.current < 2000) {
      return;
    }

    lastScannedToken.current = decodedText;
    lastScannedTime.current = currentTime;
    setProcessing(true);

    console.group("QR Code Scan");
    console.log("Scanned token (first 50 chars):", decodedText.substring(0, 50) + "...");
    console.log("Token length:", decodedText.length);

    try {
      const response = await apiService.markAttendance(decodedText);

      console.log("Success response:", response.data);
      console.groupEnd();

      const successMsg = response.data?.message || "Attendance marked successfully!";
      const username = response.data?.username || "User";

      setMessage(`${successMsg} - ${username}`);
      setState("SUCCESS");
      toast.success(`${username}: ${successMsg}`);
      playSuccessSound();

    } catch (error) {
      console.error("Error response:", error.response?.data);
      console.groupEnd();

      let errorMsg = "Failed to mark attendance";

      if (error.response?.status === 403) {
        errorMsg = "Token expired or invalid. Ask user to refresh their QR code.";
      } else if (error.response?.status === 400) {
        errorMsg = error.response?.data?.error || "Invalid token";
      } else if (error.response?.status === 500) {
        errorMsg = error.response?.data?.error || "Server error";
      } else if (error.response?.data?.error) {
        errorMsg = error.response.data.error;
      }

      setMessage(errorMsg);
      setState("ERROR");
      toast.error(errorMsg);
      playErrorSound();
    } finally {
      setProcessing(false);

      // Clear message after 3 seconds for continuous scanning
      setTimeout(() => {
        setMessage("");
        setState("");
      }, 3000);
    }
  };

  const handleError = (error) => {
    console.error("Scanner error:", error);
    toast.error("Camera error. Please check permissions.");
    playErrorSound();
  };

  const toggleScanner = () => {
    setIsScannerActive(!isScannerActive);
    setMessage("");
    setState("");
    lastScannedToken.current = "";
    lastScannedTime.current = 0;

    if (!isScannerActive) {
      toast.success("Scanner activated");
    } else {
      toast("Scanner deactivated", { icon: "⏸️" });
    }
  };

  const downloadTimesheet = async () => {
    setDownloadError("");

    if (!startDate || !endDate) {
      setDownloadError("Please select start and end dates");
      toast.error("Please select both dates");
      return;
    }

    const start = new Date(startDate);
    const end = new Date(endDate);

    if (start > end) {
      setDownloadError("Start date must be before end date");
      toast.error("Invalid date range");
      return;
    }

    try {
      setDownloading(true);

      const response = await apiService.downloadAttendanceTimesheet({
        startDate,
        endDate,
        userId: userId.trim() || null,
      });

      const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8;' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;

      const filename = userId
        ? `attendance_${userId}_${startDate}_to_${endDate}.csv`
        : `attendance_all_${startDate}_to_${endDate}.csv`;

      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      toast.success("Timesheet downloaded!");
      setDownloadError("");
    } catch (error) {
      console.error("Download error:", error);
      const errorMsg = error.response?.data?.error || "Failed to download";
      setDownloadError(errorMsg);
      toast.error(errorMsg);
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="p-3 md:p-5 space-y-6">
      <div className="mb-3 bg-white/60 backdrop-blur-sm rounded-xl p-4 shadow-sm border border-gray-100">
        <h1 className="text-2xl md:text-3xl font-bold text-center text-amber-950">
          Manage Attendance
        </h1>
      </div>

      <section className="p-4 md:p-6 bg-gradient-to-br from-amber-50 via-orange-50 to-amber-100 rounded-xl shadow-lg border-2 border-amber-200">
        <div className="flex justify-between items-center mb-4">
          <h4 className="text-xl font-bold text-amber-950">
            Scan Attendance QR Code
          </h4>
          <button
            onClick={toggleScanner}
            className={`px-6 py-2 font-bold rounded-lg transition-all shadow-md hover:shadow-lg active:scale-95 ${isScannerActive
              ? "bg-red-600 hover:bg-red-700 text-white"
              : "bg-green-600 hover:bg-green-700 text-white"
              }`}
          >
            {isScannerActive ? "Stop Scanner" : "Start Scanner"}
          </button>
        </div>

        <div className="flex flex-col items-center gap-4">
          {isScannerActive ? (
            <div className="w-full max-w-sm border-4 border-amber-300 rounded-xl overflow-hidden shadow-xl bg-black">
              <Scanner
                onScan={handleScan}
                onError={handleError}
                components={{
                  audio: false,
                  finder: true,
                }}
                constraints={{
                  facingMode: "environment",
                  aspectRatio: 1,
                }}
                formats={["qr_code"]}
                styles={{
                  container: { width: "100%" },
                  video: { width: "100%" }
                }}
                scanDelay={500}
                allowMultiple={true}
              />

              {processing && (
                <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                  <div className="w-16 h-16 border-4 border-white border-t-transparent rounded-full animate-spin"></div>
                </div>
              )}
            </div>
          ) : (
            <div className="w-full max-w-sm aspect-square flex items-center justify-center bg-gradient-to-br from-white to-gray-50 rounded-xl border-4 border-amber-300 shadow-xl">
              <div className="text-center px-6">
                <div className="w-24 h-24 bg-gradient-to-br from-amber-100 to-orange-100 rounded-full flex items-center justify-center mx-auto mb-6 shadow-lg border-2 border-amber-200">
                  <svg className="w-12 h-12 text-amber-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                </div>
                <p className="text-gray-700 font-semibold text-base leading-relaxed">
                  Click 'Start Scanner' to begin scanning QR codes
                </p>
              </div>
            </div>
          )}


          {message && (
            <div
              className={`w-full max-w-sm p-4 rounded-xl font-semibold text-sm shadow-lg ${state === "SUCCESS"
                ? "bg-green-100 text-green-800 border-2 text-center border-green-300"
                : state === "ERROR"
                  ? "bg-red-100 text-red-800 border-2 text-center border-red-300"
                  : "bg-gray-100 text-gray-700 border-2 text-center border-gray-300"
                }`}
            >
              {message}
            </div>
          )}
        </div>
      </section>

      <section className="p-4 md:p-6 bg-gradient-to-br from-amber-50 via-orange-50 to-amber-100 rounded-xl shadow-lg border-2 border-amber-200">
        <h4 className="text-xl font-bold mb-4 text-amber-950">
          Download Attendance Timesheet
        </h4>

        <div className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            <div>
              <label className="text-sm font-semibold text-amber-950 block mb-1.5">
                Start Date
              </label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="w-full px-4 py-2.5 border-2 border-amber-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500 bg-white text-sm font-medium"
              />
            </div>
            <div>
              <label className="text-sm font-semibold text-amber-950 block mb-1.5">
                End Date
              </label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="w-full px-4 py-2.5 border-2 border-amber-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500 bg-white text-sm font-medium"
              />
            </div>
            <div>
              <label className="text-sm font-semibold text-amber-950 block mb-1.5">
                Username (Optional)
              </label>
              <input
                type="text"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                placeholder="Leave empty for all"
                className="w-full px-4 py-2.5 border-2 border-amber-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500 bg-white text-sm font-medium placeholder:text-gray-400"
              />
            </div>
          </div>

          <button
            onClick={downloadTimesheet}
            disabled={downloading}
            className="w-full sm:w-auto px-8 py-3 bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700 disabled:from-green-400 disabled:to-emerald-400 disabled:cursor-not-allowed text-white font-bold rounded-xl transition-all shadow-lg hover:shadow-xl active:scale-95"
          >
            {downloading ? "Downloading..." : "Download CSV"}
          </button>

          {downloadError && (
            <div className="p-3 bg-red-100 text-red-800 rounded-lg border-2 border-red-300 font-semibold text-sm">
              {downloadError}
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
