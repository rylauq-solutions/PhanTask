import React, { useEffect, useState } from "react";
import { apiService } from "../services/api";
import { toast } from "react-hot-toast";
import LoadingSkeleton from "../components/LoadingSkeleton";
import { FaFilter } from "react-icons/fa";

// * ============================================================================
// * MANAGE NOTICES COMPONENT
// * ============================================================================
// ! This component is only accessible on desktop (screen width >= 990px)
// ? Handles notice management: view, search, filter, edit, and delete notices

const ManageNotices = () => {
    // * ============================================================================
    // * STATE MANAGEMENT
    // * ============================================================================

    // Notice list
    const [notices, setNotices] = useState([]);

    // Loading states
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(null);

    // Modal and selection states
    const [selectedNotice, setSelectedNotice] = useState(null);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showDetailsModal, setShowDetailsModal] = useState(false);

    // Search state
    const [searchTerm, setSearchTerm] = useState("");

    // Filter state: ALL, URGENT, IMPORTANT, GENERAL
    const [filter, setFilter] = useState("ALL");

    // Desktop detection state
    const [isDesktop, setIsDesktop] = useState(window.innerWidth >= 990);

    // * ============================================================================
    // * DESKTOP DETECTION
    // * ============================================================================
    // ! Component only works on desktop screens (>= 990px)

    useEffect(() => {
        const handleResize = () => {
            setIsDesktop(window.innerWidth >= 990);
        };

        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, []);

    // * ============================================================================
    // * SORT NOTICES BY DATE
    // * ============================================================================
    // ? Sorts notices by creation date (most recent first)

    const sortNoticesByDate = (noticesList) => {
        return [...noticesList].sort((a, b) => {
            if (!a.createdAt && !b.createdAt) return 0;
            if (!a.createdAt) return 1;
            if (!b.createdAt) return -1;
            return new Date(b.createdAt) - new Date(a.createdAt);
        });
    };

    // * ============================================================================
    // * FETCH NOTICES ON MOUNT
    // * ============================================================================
    // ? Fetches all notices for admin

    useEffect(() => {
        if (!isDesktop) return; // Don't fetch if not desktop

        const fetchNotices = async () => {
            try {
                const response = await apiService.getAllNoticesAdmin();
                const allNotices = response.data || [];
                setNotices(sortNoticesByDate(allNotices));
            } catch (err) {
                console.error(err);
                toast.error("Failed to load notices");
            } finally {
                setLoading(false);
            }
        };

        fetchNotices();
    }, [isDesktop]);

    // * ============================================================================
    // * REFETCH NOTICES FUNCTION
    // * ============================================================================
    // ? Re-fetches all notices after delete

    const refetchNotices = async () => {
        try {
            const response = await apiService.getAllNoticesAdmin();
            const allNotices = response.data || [];
            setNotices(sortNoticesByDate(allNotices));
        } catch (err) {
            console.error(err);
            toast.error("Failed to refresh notices");
        }
    };

    // * ============================================================================
    // * FILTERED NOTICES (WITH SEARCH AND PRIORITY)
    // * ============================================================================
    // ? Filters notices based on search term and priority filter

    const filteredNotices = notices.filter((notice) => {
        const searchLower = searchTerm.toLowerCase();
        const matchesSearch =
            (notice.title || "").toLowerCase().includes(searchLower) ||
            (notice.content || "").toLowerCase().includes(searchLower) ||
            (notice.postedBy || "").toLowerCase().includes(searchLower);

        if (filter === "ALL") return matchesSearch;
        return matchesSearch && notice.priority === filter;
    });

    // * ============================================================================
    // * DELETE NOTICE HANDLER
    // * ============================================================================
    // ! Deletes notice from the system

    const handleDelete = async () => {
        if (!selectedNotice) return;

        setActionLoading(selectedNotice.id);

        try {
            await apiService.deleteNotice(selectedNotice.id);
            toast.success("Notice deleted successfully");
            setNotices((prev) => prev.filter((n) => n.id !== selectedNotice.id));
            setShowDeleteModal(false);
            setSelectedNotice(null);
        } catch (err) {
            console.error(err);
            toast.error("Failed to delete notice");
        } finally {
            setActionLoading(null);
        }
    };

    // * ============================================================================
    // * FILTER BAR COMPONENT
    // * ============================================================================
    // ? Four filter buttons: All, Urgent, Important, General

    const FilterBar = () => (
        <div className="flex flex-wrap justify-center gap-2 mb-4">
            <button
                onClick={() => setFilter("ALL")}
                className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-colors duration-300 ${filter === "ALL"
                    ? "bg-orange-500 text-white"
                    : "bg-white border border-gray-300 text-gray-700 hover:bg-orange-100"
                    }`}
            >
                <FaFilter /> All
            </button>
            <button
                onClick={() => setFilter("URGENT")}
                className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-colors duration-300 ${filter === "URGENT"
                    ? "bg-red-500 text-white"
                    : "bg-white border border-gray-300 text-gray-700 hover:bg-red-100"
                    }`}
            >
                <FaFilter /> Urgent
            </button>
            <button
                onClick={() => setFilter("IMPORTANT")}
                className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-colors duration-300 ${filter === "IMPORTANT"
                    ? "bg-yellow-500 text-white"
                    : "bg-white border border-gray-300 text-gray-700 hover:bg-yellow-100"
                    }`}
            >
                <FaFilter /> Important
            </button>
            <button
                onClick={() => setFilter("GENERAL")}
                className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-colors duration-300 ${filter === "GENERAL"
                    ? "bg-pink-600 text-white"
                    : "bg-white border border-gray-300 text-gray-700 hover:bg-pink-100"
                    }`}
            >
                <FaFilter /> General
            </button>
        </div>
    );

    // * ============================================================================
    // * GET PRIORITY COLOR
    // * ============================================================================

    const getPriorityColor = (priority) => {
        switch (priority) {
            case "URGENT":
                return "bg-red-100 text-red-700 border-red-500";
            case "IMPORTANT":
                return "bg-yellow-100 text-yellow-700 border-yellow-500";
            case "GENERAL":
            default:
                return "bg-pink-100 text-pink-700 border-pink-600";
        }
    };

    const getPriorityBadge = (priority) => {
        switch (priority) {
            case "URGENT":
                return "bg-red-100 text-red-700";
            case "IMPORTANT":
                return "bg-yellow-100 text-yellow-700";
            case "GENERAL":
            default:
                return "bg-pink-100 text-pink-600";
        }
    };

    // * ============================================================================
    // * DESKTOP CHECK
    // * ============================================================================
    // ! Show error message if accessed from mobile or small screen

    if (!isDesktop) {
        return (
            <div className="flex items-center justify-center h-screen p-4">
                <div className="text-center bg-white p-8 rounded-xl shadow-lg border border-gray-200">
                    <h1 className="text-2xl font-bold mb-4 text-amber-950">
                        Desktop Access Required
                    </h1>
                    <p className="text-gray-700">
                        The <span className="font-semibold">Manage Notices</span> functionality is only accessible on a desktop or a large screen.
                        <br />
                        Please switch to a device with a screen width of <span className="font-semibold">990px or more</span>.
                    </p>
                </div>
            </div>
        );
    }

    // * ============================================================================
    // * LOADING STATE
    // * ============================================================================
    // ? Shows skeleton loader while fetching notices

    if (loading) {
        return (
            <div className="p-4">
                <LoadingSkeleton
                    titleHeight="h-8"
                    rows={5}
                    rowHeight="h-8"
                    hasButton={false}
                />
            </div>
        );
    }

    // * ============================================================================
    // * MAIN RENDER
    // * ============================================================================

    return (
        <div className="space-y-6 p-4">
            {/* PAGE HEADER */}
            <div className="mb-3 bg-white/60 rounded-xl p-4 shadow-sm border border-gray-100">
                <h1 className="text-2xl md:text-3xl font-bold text-center text-amber-950">
                    Manage Notices
                </h1>
            </div>

            {/* Filter Bar */}
            <FilterBar />

            {/* No notices message when filtered */}
            {filteredNotices.length === 0 && (
                <main className="w-full h-full flex flex-col items-center justify-center p-4">
                    <div className="w-20 h-20 bg-[#522320]/5 rounded-2xl flex items-center justify-center mb-4">
                        <span className="text-4xl">📢</span>
                    </div>
                    <h3 className="text-2xl font-bold text-[#522320] mb-2 text-center">
                        No {filter === "ALL" ? "" : filter.charAt(0) + filter.slice(1).toLowerCase()} Notices Found
                    </h3>
                    <p className="text-[#522320]/60 text-sm text-center max-w-md">
                        {searchTerm
                            ? "No notices match your search criteria."
                            : `No ${filter === "ALL" ? "" : filter.toLowerCase()} notices to display.`}
                    </p>
                </main>
            )}

            {/* ============================================================================ */}
            {/* NOTICES TABLE */}
            {/* ============================================================================ */}
            {filteredNotices.length > 0 && (
                <div className="border rounded-xl p-4 bg-white/80 shadow-sm border-orange-500 max-w-full">
                    <div className="mb-3 flex flex-col md:flex-row md:items-center md:justify-between gap-2">
                        <h2 className="text-xl font-bold text-orange-700">
                            {filter === "ALL" ? "All" : filter.charAt(0) + filter.slice(1).toLowerCase()} Notices
                        </h2>
                        <input
                            type="text"
                            placeholder="Search by title, content, or posted by..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full md:w-1/3 rounded-lg border border-gray-300 px-3 py-2 text-sm
                            focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                        />
                    </div>

                    <div className="overflow-x-auto w-full">
                        <table className="w-full text-sm border-collapse">
                            <thead>
                                <tr className="bg-orange-100 text-orange-900">
                                    <th className="p-3 text-center">Title</th>
                                    <th className="p-3 text-center">Priority</th>
                                    <th className="p-3 text-center">Posted By</th>
                                    <th className="p-3 text-center">Target Roles</th>
                                    <th className="p-3 text-center">Created At</th>
                                    <th className="p-3 text-center">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredNotices.map((notice) => (
                                    <tr
                                        key={notice.id}
                                        className="border-t hover:bg-orange-50 transition-colors"
                                    >
                                        <td className="p-3 font-medium text-center break-words max-w-xs">
                                            {notice.title || "Untitled"}
                                        </td>
                                        <td className="p-3 text-center">
                                            <span className={`px-2 py-1 rounded-full text-xs font-semibold ${getPriorityBadge(notice.priority)}`}>
                                                {notice.priority}
                                            </span>
                                        </td>
                                        <td className="p-3 text-center text-xs font-medium text-gray-700">
                                            {notice.postedBy || "—"}
                                        </td>
                                        <td className="p-3 text-center">
                                            <div className="flex flex-wrap gap-1 justify-center">
                                                {notice.targetRoles && notice.targetRoles.length > 0 ? (
                                                    notice.targetRoles.map((role, idx) => (
                                                        <span
                                                            key={idx}
                                                            className="inline-flex items-center rounded-full bg-[#FCE0D6] px-2 py-0.5 text-xs font-medium text-[#8c432b]"
                                                        >
                                                            {role}
                                                        </span>
                                                    ))
                                                ) : (
                                                    "—"
                                                )}
                                            </div>
                                        </td>
                                        <td className="p-3 text-center text-xs text-gray-600 font-medium">
                                            {notice.createdAt
                                                ? new Date(notice.createdAt).toLocaleDateString("en-IN", {
                                                    day: "2-digit",
                                                    month: "short",
                                                    year: "numeric",
                                                })
                                                : "—"}
                                        </td>
                                        <td className="p-3 flex items-center justify-center gap-2">
                                            <button
                                                onClick={() => {
                                                    setSelectedNotice(notice);
                                                    setShowDetailsModal(true);
                                                }}
                                                className="px-3 py-2 rounded-lg bg-orange-500 text-white font-semibold hover:bg-orange-600 hover:scale-95 transition-transform"
                                            >
                                                View
                                            </button>
                                            <button
                                                onClick={() => {
                                                    setSelectedNotice(notice);
                                                    setShowDeleteModal(true);
                                                }}
                                                className="px-3 py-2 rounded-lg bg-red-500 text-white font-semibold hover:bg-red-600 hover:scale-95 transition-all"
                                            >
                                                Delete
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* ============================================================================ */}
            {/* MODALS */}
            {/* ============================================================================ */}

            {/* DELETE MODAL */}
            {showDeleteModal && selectedNotice && (
                <DeleteNoticeModal
                    notice={selectedNotice}
                    loading={actionLoading === selectedNotice.id}
                    onClose={() => {
                        setShowDeleteModal(false);
                        setSelectedNotice(null);
                    }}
                    onConfirm={handleDelete}
                />
            )}

            {/* DETAILS MODAL */}
            {showDetailsModal && selectedNotice && (
                <NoticeDetailsModal
                    notice={selectedNotice}
                    onClose={() => {
                        setShowDetailsModal(false);
                        setSelectedNotice(null);
                    }}
                />
            )}
        </div>
    );
};

export default ManageNotices;

// * ============================================================================
// * DELETE NOTICE MODAL
// * ============================================================================
// ! Confirmation modal before deleting a notice
// ? Shows notice title and requires explicit confirmation

const DeleteNoticeModal = ({ notice, onClose, onConfirm, loading }) => {
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div className="absolute inset-0 bg-black/40" onClick={onClose} />

            {/* Modal Content */}
            <div className="relative w-[90%] sm:w-[400px] animate-slideUp">
                <div className="bg-white rounded-xl p-5 shadow-xl border border-red-300">
                    <h3 className="text-xl font-bold text-red-700 text-center">Delete Notice</h3>
                    <p className="text-sm text-gray-700 mt-3 text-center">
                        Are you sure you want to delete <span className="font-semibold">"{notice.title || "(Untitled)"}"</span>?
                    </p>

                    {/* Action Buttons */}
                    <div className="mt-5 flex gap-2">
                        <button
                            onClick={onConfirm}
                            disabled={loading}
                            className="flex-1 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white font-semibold hover:scale-95 transition"
                        >
                            {loading ? "Deleting..." : "Yes, Delete"}
                        </button>
                        <button
                            onClick={onClose}
                            className="flex-1 py-2 rounded-lg bg-stone-200 hover:bg-stone-300 text-gray-800 font-semibold hover:scale-95 transition"
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
                        animation: slideUp 0.2s ease-out forwards;
                    }
                `}
            </style>
        </div>
    );
};

// * ============================================================================
// * NOTICE DETAILS MODAL
// * ============================================================================
// ! Modal for viewing full notice details
// ? Shows all notice information in a read-only format

const NoticeDetailsModal = ({ notice, onClose }) => {
    const getPriorityColor = (priority) => {
        switch (priority) {
            case "URGENT":
                return "border-red-500";
            case "IMPORTANT":
                return "border-yellow-500";
            case "GENERAL":
            default:
                return "border-pink-600";
        }
    };

    const getPriorityBadge = (priority) => {
        switch (priority) {
            case "URGENT":
                return "bg-red-100 text-red-700";
            case "IMPORTANT":
                return "bg-yellow-100 text-yellow-700";
            case "GENERAL":
            default:
                return "bg-pink-100 text-pink-600";
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Background Overlay */}
            <div className="absolute inset-0 bg-black/40" onClick={onClose} />

            {/* Modal Container */}
            <div className="relative w-[90%] sm:w-[80%] md:w-1/2 max-h-[95vh] animate-slideUp">
                <div className={`bg-white rounded-xl p-4 md:p-6 shadow-xl border-l-4 ${getPriorityColor(notice.priority)} max-h-[95vh] overflow-y-auto`}>
                    {/* Header */}
                    <div className="mb-4">
                        <div className="flex items-start justify-between">
                            <div className="flex-1">
                                <h3 className="text-2xl font-bold text-amber-950">{notice.title || "Untitled"}</h3>
                                <span className={`inline-block mt-2 text-xs font-semibold px-2 py-1 rounded-full ${getPriorityBadge(notice.priority)}`}>
                                    {notice.priority}
                                </span>
                            </div>
                        </div>
                    </div>

                    {/* Details */}
                    <div className="space-y-3">
                        <div>
                            <label className="text-sm font-semibold text-gray-800">Content:</label>
                            <p className="text-sm text-gray-700 mt-1 p-3 bg-gray-50 rounded-lg whitespace-pre-wrap">
                                {notice.content || "No content provided"}
                            </p>
                        </div>

                        <div className="grid grid-cols-2 gap-3">
                            <div>
                                <label className="text-sm font-semibold text-gray-800">Posted By:</label>
                                <p className="text-sm text-gray-700 mt-1">{notice.postedBy || "—"}</p>
                            </div>

                            <div>
                                <label className="text-sm font-semibold text-gray-800">Created At:</label>
                                <p className="text-sm text-gray-700 mt-1">
                                    {notice.createdAt
                                        ? new Date(notice.createdAt).toLocaleDateString("en-IN", {
                                            day: "2-digit",
                                            month: "short",
                                            year: "numeric",
                                        }) +
                                        " at " +
                                        new Date(notice.createdAt).toLocaleTimeString("en-IN", {
                                            hour: "2-digit",
                                            minute: "2-digit",
                                        })
                                        : "—"}
                                </p>
                            </div>
                        </div>

                        <div>
                            <label className="text-sm font-semibold text-gray-800">Target Roles:</label>
                            <div className="flex flex-wrap gap-2 mt-1">
                                {notice.targetRoles && notice.targetRoles.length > 0 ? (
                                    notice.targetRoles.map((role, idx) => (
                                        <span
                                            key={idx}
                                            className="inline-flex items-center rounded-full bg-[#FCE0D6] px-2.5 py-1 text-xs font-medium text-[#8c432b]"
                                        >
                                            {role}
                                        </span>
                                    ))
                                ) : (
                                    <p className="text-sm text-gray-700">No roles specified</p>
                                )}
                            </div>
                        </div>
                    </div>

                    {/* Close Button */}
                    <div className="mt-6">
                        <button
                            onClick={onClose}
                            className="w-full py-2 rounded-lg bg-stone-200 hover:bg-stone-300 text-gray-800 font-semibold hover:scale-95 transition duration-300 shadow"
                        >
                            Close
                        </button>
                    </div>
                </div>
            </div>

            {/* Custom Styles */}
            <style>
                {`
                    @keyframes slideUp {
                        0% { transform: translateY(100%); opacity: 0; }
                        100% { transform: translateY(0); opacity: 1; }
                    }
                    .animate-slideUp {
                        animation: slideUp 0.2s ease-out forwards;
                    }

                    .overflow-y-auto::-webkit-scrollbar {
                        width: 8px;
                    }

                    .overflow-y-auto::-webkit-scrollbar-track {
                        background: transparent;
                        margin: 0.4rem 0;
                    }

                    .overflow-y-auto::-webkit-scrollbar-thumb {
                        background: #d1d5db;
                        border-radius: 8px;
                    }

                    .overflow-y-auto::-webkit-scrollbar-thumb:hover {
                        background: #9ca3af;
                    }
                `}
            </style>
        </div>
    );
};
