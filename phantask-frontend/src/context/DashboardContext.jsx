import React, { createContext, useContext, useReducer } from 'react';

const DashboardContext = createContext();

const initialState = {
    tasks: [],
    attendance: { percentage: 0 },
    schedule: [],
    notices: [],
    loading: false,
    error: null,
};

const reducer = (state, action) => {
    switch (action.type) {
        case 'FETCH_START':
            return { ...state, loading: true, error: null };
        case 'FETCH_SUCCESS':
            return { ...state, loading: false, ...action.payload };
        case 'FETCH_ERROR':
            return { ...state, loading: false, error: action.payload };
        default:
            return state;
    }
};

export const DashboardProvider = ({ children }) => {
    const [state, dispatch] = useReducer(reducer, initialState);

    const fetchDashboardData = async () => {
        dispatch({ type: 'FETCH_START' });
        try {
            const [tasks, attendance, schedule, notices] = await Promise.all([
                apiService.getAssignedTasks(),
                apiService.getAttendance(),
                apiService.getSchedule(),
                apiService.getNotices(),
            ]);
            dispatch({
                type: 'FETCH_SUCCESS',
                payload: {
                    tasks: tasks.data,
                    attendance: attendance.data,
                    schedule: schedule.data,
                    notices: notices.data,
                },
            });
        } catch (error) {
            dispatch({ type: 'FETCH_ERROR', payload: error.message });
        }
    };

    return (
        <DashboardContext.Provider value={{ ...state, fetchDashboardData }}>
            {children}
        </DashboardContext.Provider>
    );
};

export const useDashboard = () => useContext(DashboardContext);
