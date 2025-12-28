export const ATTENDANCE_UI = {
  NOT_CHECKED_IN: {
    text: "❌ Attendance not marked yet",
    color: "text-red-600",
    action: "Generate QR to check in"
  },
  CHECKED_IN: {
    text: "🟡 Checked in successfully",
    color: "text-yellow-600",
    action: "Generate QR to check out"
  },
  COMPLETED: {
    text: "✅ Attendance completed for today",
    color: "text-green-600",
    action: "No further action required"
  }
};
