import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from 'react-hot-toast';

const LoginForm = ({ onLoginSuccess }) => {
  // User's form input state
  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  // Update form state on user input
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // Navigation hook from React Router
  const navigate = useNavigate();

  // Handle login submission for password
  const handleSubmit = (e) => {
    e.preventDefault();

    if (!formData.email) {
      toast.error("Please enter your email to login.");
      return;
    }

    if (!formData.password) {
      toast.error("Please enter your password to login.");
      return;
    }

    // Simulated login result
    const loginSuccessful = true;

    if (loginSuccessful) {
      toast.success("Login successful!");
      sessionStorage.setItem("authToken", "open");
      // Pass email to parent handler for further use
      if (onLoginSuccess) {
        onLoginSuccess(formData.email);
      }
    } else {
      toast.error("Login failed. Check your credentials.");
    }
  };

  // Clear all fields
  const handleReset = () => {
    setFormData({
      email: "",
      password: "",
    });
  };

  // Handler for forgot password click
  const handleForgotPasswordClick = () => {
    navigate("/forgot-password"); // Navigate to forgot password route
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="w-full max-w-md h-auto bg-stone-100 bg-opacity-80 backdrop-blur-sm rounded-xl pt-4 pb-6 px-6 shadow-md flex flex-col"
    >
      {/* Form Heading */}
      <h2 className="text-3xl font-bold text-amber-950 mb-6 text-center">Login</h2>

      <div className="w-full">
        {/* Email Field */}
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

        {/* Password Field */}
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
          className="w-full mb-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
        />
      </div>

      {/* Forgot Password div */}
      <div
        onClick={handleForgotPasswordClick}
        className="mb-6 pr-2 text-sm font-semibold text-red-700 cursor-pointer hover:underline select-none text-right"
      >
        Forgot Password?
      </div>

      {/* Submit and Reset Buttons */}
      <div className="w-full flex mb-1 gap-4 ">
        <button
          type="submit"
          className="hover:scale-95 transition-transform-colors duration-300 flex-1 bg-red-700 hover:bg-red-800 text-white font-semibold py-2 rounded-lg shadow"
        >
          Login
        </button>
        <button
          type="button"
          onClick={handleReset}
          className="hover:scale-95 transition-transform-colors duration-300 flex-1 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 rounded-lg shadow"
        >
          Reset
        </button>
      </div>
    </form>
  );
};

export default LoginForm;
