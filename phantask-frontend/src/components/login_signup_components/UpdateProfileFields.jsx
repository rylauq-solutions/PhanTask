import React, { useState } from "react";
import { toast } from "react-hot-toast";
import { apiService } from "../../services/api";

const UpdateProfileFields = ({ user, onProfileUpdated }) => {
    const [isLoading, setIsLoading] = useState(false);
    const [profileData, setProfileData] = useState({
        fullName: user?.fullName || "",
        department: user?.department || "",
        phone: user?.phone || "",
        yearOfStudy: user?.yearOfStudy || "",
        dob: user?.dob || ""
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProfileData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const formDataToSend = new FormData();
            formDataToSend.append("fullName", profileData.fullName || "");
            formDataToSend.append("department", profileData.department || "");
            formDataToSend.append("phone", profileData.phone || "");
            formDataToSend.append("yearOfStudy", profileData.yearOfStudy || "");
            formDataToSend.append("dob", profileData.dob || "");

            // Get username from user prop or sessionStorage
            const username = user?.username || sessionStorage.getItem("username");

            // Use public first-login endpoint (no auth required)
            await apiService.updateProfileFirstLogin(formDataToSend, username);
            toast.success("Profile updated successfully!");

            // Call the callback to move to next step (Change Password)
            if (onProfileUpdated) {
                onProfileUpdated();
            }
        } catch (error) {
            console.error("Failed to update profile", error);
            toast.error(error.response?.data?.message || "Failed to update profile. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    const handleReset = () => {
        setProfileData({
            fullName: user?.fullName || "",
            department: user?.department || "",
            phone: user?.phone || "",
            yearOfStudy: user?.yearOfStudy || "",
            dob: user?.dob || ""
        });
    };

    return (
        <form
            onSubmit={handleSubmit}
            className="w-full max-w-md h-auto bg-stone-100 bg-opacity-80 backdrop-blur-sm rounded-xl pt-4 pb-6 px-6 shadow-md flex flex-col"
        >
            <h2 className="text-3xl font-bold text-amber-950 mb-6 text-center">Update Profile</h2>

            <label htmlFor="fullName" className="block mb-1 font-semibold text-gray-800">
                Full Name
            </label>
            <input
                type="text"
                id="fullName"
                name="fullName"
                value={profileData.fullName}
                onChange={handleChange}
                placeholder="Enter your full name"
                className="w-full mb-4 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
                required
            />

            <label htmlFor="department" className="block mb-1 font-semibold text-gray-800">
                Department
            </label>
            <input
                type="text"
                id="department"
                name="department"
                value={profileData.department}
                onChange={handleChange}
                placeholder="Enter your department"
                className="w-full mb-4 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
                required
            />

            <label htmlFor="phone" className="block mb-1 font-semibold text-gray-800">
                Phone
            </label>
            <input
                type="text"
                id="phone"
                name="phone"
                value={profileData.phone}
                onChange={handleChange}
                placeholder="Enter your phone number"
                className="w-full mb-4 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
                required
            />

            <label htmlFor="yearOfStudy" className="block mb-1 font-semibold text-gray-800">
                Year of Study
            </label>
            <input
                type="text"
                id="yearOfStudy"
                name="yearOfStudy"
                value={profileData.yearOfStudy}
                onChange={handleChange}
                placeholder="Enter your year of study"
                className="w-full mb-4 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
            />

            <label htmlFor="dob" className="block mb-1 font-semibold text-gray-800">
                Date of Birth
            </label>
            <input
                type="date"
                id="dob"
                name="dob"
                value={profileData.dob}
                onChange={handleChange}
                className="w-full mb-4 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
            />

            <div className="w-full flex gap-4">
                <button
                    type="submit"
                    disabled={isLoading}
                    className="hover:scale-95 transition-transform-colors duration-300 flex-1 bg-red-700 hover:bg-red-800 text-white font-semibold py-2 rounded-lg shadow disabled:opacity-50"
                >
                    {isLoading ? 'Updating...' : 'Update'}
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

export default UpdateProfileFields;
