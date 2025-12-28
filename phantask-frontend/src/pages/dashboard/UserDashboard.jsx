import React from "react";
import AttendanceCard from "../../components/dashboard_cards/AttendanceCard";
import NoticeBoardCard from "../../components/dashboard_cards/NoticeBoardCard";
import FeedbackSummaryCard from "../../components/dashboard_cards/FeedbackSummaryCard";
import AssignedTasksCard from "../../components/dashboard_cards/AssignedTasksCard";
import ThoughtsCard from "../../components/dashboard_cards/ThoughtsCard.jsx";

const UserDashboard = ({ attendancePercentage }) => {
    return (
        <>
            {/* Main Grid Layout */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-3 mb-3">
                <div className="h-72"><ThoughtsCard /></div>
                <div className="h-72"><AssignedTasksCard /></div>
                <div className="h-72">
                    <AttendanceCard attendancePercentage={attendancePercentage} />
                </div>
            </div>

            {/* Middle Section - 2 Columns */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mb-3">
                <div className="h-60"><FeedbackSummaryCard /></div>
                <div className="h-60"><NoticeBoardCard /></div>
            </div>
        </>
    );
};

export default UserDashboard;
