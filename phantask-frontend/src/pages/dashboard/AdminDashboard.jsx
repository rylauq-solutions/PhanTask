import React from "react";
import CreateUserCard from "../../components/dashboard_cards/CreateUserCard.jsx";
import CreateTasksCard from "../../components/dashboard_cards/CreateTasksCard.jsx";
import AddRoleCard from "../../components/dashboard_cards/AddRoleCard.jsx";
import CreateFeedbackCard from "../../components/dashboard_cards/CreateFeedbackCard.jsx";
import CreateNoticeCard from "../../components/dashboard_cards/CreateNoticeCard.jsx";
import ThoughtsCard from "../../components/dashboard_cards/ThoughtsCard.jsx";

const AdminDashboard = ({ attendancePercentage }) => {
    return (
        <>
            {/* Main Grid Layout */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mb-3">
                <div className="h-72"><ThoughtsCard /></div>
                <div className="h-72"><CreateUserCard /></div>
            </div>

            {/* Middle Section - 2 Columns */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mb-3">
                <div className="h-72"><CreateTasksCard /></div>
                <div className="h-72"><CreateFeedbackCard /></div>
            </div>

            {/* Bottom Section - 2 Columns */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mb-3">
                <div className="h-72"><CreateNoticeCard /></div>
                <div className="h-72"><AddRoleCard /></div>
            </div>
        </>
    );
};

export default AdminDashboard;
