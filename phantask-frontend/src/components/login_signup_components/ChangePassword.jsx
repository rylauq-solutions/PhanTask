import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-hot-toast";

const ChangePassword = () => {
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
        if (formData.oldPassword) {
            toast.error("Please clear old password to generate OTP.");
            return;
        }
        setIsOTPLoading(true);
        setTimeout(() => {
            toast.success("OTP sent to your registered email!");
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

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!formData.oldPassword && !formData.otp) {
            toast.error("Please enter either your old password or OTP.");
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

        // TODO: Add actual API call here for password change

        toast.success("Password changed successfully!");
        setFormData({
            oldPassword: "",
            otp: "",
            newPassword: "",
            confirmPassword: "",
        });
        setOtpSent(false);
        setOtpTimer(0);

        // Navigate to main page
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

            <div className="w-full">
                {/* OTP Block */}
                <label htmlFor="otp" className="block mb-1 font-semibold text-gray-800">
                    OTP (or use old password)
                </label>
                <div className="flex w-full mb-2">
                    <button
                        type="button"
                        className="w-[34%] bg-yellow-700 hover:bg-yellow-800 text-white font-semibold py-2 px-4 rounded-l-lg shadow transition-colors h-full disabled:opacity-50 flex items-center justify-center"
                        onClick={handleOTPRequest}
                        disabled={isOTPLoading || otpTimer > 0 || formData.oldPassword !== ""}
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
                        pattern="\d*"
                        maxLength={6}
                        value={formData.otp}
                        onChange={handleChange}
                        placeholder="Enter 6-digit OTP"
                        className="w-[66%] px-4 py-2 border border-gray-300 rounded-r-lg focus:outline-none"
                        disabled={formData.oldPassword !== ""}
                    />
                </div>

                {/* OR Divider */}
                <div className="flex items-center mb-4">
                    <div className="flex-grow border-t border-gray-500"></div>
                    <span className="mx-3 text-gray-700 font-semibold">OR</span>
                    <div className="flex-grow border-t border-gray-500"></div>
                </div>

                {/* Old Password Field */}
                <label htmlFor="oldPassword" className="block mb-1 font-semibold text-gray-800">
                    Old Password (or use OTP)
                </label>
                <input
                    type="password"
                    id="oldPassword"
                    name="oldPassword"
                    value={formData.oldPassword}
                    onChange={(e) => {
                        handleChange(e);
                        if (e.target.value) {
                            // Clear OTP when old password is typed
                            setFormData((prev) => ({ ...prev, otp: "" }));
                        }
                    }}
                    placeholder="Enter old password"
                    className="w-full mb-4 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
                    disabled={otpSent && otpTimer > 0}
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

export default ChangePassword;
