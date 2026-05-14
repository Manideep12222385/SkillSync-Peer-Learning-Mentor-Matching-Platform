import React from 'react';
import { NavLink } from 'react-router-dom';

const Sidebar = () => {
  const role = localStorage.getItem('role') || 'ROLE_LEARNER';

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <NavLink to="/app/dashboard" style={{textDecoration: 'none', color: 'inherit'}}>
          Skill<span>Sync</span>
        </NavLink>
      </div>
      <div className="sidebar-menu">
        <NavLink to="/app/dashboard" className={({ isActive }) => isActive ? 'sidebar-link active' : 'sidebar-link'}>
          🏠 Dashboard
        </NavLink>
        
        {(role === 'ROLE_LEARNER' || role === 'ROLE_USER') && (
          <NavLink to="/app/mentors" className={({ isActive }) => isActive ? 'sidebar-link active' : 'sidebar-link'}>
            🧑‍🏫 Find Mentors
          </NavLink>
        )}

        {role !== 'ROLE_ADMIN' && (
          <NavLink to="/app/sessions" className={({ isActive }) => isActive ? 'sidebar-link active' : 'sidebar-link'}>
            📅 My Sessions
          </NavLink>
        )}

        {role !== 'ROLE_ADMIN' && (
          <NavLink to="/app/reviews" className={({ isActive }) => isActive ? 'sidebar-link active' : 'sidebar-link'}>
            ⭐ Reviews
          </NavLink>
        )}
        
        {role === 'ROLE_ADMIN' && (
          <>
            <NavLink to="/app/users" className={({ isActive }) => isActive ? 'sidebar-link active' : 'sidebar-link'}>
              👥 User Management
            </NavLink>
            <NavLink to="/app/skills" className={({ isActive }) => isActive ? 'sidebar-link active' : 'sidebar-link'}>
              🛠️ Skill Catalog
            </NavLink>
          </>
        )}

        <div className="sidebar-section-title">Account</div>
        <NavLink to="/app/profile" className={({ isActive }) => isActive ? 'sidebar-link active' : 'sidebar-link'}>
          👤 My Profile
        </NavLink>
        <NavLink to="/app/notifications" className={({ isActive }) => isActive ? 'sidebar-link active' : 'sidebar-link'}>
          🔔 Notifications
        </NavLink>
        <NavLink to="/app/settings" className={({ isActive }) => isActive ? 'sidebar-link active' : 'sidebar-link'}>
          ⚙️ Settings
        </NavLink>
      </div>
    </div>
  );
};

export default Sidebar;
