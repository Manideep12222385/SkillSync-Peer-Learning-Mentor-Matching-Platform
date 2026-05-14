






import React, { useState, useEffect } from 'react';
import api from '../services/api';

const Notifications = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const role = localStorage.getItem('role') || 'ROLE_LEARNER';

  const getRelativeTime = (timestamp) => {
    if (!timestamp) return 'Recent';
    const timeDiff = new Date() - new Date(timestamp);
    const seconds = Math.floor(timeDiff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (days > 0) return `${days} day${days > 1 ? 's' : ''} ago`;
    if (hours > 0) return `${hours} hr${hours > 1 ? 's' : ''} ago`;
    if (minutes > 0) return `${minutes} min ago`;
    return 'Just now';
  };

  useEffect(() => {
    if (role === 'ROLE_ADMIN') {
      Promise.all([
        api.get('/auth-service/admin/mentor-requests').catch(() => ({ data: [] })),
        api.get('/auth-service/admin/users/active-learners').catch(() => ({ data: [] }))
      ])
        .then(([mentorsRes, learnersRes]) => {
          const pendingMentors = mentorsRes.data || [];

          const dynamicNotifs = pendingMentors.map(m => ({
            id: `pending-${m.userId}`,
            title: 'Pending Mentor Approval',
            message: `${m.username} (${m.email}) has requested to become a mentor. Please review their application.`,
            time: 'Just now',
            read: localStorage.getItem('notifs_read') === 'true',
            type: 'alert'
          }));

          setNotifications(dynamicNotifs);
        })
        .catch(err => {
          console.error(err);
          setNotifications([{ id: 'err-1', title: 'Connection Error', message: 'Could not fetch live notifications.', time: 'Just now', read: false, type: 'alert' }]);
        })
        .finally(() => setLoading(false));
    } else {
      const userId = localStorage.getItem('userId');
      if (!userId) { setLoading(false); return; }

      const endpoint = role === 'ROLE_MENTOR' 
        ? `/session-service/sessions/mentor/${userId}` 
        : `/session-service/sessions/learner/${userId}`;

      api.get(endpoint)
        .then(res => {
          const sessions = res.data || [];
          const notifs = [];
          
          sessions.forEach(s => {
            const dateStr = new Date(s.sessionTime).toLocaleDateString([], { month: 'short', day: 'numeric' });
            const timeStr = new Date(s.sessionTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            const notifTime = getRelativeTime(s.updatedAt || s.createdAt);
            
            if (role === 'ROLE_MENTOR' && s.status === 'REQUESTED') {
              notifs.push({
                id: `req-${s.id}`,
                title: 'New Session Request 🔔',
                message: `You have a new session request for ${dateStr} at ${timeStr}.`,
                time: getRelativeTime(s.createdAt),
                rawTime: s.createdAt,
                read: localStorage.getItem(`notifs_read_${s.id}`) === 'true',
                type: 'alert'
              });
            } else if (role === 'ROLE_LEARNER' && s.status === 'ACCEPTED') {
              notifs.push({
                id: `acc-${s.id}`,
                title: 'Session Accepted! 🎉',
                message: `Your session on ${dateStr} at ${timeStr} has been approved.`,
                time: notifTime,
                rawTime: s.updatedAt || s.createdAt,
                read: localStorage.getItem(`notifs_read_${s.id}`) === 'true',
                type: 'success'
              });
            } else if (role === 'ROLE_MENTOR' && s.status === 'ACCEPTED') {
              notifs.push({
                id: `acc-m-${s.id}`,
                title: 'Upcoming Session 📅',
                message: `You have an approved session scheduled for ${dateStr} at ${timeStr}.`,
                time: notifTime,
                rawTime: s.updatedAt || s.createdAt,
                read: localStorage.getItem(`notifs_read_${s.id}`) === 'true',
                type: 'info'
              });
            } else if (role === 'ROLE_LEARNER' && s.status === 'REJECTED') {
              notifs.push({
                id: `rej-${s.id}`,
                title: 'Session Rejected',
                message: `Your session request for ${dateStr} was rejected by the mentor.`,
                time: notifTime,
                rawTime: s.updatedAt || s.createdAt,
                read: localStorage.getItem(`notifs_read_${s.id}`) === 'true',
                type: 'alert'
              });
            } else if (s.status === 'CANCELLED') {
              notifs.push({
                id: `canc-${s.id}`,
                title: 'Session Cancelled',
                message: `A session scheduled for ${dateStr} at ${timeStr} has been cancelled.`,
                time: notifTime,
                rawTime: s.updatedAt || s.createdAt,
                read: localStorage.getItem(`notifs_read_${s.id}`) === 'true',
                type: 'info'
              });
            } else if (s.status === 'COMPLETED' && role === 'ROLE_LEARNER') {
              notifs.push({
                id: `comp-${s.id}`,
                title: 'Session Completed 🌟',
                message: `Your session on ${dateStr} is complete. Don't forget to leave a review!`,
                time: notifTime,
                rawTime: s.updatedAt || s.createdAt,
                read: localStorage.getItem(`notifs_read_${s.id}`) === 'true',
                type: 'info'
              });
            }
          });

          if (notifs.length === 0) {
            notifs.push({ 
              id: 'sys-1', 
              title: 'Welcome to SkillSync 👋', 
              message: 'Your account is ready! Head over to the Dashboard to get started.', 
              time: 'Just now', 
              read: true, 
              type: 'system' 
            });
          }
          
          // Sort notifications based on updatedAt/createdAt fallback to 0 if missing
          notifs.sort((a, b) => {
            const timeA = a.rawTime ? new Date(a.rawTime).getTime() : 0;
            const timeB = b.rawTime ? new Date(b.rawTime).getTime() : 0;
            return timeB - timeA;
          });
          
          setNotifications(notifs);
        })
        .catch(err => {
          console.error(err);
          setNotifications([{ id: 'err-1', title: 'Connection Error', message: 'Could not fetch live notifications.', time: 'Just now', read: false, type: 'alert' }]);
        })
        .finally(() => setLoading(false));
    }
  }, [role]);

  const markAllAsRead = () => {
    setNotifications(notifications.map(n => ({ ...n, read: true })));
    notifications.forEach(n => {
      if (n.id && n.id.toString().includes('-')) {
        const parts = n.id.toString().split('-');
        const sessionId = parts[parts.length - 1]; // Always get the last part which is s.id
        localStorage.setItem(`notifs_read_${sessionId}`, 'true');
      }
    });
    localStorage.setItem('notifs_read', 'true');
    window.dispatchEvent(new Event('notificationsRead'));
  };

  const getIcon = (type) => {
    switch(type) {
      case 'system': return '⚙️';
      case 'alert': return '⚠️';
      case 'success': return '✅';
      case 'info': return 'ℹ️';
      default: return '🔔';
    }
  };

  if (loading) return <div style={{padding: '2rem'}}>Loading notifications...</div>;

  return (
    <div className="dashboard-container">
      <div className="dashboard-header" style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
        <div>
          <h2>Notifications 🔔</h2>
          <p className="date-subtitle">Stay updated with your latest alerts and messages.</p>
        </div>
        <button onClick={markAllAsRead} style={{backgroundColor: '#f3f4f6', color: '#374151', border: '1px solid #d1d5db', padding: '0.5rem 1rem', borderRadius: '6px', fontWeight: 500, cursor: 'pointer', transition: 'all 0.2s'}}>
          Mark all as read
        </button>
      </div>

      <div style={{backgroundColor: '#fff', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)', overflow: 'hidden'}}>
        {notifications.length === 0 ? (
          <div style={{padding: '3rem', textAlign: 'center', color: '#6b7280'}}>
            <span style={{fontSize: '3rem', display: 'block', marginBottom: '1rem'}}>📭</span>
            You're all caught up! No new notifications.
          </div>
        ) : (
          <div style={{display: 'flex', flexDirection: 'column'}}>
            {notifications.map((notif, index) => (
              <div key={notif.id} style={{
                display: 'flex', gap: '1rem', padding: '1.5rem', 
                borderBottom: index < notifications.length - 1 ? '1px solid #f0f0f0' : 'none',
                backgroundColor: notif.read ? '#fff' : '#f8fafc',
                transition: 'background 0.2s'
              }}>
                <div style={{
                  width: '40px', height: '40px', borderRadius: '50%', 
                  backgroundColor: notif.read ? '#f3f4f6' : '#e0e7ff', 
                  display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.2rem'
                }}>
                  {getIcon(notif.type)}
                </div>
                <div style={{flex: 1}}>
                  <div style={{display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem'}}>
                    <h4 style={{margin: 0, fontSize: '1rem', color: '#111827', fontWeight: notif.read ? 500 : 600}}>
                      {notif.title}
                    </h4>
                    <span style={{fontSize: '0.8rem', color: '#9ca3af'}}>{notif.time}</span>
                  </div>
                  <p style={{margin: 0, color: '#4b5563', fontSize: '0.9rem', lineHeight: 1.5}}>
                    {notif.message}
                  </p>
                </div>
                {!notif.read && (
                  <div style={{display: 'flex', alignItems: 'center'}}>
                    <div style={{width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#4f46e5'}}></div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Notifications;
