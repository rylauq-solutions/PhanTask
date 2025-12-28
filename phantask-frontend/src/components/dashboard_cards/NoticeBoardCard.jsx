import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import LoadingSkeleton from '../LoadingSkeleton';
import { apiService } from '../../services/api';

const NoticeBoardCard = () => {
  const [notices, setNotices] = useState([]); // Local state for notices
  const [loading, setLoading] = useState(true); // Loading state

  // Fetch notices from API
  const fetchNotices = async () => {
    try {
      setLoading(true);
      const res = await apiService.getMyNotices();
      const allNotices = res.data || [];

      // Get date from 1 week ago
      const oneWeekAgo = new Date();
      oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);

      // Filter notices from last 7 days, sort by most recent, and take only first 6
      const recentNotices = allNotices
        .filter(notice => new Date(notice.createdAt) >= oneWeekAgo)
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 6);

      setNotices(recentNotices);
    } catch (err) {
      console.error("Failed to fetch notices:", err);
      setNotices([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotices();
  }, []);

  // Show loading skeleton while fetching
  if (loading) {
    return <LoadingSkeleton rows={3} hasButton={true} />;
  }

  // Format date as "25 Dec"
  const formatDate = (dateStr) => {
    if (!dateStr) return "-";
    const d = new Date(dateStr);
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return `${d.getDate()} ${months[d.getMonth()]}`;
  };

  // Get priority indicator color
  const getPriorityColor = (priority) => {
    switch (priority) {
      case "URGENT":
        return "bg-red-500";
      case "IMPORTANT":
        return "bg-yellow-500";
      case "GENERAL":
      default:
        return "bg-pink-600";
    }
  };

  return (
    <div className="w-full h-full rounded-xl border-2 border-[#522320] bg-[#ffffff] p-3 shadow-md shadow-[#522320]/20 transition-all duration-300 hover:shadow-xl hover:shadow-[#522320]/30 hover:-translate-y-0.5 flex flex-col">
      <span className='w-full h-full flex flex-col justify-between'>
        <h2 className="h-10 text-lg font-semibold py-1 text-[#522320] text-center">
          Notice Board
        </h2>

        {(notices.length !== 0) ? (
          <main className='h-full w-full overflow-y-auto'>
            <table className="w-full border-collapse text-[#522320]">
              <thead>
                <tr className="border-b border-[#c8a07e]">
                  <th className="py-2 text-center text-sm">Date</th>
                  <th className="py-2 text-center text-sm">Notice</th>
                </tr>
              </thead>
              <tbody>
                {notices.map((notice, idx) => (
                  <tr key={notice.id || idx} className={`${idx !== notices.length - 1 ? 'border-b border-[#c8a07e]' : ''}`}>
                    <td className="text-sm font-medium text-center py-2">
                      {formatDate(notice.createdAt)}
                    </td>
                    <td className="text-sm font-medium text-center py-2">
                      <div className="flex items-center justify-center gap-2">
                        <span className={`inline-block w-2 h-2 rounded-full flex-shrink-0 ${getPriorityColor(notice.priority)}`}></span>
                        <span className="truncate">
                          {notice.title.length > 28 ? notice.title.slice(0, 28) + "…" : notice.title}
                        </span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </main>
        ) : (
          <main className='w-full h-full flex flex-col items-center justify-center p-4 bg-gradient-to-b from-[#fff9f8]/30 to-[#fff1f0]/20 rounded-xl border-[#522320]/20 shadow-sm'>
            <div className="w-14 h-14 bg-[#522320]/5 rounded-2xl flex items-center justify-center mb-3 shadow-md shadow-[#522320]/10">
              <span className="text-2xl">📢</span>
            </div>
            <h3 className="text-xl font-bold text-[#522320] mb-1.5 leading-tight">No Recent Notices</h3>
            <p className="text-[#522320]/60 text-xs font-medium text-center max-w-[160px]">
              No notices from the last 7 days. Check back later! 📌
            </p>
          </main>
        )}

        {(notices.length !== 0) ? (
          <Link to={'/notices'}>
            <button className="w-full text-center hover:rounded-xl text-[#42260b] text-sm font-medium mt-1 py-2 hover:bg-red-100">
              View all
            </button>
          </Link>
        ) : (
          <div className="h-10 mt-1 py-2"></div>
        )}
      </span>
    </div>
  );
};

export default NoticeBoardCard;
