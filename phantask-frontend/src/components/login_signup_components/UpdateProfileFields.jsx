import React, { useState } from "react";
import { toast } from "react-hot-toast";
import { apiService } from "../../services/api";
import { createPortal } from 'react-dom';


// Define ProfileConfirmationModal BEFORE UpdateProfileFields
const ProfileConfirmationModal = ({ profileData, onClose, onConfirm, isLoading }) => {
    const [confirmed, setConfirmed] = useState(false);

    const formatDate = (dateStr) => {
        if (!dateStr) return "-";
        const d = new Date(dateStr);
        return `${d.getDate().toString().padStart(2, "0")}-${(d.getMonth() + 1)
            .toString()
            .padStart(2, "0")}-${d.getFullYear()}`;
    };

    const modalContent = (
        <div
            className="fixed inset-0 z-[9999] flex items-center justify-center p-4"
            aria-modal="true"
            role="dialog"
            style={{
                position: 'fixed',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                width: '100vw',
                height: '100vh'
            }}
        >
            {/* Backdrop */}
            <div className="absolute inset-0 bg-black/50" onClick={onClose} />

            {/* Modal Content */}
            <div className="relative w-[90%] sm:w-[500px] max-w-[500px] max-h-[90vh] animate-slideUp z-10">
                <div className="bg-white rounded-xl p-5 shadow-2xl border-2 border-amber-500 max-h-[90vh] overflow-y-auto custom-scrollbar">
                    <h3 className="text-xl font-bold text-amber-950 text-center">Confirm Profile Update</h3>

                    {/* Warning Message */}
                    <div className="mt-4 bg-amber-50 border-l-4 border-amber-500 p-3 rounded">
                        <p className="text-sm font-semibold text-amber-900">⚠️ Important Notice</p>
                        <p className="text-xs text-amber-800 mt-1">
                            This information can only be filled once. You will not be able to update it in the future.
                            Please verify all details carefully before confirming.
                        </p>
                    </div>

                    {/* Profile Data Review */}
                    <div className="mt-4 bg-gray-50 rounded-lg p-4">
                        <h4 className="text-sm font-semibold text-gray-700 mb-3">Review Your Information:</h4>

                        <div className="space-y-2.5">
                            <div className="flex justify-between items-center border-b border-gray-200 pb-2">
                                <p className="text-xs text-gray-600 font-medium">Full Name:</p>
                                <p className="text-sm font-semibold text-right">{profileData.fullName || "-"}</p>
                            </div>

                            <div className="flex justify-between items-center border-b border-gray-200 pb-2">
                                <p className="text-xs text-gray-600 font-medium">Department:</p>
                                <p className="text-sm font-semibold text-right">{profileData.department || "-"}</p>
                            </div>

                            <div className="flex justify-between items-center border-b border-gray-200 pb-2">
                                <p className="text-xs text-gray-600 font-medium">Phone:</p>
                                <p className="text-sm font-semibold text-right">{profileData.phone || "-"}</p>
                            </div>

                            <div className="flex justify-between items-center border-b border-gray-200 pb-2">
                                <p className="text-xs text-gray-600 font-medium">Year of Study:</p>
                                <p className="text-sm font-semibold text-right">{profileData.yearOfStudy || "-"}</p>
                            </div>

                            <div className="flex justify-between items-center">
                                <p className="text-xs text-gray-600 font-medium">Date of Birth:</p>
                                <p className="text-sm font-semibold text-right">{formatDate(profileData.dob) || "-"}</p>
                            </div>
                        </div>
                    </div>

                    {/* Confirmation Checkbox */}
                    <div className="mt-4 border-t pt-4">
                        <label className="flex items-center justify-center gap-2.5 text-sm cursor-pointer">
                            <input
                                type="checkbox"
                                checked={confirmed}
                                onChange={(e) => setConfirmed(e.target.checked)}
                                className="mt-1 w-4 h-4 accent-green-700 cursor-pointer flex-shrink-0"
                            />
                            <span className="text-gray-700 font-semibold leading-tight">
                                I have carefully reviewed all the information above and confirm that it is accurate.
                                I understand that this information cannot be changed later.
                            </span>
                        </label>
                    </div>

                    {/* Action Buttons */}
                    <div className="mt-5 flex gap-2">
                        <button
                            onClick={onConfirm}
                            disabled={!confirmed || isLoading}
                            className={`hover:scale-95 transition-transform duration-300 flex-1 py-2.5 rounded-lg text-white font-semibold ${confirmed && !isLoading
                                ? "bg-amber-600 hover:bg-amber-700"
                                : "bg-amber-600/50 cursor-not-allowed"
                                }`}
                        >
                            {isLoading ? "Updating..." : "Confirm & Update"}
                        </button>
                        <button
                            onClick={onClose}
                            disabled={isLoading}
                            className="hover:scale-95 transition-transform duration-300 flex-1 py-2.5 rounded-lg bg-stone-200 hover:bg-stone-300 text-gray-800 font-semibold disabled:opacity-50"
                        >
                            Cancel
                        </button>
                    </div>
                </div>
            </div>

            {/* Slide-up Animation */}
            <style>
                {`
                    @keyframes slideUp {
                        0% { transform: translateY(100%); opacity: 0; }
                        100% { transform: translateY(0); opacity: 1; }
                    }
                    .animate-slideUp {
                        animation: slideUp 0.3s ease-out forwards;
                    }

                    /* Custom scrollbar styling for modal */
                    .custom-scrollbar::-webkit-scrollbar {
                        width: 8px;
                    }

                    .custom-scrollbar::-webkit-scrollbar-track {
                        background: transparent;
                        margin: 0.4rem 0;
                    }

                    .custom-scrollbar::-webkit-scrollbar-thumb {
                        background: #d1d5db;
                        border-radius: 8px;
                    }

                    .custom-scrollbar::-webkit-scrollbar-thumb:hover {
                        background: #9ca3af;
                    }
                `}
            </style>
        </div>
    );

    // Render modal at document.body level using portal
    return createPortal(modalContent, document.body);
};


// Main Component
const UpdateProfileFields = ({ user, onProfileUpdated }) => {
    const [isLoading, setIsLoading] = useState(false);
    const [showConfirmModal, setShowConfirmModal] = useState(false);
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

    const handleFormSubmit = (e) => {
        e.preventDefault();
        setShowConfirmModal(true);
    };

    const handleConfirmUpdate = async () => {
        setIsLoading(true);

        try {
            const formDataToSend = new FormData();
            formDataToSend.append("fullName", profileData.fullName || "");
            formDataToSend.append("department", profileData.department || "");
            formDataToSend.append("phone", profileData.phone || "");
            formDataToSend.append("yearOfStudy", profileData.yearOfStudy || "");
            formDataToSend.append("dob", profileData.dob || "");

            const username = user?.username || sessionStorage.getItem("username");

            await apiService.updateProfileFirstLogin(formDataToSend, username);
            toast.success("Profile updated successfully!");

            setShowConfirmModal(false);

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
        <>
            <form
                onSubmit={handleFormSubmit}
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
                        className="hover:scale-95 transition-transform-colors duration-300 flex-1 bg-red-700 hover:bg-red-800 text-white font-semibold py-2 rounded-lg shadow"
                    >
                        Update
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

            {/* Confirmation Modal - Rendered via Portal */}
            {showConfirmModal && (
                <ProfileConfirmationModal
                    profileData={profileData}
                    onClose={() => setShowConfirmModal(false)}
                    onConfirm={handleConfirmUpdate}
                    isLoading={isLoading}
                />
            )}
        </>
    );
};

export default UpdateProfileFields;
