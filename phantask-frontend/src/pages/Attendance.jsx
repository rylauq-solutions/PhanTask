import React, { useEffect, useState, useRef } from 'react';
import QRCode from 'react-qr-code';
import { useAuth } from '../context/AuthContext';
import { apiService } from '../services/api';
import { toast } from 'react-hot-toast';

const Attendance = () => {
  const { refreshProfile } = useAuth();
  const [loading, setLoading] = useState(false);
  const [qrToken, setQrToken] = useState(null); // Start with null
  const [isRegistering, setIsRegistering] = useState(true);

  useEffect(() => {
    // Get token immediately when component mounts
    const currentToken = sessionStorage.getItem('authToken') || sessionStorage.getItem('testToken');

    if (!currentToken) {
      setIsRegistering(false);
      return;
    }

    // Set token immediately
    setQrToken(currentToken);

    // Register it with backend
    const registerToken = async () => {
      try {
        console.log('Registering token:', currentToken.substring(0, 50));
        await apiService.registerAttendanceToken(currentToken);
        console.log('Token registered successfully');
        setIsRegistering(false);
      } catch (err) {
        console.error('Token registration failed:', err);
        setIsRegistering(false);
      }
    };

    registerToken();
  }, []); // Only run once on mount

  const handleRefresh = async () => {
    setLoading(true);
    setIsRegistering(true);

    try {
      // Force refresh the auth profile
      await refreshProfile();

      // Get the NEW token after refresh
      const newToken = sessionStorage.getItem('authToken') || sessionStorage.getItem('testToken');

      if (!newToken) {
        throw new Error('No token after refresh');
      }

      // Update the QR token
      setQrToken(newToken);

      // Register the new token
      console.log('Registering new token:', newToken.substring(0, 50));
      await apiService.registerAttendanceToken(newToken);
      console.log('New token registered successfully');

      toast.success('QR code refreshed!', {
        duration: 2000,
        position: 'top-center',
      });
    } catch (err) {
      console.error('Refresh failed:', err);
      toast.error('Failed to refresh', {
        duration: 2000,
        position: 'top-center',
      });
    } finally {
      setLoading(false);
      setIsRegistering(false);
    }
  };

  // Loading state while registering
  if (isRegistering && !qrToken) {
    return (
      <div className="rounded-xl min-h-screen bg-gradient-to-br from-[#3d1f1f] via-[#4a2525] to-[#2d1515] flex items-center justify-center p-3 sm:p-5">
        <div className="bg-gradient-to-br from-amber-50 via-orange-50 to-amber-100 border-2 border-amber-200 rounded-2xl p-5 sm:p-8 shadow-2xl text-center w-full max-w-[95%] sm:max-w-md">
          <div className="flex flex-col items-center gap-3 sm:gap-4">
            <div className="animate-spin w-14 h-14 border-4 border-amber-600 border-t-transparent rounded-full"></div>
            <p className="text-amber-900 text-base sm:text-lg font-semibold">
              Generating QR Code...
            </p>
          </div>
        </div>
      </div>
    );
  }

  // No token available
  if (!qrToken) {
    return (
      <div className="rounded-xl min-h-screen bg-gradient-to-br from-[#3d1f1f] via-[#4a2525] to-[#2d1515] flex items-center justify-center p-3 sm:p-5">
        <div className="bg-gradient-to-br from-amber-50 via-orange-50 to-amber-100 border-2 border-red-900/30 rounded-2xl p-5 sm:p-8 shadow-2xl text-center w-full max-w-[95%] sm:max-w-md">
          <div className="flex flex-col items-center gap-3 sm:gap-4">
            <div className="w-14 h-14 sm:w-16 sm:h-16 bg-red-500/10 rounded-full flex items-center justify-center border-2 border-red-500/20">
              <span className="text-3xl sm:text-4xl">⚠️</span>
            </div>
            <div>
              <p className="text-red-700 text-base sm:text-lg font-semibold mb-2">
                No Authentication Token Found
              </p>
              <p className="text-gray-600 text-xs sm:text-sm">
                Please log in again
              </p>
            </div>
            <button
              onClick={() => window.location.href = '/login'}
              className="w-full px-5 py-2.5 sm:px-6 sm:py-3 bg-gradient-to-r from-orange-600 to-amber-600 hover:from-orange-700 hover:to-amber-700 text-white text-sm sm:text-base font-semibold rounded-lg transition-all shadow-lg hover:shadow-xl active:scale-95"
            >
              Go to Login
            </button>
          </div>
        </div>
      </div>
    );
  }

  // Show QR Code
  return (
    <div className="rounded-xl min-h-screen bg-gradient-to-br from-[#3d1f1f] via-[#4a2525] to-[#2d1515] flex items-center justify-center p-3 sm:p-5">
      <div className="bg-gradient-to-br from-amber-50 via-orange-50 to-amber-100 border-2 border-amber-200 rounded-2xl p-5 sm:p-8 shadow-2xl w-full max-w-[95%] sm:max-w-md">
        <div className="flex flex-col items-center justify-center space-y-4 sm:space-y-6">
          <div className="text-center">
            <h2 className="text-xl sm:text-2xl font-bold text-amber-950 mb-1.5 sm:mb-2">
              Attendance QR Code
            </h2>
            <p className="text-gray-700 text-xs sm:text-sm">
              Scan this QR code to mark your attendance
            </p>
          </div>

          <div className="bg-white p-4 sm:p-6 rounded-3xl shadow-xl border-2 border-amber-200 w-full flex items-center justify-center">
            <div className="w-full max-w-[240px] aspect-square flex items-center justify-center">
              {isRegistering ? (
                <div className="animate-pulse text-amber-600">Updating...</div>
              ) : (
                <QRCode
                  value={qrToken}
                  size={240}
                  style={{
                    height: "auto",
                    maxWidth: "100%",
                    width: "100%"
                  }}
                />
              )}
            </div>
          </div>

          <div className="text-center space-y-2.5 sm:space-y-3 w-full">
            <div className="bg-yellow-500/10 border border-yellow-600/30 rounded-lg p-2">
              <p className="text-yellow-700 text-xs font-medium">
                QR code valid for limited time
              </p>
            </div>

            <button
              onClick={handleRefresh}
              disabled={loading || isRegistering}
              className="w-full px-5 py-2.5 sm:px-6 sm:py-3 bg-gradient-to-r from-orange-600 to-amber-600 hover:from-orange-700 hover:to-amber-700 disabled:from-orange-400 disabled:to-amber-400 disabled:cursor-not-allowed text-white text-sm sm:text-base font-semibold rounded-lg transition-all shadow-lg hover:shadow-xl active:scale-95 disabled:transform-none flex items-center justify-center gap-2"
            >
              <span>{loading ? 'Refreshing...' : 'Refresh QR Code'}</span>
            </button>

            <div className="pt-2 border-t border-amber-200">
              <p className="text-gray-600 text-[10px] sm:text-xs leading-relaxed">
                Make sure the QR code is clearly visible when scanning
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Attendance;
