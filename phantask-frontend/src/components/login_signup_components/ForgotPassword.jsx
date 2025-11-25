import React, { useState, useEffect } from "react";
import { toast } from "react-hot-toast";
import { useNavigate } from "react-router-dom";
import wallpaper from '../../assets/wallpaper-1.jpg';

const ForgotPassword = () => {
    const [isOTPLoading, setIsOTPLoading] = useState(false);
    const [otpSent, setOtpSent] = useState(false);
    const [otpTimer, setOtpTimer] = useState(0);

    const [formData, setFormData] = useState({
        email: "",
        otp: "",
        newPassword: "",
        confirmPassword: "",
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleOTPRequest = () => {
        if (!formData.email) {
            toast.error("Please enter your email to generate OTP.");
            return;
        }
        setIsOTPLoading(true);
        setTimeout(() => {
            toast.success(`OTP sent to ${formData.email}!`);
            setOtpSent(true);
            setOtpTimer(120);
            setIsOTPLoading(false);
        }, 1000);
    };

    useEffect(() => {
        if (otpTimer === 0) return;

        const interval = setInterval(() => {
            setOtpTimer((prev) => {
                if (prev <= 1) {
                    clearInterval(interval);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);
        return () => clearInterval(interval);
    }, [otpTimer]);

    const navigate = useNavigate();

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!formData.email) {
            toast.error("Email is required.");
            return;
        }
        if (!formData.otp) {
            toast.error("Please enter OTP.");
            return;
        }
        if (!formData.newPassword || !formData.confirmPassword) {
            toast.error("Please fill in new password and confirm password.");
            return;
        }
        if (formData.newPassword !== formData.confirmPassword) {
            toast.error("New password and confirm password do not match.");
            return;
        }

        // TODO: Add actual forgot password API call

        toast.success("Password reset successfully!");
        setFormData({
            email: "",
            otp: "",
            newPassword: "",
            confirmPassword: "",
        });
        setOtpSent(false);
        setOtpTimer(0);

        navigate("/login");
    };

    const handleReset = () => {
        setFormData({
            email: "",
            otp: "",
            newPassword: "",
            confirmPassword: "",
        });
        setOtpSent(false);
        setOtpTimer(0);
    };

    return (
        <div
            className="max-w-screen min-h-screen overflow-x-hidden bg-cover bg-center bg-no-repeat flex justify-center items-center p-4 sm:p-8"
            style={{ backgroundImage: `url(${wallpaper})` }}
        >
            <form
                onSubmit={handleSubmit}
                className="w-full max-w-md h-auto bg-stone-100 bg-opacity-80 backdrop-blur-sm rounded-xl pt-4 pb-6 px-6 shadow-md flex flex-col"
            >
                <h2 className="text-3xl font-bold text-amber-950 mb-6 text-center">Forgot Password</h2>

                <div className="w-full">
                    {/* Email Field */}
                    <label htmlFor="email" className="block mb-1 font-semibold text-gray-800">
                        Email
                    </label>
                    <input
                        type="email"
                        id="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        placeholder="you@example.com"
                        className="w-full mb-4 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
                        required
                        disabled={otpSent && otpTimer > 0}
                    />

                    {/* OTP Block */}
                    <label htmlFor="otp" className="block mb-1 font-semibold text-gray-800">
                        OTP
                    </label>
                    <div className="flex w-full mb-6">
                        <button
                            type="button"
                            className="transition-transform-colors duration-300 w-[34%] bg-yellow-700 hover:bg-yellow-800 text-white font-semibold py-2 px-4 rounded-l-lg shadow h-full disabled:opacity-50 flex items-center justify-center"
                            onClick={handleOTPRequest}
                            disabled={isOTPLoading || otpTimer > 0 || !formData.email}
                        >
                            {otpSent && otpTimer > 0
                                ? `Resend (${otpTimer}s)`
                                : otpSent
                                    ? "Resend"
                                    : "Generate"}
                        </button>
                        <input
                            type="text"
                            id="otp"
                            name="otp"
                            inputMode="numeric"
                            maxLength={6}
                            value={formData.otp}
                            onChange={handleChange}
                            placeholder="Enter 6-digit OTP"
                            className="w-[66%] px-4 py-2 border border-gray-300 rounded-r-lg focus:outline-none"
                        />
                    </div>

                    {/* New Password Field */}
                    <label htmlFor="newPassword" className="block mb-1 font-semibold text-gray-800">
                        New Password
                    </label>
                    <input
                        type="password"
                        id="newPassword"
                        name="newPassword"
                        value={formData.newPassword}
                        onChange={handleChange}
                        placeholder="Enter new password"
                        className="w-full mb-4 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
                    />

                    {/* Confirm Password Field */}
                    <label htmlFor="confirmPassword" className="block mb-1 font-semibold text-gray-800">
                        Confirm Password
                    </label>
                    <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        placeholder="Confirm new password"
                        className="w-full mb-6 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
                    />
                </div>

                {/* Submit and Reset Buttons */}
                <div className="w-full flex gap-4 ">
                    <button
                        type="submit"
                        className="hover:scale-95 transition-transform-colors duration-300 flex-1 bg-red-700 hover:bg-red-800 text-white font-semibold py-2 rounded-lg shadow"
                    >
                        Submit
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
        </div>
    );
};

export default ForgotPassword;
