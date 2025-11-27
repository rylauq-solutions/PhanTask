import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-hot-toast";

const ChangePassword = ({ onPasswordChanged, userEmail }) => {
    const navigate = useNavigate();

    const [isOTPLoading, setIsOTPLoading] = useState(false);
    const [otpSent, setOtpSent] = useState(false);
    const [otpTimer, setOtpTimer] = useState(0);

    const [formData, setFormData] = useState({
        oldPassword: "",
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
        if (!userEmail) {
            toast.error("User email not available. Please login again.");
            return;
        }

        if (otpSent && otpTimer > 0) {
            toast.error("Please wait for timer to expire before resending OTP.");
            return;
        }

        setIsOTPLoading(true);
        setTimeout(() => {
            toast.success(`OTP sent to ${userEmail}!`);
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

    const validatePassword = (password) => {
        const minLength = 8;
        const hasUpper = /[A-Z]/.test(password);
        const hasNumber = /\d/.test(password);
        return password.length >= minLength && hasUpper && hasNumber;
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!formData.oldPassword || !formData.otp) {
            toast.error("Both Old Password and OTP are required.");
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
        if (!validatePassword(formData.newPassword)) {
            toast.error("Password must be at least 8 characters long, with uppercase letters and numbers.");
            return;
        }

        // TODO: Real API call with: { email: userEmail, oldPassword, otp, newPassword }
        console.log("API Payload:", { email: userEmail, ...formData });

        toast.success("Password changed successfully!");
        setFormData({
            oldPassword: "",
            otp: "",
            newPassword: "",
            confirmPassword: "",
        });
        setOtpSent(false);
        setOtpTimer(0);

        if (onPasswordChanged) {
            onPasswordChanged();
        }
        navigate("/");
    };

    const handleReset = () => {
        setFormData({
            oldPassword: "",
            otp: "",
            newPassword: "",
            confirmPassword: "",
        });
        setOtpSent(false);
        setOtpTimer(0);
    };

    return (
        <form
            onSubmit={handleSubmit}
            className="w-full max-w-md h-auto bg-stone-100 bg-opacity-80 backdrop-blur-sm rounded-xl pt-4 pb-6 px-6 shadow-md flex flex-col"
        >
            <h2 className="text-3xl font-bold text-amber-950 mb-6 text-center">Change Password</h2>

            {/* Show email for context */}
            {userEmail && (
                <div className="mb-4 px-3 py-2 bg-blue-100 border border-blue-300 rounded-lg text-xs sm:text-sm text-blue-800 text-center break-words max-w-full">
                    <span className="font-medium">Verifying:</span> <span className="block sm:inline">{userEmail}</span>
                </div>

            )}

            {/* Old Password Field */}
            <label htmlFor="oldPassword" className="block mb-1 font-semibold text-gray-800">
                Old Password
            </label>
            <input
                type="password"
                id="oldPassword"
                name="oldPassword"
                value={formData.oldPassword}
                onChange={handleChange}
                placeholder="Enter old password"
                className="w-full mb-4 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
                required
            />

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
                required
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
                className="w-full mb-4 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
                required
            />

            {/* OTP Block */}
            <label htmlFor="otp" className="block mb-1 font-semibold text-gray-800">
                OTP
            </label>
            <div className="flex w-full mb-6">
                <button
                    type="button"
                    className="duration-300 w-[34%] 
             bg-yellow-700 hover:bg-yellow-800 text-white font-semibold 
             px-4 py-2 md:py-2.5 rounded-l-lg shadow disabled:opacity-50 
             flex items-center justify-center text-xs sm:text-sm border border-gray-300 border-r-1 "
                    onClick={handleOTPRequest}
                    disabled={isOTPLoading || (otpSent && otpTimer > 0)}
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
                    className="w-[66%] px-4 py-2 border border-gray-300 border-l-0 rounded-r-lg focus:outline-none "
                    required
                />
            </div>

            {/* Submit and Reset Buttons */}
            <div className="w-full flex gap-4">
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
    );
};

export default ChangePassword;
