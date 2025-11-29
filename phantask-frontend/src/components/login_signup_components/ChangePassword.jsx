import React, { useState } from "react";
import { toast } from "react-hot-toast";
import { useApi } from "../../context/ApiContext";

const ChangePassword = ({ onPasswordChanged, username }) => {
    const [isLoading, setIsLoading] = useState(false);
    const [formData, setFormData] = useState({
        oldPassword: "",
        newPassword: "",
        confirmPassword: "",
    });
    const api = useApi();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const validatePassword = (password) => {
        const minLength = 8;
        const hasUpper = /[A-Z]/.test(password);
        const hasNumber = /\d/.test(password);
        return password.length >= minLength && hasUpper && hasNumber;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (formData.newPassword !== formData.confirmPassword) {
            toast.error("New password and confirm password do not match.");
            return;
        }
        if (!validatePassword(formData.newPassword)) {
            toast.error("Password must be at least 8 characters long, with uppercase letters and numbers.");
            return;
        }
        if (!username) {
            toast.error("Username not available. Please login again.");
            return;
        }

        setIsLoading(true);

        try {
            await api.changePasswordFirstLogin(formData.oldPassword, formData.newPassword, username);
            toast.success("Password changed successfully!");

            setFormData({
                oldPassword: "",
                newPassword: "",
                confirmPassword: "",
            });

            if (onPasswordChanged) {
                onPasswordChanged();
            }
        } catch (error) {
            toast.error(error.response?.data?.message || 'Password change failed');
        } finally {
            setIsLoading(false);
        }
    };

    const handleReset = () => {
        setFormData({
            oldPassword: "",
            newPassword: "",
            confirmPassword: "",
        });
    };

    return (
        <form
            onSubmit={handleSubmit}
            className="w-full max-w-md h-auto bg-stone-100 bg-opacity-80 backdrop-blur-sm rounded-xl pt-4 pb-6 px-6 shadow-md flex flex-col"
        >
            <h2 className="text-3xl font-bold text-amber-950 mb-6 text-center">Change Password</h2>

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

            <label htmlFor="newPassword" className="block mb-1 font-semibold text-gray-800">
                New Password
            </label>
            <input
                type="password"
                id="newPassword"
                name="newPassword"
                value={formData.newPassword}
                onChange={handleChange}
                placeholder="Enter new password (8+ chars, uppercase, number)"
                className="w-full mb-4 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
                required
            />

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

            <div className="w-full flex gap-4">
                <button
                    type="submit"
                    disabled={isLoading}
                    className="hover:scale-95 transition-transform-colors duration-300 flex-1 bg-red-700 hover:bg-red-800 text-white font-semibold py-2 rounded-lg shadow disabled:opacity-50"
                >
                    {isLoading ? 'Updating...' : 'Submit'}
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
