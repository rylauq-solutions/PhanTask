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
            <div className="bg-white rounded-xl p-5 shadow-xl border border-amber-300 max-h-[90vh] overflow-y-auto">
                <h3 className="text-xl font-bold text-amber-800 text-center">
                    Feedback Report
                </h3>
                <p className="text-sm text-gray-700 mt-1 text-center font-medium">
                    {feedback.title}
                </p>


                {!report ? (
                    <p className="text-center text-gray-500 mt-6">Loading report...</p>
                ) : (
                    <div className="mt-4 space-y-4">
                        <div className="flex items-center justify-between bg-amber-50 border border-amber-100 rounded-lg px-3 py-2 text-sm font-semibold text-amber-900">
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
    // State: feedbacks split by status
    const [pendingFeedbacks, setPendingFeedbacks] = useState([]);
    const [inProgressFeedbacks, setInProgressFeedbacks] = useState([]);
    const [loading, setLoading] = useState(true);


    const [selectedFeedback, setSelectedFeedback] = useState(null);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showReportModal, setShowReportModal] = useState(false);
    const [report, setReport] = useState(null);
    const [actionLoading, setActionLoading] = useState(false);


    // Separate search for each section
    const [searchTermPending, setSearchTermPending] = useState("");
    const [searchTermInProgress, setSearchTermInProgress] = useState("");


    // Filter state: ALL | PENDING | IN_PROGRESS
    const [filter, setFilter] = useState("ALL");


    // Desktop detection state
    const [isDesktop, setIsDesktop] = useState(window.innerWidth >= 990);


    // Desktop detection - Component only works on desktop screens >= 990px
    useEffect(() => {
        const handleResize = () => {
            setIsDesktop(window.innerWidth >= 990);
        };
        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, []);


    // Helper function to render roles safely
    const renderRoles = (assignedRoles) => {
        const roles = Array.isArray(assignedRoles)
            ? assignedRoles
            : typeof assignedRoles === 'string'
                ? assignedRoles.split(',').map(r => r.trim()).filter(r => r.length > 0)
                : [];

        if (roles.length === 0) {
            return <span className="text-gray-400 text-xs">No roles assigned</span>;
        }

        return roles.map((role) => (
            <span
                key={role}
                className="inline-flex items-center rounded-full bg-[#FCE0D6] px-2.5 py-1 text-xs font-medium text-[#8c432b]"
            >
                {role}
            </span>
        ));
    };


    // Fetch all feedback templates and separate by status
    const fetchFeedbacks = async () => {
        try {
            setLoading(true);
            const res = await apiService.getAllFeedbackTemplates();
            const allFeedbacks = res.data || [];

            // Separate pending (0 submissions) vs in-progress (>0 submissions)
            // Backend now returns submissionCount directly
            const pending = allFeedbacks
                .filter((fb) => fb.submissionCount === 0)
                .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

            const inProgress = allFeedbacks
                .filter((fb) => fb.submissionCount > 0)
                .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

            setPendingFeedbacks(pending);
            setInProgressFeedbacks(inProgress);
        } catch (err) {
            console.error(err);
            toast.error("Failed to load feedbacks");
        } finally {
            setLoading(false);
        }
    };


    useEffect(() => {
        if (!isDesktop) return; // Don't fetch if not desktop
        fetchFeedbacks();
    }, [isDesktop]);


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


    // Filter with search
    const filteredPendingFeedbacks = pendingFeedbacks.filter((fb) => {
        const searchLower = searchTermPending.toLowerCase();
        return fb.title.toLowerCase().includes(searchLower);
    });


    const filteredInProgressFeedbacks = inProgressFeedbacks.filter((fb) => {
        const searchLower = searchTermInProgress.toLowerCase();
        return fb.title.toLowerCase().includes(searchLower);
    });


    // Section visibility based on filter
    const showPendingSection = filter === "ALL" || filter === "PENDING";
    const showInProgressSection = filter === "ALL" || filter === "IN_PROGRESS";


    // Filter bar component
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
                onClick={() => setFilter("PENDING")}
                className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-colors duration-300 ${filter === "PENDING"
                    ? "bg-yellow-600 text-white"
                    : "bg-white border border-gray-300 text-gray-700 hover:bg-yellow-100"
                    }`}
            >
                <FaFilter /> Pending
            </button>
            <button
                onClick={() => setFilter("IN_PROGRESS")}
                className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-colors duration-300 ${filter === "IN_PROGRESS"
                    ? "bg-green-600 text-white"
                    : "bg-white border border-gray-300 text-gray-700 hover:bg-green-100"
                    }`}
            >
                <FaFilter /> In Progress
            </button>
        </div>
    );


    // DESKTOP CHECK - Show error message if accessed from mobile or small screen
    if (!isDesktop) {
        return (
            <div className="flex items-center justify-center h-screen p-4">
                <div className="text-center bg-white p-8 rounded-xl shadow-lg border border-gray-200">
                    <h1 className="text-2xl font-bold mb-4 text-amber-950">
                        Desktop Access Required
                    </h1>
                    <p className="text-gray-700">
                        The <span className="font-semibold">Manage Feedbacks</span> functionality
                        is only accessible on a desktop or a large screen.
                        <br />
                        Please switch to a device with a screen width of{" "}
                        <span className="font-semibold">990px or more</span>.
                    </p>
                </div>
            </div>
        );
    }


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
            </div>


            {/* Filter Bar */}
            <FilterBar />


            {/* NO DATA MESSAGE */}
            {filter !== "ALL" &&
                ((filter === "PENDING" && filteredPendingFeedbacks.length === 0) ||
                    (filter === "IN_PROGRESS" &&
                        filteredInProgressFeedbacks.length === 0)) && (
                    <main className="w-full h-full flex flex-col items-center justify-center p-4">
                        <h3 className="text-xl font-bold text-[#522320] mb-1.5 text-center">
                            No{" "}
                            {filter === "PENDING"
                                ? "Pending"
                                : filter === "IN_PROGRESS"
                                    ? "In-Progress"
                                    : ""}{" "}
                            Feedbacks Found
                        </h3>
                        <p className="text-[#522320]/60 text-sm text-center">
                            {filter === "PENDING"
                                ? "No pending feedback templates to display."
                                : "No in-progress feedback templates to display."}
                        </p>
                    </main>
                )}


            {/* PENDING FEEDBACK TABLE */}
            {showPendingSection && pendingFeedbacks.length > 0 && (
                <div className="border rounded-xl p-4 bg-white/80 shadow-sm border-yellow-500 max-w-full">
                    <div className="mb-3 flex flex-col md:flex-row md:items-center md:justify-between gap-2">
                        <h2 className="text-xl font-bold text-yellow-700">
                            Pending Feedbacks
                        </h2>
                        <input
                            type="text"
                            placeholder="Search by title..."
                            value={searchTermPending}
                            onChange={(e) => setSearchTermPending(e.target.value)}
                            className="w-full md:w-1/3 rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-yellow-500 focus:border-yellow-500"
                        />
                    </div>


                    {filteredPendingFeedbacks.length === 0 ? (
                        <p className="text-center text-gray-500 py-6">
                            {searchTermPending
                                ? "No feedbacks match your search."
                                : "No pending feedbacks found."}
                        </p>
                    ) : (
                        <div className="overflow-x-auto w-full">
                            <table className="w-full text-sm border-collapse">
                                <thead>
                                    <tr className="bg-yellow-100 text-yellow-900">
                                        <th className="p-3 text-center">Title</th>
                                        <th className="p-3 text-center">Assigned Roles</th>
                                        <th className="p-3 text-center">Submissions</th>
                                        <th className="p-3 text-center">Created At</th>
                                        <th className="p-3 text-center">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredPendingFeedbacks.map((fb) => (
                                        <tr
                                            key={fb.feedbackId}
                                            className="border-t hover:bg-yellow-50 transition-colors"
                                        >
                                            <td className="p-3 font-medium text-center break-words max-w-xs">
                                                {fb.title}
                                            </td>
                                            <td className="p-3 text-center break-words max-w-xs">
                                                <div className="flex flex-wrap justify-center gap-1">
                                                    {renderRoles(fb.assignedRoles)}
                                                </div>
                                            </td>
                                            <td className="p-3 text-center text-xs text-yellow-800 font-medium">
                                                {fb.submissionCount}
                                            </td>
                                            <td className="p-3 text-center text-xs text-yellow-800 font-medium">
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
                                                    className="px-3 py-2 rounded-lg bg-orange-600 text-white font-semibold hover:bg-orange-700 hover:scale-95 transition-transform text-xs"
                                                >
                                                    View
                                                </button>
                                                <button
                                                    onClick={() => {
                                                        setSelectedFeedback(fb);
                                                        setShowDeleteModal(true);
                                                    }}
                                                    className="px-3 py-2 rounded-lg bg-red-600 text-white font-semibold hover:bg-red-700 hover:scale-95 transition-transform text-xs"
                                                >
                                                    Delete
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}


            {/* IN-PROGRESS FEEDBACK TABLE */}
            {showInProgressSection && inProgressFeedbacks.length > 0 && (
                <div className="border rounded-xl p-4 bg-white/80 shadow-sm border-green-300 max-w-full">
                    <div className="mb-3 flex flex-col md:flex-row md:items-center md:justify-between gap-2">
                        <h2 className="text-xl font-bold text-green-700">
                            In-Progress Feedbacks
                        </h2>
                        <input
                            type="text"
                            placeholder="Search by title..."
                            value={searchTermInProgress}
                            onChange={(e) => setSearchTermInProgress(e.target.value)}
                            className="w-full md:w-1/3 rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
                        />
                    </div>


                    {filteredInProgressFeedbacks.length === 0 ? (
                        <p className="text-center text-gray-500 py-6">
                            {searchTermInProgress
                                ? "No feedbacks match your search."
                                : "In-progress feedbacks will appear here."}
                        </p>
                    ) : (
                        <div className="overflow-x-auto w-full">
                            <table className="w-full text-sm border-collapse">
                                <thead>
                                    <tr className="bg-green-100 text-green-900">
                                        <th className="p-3 text-center">Title</th>
                                        <th className="p-3 text-center">Assigned Roles</th>
                                        <th className="p-3 text-center">Submissions</th>
                                        <th className="p-3 text-center">Created At</th>
                                        <th className="p-3 text-center">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredInProgressFeedbacks.map((fb) => (
                                        <tr
                                            key={fb.feedbackId}
                                            className="border-t hover:bg-green-50 transition-colors"
                                        >
                                            <td className="p-3 font-medium text-center break-words max-w-xs">
                                                {fb.title}
                                            </td>
                                            <td className="p-3 text-center break-words max-w-xs">
                                                <div className="flex flex-wrap justify-center gap-1">
                                                    {renderRoles(fb.assignedRoles)}
                                                </div>
                                            </td>
                                            <td className="p-3 text-center text-xs text-green-800 font-medium">
                                                {fb.submissionCount}
                                            </td>
                                            <td className="p-3 text-center text-xs text-green-800 font-medium">
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
                                                    className="px-3 py-2 rounded-lg bg-green-600 text-white font-semibold hover:bg-green-700 hover:scale-95 transition-transform text-xs"
                                                >
                                                    View
                                                </button>
                                                <button
                                                    onClick={() => {
                                                        setSelectedFeedback(fb);
                                                        setShowDeleteModal(true);
                                                    }}
                                                    className="px-3 py-2 rounded-lg bg-red-600 text-white font-semibold hover:bg-red-700 hover:scale-95 transition-transform text-xs"
                                                >
                                                    Delete
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
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
