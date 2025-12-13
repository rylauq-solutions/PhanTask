import React, { useState, useEffect } from "react";
import { apiService } from "../../services/api";
import { toast } from "react-hot-toast";

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
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const usersRes = await apiService.getUsers();
        setUsers(usersRes.data || []);

        const rolesRes = await apiService.getRoles();
        setRoles(rolesRes.data || []);
      } catch (err) {
        toast.error("Failed to fetch users or roles");
      }
    };
    fetchData();
  }, []);

  const handleCreateTask = async () => {
    if (!taskName.trim() || !description.trim() || !dueDate) {
      toast.error("Please fill all required fields");
      return;
    }

    if (!assignedUser && !assignedRole) {
      toast.error("Please assign the task to a user or a role");
      return;
    }

    try {
      setLoading(true);
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
      onClose();
    } catch (err) {
      toast.error(err?.response?.data?.error || "Failed to create task");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center md:items-center">
      {/* Overlay */}
      <div
        className="absolute inset-0 bg-black/40 transition-opacity duration-300"
        onClick={onClose}
      />

      {/* Modal */}
      <div
        className="relative w-[90%] sm:w-[85%] md:w-2/5 max-h-[95vh] overflow-y-scroll
          bg-white rounded-xl p-4 md:p-6 shadow-xl border border-red-700/30
          transform transition-transform duration-300 ease-out
          animate-slideUp"
      >
        {/* Header */}
        <div className="mb-3 text-center">
          <h3 className="text-2xl font-bold text-amber-950">Create Task</h3>
          <p className="text-sm text-gray-700 mt-1">
            Assign a new task to a user or a role
          </p>
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

          <label className="text-sm font-semibold text-gray-800">Assign to User</label>
          <select
            value={assignedUser}
            onChange={(e) => setAssignedUser(e.target.value)}
            className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm
              focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
          >
            <option value="">-- Select User --</option>
            {users.map((u) => (
              <option key={u.uid} value={u.username}>{u.username}</option>
            ))}
          </select>

          <label className="text-sm font-semibold text-gray-800">Assign to Role</label>
          <select
            value={assignedRole}
            onChange={(e) => setAssignedRole(e.target.value)}
            className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm
              focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
          >
            <option value="">-- Select Role --</option>
            {roles.map((r) => (
              <option key={r.rid} value={r.roleName}>{r.roleName}</option>
            ))}
          </select>
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
