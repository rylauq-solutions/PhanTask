// src/pages/ManageFeedbacks.jsx
import React, { useEffect, useState } from "react";
import { apiService } from "../services/api";
import { toast } from "react-hot-toast";
import { FaFilter } from "react-icons/fa";
import LoadingSkeleton from "../components/LoadingSkeleton";

/* -------- DELETE MODAL -------- */
const DeleteFeedbackModal = ({ feedback, onClose, onConfirm, loading }) => (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
        <div className="absolute inset-0 bg-black/40" onClick={onClose} />
        <div className="relative w-[90%] sm:w-[400px] animate-slideUp">
            <div className="bg-white rounded-xl p-5 shadow-xl border border-red-300">
                <h3 className="text-xl font-bold text-red-700 text-center">
                    Delete Feedback
                </h3>
                <p className="text-sm text-gray-700 mt-3 text-center">
                    Are you sure you want to permanently delete{" "}
                    <span className="font-semibold">{feedback.title}</span> and all its
                    submissions?
                </p>
                <div className="mt-5 flex gap-2">
                    <button
                        onClick={onConfirm}
                        disabled={loading}
                        className="flex-1 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white font-semibold hover:scale-95 transition-transform disabled:opacity-60 disabled:cursor-not-allowed"
                    >
                        {loading ? "Processing..." : "Yes, Delete"}
                    </button>
                    <button
                        onClick={onClose}
                        className="flex-1 py-2 rounded-lg bg-stone-200 hover:bg-stone-300 text-gray-800 font-semibold hover:scale-95 transition-transform"
                    >
                        Cancel
                    </button>
                </div>
            </div>
        </div>
        <style>{`
      @keyframes slideUp {
        0% { transform: translateY(100%); opacity: 0; }
        100% { transform: translateY(0); opacity: 1; }
      }
      .animate-slideUp { animation: slideUp 0.2s ease-out forwards; }
    `}</style>
    </div>
);

/* -------- REPORT MODAL -------- */
const FeedbackReportModal = ({ feedback, report, onClose }) => (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
        <div className="absolute inset-0 bg-black/40" onClick={onClose} />
        <div className="relative w-[90%] sm:w-[480px] max-h-[90vh] animate-slideUp">
            <div className="bg-white rounded-xl p-5 shadow-xl border border-blue-300 max-h-[90vh] overflow-y-auto">
                <h3 className="text-xl font-bold text-blue-800 text-center">
                    Feedback Report
                </h3>
                <p className="text-sm text-gray-700 mt-1 text-center font-medium">
                    {feedback.title}
                </p>

                {!report ? (
                    <p className="text-center text-gray-500 mt-6">Loading report...</p>
                ) : (
                    <div className="mt-4 space-y-4">
                        <div className="flex items-center justify-between bg-blue-50 border border-blue-100 rounded-lg px-3 py-2 text-sm font-semibold text-blue-900">
                            <span>Overall Average (out of 10)</span>
                            <span>{report.overallAverage}</span>
                        </div>
                        <div className="flex items-center justify-between bg-emerald-50 border border-emerald-100 rounded-lg px-3 py-2 text-sm font-semibold text-emerald-900">
                            <span>Total Submissions</span>
                            <span>{report.totalSubmissions}</span>
                        </div>

                        <div>
                            <h4 className="text-sm font-semibold text-gray-800 mb-2">
                                Average rating per question (1–5)
                            </h4>
                            <div className="space-y-2">
                                {Object.entries(report.averagePerQuestion || {}).map(
                                    ([question, avg]) => (
                                        <div
                                            key={question}
                                            className="flex items-center justify-between bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm"
                                        >
                                            <span className="flex-1 mr-2">{question}</span>
                                            <span className="font-semibold text-gray-900">{avg}</span>
                                        </div>
                                    )
                                )}
                            </div>
                        </div>
                    </div>
                )}

                <div className="mt-5 flex justify-center">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 rounded-lg bg-stone-200 hover:bg-stone-300 text-gray-800 font-semibold hover:scale-95 transition-transform"
                    >
                        Close
                    </button>
                </div>
            </div>
        </div>
        <style>{`
      @keyframes slideUp {
        0% { transform: translateY(100%); opacity: 0; }
        100% { transform: translateY(0); opacity: 1; }
      }
      .animate-slideUp { animation: slideUp 0.2s ease-out forwards; }
    `}</style>
    </div>
);

