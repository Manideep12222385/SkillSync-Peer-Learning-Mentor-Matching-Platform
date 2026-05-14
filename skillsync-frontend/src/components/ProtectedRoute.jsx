import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  
  if (!token) {
    // If there is no token, redirect to login page
    return <Navigate to="/login" replace />;
  }

  // If there is a token, render the children
  return children ? children : <Outlet />;
};

export default ProtectedRoute;
