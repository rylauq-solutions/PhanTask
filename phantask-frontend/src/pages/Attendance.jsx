import React, { useState, useEffect } from "react";
import QRCode from "react-qr-code";

const Attendance = () => {
  const authToken =
    sessionStorage.getItem("authToken") ||
    sessionStorage.getItem("testToken");

  const refreshToken = useCallback(() => {
    const newToken = sessionStorage.getItem('authToken') || sessionStorage.getItem('testToken');
    setAuthToken(newToken);
  }, []);

  const [registered, setRegistered] = useState(false);

  useEffect(() => {
    if (!authToken) return;

    // Register JWT-based QR with backend
    fetch("/api/attendance/token/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${authToken}`,
      },
      body: JSON.stringify({ token: authToken }),
    }).then(() => setRegistered(true));
  }, [authToken]);

  //fallback if no token found
  if (!authToken) {
    return (
      <div>
        <p>No auth token found</p>
        <button onClick={refreshToken}>Refresh</button>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center">
      <p>Scan this QR to mark attendance</p>
      <br />
      {registered && <QRCode value={authToken} size={200} />}
      <br />
      <p style={{ fontSize: "12px" }}>
        QR valid for limited time
      </p>
      <br />
      <button onClick={refreshToken}>🔄 Refresh QR</button>
    </div>
  );
};

export default Attendance;

