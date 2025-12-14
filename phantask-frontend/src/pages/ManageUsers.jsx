import React, { useEffect, useState } from "react";
import { apiService } from "../services/api";
import { toast } from "react-hot-toast";
import LoadingSkeleton from "../components/LoadingSkeleton";
import { FaFilter } from "react-icons/fa";

// * ============================================================================
// * MANAGE USERS COMPONENT
// * ============================================================================
// ! This component is only accessible on desktop (screen width >= 990px)
// ? Handles user management: view, search, filter, deactivate, and reactivate users

const ManageUsers = () => {
    // * ============================================================================
    // * STATE MANAGEMENT
    // * ============================================================================

    // User lists
    const [activeUsers, setActiveUsers] = useState([]);
    const [inactiveUsers, setInactiveUsers] = useState([]);

    // Loading states
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(null);

    // Modal and selection states
    const [selectedUser, setSelectedUser] = useState(null);
    const [showDeactivateModal, setShowDeactivateModal] = useState(false);
    const [showReactivateModal, setShowReactivateModal] = useState(false);

    // Search states
    const [searchTermActive, setSearchTermActive] = useState("");
    const [searchTermInactive, setSearchTermInactive] = useState("");

    // Filter state: ALL, ACTIVE, INACTIVE
    const [filter, setFilter] = useState("ALL");

    // Desktop detection state
    const [isDesktop, setIsDesktop] = useState(window.innerWidth >= 990);

    // * ============================================================================
    // * DESKTOP DETECTION
    // * ============================================================================
    // ! Component only works on desktop screens (>= 990px)

    useEffect(() => {
        const handleResize = () => {
            setIsDesktop(window.innerWidth >= 990);
        };

        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, []);

    // * ============================================================================
    // * NATURAL SORT FUNCTION
    // * ============================================================================
    // ? Sorts usernames alphabetically, then numerically (e.g., user1, user2, user10)

    const naturalSort = (a, b) => {
        const regex = /^([a-zA-Z]+)(\d*)$/;
        const [, textA = "", numA = "0"] = a.username.match(regex) || [];
        const [, textB = "", numB = "0"] = b.username.match(regex) || [];

        const textCompare = textA.localeCompare(textB);
        if (textCompare !== 0) return textCompare;
        return Number(numA) - Number(numB);
    };

    // * ============================================================================
    // * FETCH USERS ON MOUNT
    // * ============================================================================
    // ? Fetches active and inactive users, filters out ADMINs, and sorts them

    useEffect(() => {
        if (!isDesktop) return; // Don't fetch if not desktop

        const fetchUsers = async () => {
            try {
                // Fetch active users
                const resActive = await apiService.getAllActiveUsers();
                const sortedActive = resActive.data
                    .filter((u) => !u.roles.includes("ADMIN"))
                    .sort(naturalSort);
                setActiveUsers(sortedActive);

                // Fetch inactive users
                const resInactive = await apiService.getAllInactiveUsers();
                const sortedInactive = resInactive.data
                    .filter((u) => !u.roles.includes("ADMIN"))
                    .sort(naturalSort);
                setInactiveUsers(sortedInactive);

            } catch (err) {
                console.error(err);
                toast.error("Failed to load users");
            } finally {
                setLoading(false);
            }
        };

        fetchUsers();
    }, [isDesktop]);

    // * ============================================================================
    // * FILTERED USERS (WITH SEARCH)
    // * ============================================================================
    // ? Filters users based on search terms (username or email)

    const filteredActiveUsers = activeUsers.filter(
        (user) =>
            user.username.toLowerCase().includes(searchTermActive.toLowerCase()) ||
            user.email.toLowerCase().includes(searchTermActive.toLowerCase())
    );

    const filteredInactiveUsers = inactiveUsers.filter(
        (user) =>
            user.username.toLowerCase().includes(searchTermInactive.toLowerCase()) ||
            user.email.toLowerCase().includes(searchTermInactive.toLowerCase())
    );

    // * ============================================================================
    // * SECTION VISIBILITY (BASED ON FILTER)
    // * ============================================================================
    // ? Determines which sections to show based on selected filter

    const showActiveSection = filter === "ALL" || filter === "ACTIVE";
    const showInactiveSection = filter === "ALL" || filter === "INACTIVE";

    // * ============================================================================
    // * DEACTIVATE USER HANDLER
    // * ============================================================================
    // ! Moves user from active list to inactive list

    const handleDeactivate = async () => {
        if (!selectedUser) return;

        setActionLoading(selectedUser.uid);

        try {
            await apiService.deactivateUser(selectedUser.uid);
            toast.success("User deactivated successfully");

            // Remove from activeUsers
            setActiveUsers(prev => prev.filter(u => u.uid !== selectedUser.uid));

            // Add to inactiveUsers
            setInactiveUsers(prev => [...prev, selectedUser]);

            setShowDeactivateModal(false);
            setSelectedUser(null);
        } catch (err) {
            console.error(err);
            toast.error("Failed to deactivate user");
        } finally {
            setActionLoading(null);
        }
    };

    // * ============================================================================
    // * REACTIVATE USER HANDLER
    // * ============================================================================
    // ! Moves user from inactive list to active list

    const handleReactivate = async () => {
        if (!selectedUser) return;

        setActionLoading(selectedUser.uid);

        try {
            await apiService.reactivateUser(selectedUser.uid);
            toast.success("User reactivated successfully");

            // Remove from inactiveUsers
            setInactiveUsers(prev => prev.filter(u => u.uid !== selectedUser.uid));

            // Add to activeUsers
            setActiveUsers(prev => [...prev, selectedUser]);

            setShowReactivateModal(false);
            setSelectedUser(null);
        } catch (err) {
            console.error(err);
            toast.error("Failed to reactivate user");
        } finally {
            setActionLoading(null);
        }
    };

    // * ============================================================================
    // * FILTER BAR COMPONENT
    // * ============================================================================
    // ? Three filter buttons: All, Active, Inactive

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
                onClick={() => setFilter("ACTIVE")}
                className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-colors duration-300 ${filter === "ACTIVE"
                    ? "bg-green-600 text-white"
                    : "bg-white border border-gray-300 text-gray-700 hover:bg-green-100"
                    }`}
            >
                <FaFilter /> Active
            </button>
            <button
                onClick={() => setFilter("INACTIVE")}
                className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-colors duration-300 ${filter === "INACTIVE"
                    ? "bg-red-600 text-white"
                    : "bg-white border border-gray-300 text-gray-700 hover:bg-red-100"
                    }`}
            >
                <FaFilter /> Inactive
            </button>
        </div>
    );

    // * ============================================================================
    // * DESKTOP CHECK
    // * ============================================================================
    // ! Show error message if accessed from mobile or small screen

    if (!isDesktop) {
        return (
            <div className="flex items-center justify-center h-screen p-4">
                <div className="text-center bg-white p-8 rounded-xl shadow-lg border border-gray-200">
                    <h1 className="text-2xl font-bold mb-4 text-amber-950">
                        Desktop Access Required
                    </h1>
                    <p className="text-gray-700">
                        The <span className="font-semibold">Manage Users</span> functionality is only accessible on a desktop or a large screen.
                        <br />
                        Please switch to a device with a screen width of <span className="font-semibold">990px or more</span>.
                    </p>
                </div>
            </div>
        );
    }

    // * ============================================================================
    // * LOADING STATE
    // * ============================================================================
    // ? Shows skeleton loader while fetching users

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

    // * ============================================================================
    // * MAIN RENDER
    // * ============================================================================

    return (
        <div className="space-y-6 p-4">
            {/* PAGE HEADER */}
            <div className="mb-3 bg-white/60 rounded-xl p-4 shadow-sm border border-gray-100">
                <h1 className="text-2xl md:text-3xl font-bold text-center text-amber-950">
                    Manage Users
                </h1>
            </div>

            {/* Filter Bar */}
            <FilterBar />

            {/* No users message when filtered */}
            {filter !== "ALL" &&
                ((filter === "ACTIVE" && filteredActiveUsers.length === 0) ||
                    (filter === "INACTIVE" && filteredInactiveUsers.length === 0)) && (
                    <main className="w-full h-full flex flex-col items-center justify-center p-4">
                        <h3 className="text-xl font-bold text-[#522320] mb-1.5 text-center">
                            No {filter === "ACTIVE" ? "Active" : "Inactive"} Users Found
                        </h3>
                        <p className="text-[#522320]/60 text-sm text-center">
                            {filter === "ACTIVE"
                                ? "No active users to display."
                                : "No inactive users to display."}
                        </p>
                    </main>
                )}

            {/* ============================================================================ */}
            {/* ACTIVE USERS TABLE */}
            {/* ============================================================================ */}
            {/* ? Shows active users with search and action buttons */}

            {showActiveSection && filteredActiveUsers.length > 0 && (
                <div className="border rounded-xl p-4 bg-white/80 shadow-sm border-green-500">
                    <div className="mb-3 flex flex-col md:flex-row md:items-center md:justify-between gap-2">
                        <h2 className="text-xl font-bold text-green-700">Active Users</h2>
                        <input
                            type="text"
                            placeholder="Search by username or email..."
                            value={searchTermActive}
                            onChange={(e) => setSearchTermActive(e.target.value)}
                            className="w-full md:w-1/3 rounded-lg border border-gray-300 px-3 py-2 text-sm
                            focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
                        />
                    </div>

                    {filteredActiveUsers.length === 0 ? (
                        <p className="text-center text-gray-500 py-6">
                            {searchTermActive ? "No users match your search." : "No active users found."}
                        </p>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-sm border-collapse">
                                <thead>
                                    <tr className="bg-green-100 text-green-900">
                                        <th className="p-3 text-center">Username</th>
                                        <th className="p-3 text-center">Email</th>
                                        <th className="p-3 text-center">Roles</th>
                                        <th className="p-3 text-center">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredActiveUsers.map((user) => (
                                        <tr
                                            key={user.uid}
                                            className="border-t hover:bg-green-50 transition-colors"
                                        >
                                            <td className="p-3 font-medium text-center">{user.username}</td>
                                            <td className="p-3 text-center">{user.email}</td>
                                            <td className="p-3">
                                                <div className="flex flex-wrap justify-center gap-1">
                                                    {(user.roles.length ? user.roles : ["N/A"]).map((role) => (
                                                        <span
                                                            key={role}
                                                            className="inline-flex items-center rounded-full bg-[#FCE0D6] px-2.5 py-1 text-xs font-medium text-[#8c432b]"
                                                        >
                                                            {role}
                                                        </span>
                                                    ))}
                                                </div>
                                            </td>
                                            <td className="p-3 flex items-center justify-center gap-2">
                                                <button className="px-3 py-2 rounded-lg bg-yellow-500 text-white font-semibold hover:bg-yellow-600 hover:scale-95 transition-transform">
                                                    Edit
                                                </button>
                                                <button
                                                    onClick={() => {
                                                        setSelectedUser(user);
                                                        setShowDeactivateModal(true);
                                                    }}
                                                    className="px-3 py-2 rounded-lg bg-red-500 text-white font-semibold hover:bg-red-600 hover:scale-95 transition-all"
                                                >
                                                    Deactivate
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

            {/* ============================================================================ */}
            {/* INACTIVE USERS TABLE */}
            {/* ============================================================================ */}
            {/* ? Shows inactive users with search and reactivate button */}

            {showInactiveSection && inactiveUsers.length > 0 && (
                <div className="border rounded-xl p-4 bg-white/80 shadow-sm border-red-300">
                    <div className="mb-3 flex flex-col md:flex-row md:items-center md:justify-between gap-2">
                        <h2 className="text-xl font-bold text-red-700">Inactive Users</h2>
                        <input
                            type="text"
                            placeholder="Search by username or email..."
                            value={searchTermInactive}
                            onChange={(e) => setSearchTermInactive(e.target.value)}
                            className="w-full md:w-1/3 rounded-lg border border-gray-300 px-3 py-2 text-sm
                            focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500"
                        />
                    </div>

                    {filteredInactiveUsers.length === 0 ? (
                        <p className="text-center text-gray-500 py-6">
                            {searchTermInactive ? "No users match your search." : "Inactive users will appear here."}
                        </p>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-sm border-collapse">
                                <thead>
                                    <tr className="bg-red-100 text-red-900">
                                        <th className="p-3 text-center">Username</th>
                                        <th className="p-3 text-center">Email</th>
                                        <th className="p-3 text-center">Roles</th>
                                        <th className="p-3 text-center">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredInactiveUsers.map((user) => (
                                        <tr
                                            key={user.uid}
                                            className="border-t hover:bg-red-50 transition-colors"
                                        >
                                            <td className="p-3 font-medium text-center">{user.username}</td>
                                            <td className="p-3 text-center">{user.email}</td>
                                            <td className="p-3">
                                                <div className="flex flex-wrap justify-center gap-1">
                                                    {(user.roles.length ? user.roles : ["N/A"]).map((role) => (
                                                        <span
                                                            key={role}
                                                            className="inline-flex items-center rounded-full bg-[#FCE0D6] px-2.5 py-1 text-xs font-medium text-[#8c432b]"
                                                        >
                                                            {role}
                                                        </span>
                                                    ))}
                                                </div>
                                            </td>
                                            <td className="p-3 text-center">
                                                <button
                                                    onClick={() => {
                                                        setSelectedUser(user);
                                                        setShowReactivateModal(true);
                                                    }}
                                                    className="px-3 py-2 rounded-lg bg-green-500 text-white font-semibold hover:bg-green-600 hover:scale-95 transition-all"
                                                >
                                                    Reactivate
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

            {/* ============================================================================ */}
            {/* MODALS */}
            {/* ============================================================================ */}

            {/* DEACTIVATE MODAL */}
            {showDeactivateModal && selectedUser && (
                <DeactivateUserModal
                    user={selectedUser}
                    loading={actionLoading === selectedUser.uid}
                    onClose={() => {
                        setShowDeactivateModal(false);
                        setSelectedUser(null);
                    }}
                    onConfirm={handleDeactivate}
                />
            )}

            {/* REACTIVATE MODAL */}
            {showReactivateModal && selectedUser && (
                <ReactivateUserModal
                    user={selectedUser}
                    loading={actionLoading === selectedUser.uid}
                    onClose={() => {
                        setShowReactivateModal(false);
                        setSelectedUser(null);
                    }}
                    onConfirm={handleReactivate}
                />
            )}

        </div>
    );
};

export default ManageUsers;

// * ============================================================================
// * DEACTIVATE USER MODAL
// * ============================================================================
// ! Confirmation modal before deactivating a user
// ? Shows user's username and requires explicit confirmation

const DeactivateUserModal = ({ user, onClose, onConfirm, loading }) => {
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div className="absolute inset-0 bg-black/40" onClick={onClose} />

            {/* Modal Content */}
            <div className="relative w-[90%] sm:w-[400px] animate-slideUp">
                <div className="bg-white rounded-xl p-5 shadow-xl border border-red-300">
                    <h3 className="text-xl font-bold text-red-700 text-center">Deactivate User</h3>
                    <p className="text-sm text-gray-700 mt-3 text-center">
                        Are you sure you want to deactivate <span className="font-semibold">{user.username}</span>?
                    </p>

                    {/* Action Buttons */}
                    <div className="mt-5 flex gap-2">
                        <button
                            onClick={onConfirm}
                            disabled={loading}
                            className="flex-1 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white font-semibold hover:scale-95 transition"
                        >
                            {loading ? "Processing..." : "Yes, Deactivate"}
                        </button>
                        <button
                            onClick={onClose}
                            className="flex-1 py-2 rounded-lg bg-stone-200 hover:bg-stone-300 text-gray-800 font-semibold hover:scale-95 transition"
                        >
                            Cancel
                        </button>
                    </div>
                </div>
            </div>

            {/* Slide-up Animation */}
            <style>
                {`
                    @keyframes slideUp {
                        0% { transform: translateY(100%); opacity: 0; }
                        100% { transform: translateY(0); opacity: 1; }
                    }
                    .animate-slideUp {
                        animation: slideUp 0.2s ease-out forwards;
                    }
                `}
            </style>
        </div>
    );
};

// * ============================================================================
// * REACTIVATE USER MODAL
// * ============================================================================
// ! Confirmation modal before reactivating a user
// ? Shows user's username and requires explicit confirmation

const ReactivateUserModal = ({ user, onClose, onConfirm, loading }) => {
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div className="absolute inset-0 bg-black/40" onClick={onClose} />

            {/* Modal Content */}
            <div className="relative w-[90%] sm:w-[400px] animate-slideUp">
                <div className="bg-white rounded-xl p-5 shadow-xl border border-green-300">
                    <h3 className="text-xl font-bold text-green-700 text-center">Reactivate User</h3>
                    <p className="text-sm text-gray-700 mt-3 text-center">
                        Are you sure you want to reactivate <span className="font-semibold">{user.username}</span>?
                    </p>

                    {/* Action Buttons */}
                    <div className="mt-5 flex gap-2">
                        <button
                            onClick={onConfirm}
                            disabled={loading}
                            className="flex-1 py-2 rounded-lg bg-green-600 hover:bg-green-700 text-white font-semibold hover:scale-95 transition"
                        >
                            {loading ? "Processing..." : "Yes, Reactivate"}
                        </button>
                        <button
                            onClick={onClose}
                            className="flex-1 py-2 rounded-lg bg-stone-200 hover:bg-stone-300 text-gray-800 font-semibold hover:scale-95 transition"
                        >
                            Cancel
                        </button>
                    </div>
                </div>
            </div>

            {/* Slide-up Animation */}
            <style>
                {`
                    @keyframes slideUp {
                        0% { transform: translateY(100%); opacity: 0; }
                        100% { transform: translateY(0); opacity: 1; }
                    }
                    .animate-slideUp {
                        animation: slideUp 0.2s ease-out forwards;
                    }
                `}
            </style>
        </div>
    );
};