/* -------- MAIN COMPONENT -------- */
const ManageFeedbacks = () => {
    const [feedbacks, setFeedbacks] = useState([]);
    const [loading, setLoading] = useState(true);

    const [selectedFeedback, setSelectedFeedback] = useState(null);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showReportModal, setShowReportModal] = useState(false);
    const [report, setReport] = useState(null);
    const [actionLoading, setActionLoading] = useState(false);

    const [searchTerm, setSearchTerm] = useState("");
    const [statusFilter, setStatusFilter] = useState("ALL"); // ALL | PENDING | IN_PROGRESS

    // Fetch all feedback templates
    const fetchFeedbacks = async () => {
        try {
            setLoading(true);
            const res = await apiService.getAllFeedbackTemplates();
            setFeedbacks(res.data || []);
        } catch (err) {
            console.error(err);
            toast.error("Failed to load feedbacks");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchFeedbacks();
    }, []);

    // Delete chosen feedback
    const handleDelete = async () => {
        if (!selectedFeedback) return;
        try {
            setActionLoading(true);
            await apiService.deleteFeedbackTemplate(selectedFeedback.feedbackId);
            toast.success("Feedback deleted successfully");
            setShowDeleteModal(false);
            setSelectedFeedback(null);
            fetchFeedbacks();
        } catch (err) {
            console.error(err);
            toast.error(err?.response?.data || "Failed to delete feedback");
        } finally {
            setActionLoading(false);
        }
    };

    // Load report and open modal
    const openReport = async (fb) => {
        try {
            setSelectedFeedback(fb);
            setShowReportModal(true);
            setReport(null);
            const res = await apiService.getFeedbackReport(fb.feedbackId);
            setReport(res.data);
        } catch (err) {
            console.error(err);
            toast.error(err?.response?.data || "Failed to load report");
            setShowReportModal(false);
            setSelectedFeedback(null);
        }
    };

    // Determine status using report.totalSubmissions:
    // 0 -> Pending, >=1 -> In Progress.[web:6][file:1]
    const getStatusForFeedback = (fb) => {
        // If we already have a report loaded for this fb, use it; otherwise fall back to a cached field if you add one later.
        // For list-level filtering, simpler approach: store a lightweight `submissionCount` in feedback if you extend backend.
        const submissionCount = fb.submissionCount ?? fb.totalSubmissions ?? 0;
        return submissionCount > 0 ? "IN_PROGRESS" : "PENDING";
    };

    // Filter by search + status
    const searched = feedbacks.filter((f) =>
        f.title.toLowerCase().includes(searchTerm.toLowerCase())
    );
    const filteredFeedbacks = searched.filter((fb) => {
        const status = getStatusForFeedback(fb);
        if (statusFilter === "ALL") return true;
        if (statusFilter === "PENDING") return status === "PENDING";
        if (statusFilter === "IN_PROGRESS") return status === "IN_PROGRESS";
        return true;
    });

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

    return (
        <div className="space-y-6 p-4">
            {/* PAGE HEADER */}
            <div className="mb-3 bg-white/60 rounded-xl p-4 shadow-sm border border-gray-100">
                <h1 className="text-2xl md:text-3xl font-bold text-center text-amber-950">
                    Manage Feedbacks
                </h1>
                <div className="mt-3 flex flex-col md:flex-row md:items-center md:justify-between gap-3">
                    {/* Filter buttons */}
                    <div className="flex flex-wrap justify-center gap-2">
                        <button
                            onClick={() => setStatusFilter("ALL")}
                            className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-colors duration-300 ${statusFilter === "ALL"
                                    ? "bg-orange-500 text-white"
                                    : "bg-white border border-gray-300 text-gray-700 hover:bg-orange-100"
                                }`}
                        >
                            <FaFilter />
                            All
                        </button>
                        <button
                            onClick={() => setStatusFilter("PENDING")}
                            className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-colors duration-300 ${statusFilter === "PENDING"
                                    ? "bg-yellow-500 text-white"
                                    : "bg-white border border-gray-300 text-gray-700 hover:bg-yellow-100"
                                }`}
                        >
                            <FaFilter />
                            Pending
                        </button>
                        <button
                            onClick={() => setStatusFilter("IN_PROGRESS")}
                            className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-colors duration-300 ${statusFilter === "IN_PROGRESS"
                                    ? "bg-green-600 text-white"
                                    : "bg-white border border-gray-300 text-gray-700 hover:bg-green-100"
                                }`}
                        >
                            <FaFilter />
                            In Progress
                        </button>
                    </div>

                    {/* Search */}
                    <div className="flex justify-center md:justify-end">
                        <input
                            type="text"
                            placeholder="Search by title..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full md:w-64 rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-600 focus:border-amber-600"
                        />
                    </div>
                </div>
            </div>

            {/* NO DATA MESSAGE */}
            {filteredFeedbacks.length === 0 && (
                <main className="w-full h-full flex flex-col items-center justify-center p-4">
                    <h3 className="text-xl font-bold text-[#522320] mb-1.5 text-center">
                        No Feedbacks Found
                    </h3>
                    <p className="text-[#522320]/60 text-sm text-center">
                        {searchTerm
                            ? "No feedbacks match your search."
                            : statusFilter === "PENDING"
                                ? "No pending feedback templates."
                                : statusFilter === "IN_PROGRESS"
                                    ? "No in-progress feedback templates."
                                    : "Create feedback templates from the dashboard to manage them here."}
                    </p>
                </main>
            )}

            {/* FEEDBACK TABLE */}
            {filteredFeedbacks.length > 0 && (
                <div className="border rounded-xl p-4 bg-white/80 shadow-sm max-w-full">
                    <h2 className="text-xl font-bold text-amber-900 mb-3 text-center md:text-left">
                        All Feedback Templates
                    </h2>
                    <div className="overflow-x-auto w-full">
                        <table className="w-full text-sm border-collapse">
                            <thead>
                                <tr className="bg-amber-100 text-amber-900">
                                    <th className="p-3 text-center">Title</th>
                                    <th className="p-3 text-center">Status</th>
                                    <th className="p-3 text-center">Assigned Roles</th>
                                    <th className="p-3 text-center">Questions</th>
                                    <th className="p-3 text-center">Created At</th>
                                    <th className="p-3 text-center">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredFeedbacks.map((fb) => {
                                    const status = getStatusForFeedback(fb);
                                    return (
                                        <tr
                                            key={fb.feedbackId}
                                            className="border-t hover:bg-amber-50 transition-colors"
                                        >
                                            <td className="p-3 font-medium text-center">{fb.title}</td>
                                            <td className="p-3 text-center">
                                                {status === "PENDING" ? (
                                                    <span className="px-2 py-1 rounded-full bg-yellow-100 text-yellow-700 text-xs font-semibold">
                                                        Pending
                                                    </span>
                                                ) : (
                                                    <span className="px-2 py-1 rounded-full bg-green-100 text-green-700 text-xs font-semibold">
                                                        In Progress
                                                    </span>
                                                )}
                                            </td>
                                            <td className="p-3">
                                                <div className="flex flex-wrap justify-center gap-1">
                                                    {fb.assignedRoles
                                                        ?.split(",")
                                                        .map((role) => role.trim())
                                                        .filter((role) => role.length > 0)
                                                        .map((role) => (
                                                            <span
                                                                key={role}
                                                                className="inline-flex items-center rounded-full bg-yellow-100 px-2.5 py-1 text-xs font-medium text-yellow-800"
                                                            >
                                                                {role}
                                                            </span>
                                                        ))}
                                                </div>
                                            </td>
                                            <td className="p-3 text-xs text-gray-800 max-w-xs">
                                                <ul className="list-disc list-inside space-y-0.5 text-left">
                                                    {fb.questions
                                                        ?.split(",")
                                                        .map((q) => q.trim())
                                                        .filter((q) => q.length > 0)
                                                        .map((q, idx) => (
                                                            <li key={idx}>{q}</li>
                                                        ))}
                                                </ul>
                                            </td>
                                            <td className="p-3 text-center text-xs text-gray-700">
                                                {fb.createdAt
                                                    ? new Date(fb.createdAt).toLocaleDateString("en-IN", {
                                                        day: "2-digit",
                                                        month: "short",
                                                        year: "numeric",
                                                    })
                                                    : "—"}
                                            </td>
                                            <td className="p-3 flex items-center justify-center gap-2">
                                                <button
                                                    onClick={() => openReport(fb)}
                                                    className="px-3 py-2 rounded-lg bg-blue-500 text-white font-semibold hover:bg-blue-600 hover:scale-95 transition-transform text-xs"
                                                >
                                                    View Report
                                                </button>
                                                <button
                                                    onClick={() => {
                                                        setSelectedFeedback(fb);
                                                        setShowDeleteModal(true);
                                                    }}
                                                    className="px-3 py-2 rounded-lg bg-red-500 text-white font-semibold hover:bg-red-600 hover:scale-95 transition-transform text-xs"
                                                >
                                                    Delete
                                                </button>
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* DELETE MODAL */}
            {showDeleteModal && selectedFeedback && (
                <DeleteFeedbackModal
                    feedback={selectedFeedback}
                    loading={actionLoading}
                    onClose={() => {
                        setShowDeleteModal(false);
                        setSelectedFeedback(null);
                    }}
                    onConfirm={handleDelete}
                />
            )}

            {/* REPORT MODAL */}
            {showReportModal && selectedFeedback && (
                <FeedbackReportModal
                    feedback={selectedFeedback}
                    report={report}
                    onClose={() => {
                        setShowReportModal(false);
                        setSelectedFeedback(null);
                        setReport(null);
                    }}
                />
            )}
        </div>
    );
};

export default ManageFeedbacks;
