import React, { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { apiService } from "../services/api";
import { toast } from "react-hot-toast";
import { FaFilter, FaStar } from "react-icons/fa";

const Feedback = () => {
  const { user, loading } = useAuth();

  const [feedbacks, setFeedbacks] = useState([]);
  const [pageLoading, setPageLoading] = useState(true);

  // Search term
  const [searchTerm, setSearchTerm] = useState("");

  // Modal state
  const [selectedFeedback, setSelectedFeedback] = useState(null);
  const [ratings, setRatings] = useState({});
  const [confirmVisible, setConfirmVisible] = useState(false);
  const [confirmed, setConfirmed] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // Fetch available feedbacks for logged-in user
  useEffect(() => {
    const fetchFeedbacks = async () => {
      if (!user) {
        setPageLoading(false);
        return;
      }
      try {
        const res = await apiService.getAvailableFeedbackForUser();
        setFeedbacks(res.data || []);
      } catch (err) {
        console.error("Failed to fetch feedbacks:", err);
        toast.error("Failed to fetch feedbacks.");
      } finally {
        setPageLoading(false);
      }
    };
    fetchFeedbacks();
  }, [user]);

  if (loading || pageLoading) {
    return <div className="p-4">Loading feedbacks...</div>;
  }

  // Prepare data for UI
  const parsedFeedbacks = feedbacks.map((fb) => ({
    ...fb,
    questionList: fb.questions
      ?.split(",")
      .map((q) => q.trim())
      .filter((q) => q.length > 0),
  }));

  // Search filter
  const filteredFeedbacks = parsedFeedbacks.filter((fb) =>
    fb.title.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const openModal = (fb) => {
    const questionList =
      fb.questions
        ?.split(",")
        .map((q) => q.trim())
        .filter((q) => q.length > 0) || [];
    setSelectedFeedback({ ...fb, questionList });
    // Initialize ratings with 0 (no rating)
    const initialRatings = {};
    questionList.forEach((q) => {
      initialRatings[q] = 0;
    });
    setRatings(initialRatings);
    setConfirmVisible(false);
    setConfirmed(false);
    setSubmitting(false);
  };

  const closeModal = () => {
    setSelectedFeedback(null);
    setRatings({});
    setConfirmVisible(false);
    setConfirmed(false);
    setSubmitting(false);
  };

  const handleRatingChange = (question, value) => {
    setRatings((prev) => ({ ...prev, [question]: value }));
  };

  const handleSubmitFeedback = async () => {
    if (!selectedFeedback) return;

    // Validation: all questions must be rated (1-5)
    const invalid = Object.entries(ratings).some(([, v]) => v === 0 || v < 1 || v > 5);
    if (invalid) {
      toast.error("Please provide a rating (1–5) for every question.");
      return;
    }
    if (!confirmed) {
      toast.error("Please confirm before submitting.");
      return;
    }

    setSubmitting(true);
    try {
      await apiService.submitFeedback(selectedFeedback.feedbackId, ratings);
      toast.success("Feedback submitted successfully!");

      // Remove this feedback from the list
      setFeedbacks((prev) =>
        prev.filter((fb) => fb.feedbackId !== selectedFeedback.feedbackId)
      );
      closeModal();
    } catch (err) {
      console.error(err);
      const msg =
        err?.response?.data ||
        err?.response?.data?.error ||
        "Failed to submit feedback.";
      toast.error(msg);
      setSubmitting(false);
    }
  };

  // Star Rating Component
  const StarRating = ({ question, rating, onChange }) => (
    <div className="flex items-center gap-1">
      {[1, 2, 3, 4, 5].map((star) => (
        <button
          key={star}
          type="button"
          onClick={() => onChange(question, star)}
          className="transition-transform hover:scale-110 focus:outline-none"
        >
          <FaStar
            className={`w-6 h-6 ${star <= rating ? "text-amber-500" : "text-gray-300"
              }`}
          />
        </button>
      ))}
      <span className="ml-2 text-sm text-gray-600 font-medium">
        {rating > 0 ? `${rating}/5` : "Not rated"}
      </span>
    </div>
  );

  // Card for each feedback
  const FeedbackCard = ({ fb }) => (
    <div className="border border-amber-400 rounded-xl p-4 bg-white shadow-sm hover:-translate-y-0.5 transition-all duration-300 flex flex-col justify-between">
      <div>
        <h3 className="font-semibold text-lg text-center text-amber-900">
          {fb.title}
        </h3>
        <p className="mt-3 text-xs text-gray-600 text-center">
          {fb.questionList?.length || 0} question
          {fb.questionList?.length === 1 ? "" : "s"}
        </p>
      </div>
      <button
        onClick={() => openModal(fb)}
        className="mt-4 w-[80%] mx-auto py-2 rounded-lg bg-amber-700 text-white text-sm font-semibold hover:bg-amber-800 hover:scale-95 transition-transform duration-300"
      >
        Fill Feedback
      </button>
    </div>
  );


  // Modal for answering feedback
  const FeedbackModal = () => {
    if (!selectedFeedback) return null;

    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center">
        {/* Overlay */}
        <div className="absolute inset-0 bg-black/40" onClick={closeModal} />

        {/* Modal */}
        <div className="relative w-[90%] sm:w-[85%] md:w-3/5 lg:w-2/5 max-h-[95vh]">
          <div className="bg-white rounded-xl p-4 md:p-6 shadow-xl flex flex-col max-h-[95vh] overflow-y-auto border-2 border-amber-700/60">
            {/* Header */}
            <div className="mb-3 flex-shrink-0 text-center">
              <h3 className="text-2xl font-bold text-amber-950">
                {selectedFeedback.title}
              </h3>
              <p className="text-xs text-gray-600 mt-1">
                Please rate each question from 1 (poor) to 5 (excellent).
              </p>
            </div>

            {/* Questions */}
            <div className="flex-1 flex flex-col gap-4">
              {selectedFeedback.questionList.map((q, idx) => (
                <div
                  key={q}
                  className="border border-gray-200 rounded-lg p-4 flex flex-col gap-3 bg-gray-50"
                >
                  <p className="text-sm font-medium text-gray-800">
                    {idx + 1}. {q}
                  </p>
                  <StarRating
                    question={q}
                    rating={ratings[q]}
                    onChange={handleRatingChange}
                  />
                </div>
              ))}
            </div>

            {/* Footer */}
            <div className="mt-4 flex-shrink-0">
              {!confirmVisible ? (
                <div className="flex gap-2">
                  <button
                    onClick={() => setConfirmVisible(true)}
                    className="flex-1 py-2 rounded-lg bg-amber-700 text-white hover:bg-amber-800 font-semibold hover:scale-95 transition-transform duration-300 shadow"
                  >
                    Submit Feedback
                  </button>
                  <button
                    onClick={closeModal}
                    className="flex-1 py-2 rounded-lg bg-stone-200 hover:bg-stone-300 text-gray-800 font-semibold hover:scale-95 transition-transform duration-300 shadow"
                  >
                    Cancel
                  </button>
                </div>
              ) : (
                <div className="flex flex-col gap-3 border-t pt-3">
                  <label className="flex items-center gap-2 justify-center text-sm">
                    <input
                      type="checkbox"
                      checked={confirmed}
                      onChange={(e) => setConfirmed(e.target.checked)}
                      className="accent-amber-800"
                    />
                    I confirm that these ratings reflect my honest feedback.
                  </label>
                  <div className="flex gap-2">
                    <button
                      onClick={handleSubmitFeedback}
                      disabled={!confirmed || submitting}
                      className={`flex-1 py-2 rounded-lg text-white font-semibold hover:scale-95 transition-transform duration-300 shadow ${confirmed
                        ? "bg-green-600 hover:bg-green-700"
                        : "bg-green-600/60 cursor-not-allowed"
                        }`}
                    >
                      {submitting ? "Submitting..." : "Confirm & Submit"}
                    </button>
                    <button
                      onClick={() => {
                        setConfirmVisible(false);
                        setConfirmed(false);
                      }}
                      className="flex-1 py-2 rounded-lg bg-stone-200 hover:bg-stone-300 text-gray-800 font-semibold hover:scale-95 transition-transform duration-300 shadow"
                    >
                      Back
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Scrollbar styling */}
          <style>{`
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
          `}</style>
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-6 p-4">
      {/* Header */}
      <div className="mb-3 bg-white/60 rounded-xl p-4 shadow-sm border border-gray-100">
        <h1 className="text-2xl text-center md:text-3xl font-bold text-amber-950">
          Feedback
        </h1>
      </div>



      {/* Empty state */}
      {filteredFeedbacks.length === 0 && (
        <main className="w-full h-full flex flex-col items-center justify-center p-4">
          <h3 className="text-xl font-bold text-[#522320] mb-1.5 text-center">
            No Feedback Pending
          </h3>
          <p className="text-[#522320]/60 text-sm text-center">
            {searchTerm
              ? "No feedbacks match your search."
              : "There are no feedback forms assigned to you right now."}
          </p>
        </main>
      )}

      {/* Cards */}
      {filteredFeedbacks.length > 0 && (
        <>
          {/* Search */}
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-2">
            <div className="flex items-center justify-center md:justify-start gap-2">
              <FaFilter className="text-gray-500" />
              <span className="text-sm text-gray-700">
                Available Feedback Forms
              </span>
            </div>
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
          <div className="border border-amber-400 rounded-xl p-4 bg-white/80 shadow-sm">
            <h2 className="text-xl font-bold mb-3 text-center md:text-left text-amber-700">
              Available Feedbacks
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {filteredFeedbacks.map((fb) => (
                <FeedbackCard key={fb.feedbackId} fb={fb} />
              ))}
            </div>
          </div>

        </>
      )}

      <FeedbackModal />
    </div>
  );
};

export default Feedback;
