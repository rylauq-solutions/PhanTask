import React, { useState } from "react";
import mascot from "../assets/Mascot-Phantask.png";
import { useNavigate } from "react-router-dom";
import { apiService } from "../services/api";
import { useAuth } from "../context/AuthContext";
import { toast } from 'react-hot-toast';

const UserProfile = () => {
    // * Context and Navigation
    const { user, refreshProfile } = useAuth();
    const navigate = useNavigate();

    // * State Management - Profile Picture Only
    const [file, setFile] = useState(null);
    const [previewImage, setPreviewImage] = useState(null);

    // * Handle Image Selection with Validation
    const handleImageChange = (e) => {
        const selectedFile = e.target.files[0];
        if (selectedFile) {
            const maxSize = 2 * 1024 * 1024; // 2 MB limit
            if (selectedFile.size > maxSize) {
                toast.error("File is too large. Maximum allowed size is 2 MB.");
                return;
            }

            setFile(selectedFile);
            const reader = new FileReader();
            reader.onloadend = () => setPreviewImage(reader.result);
            reader.readAsDataURL(selectedFile);
        }
    };

    // * Reset Profile Picture Selection
    const handleResetProfilePic = () => {
        setFile(null);
        setPreviewImage(null);
    };

    // * Update Profile Picture via API
    const handleUpdateProfile = async () => {
        if (!file) {
            toast.error("Please select a profile picture to update.");
            return;
        }

        try {
            const formDataToSend = new FormData();

            // Append all existing fields to prevent them from being set to null
            formDataToSend.append("fullName", user.fullName || "");
            formDataToSend.append("department", user.department || "");
            formDataToSend.append("phone", user.phone || "");
            formDataToSend.append("yearOfStudy", user.yearOfStudy || "");
            formDataToSend.append("dob", user.dob || "");

            // Append the new profile picture
            formDataToSend.append("profilePic", file);

            await apiService.updateProfile(formDataToSend);
            await refreshProfile();

            toast.success("Profile picture updated successfully!");
            navigate("/profile");
        } catch (err) {
            console.error("Failed to update profile", err);
            toast.error("Failed to update profile. Please try again.");
        }
    };

    // * Format Date Helper Function
    const formatDate = (dateStr) => {
        if (!dateStr) return "-";
        const d = new Date(dateStr);
        return `${d.getDate().toString().padStart(2, "0")}-${(d.getMonth() + 1)
            .toString()
            .padStart(2, "0")}-${d.getFullYear()}`;
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-50 to-orange-50 p-3 md:p-4">
            <div className="max-w-4xl mx-auto flex items-center justify-center py-8">
                {/* ! Main Profile Card */}
                <section className="w-full rounded-2xl border-2 border-[#522320] bg-[#ffffff]/40 p-3 shadow-md shadow-[#522320]/20 transition-all duration-300 hover:shadow-xl hover:shadow-[#522320]/30 hover:-translate-y-0.5 px-5 sm:px-6 md:px-8 py-6 flex flex-col gap-6 relative">

                    {/* * Header Section */}
                    <header className="flex flex-col lg:flex-row items-center lg:justify-between gap-4">
                        <div className="flex flex-col items-center w-full text-center px-4 sm:px-6">
                            {/* Title with Back Icon */}
                            <div className="relative">
                                <h2 className="text-xl sm:text-2xl md:text-3xl font-bold text-amber-950">
                                    Update Profile Picture
                                </h2>

                                {/* Back Icon */}
                                <i
                                    className="fa-solid fa-hand-point-left text-lg sm:text-xl md:text-2xl text-amber-950 cursor-pointer absolute -left-8 sm:-left-10 md:-left-12 top-1/2 -translate-y-1/2"
                                    onClick={() => navigate("/profile")}
                                ></i>
                            </div>

                            {/* Subtitle */}
                            <p className="text-sm sm:text-base font-normal text-amber-950 mt-2">
                                Update your profile picture. Other details are read-only.
                            </p>
                        </div>

                        {/* Current Profile Picture Display */}
                        <div className="h-16 w-16 lg:absolute lg:top-6 lg:right-6 rounded-full border-2 border-orange-400 flex items-center justify-center flex-shrink-0">
                            <img
                                src={previewImage || user.profilePic || mascot}
                                alt="profile"
                                className="h-full w-full object-cover rounded-full"
                            />
                        </div>
                    </header>

                    {/* * User Information Section - Read Only */}
                    <div className="text-base text-[#522320] pb-4 pt-2">
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            {/* Full Name */}
                            <div className="bg-white/40 rounded-lg p-3 border border-[#E7B9AE]/80">
                                <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">
                                    Full Name
                                </span>
                                <span className="text-[#5b3627]">{user.fullName || "N/A"}</span>
                            </div>

                            {/* Phone */}
                            <div className="bg-white/40 rounded-lg p-3 border border-[#E7B9AE]/80">
                                <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">
                                    Phone
                                </span>
                                <span className="text-[#5b3627]">{user.phone || "N/A"}</span>
                            </div>

                            {/* Email */}
                            <div className="bg-white/40 rounded-lg p-3 border border-[#E7B9AE]/80">
                                <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">
                                    Email
                                </span>
                                <span className="text-[#5b3627] break-all">{user.email || "N/A"}</span>
                            </div>

                            {/* Department */}
                            <div className="bg-white/40 rounded-lg p-3 border border-[#E7B9AE]/80">
                                <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">
                                    Department
                                </span>
                                <span className="text-[#5b3627]">{user.department || "N/A"}</span>
                            </div>

                            {/* Date of Birth */}
                            <div className="bg-white/40 rounded-lg p-3 border border-[#E7B9AE]/80">
                                <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">
                                    Date of Birth
                                </span>
                                <span className="text-[#5b3627]">{formatDate(user.dob)}</span>
                            </div>

                            {/* Year of Study */}
                            <div className="bg-white/40 rounded-lg p-3 border border-[#E7B9AE]/80">
                                <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">
                                    Year of Study
                                </span>
                                <span className="text-[#5b3627]">{user.yearOfStudy || "N/A"}</span>
                            </div>
                        </div>

                        {/* Separator */}
                        <div className="border-t border-[#E7B9AE]/30 my-4"></div>
                    </div>

                    {/* * Profile Picture Upload Section */}
                    <div className="bg-orange-50 rounded-2xl p-4 border-2 border-stone-200 shadow-sm flex flex-col items-center justify-center gap-3 hover:border-red-800 transition-colors duration-300">
                        <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">Profile Picture</span>

                        {/* Profile Picture Preview */}
                        <div className="h-24 w-24 rounded-full border-2 border-orange-400 overflow-hidden flex items-center justify-center">
                            <img
                                src={previewImage || user.profilePic || mascot}
                                alt="profile preview"
                                className="bg-white h-full w-full object-cover"
                            />
                        </div>

                        {/* File Input and Reset Button */}
                        <div className="w-full md:w-1/2 flex justify-around items-center mt-2">
                            <input
                                type="file"
                                accept="image/*"
                                onChange={handleImageChange}
                                className="w-2/3 block text-sm text-[#5b3627] file:mr-2 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-red-700 file:text-white hover:file:bg-red-800 hover:file:cursor-pointer cursor-pointer hover:file:scale-95 file:transition-transform file:duration-300"
                            />

                            {/* Reset Button */}
                            <button
                                type="button"
                                onClick={handleResetProfilePic}
                                className="w-8 h-8 flex items-center justify-center rounded-2xl border-2 border-red-600 text-red-600 text-xl font-bold shadow-sm hover:bg-red-600 hover:text-white transition-all duration-300 hover:scale-95"
                            >
                                &times;
                            </button>
                        </div>
                    </div>

                    {/* * Footer - Update Button */}
                    <div className="flex justify-center md:justify-end pt-2 border-t border-[#E7B9AE]/30">
                        <button
                            type="button"
                            onClick={handleUpdateProfile}
                            className="w-full md:w-auto hover:scale-95 transition-transform duration-300 bg-red-700 hover:bg-red-800 text-white font-semibold py-2.5 px-6 rounded-lg shadow flex items-center justify-center gap-2"
                        >
                            <i className="fa-solid fa-pen"></i>
                            <span>Update</span>
                        </button>
                    </div>
                </section>
            </div>
        </div>
    );
};

export default UserProfile;
