import React, { createContext, useContext } from 'react';
import { apiService } from '../services/api';

const ApiContext = createContext();

export const ApiProvider = ({ children }) => {
    return (
        <ApiContext.Provider value={{ api: apiService }}>
            {children}
        </ApiContext.Provider>
    );
};

export const useApi = () => {
    const context = useContext(ApiContext);
    if (!context) {
        throw new Error('useApi must be used within ApiProvider');
    }
    return context.api;
};
