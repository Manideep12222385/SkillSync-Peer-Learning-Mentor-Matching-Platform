import React, { useState, useEffect } from 'react';
import api from '../services/api';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      // Fetch active mentors, active learners, and blocked users to build a complete list
      const [learners, mentors, blocked, pending, rejected] = await Promise.all([
        api.get('/auth-service/admin/users/active-learners').catch(() => ({ data: [] })),
        api.get('/auth-service/admin/users/active-mentors').catch(() => ({ data: [] })),
        api.get('/auth-service/admin/users/blocked').catch(() => ({ data: [] })),
        api.get('/auth-service/admin/mentor-requests').catch(() => ({ data: [] })),
        api.get('/auth-service/admin/users/rejected').catch(() => ({ data: [] }))
      ]);

      const allUsers = [
        ...(learners.data || []),
        ...(mentors.data || []),
        ...(blocked.data || []),
        ...(pending.data || []),
        ...(rejected.data || [])
      ].sort((a, b) => b.userId - a.userId);

      // Remove duplicates just in case
      const uniqueUsers = Array.from(new Map(allUsers.map(u => [u.userId, u])).values());
      
      setUsers(uniqueUsers);
      setError(null);
    } catch (err) {
      setError('Failed to load user data');
    } finally {
      setLoading(false);
    }
  };

  const handleBlockUser = async (id, currentStatus) => {
    if (currentStatus === 'BLOCKED') return;
    if (window.confirm('Are you sure you want to block this user?')) {
      try {
        await api.put(`/auth-service/admin/users/${id}/block`);
        fetchUsers();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to block user');
      }
    }
  };

  const handleActivateUser = async (id) => {
    try {
      await api.put(`/auth-service/admin/users/${id}/activate`);
      fetchUsers();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to activate user');
    }
  };

  const handleApproveMentor = async (id) => {
    try {
      await api.put(`/auth-service/admin/mentors/${id}/approve`);
      fetchUsers();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to approve mentor');
    }
  };

  const handleRejectMentor = async (id) => {
    if (window.confirm('Are you sure you want to reject this mentor?')) {
      try {
        await api.put(`/auth-service/admin/mentors/${id}/reject`);
        fetchUsers();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to reject mentor');
      }
    }
  };

  if (loading) return <div style={{padding: '2rem'}}>Loading user data...</div>;

  const filteredUsers = users.filter(user => 
    user.username.toLowerCase().includes(searchQuery.toLowerCase()) || 
    user.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
    user.role.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="dashboard-container">
      <div className="dashboard-header" style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
        <div>
          <h2>User Management 👥</h2>
          <p className="date-subtitle">Manage platform users, roles, and access.</p>
        </div>
        <div>
          <input 
            type="text" 
            placeholder="Search users..." 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{padding: '0.75rem 1rem', borderRadius: '8px', border: '1px solid #d1d5db', width: '300px'}}
          />
        </div>
      </div>

      {error && <div style={{color: 'red', marginBottom: '1rem'}}>{error}</div>}

      <div style={{backgroundColor: '#fff', borderRadius: '12px', padding: '1.5rem', boxShadow: '0 2px 8px rgba(0,0,0,0.05)'}}>
        <table className="sessions-table" style={{width: '100%', borderCollapse: 'collapse'}}>
          <thead>
            <tr style={{borderBottom: '2px solid #f0f0f0', textAlign: 'left'}}>
              <th style={{padding: '1rem'}}>ID</th>
              <th style={{padding: '1rem'}}>USERNAME</th>
              <th style={{padding: '1rem'}}>EMAIL</th>
              <th style={{padding: '1rem'}}>ROLE</th>
              <th style={{padding: '1rem'}}>STATUS</th>
              <th style={{padding: '1rem'}}>ACTIONS</th>
            </tr>
          </thead>
          <tbody>
            {filteredUsers.map(user => (
              <tr key={user.userId} style={{borderBottom: '1px solid #f0f0f0'}}>
                <td style={{padding: '1rem'}}>#{user.userId}</td>
                <td style={{padding: '1rem', fontWeight: 600}}>{user.username}</td>
                <td style={{padding: '1rem', color: '#6b7280'}}>{user.email}</td>
                <td style={{padding: '1rem'}}>
                  <span style={{
                    padding: '4px 8px', borderRadius: '4px', fontSize: '0.8rem', fontWeight: 600,
                    backgroundColor: user.role === 'ROLE_MENTOR' ? '#e0e7ff' : '#fce7f3',
                    color: user.role === 'ROLE_MENTOR' ? '#4f46e5' : '#db2777'
                  }}>
                    {user.role.replace('ROLE_', '')}
                  </span>
                </td>
                <td style={{padding: '1rem'}}>
                  <span className={`status-badge ${user.accountStatus.toLowerCase()}`}>
                    {user.accountStatus}
                  </span>
                </td>
                <td style={{padding: '1rem'}}>
                  {user.accountStatus === 'PENDING' && user.role === 'ROLE_MENTOR' && (
                    <>
                      <button onClick={() => handleApproveMentor(user.userId)} style={{background: '#10b981', color: '#fff', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer', marginRight: '8px'}}>
                        Approve
                      </button>
                      <button onClick={() => handleRejectMentor(user.userId)} style={{background: '#ef4444', color: '#fff', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer', marginRight: '8px'}}>
                        Reject
                      </button>
                    </>
                  )}
                  {user.accountStatus === 'BLOCKED' ? (
                    <button onClick={() => handleActivateUser(user.userId)} style={{background: '#3b82f6', color: '#fff', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer'}}>
                      Unblock
                    </button>
                  ) : (
                    user.accountStatus === 'ACTIVE' && (
                      <button onClick={() => handleBlockUser(user.userId, user.accountStatus)} style={{background: '#ef4444', color: '#fff', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer'}}>
                        Block
                      </button>
                    )
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {filteredUsers.length === 0 && <p style={{padding: '2rem', textAlign: 'center'}}>No users found matching "{searchQuery}".</p>}
      </div>
    </div>
  );
};

export default UserManagement;
