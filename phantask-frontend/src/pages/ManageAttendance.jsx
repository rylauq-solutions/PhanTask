import { Html5QrcodeScanner } from "html5-qrcode";
import { useEffect, useState } from "react";
import { ATTENDANCE_UI } from "../constants/attendanceUiMessages";

export default function ManageAttendance() {
  /* =======================
     SCANNER STATE
     ======================= */
  const [message, setMessage] = useState("");
  const [state, setState] = useState("");

  /* =======================
     TIMESHEET STATE
     ======================= */
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [userId, setUserId] = useState("");
  const [downloadError, setDownloadError] = useState("");

  /* =======================
     QR SCANNER LOGIC
     ======================= */
  useEffect(() => {
    const scanner = new Html5QrcodeScanner(
      "qr-scanner",
      { fps: 10, qrbox: 250 },
      false
    );

    scanner.render(
      async (decodedText) => {
        try {
          const res = await fetch("/api/attendance/mark", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${sessionStorage.getItem("authToken")}`,
            },
            body: JSON.stringify({ token: decodedText }),
          });

          if (!res.ok) {
            if (res.status === 410) {
              throw new Error("EXPIRED");
            }
            throw new Error("INVALID");
          }

          const data = await res.json();

          // backend-driven UI state
          setState(data.state);
          setMessage(ATTENDANCE_UI[data.state]?.text || "Attendance updated");

          // stop scanner after successful scan
          scanner.clear();

        } catch (e) {
          setState("");
          if (e.message === "EXPIRED") {
            setMessage("QR expired. Ask user to regenerate QR.");
          } else {
            setMessage("Invalid QR. Please scan again.");
          }
        }
      },
      () => {}
    );

    return () => scanner.clear();
  }, []);

  /* =======================
     TIMESHEET DOWNLOAD
     ======================= */
  const downloadTimesheet = async () => {
    setDownloadError("");

    if (!startDate || !endDate) {
      setDownloadError("Please select start and end dates");
      return;
    }

    const body = {
      startDate,
      endDate,
      ...(userId && { userId }),
    };

    try {
      const res = await fetch(
        "/api/admin/attendance/percentage/download",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${sessionStorage.getItem("authToken")}`,
          },
          body: JSON.stringify(body),
        }
      );

      if (!res.ok) {
        throw new Error();
      }

      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);

      const a = document.createElement("a");
      a.href = url;
      a.download = `attendance_${startDate}_to_${endDate}.csv`;
      document.body.appendChild(a);
      a.click();
      a.remove();

      window.URL.revokeObjectURL(url);
    } catch {
      setDownloadError("Unable to download timesheet");
    }
  };

  /* =======================
     UI
     ======================= */
  return (
    <div style={{ padding: 20 }}>
      <h3>Manage Attendance (Admin)</h3>

      {/* ===== QR SCANNER ===== */}
      <section style={{ marginBottom: 30 }}>
        <h4>Scan Attendance QR</h4>

        <div id="qr-scanner" style={{ width: 300 }} />

        {message && (
          <p
            style={{
              marginTop: 10,
              fontWeight: "bold",
              color:
                state === "COMPLETED"
                  ? "green"
                  : state === "CHECKED_IN"
                  ? "orange"
                  : "red",
            }}
          >
            {message}
          </p>
        )}
      </section>

      <hr />

      {/* ===== TIMESHEET DOWNLOAD ===== */}
      <section style={{ marginTop: 30 }}>
        <h4>⬇️ Download Attendance Timesheet</h4>

        <div style={{ display: "flex", gap: 10, marginBottom: 10 }}>
          <input
            type="date"
            onChange={(e) => setStartDate(e.target.value)}
          />
          <input
            type="date"
            onChange={(e) => setEndDate(e.target.value)}
          />
          <input
            placeholder="User ID (optional)"
            onChange={(e) => setUserId(e.target.value)}
          />
        </div>

        <button onClick={downloadTimesheet}>
          Download CSV
        </button>

        {downloadError && (
          <p style={{ color: "red", marginTop: 8 }}>
            {downloadError}
          </p>
        )}
      </section>
    </div>
  );
}
