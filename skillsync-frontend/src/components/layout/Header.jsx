import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import api from '../../services/api';

const Header = () => {
  const username = localStorage.getItem('username') || 'User';
  const role = localStorage.getItem('role') || 'ROLE_LEARNER';
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const navigate = useNavigate();
  const location = useLocation();

  const getPageTitle = () => {
    const path = location.pathname;
    if (path.includes('dashboard')) return 'Dashboard';
    if (path.includes('mentors')) return 'Find Mentors';
    if (path.includes('sessions')) return 'My Sessions';
    if (path.includes('reviews')) return 'Reviews';
    if (path.includes('profile')) return 'My Profile';
    if (path.includes('notifications')) return 'Notifications';
    if (path.includes('settings')) return 'Settings';
    if (path.includes('book')) return 'Book Session';
    return '';
  };

  useEffect(() => {
    const fetchUnread = () => {
      if (role === 'ROLE_ADMIN') {
        const isRead = localStorage.getItem('notifs_read') === 'true';
        if (!isRead) {
          api.get('/auth-service/admin/mentor-requests').catch(() => ({ data: [] }))
            .then(mentorsRes => {
              const pending = mentorsRes.data?.length || 0;
              setUnreadCount(pending);
            });
        } else {
          setUnreadCount(0);
        }
      } else {
        const userId = localStorage.getItem('userId');
        if (!userId) return;

        const endpoint = role === 'ROLE_MENTOR' 
          ? `/session-service/sessions/mentor/${userId}` 
          : `/session-service/sessions/learner/${userId}`;

        api.get(endpoint)
          .then(res => {
            const sessions = res.data || [];
            let unread = 0;
            
            sessions.forEach(s => {
              let isNotif = false;
              if (role === 'ROLE_MENTOR' && s.status === 'REQUESTED') isNotif = true;
              else if (role === 'ROLE_MENTOR' && s.status === 'ACCEPTED') isNotif = true;
              else if (role === 'ROLE_LEARNER' && s.status === 'ACCEPTED') isNotif = true;
              else if (role === 'ROLE_LEARNER' && s.status === 'REJECTED') isNotif = true;
              else if (s.status === 'CANCELLED') isNotif = true;
              else if (s.status === 'COMPLETED' && role === 'ROLE_LEARNER') isNotif = true;
              
              if (isNotif && localStorage.getItem(`notifs_read_${s.id}`) !== 'true') {
                unread++;
              }
            });

            setUnreadCount(unread);
          }).catch(() => setUnreadCount(0));
      }
    };

    fetchUnread();

    const handleRead = () => setUnreadCount(0);
    window.addEventListener('notificationsRead', handleRead);
    return () => window.removeEventListener('notificationsRead', handleRead);
  }, [role]);

  const getInitials = (name) => {
    return name
      .split(' ')
      .map(part => part[0])
      .join('')
      .toUpperCase()
      .substring(0, 2);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
    localStorage.removeItem('userId');
    window.location.href = '/login';
  };

  return (
    <header className="header">
      <div className="header-title">
        <h2 style={{ margin: 0, fontSize: '1.25rem', color: '#111827' }}>{getPageTitle()}</h2>
      </div>
      <div className="header-user">
        <div className="notification-bell" style={{cursor: 'pointer'}} onClick={() => navigate('/app/notifications')}>
          🔔
          {unreadCount > 0 && <span className="notification-badge">{unreadCount}</span>}
        </div>
        <div style={{position: 'relative'}}>
          <div className="user-avatar" title={username} style={{cursor: 'pointer'}} onClick={() => setDropdownOpen(!dropdownOpen)}>
            {getInitials(username)}
          </div>
          
          {dropdownOpen && (
            <div style={{position: 'absolute', right: 0, top: '50px', backgroundColor: '#fff', boxShadow: '0 4px 12px rgba(0,0,0,0.1)', borderRadius: '8px', width: '150px', zIndex: 100, overflow: 'hidden'}}>
              <div style={{padding: '10px 15px', borderBottom: '1px solid #f0f0f0', color: '#111827', fontWeight: 600}}>
                {username}
              </div>
              <div style={{padding: '10px 15px', cursor: 'pointer', color: '#4b5563', fontSize: '0.9rem'}} onClick={() => { setDropdownOpen(false); navigate('/app/settings'); }}>
                ⚙️ Settings
              </div>
              <div style={{padding: '10px 15px', cursor: 'pointer', color: '#e11d48', fontSize: '0.9rem', borderTop: '1px solid #f0f0f0'}} onClick={handleLogout}>
                🚪 Log out
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
