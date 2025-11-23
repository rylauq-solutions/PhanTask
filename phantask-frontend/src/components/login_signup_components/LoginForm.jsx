import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from 'react-hot-toast';

// ! LoginForm component provides login either through password or OTP, with OTP email sending and toast notifications.
const LoginForm = () => {
  // * State for OTP loading, sent flag, and timer for resend logic
  const [isOTPLoading, setIsOTPLoading] = useState(false);
  const [otpSent, setOtpSent] = useState(false);
  const [otpTimer, setOtpTimer] = useState(0);

  // * User's form input state
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    otp: "",
  });

  // * Update form state on user input
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // ! Handle OTP generation and resend
  const handleOTPRequest = async () => {
    if (!formData.email) {
      // ? Warn: Require email before sending OTP
      toast.error("Please enter your email first!");
      return;
    }
    setIsOTPLoading(true);
    // * Simulate sending OTP (replace this with real API call)
    setTimeout(() => {
      toast.success("OTP sent to your email!");
      setOtpSent(true);
      setOtpTimer(120); // ! 2 minutes cooldown for resend
      setIsOTPLoading(false);
    }, 1000);
  };

  // * Start timer countdown for OTP resend
  useEffect(() => {
    if (!otpTimer) return;
    const interval = setInterval(() => {
      setOtpTimer((prev) => {
        if (prev <= 1) {
          clearInterval(interval); // ! End timer at zero
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(interval);
  }, [otpTimer]);

  // * Navigation hook from React Router
  const navigate = useNavigate();

  // ! Handle login submission for password or OTP
  const handleSubmit = (e) => {
    e.preventDefault();
    // ? Either password or OTP is required
    if (!formData.password && !formData.otp) {
      toast.error("Please enter either your password or OTP to login.");
      return;
    }

    // TODO: Replace this with actual authentication logic (API call)
    const loginSuccessful = true; // * Simulated result

    if (loginSuccessful) {
      toast.success("Login successful!");
      sessionStorage.setItem("authToken", "open");
      navigate("/");
    } else {
      toast.error("Login failed. Check your credentials or OTP.");
    }
  };

  // * Clear all fields
  const handleReset = () => {
    setFormData({
      email: "",
      password: "",
      otp: "",
    });
  };

  //! ---------------------- JSX RETURN BELOW ----------------------
  return (
    <form
      onSubmit={handleSubmit}
      className="w-full max-w-md h-auto bg-stone-100 bg-opacity-80 backdrop-blur-sm rounded-xl pt-4 pb-6 px-6 shadow-md flex flex-col"
    >
      {/* * Form Heading */}
      <h2 className="text-3xl font-bold text-amber-950 mb-6 text-center">Login</h2>

      <div className="w-full">
        {/* * Email Field */}
        <label htmlFor="email" className="block mb-1 font-semibold text-gray-800">
          Email
        </label>
        <input
          type="email"
          id="email"
          name="email"
          required
          value={formData.email}
          onChange={handleChange}
          placeholder="you@example.com"
          className="w-full mb-4 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
        />

        {/* * Password Field */}
        <label htmlFor="password" className="block mb-1 font-semibold text-gray-800">
          Password
        </label>
        <input
          type="password"
          id="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
          placeholder="Enter password"
          className="w-full mb-4 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
        />

        {/* ! OR Divider */}
        <div className="flex items-center">
          <div className="flex-grow border-t border-gray-500"></div>
          <span className="mx-3 text-gray-700 font-semibold">OR</span>
          <div className="flex-grow border-t border-gray-500"></div>
        </div>

        {/* * OTP Block: Generate/Resend + Input */}
        <label htmlFor="otp" className="block mb-1 font-semibold text-gray-800">
          OTP
        </label>
        <div className="flex w-full mb-6">
          <button
            type="button"
            className="w-[34%] bg-yellow-700 hover:bg-yellow-800 text-white font-semibold py-2 px-4 rounded-l-lg shadow transition-colors h-full disabled:opacity-50"
            onClick={handleOTPRequest}
            disabled={isOTPLoading || otpTimer > 0}
          >
            {otpSent && otpTimer > 0
              ? `Resend (${otpTimer}s)`
              : (otpSent ? "Resend" : "Generate")}
          </button>
          <input
            type="text"
            id="otp"
            name="otp"
            inputMode="numeric"
            pattern="\d*"
            maxLength={6}
            value={formData.otp}
            onChange={handleChange}
            placeholder="Enter 6-digit OTP"
            className="w-[66%] px-4 py-2 border border-gray-300 rounded-r-lg focus:outline-none"
          />
        </div>
      </div>

      {/* * Submit and Reset Buttons */}
      <div className="w-full flex gap-4 ">
        <button
          type="submit"
          className="flex-1 bg-red-700 hover:bg-red-800 text-white font-semibold py-2 rounded-lg shadow transition-colors"
        >
          Submit
        </button>
        <button
          type="button"
          onClick={handleReset}
          className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 rounded-lg shadow transition-colors"
        >
          Reset
        </button>
      </div>
    </form>
  );
};

export default LoginForm;
