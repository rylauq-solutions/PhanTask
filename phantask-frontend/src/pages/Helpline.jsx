import React, { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { FaFilter, FaTicketAlt, FaCheckCircle, FaClock } from "react-icons/fa";
import { toast } from "react-hot-toast";
import LoadingSkeleton from "../components/LoadingSkeleton";
import Select from "react-select";
import { apiService } from "../services/api";

const Helpline = () => {
  const { user } = useAuth();
  const [myRaisedTickets, setMyRaisedTickets] = useState([]);
  const [pendingTickets, setPendingTickets] = useState([]);
  const [resolvedTickets, setResolvedTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [isDesktop, setIsDesktop] = useState(window.innerWidth >= 990);

  const [userEmailMap, setUserEmailMap] = useState({});

  const canViewAssignedTickets = user?.roles?.some((role) =>
    ["HR", "MANAGER", "SUPPORT", "ADMIN"].includes(role)
  );

  useEffect(() => {
    const handleResize = () => {
      setIsDesktop(window.innerWidth >= 990);
    };

    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    const fetchUsers = async () => {
      const canFetchUsers = user?.roles?.some(role =>
        ["ADMIN", "HR", "MANAGER", "SUPPORT"].includes(role)
      );

      if (!canFetchUsers) {
        return;
      }

      try {
        const response = await apiService.getAllActiveUsers();
        const users = response.data || [];

        const emailMap = {};
        users.forEach(u => {
          if (u.uid && u.email) {
            emailMap[u.uid] = u.email;
          }
        });

        setUserEmailMap(emailMap);
      } catch (error) {
        console.error("Error fetching users for email lookup:", error);
      }
    };

    if (user) {
      fetchUsers();
    }
  }, [user]);

  useEffect(() => {
    if (!isDesktop) return;

    let isMounted = true;

    const fetchTickets = async () => {
      try {
        setLoading(true);

        const myRaisedResponse = await apiService.getMyRaisedTickets();

        if (!isMounted) return;

        setMyRaisedTickets(myRaisedResponse.data || []);

        if (canViewAssignedTickets) {
          const [pendingResponse, resolvedResponse] = await Promise.all([
            apiService.getMyPendingTickets(),
            apiService.getMyResolvedTickets(),
          ]);

          if (!isMounted) return;

          setPendingTickets(pendingResponse.data || []);
          setResolvedTickets(resolvedResponse.data || []);
        }
      } catch (error) {
        if (!isMounted) return;

        console.error("Error fetching tickets:", error);

        if (error.response?.status !== 404) {
          toast.error("Failed to fetch tickets");
        } else {
          setMyRaisedTickets([]);
          setPendingTickets([]);
          setResolvedTickets([]);
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    fetchTickets();
    window.refreshHelplineTickets = fetchTickets;

    return () => {
      isMounted = false;
      delete window.refreshHelplineTickets;
    };
  }, [canViewAssignedTickets, isDesktop]);

  if (!isDesktop) {
    return (
      <div className="flex items-center justify-center h-screen p-4">
        <div className="text-center bg-white p-8 rounded-xl shadow-lg border border-gray-200">
          <h1 className="text-2xl font-bold mb-4 text-amber-950">
            Desktop Access Required
          </h1>
          <p className="text-gray-700">
            The <span className="font-semibold">Helpline Ticket System</span> is only
            accessible on a desktop or a large screen.
            <br />
            Please switch to a device with a screen width of{" "}
            <span className="font-semibold">990px or more</span>.
          </p>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="space-y-6 p-4">
        <div className="mb-3 bg-white/60 backdrop-blur-sm rounded-xl p-4 shadow-sm border border-gray-100">
          <h1 className="text-2xl text-center md:text-3xl font-bold text-amber-950">
            Helpline Ticket System
          </h1>
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <LoadingSkeleton rows={6} rowHeight="h-6" hasButton={true} />
          <div className="lg:col-span-2">
            <LoadingSkeleton rows={6} rowHeight="h-6" hasButton={false} />
          </div>
        </div>
      </div>
    );
  }

  const filteredMyTickets = myRaisedTickets.filter((t) => {
    if (filter === "ALL") return true;
    if (filter === "PENDING") return t.status === "PENDING";
    if (filter === "RESOLVED") return t.status === "RESOLVED";
    return true;
  });

  const sortedMyTickets = [...filteredMyTickets].sort(
    (a, b) => new Date(b.raisedAt) - new Date(a.raisedAt)
  );

  const formatDate = (dateStr) => {
    if (!dateStr) return "—";
    const d = new Date(dateStr);
    return d.toLocaleDateString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  };

  const formatDateTime = (dateStr) => {
    if (!dateStr) return "—";
    const d = new Date(dateStr);
    const date = d.toLocaleDateString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
    const time = `${d.getHours().toString().padStart(2, "0")}:${d
      .getMinutes()
      .toString()
      .padStart(2, "0")}`;
    return `${date}, ${time}`;
  };

  const getPriorityColor = (priority) => {
    switch (priority) {
      case "HIGH":
        return "#DC2626";
      case "MEDIUM":
        return "#EAB308";
      case "LOW":
      default:
        return "#16A34A";
    }
  };

  const getPriorityBadge = (priority) => {
    switch (priority) {
      case "HIGH":
        return "bg-red-100 text-red-700";
      case "MEDIUM":
        return "bg-yellow-100 text-yellow-700";
      case "LOW":
      default:
        return "bg-green-100 text-green-700";
    }
  };

  const getStatusBadge = (status) => {
    return status === "PENDING"
      ? "bg-red-600 text-white"
      : "bg-green-600 text-white";
  };

  const openModal = (ticket) => {
    setSelectedTicket(ticket);
  };

  const closeModal = () => {
    setSelectedTicket(null);
  };

  const openTicketsCount = myRaisedTickets.filter(
    (t) => t.status === "PENDING"
  ).length;
  const pendingTicketsCount = pendingTickets.length;
  const resolvedTicketsCount = resolvedTickets.length;

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
        onClick={() => setFilter("PENDING")}
        className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-colors duration-300 ${filter === "PENDING"
            ? "bg-red-600 text-white"
            : "bg-white border border-gray-300 text-gray-700 hover:bg-red-100"
          }`}
      >
        <FaFilter /> Pending
      </button>
      <button
        onClick={() => setFilter("RESOLVED")}
        className={`flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition-colors duration-300 ${filter === "RESOLVED"
            ? "bg-green-700 text-white"
            : "bg-white border border-gray-300 text-gray-700 hover:bg-green-100"
          }`}
      >
        <FaFilter /> Resolved
      </button>
    </div>
  );

  const TicketCard = ({ ticket }) => (
    <div
      className="border-2 rounded-xl p-4 bg-white shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300 flex flex-col justify-between"
      style={{ borderColor: getPriorityColor(ticket.priority) }}
    >
      <div>
        <div className="flex items-start justify-between mb-2">
          <h3 className="font-semibold text-lg flex-1">#{ticket.ticketId}</h3>
          <span
            className={`text-xs font-semibold px-2 py-1 rounded-full ${getStatusBadge(
              ticket.status
            )}`}
          >
            {ticket.status}
          </span>
        </div>
        <p className="text-sm text-gray-800 mb-2">{ticket.description}</p>
        <div className="space-y-1">
          <p className="text-xs text-gray-600">
            <span className="font-semibold">Priority:</span>{" "}
            <span
              className={`px-2 py-0.5 rounded-full text-xs font-semibold ${getPriorityBadge(
                ticket.priority
              )}`}
            >
              {ticket.priority}
            </span>
          </p>
          <p className="text-xs text-gray-600">
            <span className="font-semibold">Raised:</span>{" "}
            {formatDateTime(ticket.raisedAt)}
          </p>
          <p className="text-xs text-gray-600">
            <span className="font-semibold">Due:</span> {formatDate(ticket.dueDate)}
          </p>
        </div>
      </div>
      <button
        onClick={() => openModal(ticket)}
        className="mt-4 w-full py-2 rounded-lg text-white text-sm font-medium hover:scale-95 transition-transform duration-300"
        style={{ backgroundColor: getPriorityColor(ticket.priority) }}
      >
        View Details
      </button>
    </div>
  );

  return (
    <div className="space-y-6 p-4">
      <div className="mb-3 bg-white/60 backdrop-blur-sm rounded-xl p-4 shadow-sm border border-gray-100">
        <h1 className="text-2xl text-center md:text-3xl font-bold text-amber-950">
          Helpline Ticket System
        </h1>
      </div>

      <RaiseTicketCard />

      {myRaisedTickets.length > 0 && (
        <>
          <FilterBar />

          <div className="border-2 rounded-xl p-4 bg-white/80 shadow-sm border-[#7c312c]">
            <h2 className="text-xl font-bold text-[#7c312c] mb-4">My Raised Tickets</h2>

            {sortedMyTickets.length === 0 ? (
              <div className="text-center py-8">
                <div className="w-16 h-16 bg-[#7c312c]/5 rounded-2xl flex items-center justify-center mx-auto mb-3">
                  <span className="text-3xl">🎫</span>
                </div>
                <p className="text-[#7c312c]/60 text-sm">
                  No {filter.toLowerCase()} tickets found.
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {sortedMyTickets.map((ticket) => (
                  <TicketCard key={ticket.ticketId} ticket={ticket} />
                ))}
              </div>
            )}
          </div>
        </>
      )}

      {canViewAssignedTickets && pendingTickets.length > 0 && (
        <div className="border-2 rounded-xl p-4 bg-white/80 shadow-sm border-yellow-500">
          <h2 className="text-xl font-bold text-yellow-700 mb-4">
            Pending Tickets (Assigned to My Role)
          </h2>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-yellow-100 border-b-2 border-yellow-500/20">
                <tr>
                  <th className="px-4 py-3 text-left font-semibold text-gray-700">ID</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-700">
                    Description
                  </th>
                  <th className="px-4 py-3 text-center font-semibold text-gray-700">
                    Priority
                  </th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-700">
                    Raised By
                  </th>
                  <th className="px-4 py-3 text-center font-semibold text-gray-700">
                    Raised At
                  </th>
                  <th className="px-4 py-3 text-center font-semibold text-gray-700">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody>
                {pendingTickets.map((ticket) => (
                  <tr
                    key={ticket.ticketId}
                    className="border-b border-gray-200 hover:bg-yellow-50 transition-colors"
                  >
                    <td className="px-4 py-3 font-medium text-gray-900 whitespace-nowrap">
                      #{ticket.ticketId}
                    </td>
                    <td className="px-4 py-3 text-gray-800 break-words max-w-xs">
                      {ticket.description}
                    </td>
                    <td className="px-4 py-3 text-center whitespace-nowrap">
                      <span
                        className={`inline-block px-3 py-1 text-xs font-semibold rounded-full ${getPriorityBadge(
                          ticket.priority
                        )}`}
                      >
                        {ticket.priority}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-gray-600 text-xs font-medium break-words max-w-[200px]">
                      {ticket.raisedByEmail}
                    </td>
                    <td className="px-4 py-3 text-center text-xs text-gray-600 font-medium whitespace-nowrap">
                      {formatDateTime(ticket.raisedAt)}
                    </td>
                    <td className="px-4 py-3 text-center whitespace-nowrap">
                      <button
                        onClick={() => openModal(ticket)}
                        className="px-3 py-1 rounded-lg bg-yellow-500 text-white text-xs font-semibold hover:bg-yellow-600 hover:scale-95 transition-transform"
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {canViewAssignedTickets && resolvedTickets.length > 0 && (
        <div className="border-2 rounded-xl p-4 bg-white/80 shadow-sm border-green-600">
          <h2 className="text-xl font-bold text-green-700 mb-4">Resolved Tickets</h2>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-green-100 border-b-2 border-green-600/20">
                <tr>
                  <th className="px-4 py-3 text-left font-semibold text-gray-700">ID</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-700">
                    Description
                  </th>
                  <th className="px-4 py-3 text-center font-semibold text-gray-700">
                    Priority
                  </th>
                  <th className="px-4 py-3 text-center font-semibold text-gray-700">
                    Resolved At
                  </th>
                  <th className="px-4 py-3 text-center font-semibold text-gray-700">
                    Resolved By
                  </th>
                </tr>
              </thead>
              <tbody>
                {resolvedTickets.map((ticket) => {
                  const isAdmin = user?.roles?.some(role =>
                    role === "ADMIN" || role?.authority === "ROLE_ADMIN"
                  );

                  const resolverDisplay = isAdmin
                    ? (ticket.resolvedByEmail ||
                      userEmailMap[ticket.resolvedByUserId] ||
                      userEmailMap[ticket.resolvedBy] ||
                      "N/A")
                    : "Admin Only";

                  return (
                    <tr
                      key={ticket.ticketId}
                      className="border-b border-gray-200 hover:bg-green-50 transition-colors"
                    >
                      <td className="px-4 py-3 font-medium text-gray-900 whitespace-nowrap">
                        #{ticket.ticketId}
                      </td>
                      <td className="px-4 py-3 text-gray-800 break-words max-w-xs">
                        {ticket.description}
                      </td>
                      <td className="px-4 py-3 text-center whitespace-nowrap">
                        <span
                          className={`inline-block px-3 py-1 text-xs font-semibold rounded-full ${getPriorityBadge(
                            ticket.priority
                          )}`}
                        >
                          {ticket.priority}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-center text-xs text-gray-600 font-medium whitespace-nowrap">
                        {formatDateTime(ticket.resolvedAt)}
                      </td>
                      <td className="px-4 py-3 text-center text-xs font-medium break-words max-w-[200px]">
                        {isAdmin ? (
                          <span className="text-gray-700">{resolverDisplay}</span>
                        ) : (
                          <span className="text-gray-500 italic flex items-center justify-center gap-1">
                            <span>🔒</span>
                            <span>Admin Only</span>
                          </span>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatCard
          icon={<FaTicketAlt />}
          title="Open Tickets"
          count={openTicketsCount}
          color="bg-blue-500"
        />

        {canViewAssignedTickets && (
          <>
            <StatCard
              icon={<FaClock />}
              title="Pending Tickets"
              count={pendingTicketsCount}
              color="bg-yellow-500"
            />
            <StatCard
              icon={<FaCheckCircle />}
              title="Resolved Tickets"
              count={resolvedTicketsCount}
              color="bg-green-600"
            />
          </>
        )}
      </div>

      {selectedTicket && (
        <TicketDetailsModal
          ticket={selectedTicket}
          onClose={closeModal}
          userRoles={user?.roles || []}
          userEmailMap={userEmailMap}
        />
      )}
    </div>
  );
};

export default Helpline;

const RaiseTicketCard = () => {
  const [showModal, setShowModal] = useState(false);

  return (
    <>
      <div className="w-full rounded-xl border-2 border-[#7c312c] bg-white p-4 shadow-md shadow-[#7c312c]/20 transition-all duration-300 hover:shadow-xl hover:shadow-[#7c312c]/30">
        <div className="flex flex-col md:flex-row items-center gap-4">
          <div className="w-16 h-16 bg-[#7c312c]/5 rounded-2xl flex items-center justify-center shadow-md flex-shrink-0">
            <span className="text-3xl">🎫</span>
          </div>
          <div className="flex-1 text-center md:text-left">
            <h3 className="text-xl font-bold text-[#7c312c] mb-1">Need Help?</h3>
            <p className="text-[#7c312c]/60 text-sm">
              Create a ticket and assign it to the appropriate role
            </p>
          </div>
          <button
            onClick={() => setShowModal(true)}
            className="px-6 py-3 bg-[#7c312c] text-white rounded-lg font-semibold hover:bg-[#42260b] transition-all duration-300 shadow-md hover:shadow-lg hover:scale-95"
          >
            Raise Ticket
          </button>
        </div>
      </div>

      {showModal && <RaiseTicketModal onClose={() => setShowModal(false)} />}
    </>
  );
};

const RaiseTicketModal = ({ onClose }) => {
  const [assignedRole, setAssignedRole] = useState("");
  const [priority, setPriority] = useState("");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [confirmVisible, setConfirmVisible] = useState(false);
  const [confirmed, setConfirmed] = useState(false);

  const [roleOptions, setRoleOptions] = useState([]);
  const [rolesLoading, setRolesLoading] = useState(true);

  useEffect(() => {
    const fetchRoles = async () => {
      try {
        setRolesLoading(true);
        const response = await apiService.getAllRoles();
        const rolesData = response.data || [];

        const filteredRoles = rolesData
          .filter(role => {
            const roleName = role.roleName || role;
            return roleName !== "USER" && roleName !== "TRAINEE";
          })
          .map(role => {
            const roleName = role.roleName || role;
            return {
              value: roleName,
              label: roleName
            };
          });

        filteredRoles.sort((a, b) => {
          if (a.value === "ADMIN") return 1;
          if (b.value === "ADMIN") return -1;
          return a.label.localeCompare(b.label);
        });

        setRoleOptions([
          { value: "", label: "Select Role..." },
          ...filteredRoles
        ]);
      } catch (error) {
        console.error("Error fetching roles:", error);
        toast.error("Failed to load roles");
        setRoleOptions([
          { value: "", label: "Select Role..." },
          { value: "HR", label: "HR" },
          { value: "MANAGER", label: "MANAGER" },
          { value: "SUPPORT", label: "SUPPORT" },
          { value: "ADMIN", label: "ADMIN" },
        ]);
      } finally {
        setRolesLoading(false);
      }
    };

    fetchRoles();
  }, []);

  const priorityOptions = [
    { value: "", label: "Select Priority..." },
    { value: "HIGH", label: "High" },
    { value: "MEDIUM", label: "Medium" },
    { value: "LOW", label: "Low" },
  ];

  const selectStyles = {
    control: (base, state) => ({
      ...base,
      minHeight: "40px",
      borderRadius: "0.5rem",
      borderWidth: "1px",
      borderColor: state.isFocused ? "#dc2626" : "#d1d5db",
      boxShadow: state.isFocused ? "0 0 0 2px rgba(220, 38, 38, 1)" : "none",
      padding: "0.5rem 0.75rem",
      fontSize: "0.875rem",
      backgroundColor: "white",
      "&:hover": {
        borderColor: state.isFocused ? "#dc2626" : "#d1d5db",
      },
    }),
    valueContainer: (base) => ({
      ...base,
      padding: "0",
    }),
    input: (base) => ({
      ...base,
      margin: "0",
      padding: "0",
    }),
    indicatorsContainer: (base) => ({
      ...base,
      height: "24px",
    }),
    option: (base, state) => ({
      ...base,
      fontSize: "0.875rem",
      backgroundColor: state.isSelected
        ? "#facc15"
        : state.isFocused
          ? "#fef3c7"
          : "white",
      color: state.isSelected ? "#422006" : "#111827",
      cursor: "pointer",
      padding: "0.5rem 0.75rem",
      borderRadius: "0.375rem",
    }),
    placeholder: (base) => ({
      ...base,
      color: "#9ca3af",
      fontSize: "0.875rem",
      margin: "0",
    }),
    singleValue: (base) => ({
      ...base,
      fontSize: "0.875rem",
      color: "#111827",
      margin: "0",
    }),
    menu: (base) => ({
      ...base,
      borderRadius: "0.5rem",
      zIndex: 9999,
    }),
    menuPortal: (base) => ({ ...base, zIndex: 9999 }),
    menuList: (base) => ({
      ...base,
      maxHeight: "200px",
      paddingTop: "0.125rem",
      paddingBottom: "0.125rem",
      scrollbarWidth: "thin",
      scrollbarColor: "#d1d5db transparent",
      "&::-webkit-scrollbar": {
        width: "8px",
      },
      "&::-webkit-scrollbar-track": {
        background: "transparent",
        margin: "0.125rem 0",
      },
      "&::-webkit-scrollbar-thumb": {
        backgroundColor: "#d1d5db",
        borderRadius: "8px",
      },
      "&::-webkit-scrollbar-thumb:hover": {
        backgroundColor: "#9ca3af",
      },
    }),
  };

  const handleCreateTicket = async () => {
    if (!assignedRole) return toast.error("Please select a role");
    if (!priority) return toast.error("Please select priority");
    if (!description.trim()) return toast.error("Description is required");
    if (!confirmed) return toast.error("Please confirm before proceeding");

    setLoading(true);
    try {
      await apiService.raiseHelplineTicket({
        assignedRoleName: assignedRole,
        priority: priority,
        description: description.trim(),
      });

      toast.success("Ticket created successfully!", { duration: 3000 });

      if (window.refreshHelplineTickets) {
        window.refreshHelplineTickets();
      }

      onClose();
    } catch (error) {
      console.error("Error creating ticket:", error);
      toast.error(
        error.response?.data?.message || "Failed to create ticket"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />

      <div className="relative w-[90%] sm:w-[80%] md:w-2/5 max-h-[95vh] animate-slideUp">
        <div className="bg-white rounded-xl p-4 md:p-6 shadow-xl flex flex-col border border-red-700/30 max-h-[95vh] overflow-y-auto">
          <div className="mb-3 text-center flex-shrink-0">
            <h3 className="text-2xl font-bold text-amber-950">Create Ticket</h3>
            <p className="text-sm text-gray-700 mt-1">
              Describe your issue and assign to appropriate role
            </p>
          </div>

          <div className="flex-1 flex flex-col gap-3">
            <label className="text-sm font-semibold text-gray-800">Assign Role</label>
            {rolesLoading ? (
              <div className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-500">
                Loading roles...
              </div>
            ) : (
              <Select
                styles={selectStyles}
                placeholder="Select Role..."
                value={roleOptions.find((r) => r.value === assignedRole)}
                onChange={(opt) => setAssignedRole(opt?.value || "")}
                options={roleOptions}
                menuPortalTarget={document.body}
                menuPosition="fixed"
              />
            )}

            <label className="text-sm font-semibold text-gray-800 mt-1">Priority</label>
            <Select
              styles={selectStyles}
              placeholder="Select Priority..."
              value={priorityOptions.find((p) => p.value === priority)}
              onChange={(opt) => setPriority(opt?.value || "")}
              options={priorityOptions}
              menuPortalTarget={document.body}
              menuPosition="fixed"
            />

            <label className="text-sm font-semibold text-gray-800 mt-1">Description</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Describe your issue..."
              rows="5"
              maxLength={500}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm
              focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600 resize-none"
            />
          </div>

          <div className="mt-4 flex-shrink-0">
            {!confirmVisible ? (
              <div className="flex gap-2">
                <button
                  onClick={() => setConfirmVisible(true)}
                  className="flex-1 py-2 rounded-lg bg-red-700 hover:bg-red-800 text-white font-semibold hover:scale-95 transition-transform duration-300 shadow"
                >
                  Create Ticket
                </button>

                <button
                  onClick={onClose}
                  className="flex-1 py-2 rounded-lg bg-stone-200 hover:bg-stone-300 text-gray-800 font-semibold hover:scale-95 transition-transform duration-300 shadow"
                >
                  Cancel
                </button>
              </div>
            ) : (
              <div className="flex flex-col gap-3 border-t pt-3">
                <label className="flex items-center gap-2 justify-center text-sm font-medium text-gray-700">
                  <input
                    type="checkbox"
                    checked={confirmed}
                    onChange={(e) => setConfirmed(e.target.checked)}
                    className="accent-red-700"
                  />
                  I confirm all details are correct.
                </label>

                <div className="flex gap-2">
                  <button
                    onClick={handleCreateTicket}
                    disabled={!confirmed || loading}
                    className={`flex-1 py-2 rounded-lg text-white font-semibold
                      hover:scale-95 transition-transform duration-300 shadow
                      ${confirmed
                        ? "bg-green-700 hover:bg-green-800"
                        : "bg-green-700/60 cursor-not-allowed"
                      }`}
                  >
                    {loading ? "Creating..." : "Confirm & Create"}
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
      </div>

      <style>
        {`
          @keyframes slideUp {
            0% { transform: translateY(100%); opacity: 0; }
            100% { transform: translateY(0); opacity: 1; }
          }
          .animate-slideUp { animation: slideUp 0.2s ease-out forwards; }

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
        `}
      </style>
    </div>
  );
};

const TicketDetailsModal = ({ ticket, onClose, userRoles, userEmailMap }) => {
  const { user } = useAuth();
  const [resolving, setResolving] = useState(false);
  const [showResolveConfirm, setShowResolveConfirm] = useState(false);
  const [confirmed, setConfirmed] = useState(false);

  const formatDate = (dateStr) => {
    if (!dateStr) return "—";
    const d = new Date(dateStr);
    return d.toLocaleDateString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  };

  const formatDateTime = (dateStr) => {
    if (!dateStr) return "—";
    const d = new Date(dateStr);
    const date = d.toLocaleDateString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
    const time = `${d.getHours().toString().padStart(2, "0")}:${d
      .getMinutes()
      .toString()
      .padStart(2, "0")}`;
    return `${date}, ${time}`;
  };

  const getPriorityColor = (priority) => {
    switch (priority) {
      case "HIGH":
        return "border-red-600";
      case "MEDIUM":
        return "border-yellow-500";
      case "LOW":
      default:
        return "border-green-600";
    }
  };

  const getPriorityBadge = (priority) => {
    switch (priority) {
      case "HIGH":
        return "bg-red-100 text-red-700";
      case "MEDIUM":
        return "bg-yellow-100 text-yellow-700";
      case "LOW":
      default:
        return "bg-green-100 text-green-700";
    }
  };

  const getStatusBadge = (status) => {
    return status === "PENDING"
      ? "bg-red-600 text-white"
      : "bg-green-600 text-white";
  };

  const canResolveTicket = () => {
    if (!ticket.status || ticket.status !== "PENDING") {
      return false;
    }

    if (!userRoles || !Array.isArray(userRoles) || userRoles.length === 0) {
      return false;
    }

    const normalizedRoles = userRoles
      .map((role) => {
        if (typeof role === "string") {
          return role.replace(/^ROLE_/, "").toUpperCase();
        }
        if (role?.authority) {
          return role.authority.replace(/^ROLE_/, "").toUpperCase();
        }
        return "";
      })
      .filter((r) => r !== "");

    const hasPermission =
      normalizedRoles.includes("ADMIN") ||
      normalizedRoles.includes("SUPPORT") ||
      normalizedRoles.includes(ticket.assignedRoleName?.toUpperCase());

    return hasPermission;
  };

  const handleResolveTicket = async () => {
    if (!confirmed) return toast.error("Please confirm before proceeding");

    setResolving(true);
    try {
      await apiService.resolveTicket(ticket.ticketId);

      toast.success("Ticket marked as resolved!", { duration: 3000 });

      if (window.refreshHelplineTickets) {
        window.refreshHelplineTickets();
      }

      onClose();
    } catch (error) {
      console.error("Error resolving ticket:", error);
      toast.error(error?.response?.data?.message || "Failed to resolve ticket");
    } finally {
      setResolving(false);
      setShowResolveConfirm(false);
      setConfirmed(false);
    }
  };

  const isAdmin = user?.roles?.some(role =>
    role === "ADMIN" || role?.authority === "ROLE_ADMIN"
  );

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />

      <div className="relative w-[90%] sm:w-[80%] md:w-2/5 max-h-[95vh] animate-slideUp">
        <div
          className={`bg-white rounded-xl p-6 shadow-xl border-l-4 ${getPriorityColor(
            ticket.priority
          )} max-h-[95vh] overflow-y-auto`}
        >
          <div className="mb-4">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <h3 className="text-2xl font-bold text-amber-950">
                  Ticket #{ticket.ticketId}
                </h3>
                <div className="flex items-center gap-2 mt-2">
                  <span
                    className={`text-xs font-semibold px-2 py-1 rounded-full ${getPriorityBadge(
                      ticket.priority
                    )}`}
                  >
                    {ticket.priority}
                  </span>

                  <span
                    className={`text-xs font-semibold px-2 py-1 rounded-full ${getStatusBadge(
                      ticket.status
                    )}`}
                  >
                    {ticket.status}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div className="space-y-3">
            <div>
              <label className="text-sm font-semibold text-gray-800">Description:</label>
              <p className="text-sm text-gray-700 mt-1 p-3 bg-gray-50 rounded-lg">
                {ticket.description}
              </p>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-sm font-semibold text-gray-800">Assigned Role:</label>
                <p className="text-sm text-gray-700 mt-1">{ticket.assignedRoleName}</p>
              </div>

              <div>
                <label className="text-sm font-semibold text-gray-800">Raised By:</label>
                <p className="text-sm text-gray-700 mt-1">
                  {ticket.raisedByEmail || "—"}
                </p>
              </div>

              <div>
                <label className="text-sm font-semibold text-gray-800">Raised At:</label>
                <p className="text-sm text-gray-700 mt-1">
                  {formatDateTime(ticket.raisedAt)}
                </p>
              </div>

              <div>
                <label className="text-sm font-semibold text-gray-800">Due Date:</label>
                <p className="text-sm text-gray-700 mt-1">{formatDate(ticket.dueDate)}</p>
              </div>

              {ticket.status === "RESOLVED" && ticket.resolvedAt && (
                <>
                  <div>
                    <label className="text-sm font-semibold text-gray-800">
                      Resolved At:
                    </label>
                    <p className="text-sm text-gray-700 mt-1">
                      {formatDateTime(ticket.resolvedAt)}
                    </p>
                  </div>

                  <div>
                    <label className="text-sm font-semibold text-gray-800">
                      Resolved By:
                    </label>
                    <p className="text-sm text-gray-700 mt-1">
                      {isAdmin
                        ? (userEmailMap[ticket.resolvedByUserId] ||
                          ticket.resolvedByEmail ||
                          "N/A")
                        : "🔒 Admin Only"}
                    </p>
                  </div>
                </>
              )}
            </div>
          </div>

          <div className="mt-6">
            {!showResolveConfirm ? (
              <div className="flex gap-2">
                {canResolveTicket() && (
                  <button
                    onClick={() => setShowResolveConfirm(true)}
                    className="flex-1 py-2 rounded-lg bg-green-600 hover:bg-green-700 text-white font-semibold hover:scale-95 transition-transform duration-300 shadow"
                  >
                    Mark as Resolved
                  </button>
                )}

                <button
                  onClick={onClose}
                  className={`${canResolveTicket() ? "flex-1" : "w-full"
                    } py-2 rounded-lg bg-stone-200 hover:bg-stone-300 text-gray-800 font-semibold hover:scale-95 transition duration-300 shadow`}
                >
                  Close
                </button>
              </div>
            ) : (
              <div className="flex flex-col gap-3 border-t pt-3">
                <label className="flex items-center gap-2 justify-center text-sm font-medium text-gray-700">
                  <input
                    type="checkbox"
                    checked={confirmed}
                    onChange={(e) => setConfirmed(e.target.checked)}
                    className="accent-green-600"
                  />
                  Are you sure you want to mark this ticket as resolved?
                </label>

                <div className="flex gap-2">
                  <button
                    onClick={handleResolveTicket}
                    disabled={!confirmed || resolving}
                    className={`flex-1 py-2 rounded-lg text-white font-semibold hover:scale-95 transition-transform duration-300 shadow ${confirmed && !resolving
                        ? "bg-green-600 hover:bg-green-700"
                        : "bg-green-600/60 cursor-not-allowed"
                      }`}
                  >
                    {resolving ? "Resolving..." : "Confirm & Resolve"}
                  </button>

                  <button
                    onClick={() => {
                      setShowResolveConfirm(false);
                      setConfirmed(false);
                    }}
                    disabled={resolving}
                    className="flex-1 py-2 rounded-lg bg-stone-200 hover:bg-stone-300 text-gray-800 font-semibold hover:scale-95 transition duration-300 shadow"
                  >
                    Back
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      <style>
        {`
          @keyframes slideUp {
            0% { transform: translateY(100%); opacity: 0; }
            100% { transform: translateY(0); opacity: 1; }
          }
          .animate-slideUp {
            animation: slideUp 0.2s ease-out forwards;
          }

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
        `}
      </style>
    </div>
  );
};

const StatCard = ({ icon, title, count, color }) => {
  return (
    <div className="rounded-xl border-2 border-[#7c312c]/30 bg-white p-6 shadow-md hover:shadow-lg transition-all duration-300">
      <div className="flex items-center gap-4">
        <div
          className={`w-14 h-14 ${color} rounded-2xl flex items-center justify-center text-2xl text-white shadow-md`}
        >
          {icon}
        </div>
        <div>
          <p className="text-sm font-medium text-gray-600">{title}</p>
          <p className="text-3xl font-bold text-[#4b201d]">{count}</p>
        </div>
      </div>
    </div>
  );
};
