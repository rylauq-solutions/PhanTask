import React, { useState, useEffect } from "react";
import { apiService } from "../../services/api";
import { toast } from "react-hot-toast";
import Select from "react-select";

const CreateTaskCard = () => {
  const [showModal, setShowModal] = useState(false);

  return (
    <>
      <div className="w-full h-full rounded-xl border-2 border-[#522320] bg-[#ffffff] p-3 shadow-md shadow-[#522320]/20 transition-all duration-300 hover:shadow-xl hover:shadow-[#522320]/30 hover:-translate-y-0.5 flex flex-col">
        <span className="w-full h-full flex flex-col justify-between">
          <h2 className="h-10 text-lg font-semibold py-1 text-[#522320] text-center">
            Create Task
          </h2>

          <main className="w-full h-full flex flex-col items-center justify-center p-4 bg-gradient-to-b from-[#fff9f8]/30 to-[#fff1f0]/20 rounded-xl border-[#522320]/20 shadow-sm">
            <div className="w-14 h-14 bg-[#522320]/5 rounded-2xl flex items-center justify-center mb-3 shadow-md shadow-[#522320]/10">
              <span className="text-2xl">📝</span>
            </div>

            <h3 className="text-xl font-bold text-[#522320] mb-1.5 leading-tight">
              Assign New Task
            </h3>

            <p className="text-[#522320]/60 text-xs font-medium text-center leading-tight max-w-[160px]">
              Create and assign tasks to users or groups efficiently.
            </p>
          </main>

          <button
            onClick={() => setShowModal(true)}
            className="w-full text-center hover:rounded-xl text-[#42260b] text-sm font-medium mt-1 py-2 hover:bg-red-100"
          >
            Assign Task
          </button>
        </span>
      </div>

      {showModal && <CreateTaskModal onClose={() => setShowModal(false)} />}
    </>
  );
};

export default CreateTaskCard;

