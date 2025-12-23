import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiService } from '../../services/api';

const FeedbackSummaryCard = () => {
  const [submittedCount, setSubmittedCount] = useState(0);
  const [pendingCount, setPendingCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchFeedbackCounts = async () => {
      try {
        setLoading(true);

        // Fetch total submitted feedbacks count
        const submittedRes = await apiService.getSubmittedFeedbackCount();
        setSubmittedCount(submittedRes.data || 0);

        // Fetch available (pending) feedbacks
        const availableRes = await apiService.getAvailableFeedbackForUser();
        setPendingCount(availableRes.data?.length || 0);
      } catch (err) {
        console.error('Failed to fetch feedback counts:', err);
        setSubmittedCount(0);
        setPendingCount(0);
      } finally {
        setLoading(false);
      }
    };

    fetchFeedbackCounts();
  }, []);

  const feedbackStatus = [
    { type: 'Submitted', count: submittedCount },
    { type: 'Pending', count: pendingCount },
  ];

  return (
    <div className="w-full h-full rounded-xl border-2 border-[#522320] bg-[#ffffff] p-4 shadow-md shadow-[#522320]/20 transition-all duration-300 hover:shadow-xl hover:shadow-[#522320]/30 hover:-translate-y-0.5 flex flex-col">
      <div className='w-full h-full flex flex-col justify-between'>

        <h2 className="text-lg font-semibold text-[#522320] mb-4 text-center">
          Feedback Summary
        </h2>

        <main className='w-full h-full flex flex-col justify-center items-center'>
          {loading ? (
            <p className="text-sm text-gray-500">Loading...</p>
          ) : (
            <ul className="w-full space-y-3">
              {feedbackStatus.map(({ type, count }) => (
                <li key={type} className="flex justify-between items-center text-[#522320] text-sm font-medium">
                  <span>{type} Feedbacks</span>
                  <span className={`font-bold px-3 py-1.5 rounded-full text-sm ${type === 'Pending'
                    ? 'bg-red-600 text-[#fff1f0]'
                    : 'bg-green-600 text-white'
                    }`}>
                    {count}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </main>

        <Link to={'/feedback'} className='w-full'>
          <button className="w-full text-center hover:rounded-xl text-[#42260b] text-sm font-medium mt-1 py-2 hover:bg-red-100 transition-colors duration-200">
            View Pending
          </button>
        </Link>
      </div>
    </div>
  );
};

export default FeedbackSummaryCard;
