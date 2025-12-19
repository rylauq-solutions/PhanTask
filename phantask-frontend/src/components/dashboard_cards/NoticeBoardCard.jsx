import React from 'react';
import { Link } from 'react-router-dom';

const notices = [
  // { id: 1, date: '25 Oct', message: 'Library will be closed this Friday.' },
  // { id: 2, date: '25 Oct', message: 'Lost & Found: Blue backpack at reception.' },
  // { id: 3, date: '24 Oct', message: 'Annual Sports registrations open now!' },
  // { id: 4, date: '24 Oct', message: 'Annual Sports registrations open now!' },
  // { id: 5, date: '24 Oct', message: 'Annual Sports registrations open now!' },
  // { id: 6, date: '24 Oct', message: 'Annual Sports registrations open now!' },
];

const NoticeBoardCard = () => {
  return (
    <div className="w-full h-full rounded-xl border-2 border-[#522320] bg-[#ffffff] p-3 shadow-md shadow-[#522320]/20 transition-all duration-300 hover:shadow-xl hover:shadow-[#522320]/30 hover:-translate-y-0.5 flex flex-col">
      <span className='w-full h-full flex flex-col justify-between'>

        <h2 className="h-10 text-lg font-semibold py-1 text-[#522320] text-center">
          Notice Board
        </h2>

        {(notices.length !== 0) ? (
          <main className='h-full w-full overflow-y-auto'>
            <ul className="w-full flex flex-col gap-2">
              {notices.map(notice => (
                <li key={notice.id} className="flex items-center">
                  <span className="text-sm text-[#73462a] font-medium w-20 text-center flex-shrink-0">
                    {notice.date}
                  </span>
                  <span className="ml-3 text-sm text-[#522320]">{notice.message}</span>
                </li>
              ))}
            </ul>
          </main>
        ) : (
          <main className='w-full h-full flex flex-col items-center justify-center p-4 bg-gradient-to-b from-[#fff9f8]/30 to-[#fff1f0]/20 rounded-xl border-[#522320]/20 shadow-sm'>
            <div className="w-14 h-14 bg-[#522320]/5 rounded-2xl flex items-center justify-center mb-3 shadow-md shadow-[#522320]/10">
              <span className="text-2xl">📢</span>
            </div>
            <h3 className="text-xl font-bold text-[#522320] mb-1.5 leading-tight">No Notices</h3>
            <p className="text-[#522320]/60 text-xs font-medium text-center max-w-[160px]">
              Check back later for updates! Stay Tuned! 📌
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
