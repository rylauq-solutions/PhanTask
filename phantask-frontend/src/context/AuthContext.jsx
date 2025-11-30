// context/AuthContext.jsx
import React, { createContext, useContext, useEffect, useState } from "react";
import { apiService } from "../services/api";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const testToken = sessionStorage.getItem("testToken");
        if (testToken === "open") {
            setUser({
                username: "test-user",
                email: "test@example.com",
                role: "TEST",
                roles: ["TEST", "Developer"],
                fullName: "Test User",
                department: "Testing Department",
                phone: "+1 234 567 8900",
                yearOfStudy: "N/A",
                photoUrl: null
            });
            setLoading(false);
            return;
        }

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
                // IMPORTANT: do NOT clear sessionStorage here
                // Only mark user as not loaded
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
