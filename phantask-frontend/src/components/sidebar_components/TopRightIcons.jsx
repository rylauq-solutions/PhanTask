import React from 'react'
import ProfileDropDown from '../ProfileDropDown';
import Notification from "../Notification";
import { useAuth } from "../../context/AuthContext";
import mascot from "../../assets/Mascot-Phantask.png";

const TopRightIcons = ({ isMobileOpen }) => {
    const { user } = useAuth(); // Get user from context

    // console.log(user);

    return (
        <div>
            {!isMobileOpen && (
                <>
                    <div className="fixed top-3 right-16 z-50">
                        <Notification />
                    </div>

                    <div className="fixed top-3 right-5 z-50">
                        <ProfileDropDown profilePic={user?.profilePic || mascot} />
                    </div>
                </>
            )}
        </div>
    )
}

export default TopRightIcons