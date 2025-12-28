import React from 'react';
import QRCode from 'react-qr-code';
import { useAuth } from '../context/AuthContext';

const Attendance = () => {
  const { refreshProfile } = useAuth();

  const authToken =
    sessionStorage.getItem('authToken') ||
    sessionStorage.getItem('testToken');

  const handleRefresh = async () => {
    await refreshProfile();
  };

  if (!authToken) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 flex items-center justify-center p-5">
        <div className="bg-gray-800 border-2 border-gray-700 rounded-xl p-8 shadow-2xl text-center">
          <p className="text-red-400 text-lg font-semibold mb-4">
            ⚠️ No auth token found
          </p>
          <button
            onClick={handleRefresh}
            className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition-colors shadow-lg"
          >
            Refresh Token
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 flex items-center justify-center p-5">
      <div className="bg-gray-800 border-2 border-gray-700 rounded-xl p-8 shadow-2xl max-w-md w-full">
        <div className="flex flex-col items-center justify-center space-y-6">
          {/* Header */}
          <div className="text-center">
            <h2 className="text-2xl font-bold text-white mb-2">
              📱 Attendance QR Code
            </h2>
            <p className="text-gray-400 text-sm">
              Scan this QR code to mark your attendance
            </p>
          </div>

          {/* QR Code Container */}
          <div className="bg-white p-6 rounded-lg shadow-xl">
            <QRCode value={authToken} size={220} />
          </div>

          {/* Footer Info */}
          <div className="text-center space-y-3 w-full">
            <p className="text-gray-400 text-xs">
              ⏰ QR code valid for limited time
            </p>

            {/* Refresh Button */}
            <button
              onClick={handleRefresh}
              className="w-full px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white font-semibold rounded-lg transition-all shadow-lg hover:shadow-xl transform hover:scale-105 flex items-center justify-center gap-2"
            >
              <span>🔄</span>
              <span>Refresh QR Code</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Attendance;