const CreateTaskModal = ({ onClose }) => {
  const [taskName, setTaskName] = useState("");
  const [description, setDescription] = useState("");
  const [dueDate, setDueDate] = useState("");
  const [assignedUser, setAssignedUser] = useState("");
  const [assignedRole, setAssignedRole] = useState("");
  const [users, setUsers] = useState([]);
  const [assignedRoleByUsers, setAssignedRoleByUsers] = useState("");

  const [loading, setLoading] = useState(false);

  const roleOptions = [
    { value: "", label: "Select Role..." },
    { value: "USER", label: "User" },
    { value: "HR", label: "HR" },
    { value: "STUDENT", label: "Student" },
    { value: "ADMIN", label: "Admin" }
  ];

  const userOptions = [
    { value: "", label: "Select User..." },
    ...users.map(user => ({
      value: user.username,
      label: `${user.username} (${user.email})`
    }))
  ];

  // Custom styles for react-select
  const selectStyles = {
    control: (base, state) => ({
      ...base,
      minHeight: "40px",
      borderRadius: "0.5rem",
      borderColor: state.isFocused ? "#dc2626" : "#d1d5db",
      boxShadow: state.isFocused
        ? "0 0 0 2px rgb(220 38 38 / 0.5)"
        : "none",
      "&:hover": {
        borderColor: "#dc2626",
      },
    }),

    option: (base, state) => ({
      ...base,
      fontSize: "0.875rem", // match input text-sm
      backgroundColor: state.isSelected
        ? "#facc15" // yellow-400
        : state.isFocused
          ? "#fef3c7" // yellow-100 (soft hover)
          : "white",
      color: state.isSelected ? "#422006" : "#111827",
      cursor: "pointer",
    }),

    placeholder: (base) => ({
      ...base,
      color: "#9ca3af",
      fontSize: "0.875rem",
    }),

    singleValue: (base) => ({
      ...base,
      fontSize: "0.875rem",
      color: "#111827",
    }),

    menu: (base) => ({
      ...base,
      borderRadius: "0.5rem",
      zIndex: 50,
    }),
  };



  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const usersRes = await apiService.getAllActiveUsers();
        const fetchedUsers = usersRes.data || [];

        // Natural sort by username
        const naturalSort = (a, b) => {
          const regex = /^([a-zA-Z]+)(\d*)$/;
          const [, textA = "", numA = "0"] = a.username.match(regex) || [];
          const [, textB = "", numB = "0"] = b.username.match(regex) || [];

          const textCompare = textA.localeCompare(textB);
          if (textCompare !== 0) return textCompare;
          return Number(numA) - Number(numB);
        };

        const sortedUsers = fetchedUsers.sort(naturalSort);
        setUsers(sortedUsers);
      } catch (err) {
        toast.error("Failed to fetch active users");
      }
    };
    fetchUsers();
  }, []);



  const handleCreateTask = async () => {
    if (!taskName.trim() || !description.trim() || !dueDate) {
      toast.error("Please fill all required fields");
      return;
    }

    if (!assignedUser && !assignedRole && !assignedRoleByUsers) {
      toast.error("Please assign the task to a user or a role");
      return;
    }

    try {
      setLoading(true);

      if (assignedRoleByUsers) {
        // Assign to all users with selected role
        const usersWithRole = users.filter(user =>
          user.roles.includes(assignedRoleByUsers)
        );

        if (usersWithRole.length === 0) {
          toast.error(`No users found with role ${assignedRoleByUsers}`);
          return;
        }

        // Fire API for each user
        for (const user of usersWithRole) {
          const taskData = {
            taskName,
            description,
            dueDate,
            assignedToUser: user.username,
            assignedToRole: null, // since it's per-user
            status: "PENDING",
          };

          await apiService.createTask(taskData);
        }

        toast.success(`Task created for all users with role ${assignedRoleByUsers}`);
      } else {
        // Normal single assignment
        const taskData = {
          taskName,
          description,
          dueDate,
          assignedToUser: assignedUser || null,
          assignedToRole: assignedRole || null,
          status: "PENDING",
        };

        const res = await apiService.createTask(taskData);
        toast.success(`Task "${res.data.taskName}" created successfully!`);
      }

      onClose();
    } catch (err) {
      toast.error(err?.response?.data?.error || "Failed to create task");
    } finally {
      setLoading(false);
    }
  };


  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center md:items-center">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />

      <div className="relative w-[90%] sm:w-[85%] md:w-2/5 max-h-[95vh] overflow-y-scroll
          bg-white rounded-xl p-4 md:p-6 shadow-xl border border-red-700/30
          transform transition-transform duration-300 ease-out
          animate-slideUp">
        {/* Header */}
        <div className="mb-3 text-center">
          <h3 className="text-2xl font-bold text-amber-950">Create Task</h3>
          <p className="text-sm text-gray-700 mt-1">Assign a new task to a user or a role</p>
        </div>

        {/* Body */}
        <div className="flex-1 flex flex-col gap-3">
          <label className="text-sm font-semibold text-gray-800">Task Name</label>
          <input
            type="text"
            value={taskName}
            onChange={(e) => setTaskName(e.target.value)}
            placeholder="Enter task name"
            className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm
              focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
          />

          <label className="text-sm font-semibold text-gray-800">Description</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Enter task description"
            className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm
              focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
          />

          <label className="text-sm font-semibold text-gray-800">Due Date</label>
          <input
            type="date"
            value={dueDate}
            onChange={(e) => setDueDate(e.target.value)}
            className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm
              focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
          />

          <label className="text-sm font-semibold text-gray-800">
            Assign to User
          </label>

          <Select
            styles={selectStyles}
            isDisabled={assignedRoleByUsers !== "" || assignedRole !== ""}
            placeholder="Select User..."
            value={
              assignedUser
                ? userOptions.find(u => u.value === assignedUser)
                : null
            }
            onChange={(opt) => {
              setAssignedUser(opt?.value || "");
              if (opt) {
                setAssignedRole("");
                setAssignedRoleByUsers("");
              }
            }}
            options={userOptions}
          />


          {/* OR Separator */}
          <div className="flex items-center my-1">
            <div className="flex-grow border-t border-[#522320]/40"></div>
            <span className="mx-2 text-xs text-[#522320]/60 font-medium bg-white px-2">OR</span>
            <div className="flex-grow border-t border-[#522320]/40"></div>
          </div>

          {/* Assign to Users by Role */}
          <label className="text-sm font-semibold text-gray-800 mt-2">
            Assign to Users by Role
          </label>
          <Select
            styles={selectStyles}
            isDisabled={assignedUser !== "" || assignedRole !== ""}
            placeholder="Select Role..."
            value={
              assignedRoleByUsers
                ? roleOptions.find(r => r.value === assignedRoleByUsers)
                : null
            }
            onChange={(opt) => {
              setAssignedRoleByUsers(opt?.value || "");
              if (opt) {
                setAssignedUser("");
                setAssignedRole("");
              }
            }}
            options={roleOptions}
          />

          {/* OR Separator */}
          <div className="flex items-center my-1">
            <div className="flex-grow border-t border-[#522320]/40"></div>
            <span className="mx-2 text-xs text-[#522320]/60 font-medium bg-white px-2">OR</span>
            <div className="flex-grow border-t border-[#522320]/40"></div>
          </div>

          {/* Assign to Role */}
          <label className="text-sm font-semibold text-gray-800">Assign to Role</label>
          <Select
            styles={selectStyles}
            isDisabled={assignedUser !== "" || assignedRoleByUsers != ""}
            placeholder="Select Role..."
            value={
              assignedRole
                ? roleOptions.find(r => r.value === assignedRole)
                : null
            }
            onChange={(opt) => {
              setAssignedRole(opt?.value || "");
              if (opt) {
                setAssignedUser("");
                setAssignedRoleByUsers("");
              }
            }}
            options={roleOptions}
          />

        </div>

        {/* Footer */}
        <div className="mt-4 flex gap-2">
          <button
            onClick={handleCreateTask}
            disabled={loading}
            className={`flex-1 py-2 rounded-lg text-white font-semibold
              hover:scale-95 transition-transform duration-300 shadow
              ${loading ? "bg-green-600/60 cursor-not-allowed" : "bg-green-600 hover:bg-green-700"}`}
          >
            {loading ? "Creating..." : "Create Task"}
          </button>

          <button
            onClick={onClose}
            className="flex-1 py-2 rounded-lg bg-stone-200 hover:bg-stone-300
              text-gray-800 font-semibold hover:scale-95 transition-transform duration-300 shadow"
          >
            Cancel
          </button>
        </div>
      </div>

      {/* Tailwind animation */}
      <style>
        {`
          @keyframes slideUp {
            0% { transform: translateY(100%); opacity: 0; }
            100% { transform: translateY(0); opacity: 1; }
          }
          .animate-slideUp {
            animation: slideUp 0.2s ease-out forwards;
          }

          /* Custom scrollbar for modal */
          .overflow-y-scroll::-webkit-scrollbar {
            width: 8px;
          }

          .overflow-y-scroll::-webkit-scrollbar-track {
            background: transparent;
            margin: 0.4rem 0;
          }

          .overflow-y-scroll::-webkit-scrollbar-thumb {
            background: #d1d5db;
            border-radius: 8px;
          }

          .overflow-y-scroll::-webkit-scrollbar-thumb:hover {
            background: #9ca3af;
          }
        `}
      </style>
    </div>
  );
};
