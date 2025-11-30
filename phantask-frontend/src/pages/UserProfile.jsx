import React, { useEffect, useState } from "react";
import { apiService } from "../services/api";
import { toast } from "react-hot-toast";
import LoadingSkeleton from "../components/LoadingSkeleton";

const UserProfile = ({ onEdit }) => {
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        apiService
            .getUserProfile()
            .then((res) => setProfile(res.data))
            .catch((err) => {
                toast.error(err.response?.data?.message || "Failed to load profile");
            })
            .finally(() => setLoading(false));
    }, []);

    if (loading) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-gray-50 to-orange-50 p-3 md:p-4">
                <div className="max-w-7xl mx-auto flex items-center justify-center">
                    <LoadingSkeleton
                        titleHeight="h-6"
                        rows={6}
                        rowHeight="h-5"
                        hasButton={true}
                        className="w-[90%] h-[90%] max-w-4xl"
                    />
                </div>
            </div>
        );
    }

    if (!profile) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-gray-50 to-orange-50 p-3 md:p-4">
                <div className="max-w-7xl mx-auto flex items-center justify-center text-sm text-[#522320]">
                    Unable to load profile.
                </div>
            </div>
        );
    }

    return (
        <div className="h-screen bg-gradient-to-br from-gray-50 to-orange-50 p-3 md:p-4">
            <div className="max-w-[90%] h-[90%] mx-auto flex items-center justify-center">
                {/* Main profile card – similar feel to Login container */}
                <section className="w-full h-full rounded-2xl bg-white/60 backdrop-blur-sm border border-[#E7B9AE] shadow-md shadow-[#522320]/20 px-5 sm:px-6 md:px-8 py-5 flex flex-col gap-4">
                    {/* Header */}
                    <header className="flex items-center justify-between gap-3">
                        <div className="flex flex-col w-full">
                            <h2 className="text-2xl text-center font-extrabold text-[#522320]">
                                My Profile
                            </h2>
                            <p className="text-xs sm:text-sm text-center text-[#8c5c4a] mt-1">
                                Manage your PhanTask account details.
                            </p>
                        </div>
                        <div className="h-12 w-12 sm:h-14 sm:w-14 rounded-full bg-[#FFD0D0] flex items-center justify-center text-lg font-bold text-[#662222]">
                            {profile.username?.[0]?.toUpperCase() || "U"}
                        </div>
                    </header>

                    {/* Content – flex, responsive like your login layout */}
                    <div className="flex flex-col lg:flex-row gap-6 mt-2 text-sm text-[#522320]">
                        {/* Left column */}
                        <div className="flex-1 flex flex-col gap-2">
                            <div>
                                <span className="font-semibold text-[#3b1d18]">
                                    Username:
                                </span>{" "}
                                <span className="text-[#5b3627] break-all">
                                    {profile.username}
                                </span>
                            </div>
                            <div>
                                <span className="font-semibold text-[#3b1d18]">Email:</span>{" "}
                                <span className="text-[#5b3627] break-all">
                                    {profile.email}
                                </span>
                            </div>
                            <div>
                                <span className="font-semibold text-[#3b1d18]">
                                    Primary role:
                                </span>{" "}
                                <span className="inline-flex items-center rounded-full bg-[#FCE0D6] px-2 py-0.5 text-[11px] font-medium text-[#8c432b]">
                                    {profile.role}
                                </span>
                            </div>
                            <div>
                                <span className="font-semibold text-[#3b1d18]">
                                    All roles:
                                </span>{" "}
                                <span className="text-[#5b3627]">
                                    {Array.from(profile.roles || []).join(", ") || "N/A"}
                                </span>
                            </div>
                        </div>

                        {/* Right column */}
                        <div className="flex-1 flex flex-col gap-2">
                            <div>
                                <span className="font-semibold text-[#3b1d18]">
                                    Full name:
                                </span>{" "}
                                <span className="text-[#5b3627]">
                                    {profile.fullName || "Not set"}
                                </span>
                            </div>
                            <div>
                                <span className="font-semibold text-[#3b1d18]">
                                    Department:
                                </span>{" "}
                                <span className="text-[#5b3627]">
                                    {profile.department || "Not set"}
                                </span>
                            </div>
                            <div>
                                <span className="font-semibold text-[#3b1d18]">Phone:</span>{" "}
                                <span className="text-[#5b3627]">
                                    {profile.phone || "Not set"}
                                </span>
                            </div>
                            <div>
                                <span className="font-semibold text-[#3b1d18]">
                                    Year of study:
                                </span>{" "}
                                <span className="text-[#5b3627]">
                                    {profile.yearOfStudy || "N/A"}
                                </span>
                            </div>
                        </div>
                    </div>

                    {/* Footer – Edit button */}
                    <div className="mt-3 flex justify-end">
                        <button
                            type="button"
                            onClick={onEdit}
                            className="inline-flex items-center gap-1 rounded-lg bg-[#992D2D] px-4 py-2 text-sm font-semibold text-[#FFEAEA] shadow-sm hover:bg-[#7d2323] hover:shadow-md hover:-translate-y-0.5 transition-all duration-200"
                        >
                            <i className="fa-solid fa-pen" />
                            <span>Edit details</span>
                        </button>
                    </div>
                </section>
            </div>
        </div>
    );
};

export default UserProfile;
