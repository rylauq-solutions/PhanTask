// * ============================================================================
// * ROLE CONSTANTS
// * ============================================================================
// ! Centralized role definitions - fetched dynamically from backend
// ? This file provides utilities to work with roles from your database

// * ============================================================================
// * DEFAULT FALLBACK ROLES
// * ============================================================================
// ! Used only if API call fails - update these to match your DB enum
export const DEFAULT_ROLES = ["USER", "TRAINEE", "ADMIN"];

export const DEFAULT_ROLE_OPTIONS = [
  { value: "USER", label: "User" },
  { value: "TRAINEE", label: "Trainee" },
  { value: "ADMIN", label: "Admin" },
];

// * ============================================================================
// * ROLE CONSTANTS (for comparison/validation)
// * ============================================================================
export const ROLES = {
  USER: "USER",
  TRAINEE: "TRAINEE",
  ADMIN: "ADMIN",
};

// * ============================================================================
// * UTILITY FUNCTIONS
// * ============================================================================

/**
 * Transforms backend roles array to React-Select options format
 * @param {string[]} rolesArray - Array of role strings from backend
 * @returns {Array<{value: string, label: string}>} - Formatted options
 *
 * @example
 * ! transformRolesToOptions(["USER", "TRAINEE"])
 * Returns: [
 *   { value: "USER", label: "User" },
 *   { value: "TRAINEE", label: "Trainee" }
 * ]
 */
export const transformRolesToOptions = (rolesArray) => {
  if (!rolesArray || !Array.isArray(rolesArray)) {
    console.warn("Invalid roles array, using defaults");
    return DEFAULT_ROLE_OPTIONS;
  }

  return rolesArray.map((role) => {
    // Capitalize first letter: "USER" -> "User", "TRAINEE" -> "Trainee"
    return {
      value: role,
      label: role.charAt(0) + role.slice(1).toLowerCase(),
    };
  });
};

/**
 * Filters out ADMIN role from options (for task assignment)
 * @param {Array<{value: string, label: string}>} roleOptions
 * @returns {Array<{value: string, label: string}>}
 */
export const filterOutAdmin = (roleOptions) => {
  return roleOptions.filter((role) => role.value !== ROLES.ADMIN);
};

/**
 * Adds placeholder option at the beginning
 * @param {Array<{value: string, label: string}>} roleOptions
 * @returns {Array<{value: string, label: string}>}
 */
export const addPlaceholder = (roleOptions) => {
  return [{ value: "", label: "Select Role..." }, ...roleOptions];
};

/**
 * Gets role options without ADMIN and with placeholder (for task assignment dropdowns)
 * @param {Array<{value: string, label: string}>} roleOptions
 * @returns {Array<{value: string, label: string}>}
 */
export const getRoleOptionsWithoutAdmin = (roleOptions) => {
  return addPlaceholder(filterOutAdmin(roleOptions));
};
