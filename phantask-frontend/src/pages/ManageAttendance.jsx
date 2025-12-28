import { Scanner } from "@yudiel/react-qr-scanner";
import { useState } from "react";

export default function ManageAttendance() {
  /* =======================
     SCANNER STATE
     ======================= */
  const [message, setMessage] = useState("");
  const [state, setState] = useState("");
  const [isScanning, setIsScanning] = useState(true);
  const [scannedToken, setScannedToken] = useState("");

  /* =======================
     TIMESHEET STATE
     ======================= */
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [userId, setUserId] = useState("");
  const [downloadError, setDownloadError] = useState("");

  /* =======================
     QR SCANNER LOGIC - OPTIMIZED
     ======================= */
  const handleScan = (result) => {
    if (!result || result.length === 0 || !isScanning) return;

    const decodedText = result[0].rawValue;

    // Prevent duplicate scans
    if (decodedText === scannedToken) return;

    // Stop scanning immediately after successful read
    setIsScanning(false);
    setScannedToken(decodedText);

    // Display the scanned token
    setMessage(`Scanned Token: ${decodedText.substring(0, 20)}...`);
    setState("SUCCESS");

    // Here you would normally call your API
    console.log("Scanned:", decodedText);
  };

  const handleError = (error) => {
    console.error("Scanner error:", error);
    setMessage("Scanner error. Please check camera permissions.");
    setState("ERROR");
  };

  const resetScanner = () => {
    setIsScanning(true);
    setScannedToken("");
    setMessage("");
    setState("");
  };

  /* =======================
     TIMESHEET DOWNLOAD
     ======================= */
  const downloadTimesheet = () => {
    setDownloadError("");

    if (!startDate || !endDate) {
      setDownloadError("Please select start and end dates");
      return;
    }

    console.log("Downloading timesheet:", {
      startDate,
      endDate,
      userId: userId || "all users",
    });

    setDownloadError("");
    alert(`Would download timesheet from ${startDate} to ${endDate}`);
  };

  /* =======================
     UI
     ======================= */
  return (
    <div className="p-5">
      <h3 className="text-2xl font-bold mb-6 text-gray-800">
        Manage Attendance (Admin)
      </h3>

      {/* ===== QR SCANNER ===== */}
      <section className="mb-8 p-5 bg-white rounded-lg shadow-md">
        <h4 className="text-xl font-semibold mb-4 text-gray-700">
          📷 Scan Attendance QR
        </h4>

        <div className="flex flex-col items-center">
          {isScanning ? (
            <div className="w-80 max-w-full border-4 border-gray-300 rounded-lg overflow-hidden">
              <Scanner
                onScan={handleScan}
                onError={handleError}
                // PERFORMANCE OPTIMIZATIONS
                components={{
                  audio: false, // Disable audio feedback
                  finder: true, // Show finder box for better targeting
                }}
                constraints={{
                  facingMode: "environment", // Use back camera
                  aspectRatio: 1, // Square aspect ratio for better QR detection
                }}
                formats={[
                  // Only scan QR codes, not all barcode formats
                  "qr_code"
                ]}
                styles={{
                  container: { width: "100%" },
                  video: { width: "100%" }
                }}
                // Additional performance settings
                scanDelay={100} // Scan every 100ms (faster than default)
                allowMultiple={false} // Only detect one code at a time
              />
            </div>
          ) : (
            <div className="w-80 h-80 flex items-center justify-center bg-gray-100 rounded-lg border-4 border-gray-300">
              <p className="text-gray-500 text-center px-4">
                Scanner paused. Click "Scan Again" to continue.
              </p>
            </div>
          )}

          {message && (
            <div
              className={`mt-4 p-3 rounded-lg font-semibold text-center max-w-md ${state === "SUCCESS"
                  ? "bg-green-100 text-green-700"
                  : state === "ERROR"
                    ? "bg-red-100 text-red-700"
                    : "bg-gray-100 text-gray-700"
                }`}
            >
              {message}
            </div>
          )}

          {scannedToken && (
            <div className="mt-4 w-80 max-w-full">
              <button
                onClick={resetScanner}
                className="w-full py-2 px-4 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition-colors"
              >
                Scan Again
              </button>
            </div>
          )}
        </div>
      </section>

      <hr className="my-8 border-gray-300" />

      {/* ===== TIMESHEET DOWNLOAD ===== */}
      <section className="p-5 bg-white rounded-lg shadow-md">
        <h4 className="text-xl font-semibold mb-4 text-gray-700">
          ⬇️ Download Attendance Timesheet
        </h4>

        <div className="flex flex-wrap gap-3 mb-4">
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="Start Date"
          />
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="End Date"
          />
          <input
            type="text"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            placeholder="User ID (optional)"
            className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 flex-1 min-w-[200px]"
          />
        </div>

        <button
          onClick={downloadTimesheet}
          className="px-6 py-2 bg-green-600 hover:bg-green-700 text-white font-semibold rounded-lg transition-colors"
        >
          Download CSV
        </button>

        {downloadError && (
          <p className="mt-3 text-red-600 font-medium">{downloadError}</p>
        )}
      </section>
    </div>
  );
}
