import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useDashboard } from '../../context/DashboardContext';
import LoadingSkeleton from '../LoadingSkeleton';

const ScheduleCard = () => {
    const { schedule, loading } = useDashboard();
    const [localLoading, setLocalLoading] = useState(true);

    useEffect(() => {
        const timer = setTimeout(() => {
            setLocalLoading(false);
        }, 1000);
        return () => clearTimeout(timer);
    }, []);

    if (loading || localLoading) {
        return <LoadingSkeleton rows={3} hasButton={true} />;
    }

    const scheduleData = schedule || [];

    return (
        <div className="w-full h-full rounded-xl border-2 border-[#522320] bg-[#ffffff] p-3 shadow-md shadow-[#522320]/20 transition-all duration-300 hover:shadow-xl hover:shadow-[#522320]/30 hover:-translate-y-0.5 flex flex-col">
            <span className='w-full h-full flex flex-col justify-between'>
                <h2 className="h-10 text-lg font-semibold py-1 text-[#522320] text-center">Schedule</h2>

                {scheduleData.length !== 0 ? (
                    <main className='w-full overflow-y-auto'>
                        <table className="w-full border-collapse text-[#522320]">
                            <thead>
                                <tr className="border-b border-[#c8a07e]">
                                    <th className="py-2 text-center text-sm">Date</th>
                                    <th className="py-2 text-center text-sm">Subject</th>
                                    <th className="py-2 text-center text-sm">Time</th>
                                </tr>
                            </thead>
                            <tbody>
                                {scheduleData.map(({ date, subject, time }, idx) => (
                                    <tr key={idx} className={`${idx !== scheduleData.length - 1 ? 'border-b border-[#c8a07e]' : ''}`}>
                                        <td className="text-sm font-medium text-center py-2">{date}</td>
                                        <td className="text-sm font-medium text-center py-2">{subject}</td>
                                        <td className="text-sm font-medium text-center py-2">{time}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </main>
                ) : (
                    <main className='w-full h-full flex flex-col items-center justify-center p-4 bg-gradient-to-b from-[#fff9f8]/30 to-[#fff1f0]/20 rounded-xl border-[#522320]/20 shadow-sm'>
                        <div className="w-14 h-14 bg-[#522320]/5 rounded-2xl flex items-center justify-center mb-3 shadow-md shadow-[#522320]/10">
                            <span className="text-2xl">📅</span>
                        </div>
                        <h3 className="text-xl font-bold text-[#522320] mb-1.5 leading-tight">No Schedule</h3>
                        <p className="text-[#522320]/60 text-xs font-medium text-center leading-tight max-w-[140px]">
                            Check back later! 🚀
                        </p>
                    </main>
                )}

                {scheduleData.length !== 0 && (
                    <Link to="/schedule">
                        <button className="w-full text-center hover:rounded-xl text-[#42260b] text-sm font-medium mt-1 py-2 hover:bg-red-100">
                            View all
                        </button>
                    </Link>
                )}
            </span>
        </div>
    );
};

export default ScheduleCard;
