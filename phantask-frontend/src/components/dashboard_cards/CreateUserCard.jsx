import React, { useState } from "react";
import { apiService } from "../../services/api";
import { toast } from "react-hot-toast";

const CreateUserCard = () => {
  const [showModal, setShowModal] = useState(false);

  return (
    <>
      <div className="w-full h-full rounded-xl border-2 border-[#522320] bg-[#ffffff] p-3 shadow-md shadow-[#522320]/20 transition-all duration-300 hover:shadow-xl hover:shadow-[#522320]/30 hover:-translate-y-0.5 flex flex-col">
        <span className="w-full h-full flex flex-col justify-between">
          <h2 className="h-10 text-lg font-semibold py-1 text-[#522320] text-center">
            Create User
          </h2>

          {/* Empty-state content (same style as AssignedTasksCard) */}
          <main className="w-full h-full flex flex-col items-center justify-center p-4 bg-gradient-to-b from-[#fff9f8]/30 to-[#fff1f0]/20 rounded-xl border-[#522320]/20 shadow-sm">
            <div className="w-14 h-14 bg-[#522320]/5 rounded-2xl flex items-center justify-center mb-3 shadow-md shadow-[#522320]/10">
              <span className="text-2xl">👤</span>
            </div>

            <h3 className="text-xl font-bold text-[#522320] mb-1.5 leading-tight">
              Add New User
            </h3>

            <p className="text-[#522320]/60 text-xs font-medium text-center leading-tight max-w-[160px]">
              Create a user account using an email address.
            </p>
          </main>

          <button
            onClick={() => setShowModal(true)}
            className="w-full text-center hover:rounded-xl text-[#42260b] text-sm font-medium mt-1 py-2 hover:bg-red-100"
          >
            Create User
          </button>
        </span>
      </div>

      {showModal && <CreateUserModal onClose={() => setShowModal(false)} />}
    </>
  );
};

export default CreateUserCard;


const CreateUserModal = ({ onClose }) => {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [confirmVisible, setConfirmVisible] = useState(false);
  const [confirmed, setConfirmed] = useState(false);

  const handleCreateUser = async () => {
    if (!email.trim()) {
      toast.error("Email address is required");
      return;
    }

    if (!confirmed) {
      toast.error("Please confirm the email before proceeding");
      return;
    }

    try {
      setLoading(true);

      const res = await apiService.createAccount(email);

      toast.success(
        `User created 🎉\nUsername: ${res.data.username}\n${res.data.tempPasswordMessage}`,
        { duration: 4000 }
      );

      onClose();
    } catch (err) {
      toast.error(
        err?.response?.data?.error || "Failed to create user"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Overlay */}
      <div
        className="absolute inset-0 bg-black/40"
        onClick={onClose}
      />

      {/* Modal */}
      <div className="relative w-[90%] sm:w-[85%] md:w-2/5 max-h-[95vh] overflow-auto">
        <div className="bg-white rounded-xl p-4 md:p-6 shadow-xl h-full flex flex-col border border-red-700/30">

          {/* Header */}
          <div className="mb-3 text-center">
            <h3 className="text-2xl font-bold text-amber-950">
              Create User
            </h3>
            <p className="text-sm text-gray-700 mt-1">
              Add a new user using email address
            </p>
          </div>

          {/* Body */}
          <div className="flex-1">
            <label className="text-sm font-semibold text-gray-800">
              Email Address
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="user@example.com"
              className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm
                focus:outline-none focus:ring-2 focus:ring-red-600 focus:border-red-600"
            />

          </div>

          {/* Footer */}
          <div className="mt-4">
            {!confirmVisible ? (
              <div className="flex gap-2">
                <button
                  onClick={() => setConfirmVisible(true)}
                  className="flex-1 py-2 rounded-lg bg-red-700 hover:bg-red-800
                    text-white font-semibold hover:scale-95 transition-transform duration-300 shadow"
                >
                  Create User
                </button>

                <button
                  onClick={onClose}
                  className="flex-1 py-2 rounded-lg bg-stone-200 hover:bg-stone-300
                    text-gray-800 font-semibold hover:scale-95 transition-transform duration-300 shadow"
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
                  I confirm the email is correct.
                </label>

                <div className="flex gap-2">
                  <button
                    onClick={handleCreateUser}
                    disabled={!confirmed || loading}
                    className={`flex-1 py-2 rounded-lg text-white font-semibold
                      hover:scale-95 transition-transform duration-300 shadow
                      ${confirmed
                        ? "bg-green-600 hover:bg-green-700"
                        : "bg-green-600/60 cursor-not-allowed"
                      }`}
                  >
                    {loading ? "Creating..." : "Confirm & Create"}
                  </button>

                  <button
                    onClick={() => {
                      setConfirmVisible(false);
                      setConfirmed(false);
                    }}
                    className="flex-1 py-2 rounded-lg bg-stone-200 hover:bg-stone-300
                      text-gray-800 font-semibold hover:scale-95 transition-transform duration-300 shadow"
                  >
                    Back
                  </button>
                </div>
              </div>
            )}
          </div>

        </div>
      </div>
    </div>
  );
};
