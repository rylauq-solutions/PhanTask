import React from "react";
import { useAuth } from "../context/AuthContext";
import mascot from "../assets/Mascot-Phantask.png";
import LoadingSkeleton from "../components/LoadingSkeleton";
import { useNavigate } from "react-router-dom";

const UserProfile = () => {
    const { user, loading } = useAuth();
    const navigate = useNavigate();

    if (loading) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-gray-50 to-orange-50 p-3 md:p-4">
                <div className="max-w-7xl mx-auto flex items-center justify-center">
                    <LoadingSkeleton />
                </div>
            </div>
        );
    }

    if (!user) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-gray-50 to-orange-50 p-3 md:p-4 flex items-center justify-center">
                <div className="w-full max-w-sm rounded-2xl bg-white/60 backdrop-blur-sm border border-[#E7B9AE] shadow-md shadow-[#522320]/20 p-8 text-center">
                    <div className="mb-4">
                        <i className="fa-solid fa-circle-exclamation text-5xl text-red-700"></i>
                    </div>
                    <h3 className="text-xl font-semibold text-amber-950 mb-2">
                        Unable to Load Profile
                    </h3>
                    <p className="text-sm text-[#5b3627] mb-6">
                        We couldn't retrieve your profile information.<br /> Please try again later.
                    </p>
                </div>
            </div>
        );
    }

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
                {/* Main profile card */}
                <section className="w-full rounded-2xl border-2 border-[#522320] bg-[#ffffff]/40 p-3 shadow-md shadow-[#522320]/20 transition-all duration-300 hover:shadow-xl hover:shadow-[#522320]/30 hover:-translate-y-0.5 px-5 sm:px-6 md:px-8 py-6 flex flex-col gap-6 relative">

                    {/* Header */}
                    <header className="flex flex-col lg:flex-row items-center lg:justify-between gap-4">
                        <div className="flex flex-col items-center w-full text-center px-4 sm:px-6">
                            <h2 className="text-xl sm:text-2xl md:text-3xl font-bold text-amber-950">
                                My Profile
                            </h2>

                            {/* Subtitle */}
                            <p className="text-sm sm:text-base font-normal text-amber-950 mt-2">
                                Manage your PhanTask account details.
                            </p>
                        </div>

                        <div className="h-16 w-16 lg:absolute lg:top-6 lg:right-6 rounded-full border-2 border-orange-400 flex items-center justify-center flex-shrink-0">
                            <img
                                src={user.profilePic || mascot}
                                alt="profile"
                                className="h-full w-full object-cover rounded-full"
                                onError={(e) => (e.target.src = mascot)}
                            />
                        </div>
                    </header>

                    {/* Content - Two Column Grid */}
                    <div className="text-base text-[#522320] pb-4 pt-2">
                        {/* Top Section */}
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <div className="bg-white/40 rounded-lg p-3 border border-[#E7B9AE]/80">
                                <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">
                                    Username
                                </span>
                                <span className="text-[#5b3627] break-all">{user.username || "N/A"}</span>
                            </div>

                            <div className="bg-white/40 rounded-lg p-3 border border-[#E7B9AE]/80">
                                <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">
                                    Email
                                </span>
                                <span className="text-[#5b3627] break-all">{user.email || "N/A"}</span>
                            </div>

                            <div className="bg-white/40 rounded-lg p-3 border border-[#E7B9AE]/80">
                                <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">
                                    Primary Role
                                </span>
                                <span className="inline-flex items-center rounded-full bg-[#FCE0D6] px-2.5 py-1 text-xs font-medium text-[#8c432b]">
                                    {user.role || (user.roles && user.roles.length > 0 ? user.roles[0] : "N/A")}
                                </span>
                            </div>

                            <div className="bg-white/40 rounded-lg p-3 border border-[#E7B9AE]/80">
                                <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">
                                    Department
                                </span>
                                <span className="text-[#5b3627]">{user.department || "N/A"}</span>
                            </div>
                        </div>

                        {/* Separator */}
                        <div className="border-t border-[#E7B9AE]/30 my-4"></div>

                        {/* Bottom Section */}
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <div className="bg-white/40 rounded-lg p-3 border border-[#E7B9AE]/80">
                                <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">
                                    Full Name
                                </span>
                                <span className="text-[#5b3627]">{user.fullName || "N/A"}</span>
                            </div>

                            <div className="bg-white/40 rounded-lg p-3 border border-[#E7B9AE]/80">
                                <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">
                                    Phone
                                </span>
                                <span className="text-[#5b3627]">{user.phone || "N/A"}</span>
                            </div>

                            <div className="bg-white/40 rounded-lg p-3 border border-[#E7B9AE]/80">
                                <span className="block text-sm font-semibold text-[#3b1d18] mb-1.5">
                                    Date of Birth
                                </span>
                                <span className="text-[#5b3627]">{formatDate(user.dob)}</span>
                            </div>

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

                    {/* Footer – Edit button in bottom right */}
                    <div className="flex justify-center md:justify-end pt-2 border-t border-[#E7B9AE]/30">
                        <button
                            type="button"
                            onClick={() => navigate("/update-profile")}
                            className="w-full md:w-auto hover:scale-95 transition-transform duration-300 bg-red-700 hover:bg-red-800 text-white font-semibold py-2.5 px-6 rounded-lg shadow flex items-center justify-center gap-2"
                        >
                            <i className="fa-solid fa-pen"></i>
                            <span>Edit Profile Picture</span>
                        </button>
                    </div>
                </section>
            </div>
        </div>
    );
};

export default UserProfile;
