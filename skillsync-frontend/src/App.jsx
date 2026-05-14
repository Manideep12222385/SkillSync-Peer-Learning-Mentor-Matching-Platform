import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import Dashboard from './pages/Dashboard';
import MentorsList from './pages/MentorsList';
import BookSession from './pages/BookSession';
import Home from './pages/Home';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import UserManagement from './pages/UserManagement';
import Profile from './pages/Profile';
import Notifications from './pages/Notifications';
import Settings from './pages/Settings';
import Reviews from './pages/Reviews';
import SkillCatalog from './pages/SkillCatalog';
import MySessions from './pages/MySessions';

function App() {
  useEffect(() => {
    // Initialize global theme
    const theme = localStorage.getItem('theme') || 'light';
    if (theme === 'dark') {
      document.body.classList.add('dark-theme');
    }
  }, []);

  return (
    <Router>
      <Routes>
        {/* Public Landing Page */}
        <Route path="/" element={
          <>
            <Navbar />
            <Home />
          </>
        } />

        {/* Public Auth Routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />

        {/* Protected/App Routes within Main Layout */}
        <Route path="/app" element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }>
          <Route index element={<Navigate to="/app/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="mentors" element={<MentorsList />} />
          <Route path="book" element={<BookSession />} />

          <Route path="users" element={<UserManagement />} />
          <Route path="skills" element={<SkillCatalog />} />
          <Route path="sessions" element={<MySessions />} />
          <Route path="reviews" element={<Reviews />} />
          <Route path="profile" element={<Profile />} />
          <Route path="notifications" element={<Notifications />} />
          <Route path="settings" element={<Settings />} />
        </Route>

        {/* Redirect old routes */}
        <Route path="/dashboard" element={<Navigate to="/app/dashboard" replace />} />
        <Route path="/mentors" element={<Navigate to="/app/mentors" replace />} />
        <Route path="/sessions" element={<Navigate to="/app/sessions" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
