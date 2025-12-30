import React from 'react';
import { FaCog, FaWrench, FaPaintBrush, FaBell, FaLock, FaUserCog } from 'react-icons/fa';

const Settings = () => {
  // Feature cards data
  const features = [
    {
      icon: <FaPaintBrush className="text-4xl text-purple-600" />,
      title: "Appearance",
      description: "Customize the look and feel of your dashboard"
    },
    {
      icon: <FaCog className="text-4xl text-red-600" />,
      title: "Profile Settings",
      description: "Configure system-wide preferences and defaults"
    },
    {
      icon: <FaWrench className="text-4xl text-orange-600" />,
      title: "Advanced Options",
      description: "Access advanced configuration and integrations"
    }
  ];

  return (
    <div className="min-h-screen p-4 md:p-6">
      {/* Page Header */}
      <div className="mb-6 bg-white/60 rounded-xl p-6 shadow-sm border border-gray-100">
        <div className="flex items-center justify-center gap-3 mb-2">
          <h1 className="text-2xl md:text-3xl font-bold text-amber-950">
            Settings
          </h1>
        </div>
        <p className="text-center text-gray-600 text-sm md:text-base">
          Configure your PhanTask experience
        </p>
      </div>

      {/* Coming Soon Card */}
      <div className="max-w-4xl mx-auto mb-8">
        <div className="bg-gradient-to-br from-amber-50 to-orange-50 border-2 border-amber-200 rounded-xl p-8 md:p-12 shadow-lg text-center">
          <div className="mb-6">
            <div className="inline-flex items-center justify-center w-20 h-20 md:w-24 md:h-24 bg-white rounded-full shadow-md mb-4">
              <FaCog className="text-4xl md:text-5xl text-amber-600 animate-spin-slow" />
            </div>
          </div>

          <h2 className="text-3xl md:text-4xl font-bold text-amber-950 mb-4">
            Coming Soon!
          </h2>

          <p className="text-gray-700 text-base md:text-lg mb-6 max-w-2xl mx-auto">
            We're working hard to bring you comprehensive settings and customization options.
            Stay tuned for exciting updates!
          </p>

          <div className="inline-flex items-center gap-2 px-6 py-3 bg-amber-500 text-white rounded-full font-semibold shadow-md">
            <span className="animate-pulse">🚀</span>
            <span>Under Development</span>
          </div>
        </div>
      </div>

      {/* Feature Preview Grid */}
      <div className="max-w-6xl mx-auto">
        <h3 className="text-xl md:text-2xl font-bold text-amber-950 mb-6 text-center">
          What's Coming Next
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 md:gap-6">
          {features.map((feature, index) => (
            <div
              key={index}
              className="bg-white/80 rounded-xl p-6 shadow-sm border border-gray-200 hover:shadow-lg hover:border-amber-300 transition-all duration-300 transform hover:scale-105"
            >
              <div className="flex flex-col items-center text-center">
                <div className="mb-4 p-4 bg-gray-50 rounded-full">
                  {feature.icon}
                </div>

                <h4 className="text-lg font-bold text-amber-950 mb-2">
                  {feature.title}
                </h4>

                <p className="text-sm text-gray-600">
                  {feature.description}
                </p>

                <div className="mt-4 px-4 py-1 bg-amber-100 text-amber-700 rounded-full text-xs font-semibold">
                  Coming Soon
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Footer Message */}
      <div className="mt-12 text-center">
        <p className="text-gray-500 text-sm">
          Have suggestions for settings features?
          <span className="text-amber-600 font-semibold ml-1 cursor-pointer hover:underline">
            Let us know!
          </span>
        </p>
      </div>

      {/* Custom Animation Styles */}
      <style jsx>{`
        @keyframes spin-slow {
          from {
            transform: rotate(0deg);
          }
          to {
            transform: rotate(360deg);
          }
        }

        .animate-spin-slow {
          animation: spin-slow 10s linear infinite;
        }
      `}</style>
    </div>
  );
};

export default Settings;
