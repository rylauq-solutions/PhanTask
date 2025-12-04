import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import LoadingSkeleton from '../LoadingSkeleton';
import { apiService } from '../../services/api';

const AssignedTasksCard = () => {
    const [pendingTasks, setPendingTasks] = useState([]); // Local state for tasks
    const [loading, setLoading] = useState(true);         // Loading state

    // Fetch pending tasks from API
    const fetchPendingTasks = async () => {
        try {
            setLoading(true);
            const res = await apiService.getMyPendingTasks();
            setPendingTasks(res.data || []);
        } catch (err) {
            console.error("Failed to fetch pending tasks:", err);
            setPendingTasks([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchPendingTasks();
    }, []);

    // Show loading skeleton while fetching
    if (loading) {
        return <LoadingSkeleton rows={3} hasButton={true} />;
    }

    const formatDate = (dateStr) => {
        if (!dateStr) return "-";
        const d = new Date(dateStr);
        return `${d.getDate().toString().padStart(2, "0")}-${(d.getMonth() + 1)
            .toString()
            .padStart(2, "0")}-${d.getFullYear()}`;
    };

    return (
        <div className="w-full h-full rounded-xl border-2 border-[#522320] bg-[#ffffff] p-3 shadow-md shadow-[#522320]/20 transition-all duration-300 hover:shadow-xl hover:shadow-[#522320]/30 hover:-translate-y-0.5 flex flex-col">
            <span className='w-full h-full flex flex-col justify-between'>
                <h2 className="h-10 text-lg font-semibold py-1 text-[#522320] text-center">Pending Tasks</h2>

                {(pendingTasks.length !== 0) ?
                    <main className='h-full w-full overflow-y-auto'>
                        <table className="w-full border-collapse text-[#522320]">
                            <thead>
                                <tr className="border-b border-[#c8a07e]">
                                    <th className="py-2 text-center text-sm">Due Date</th>
                                    <th className="py-2 text-center text-sm">Tasks</th>
                                </tr>
                            </thead>
                            <tbody>
                                {pendingTasks.map(({ dueDate, taskName, id }, idx) => (
                                    <tr key={id || idx} className={`${idx !== pendingTasks.length - 1 ? 'border-b border-[#c8a07e]' : ''}`}>
                                        <td className="text-sm font-medium text-center py-2">{formatDate(dueDate)}</td>
                                        <td className="text-sm font-medium text-center py-2">
                                            {taskName.length > 30 ? taskName.slice(0, 30) + "…" : taskName}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </main> :
                    <main className='w-full h-full flex flex-col items-center justify-center p-4 bg-gradient-to-b from-[#fff9f8]/30 to-[#fff1f0]/20 rounded-xl border-[#522320]/20 shadow-sm'>
                        <div className="w-14 h-14 bg-[#522320]/5 rounded-2xl flex items-center justify-center mb-3 shadow-md shadow-[#522320]/10">
                            <span className="text-2xl">🎉</span>
                        </div>
                        <h3 className="text-xl font-bold text-[#522320] mb-1.5 leading-tight">All Caught Up!</h3>
                        <p className="text-[#522320]/60 text-xs font-medium text-center leading-tight max-w-[140px]">
                            No pending tasks.<br></br> Great work! 🚀
                        </p>
                    </main>
                }

                {(pendingTasks.length !== 0) && <Link to={'/tasks'}>
                    <button className="w-full text-center hover:rounded-xl text-[#42260b] text-sm font-medium mt-1 py-2 hover:bg-red-100">
                        View all
                    </button>
                </Link>}
            </span>
        </div>
    )
}

export default AssignedTasksCard;
