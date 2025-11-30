// context/AuthContext.jsx
import React, { createContext, useContext, useEffect, useState } from "react";
import { apiService } from "../services/api";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {

    // Read test token synchronously BEFORE any render
    const testToken = sessionStorage.getItem("testToken");

    // Instant user initialization here — BEFORE effect runs
    const [user, setUser] = useState(
        testToken === "open"
            ? {
                username: "test_user",
                email: "test_user@example.com",
                role: "TEST",
                roles: ["TEST", "Developer"],
                fullName: "Tester User",
                department: "Testing Department",
                phone: "+91 234 567 8900",
                yearOfStudy: "N/A",
                photoUrl: null
            }
            : null
    );

    // For test mode, loading should immediately be false
    const [loading, setLoading] = useState(testToken === "open" ? false : true);

    useEffect(() => {
        // Skip API call in test mode
        if (testToken === "open") return;

        const token = sessionStorage.getItem("authToken");
        if (!token) {
            setLoading(false);
            return;
        }

        apiService
            .getUserProfile()
            .then((res) => {
                setUser(res.data);
            })
            .catch((err) => {
                console.log(
                    "getUserProfile failed",
                    err?.config?.url,
                    err.response?.status,
                    err.response?.data
                );
                setUser(null);
            })
            .finally(() => setLoading(false));
    }, []);

    const value = {
        user,
        loading,
        isAuthenticated: !!user,
        setUser,
        logout: () => {
            sessionStorage.clear();
            setUser(null);
            window.location.href = "/login";
        },
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => useContext(AuthContext);
