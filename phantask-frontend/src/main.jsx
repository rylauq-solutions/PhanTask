import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import { ApiProvider } from './context/ApiContext.jsx';
import { DashboardProvider } from './context/DashboardContext.jsx';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'; // v5 import
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
  },
});

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <ApiProvider>
        <DashboardProvider>
          <App />
        </DashboardProvider>
      </ApiProvider>
    </QueryClientProvider>
  </React.StrictMode>
);
