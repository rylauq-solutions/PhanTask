import React, { createContext, useContext, useEffect, useState, useCallback } from "react";
import { apiService } from "../services/api";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Memoized function to refresh profile
    const refreshProfile = useCallback(async () => {
        const token = sessionStorage.getItem("authToken");
        if (!token) {
            setLoading(false);
            setUser(null);
            return;
        }

        try {
            const res = await apiService.getUserProfile();
            setUser(res.data);
        } catch (err) {
            console.error("Failed to fetch profile:", err);
            setUser(null);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        refreshProfile();
    }, [refreshProfile]);

    const logout = (navigate) => {
    sessionStorage.clear();
    setUser(null);
    if (navigate) navigate("/login"); // optional redirect
};

    return (
        <AuthContext.Provider value={{ user, loading, isAuthenticated: !!user, setUser, refreshProfile, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
