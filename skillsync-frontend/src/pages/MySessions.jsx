import React, { useState, useEffect } from 'react';
import api from '../services/api';
import './Dashboard.css'; // Reuse dashboard styles

const MySessions = () => {
  const [sessions, setSessions] = useState([]);
  const [userNames, setUserNames] = useState({});
  const [reviewedSessionIds, setReviewedSessionIds] = useState(new Set());
  const [statusFilter, setStatusFilter] = useState('ALL');
  const role = localStorage.getItem('role');
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    if (userId) {
      if (role === 'ROLE_LEARNER') {
        api.get(`/review-service/reviews/learner/${userId}`)
          .then(res => {
            const reviewIds = new Set(res.data.map(r => r.sessionId));
            setReviewedSessionIds(reviewIds);
          })
          .catch(() => console.error("Could not fetch reviewed sessions"));
      }

      const endpoint = role === 'ROLE_MENTOR' 
        ? `/session-service/sessions/mentor/${userId}` 
        : `/session-service/sessions/learner/${userId}`;
        
      api.get(endpoint)
        .then(res => {
          const fetchedSessions = res.data || [];
          const now = new Date();
          const processedSessions = fetchedSessions.map(session => {
             if (session.status === 'ACCEPTED') {
                const startTime = new Date(session.sessionTime);
                const endTime = new Date(startTime.getTime() + session.durationMinutes * 60000);
                if (endTime < now) {
                   api.post(`/session-service/sessions/${session.id}/complete`).catch(e => console.error("Auto complete error", e));
                   return { ...session, status: 'COMPLETED' };
                }
             }
             return session;
          });
          setSessions(processedSessions);
          
          processedSessions.forEach(session => {
            if (role === 'ROLE_MENTOR') {
              const targetId = session.learnerId;
              if (targetId && !userNames[targetId]) {
                api.get(`/auth-service/auth/internal/users/${targetId}/name`)
                  .then(nameRes => {
                    setUserNames(prev => ({...prev, [targetId]: nameRes.data}));
                  })
                  .catch(() => {});
              }
            } else {
              const profileId = session.mentorId;
              if (profileId && !userNames[`mentor_${profileId}`]) {
                // First get the true userId, then get the name
                api.get(`/mentor-service/mentors/internal/${profileId}/userid`)
                  .then(idRes => {
                    const realUserId = idRes.data;
                    return api.get(`/auth-service/auth/internal/users/${realUserId}/name`);
                  })
                  .then(nameRes => {
                    setUserNames(prev => ({...prev, [`mentor_${profileId}`]: nameRes.data}));
                  })
                  .catch(() => {});
              }
            }
          });
        })
        .catch(err => console.error('Failed to fetch sessions', err));
    }
  }, [userId, role]);

  const handleAcceptSession = async (sessionId) => {
    const link = window.prompt("Enter Google Meet or Zoom link for this session:");
    if (link === null) return; // User cancelled
    if (!link.trim()) {
      alert("Meeting link is required to approve the session.");
      return;
    }
    try {
      await api.post(`/session-service/sessions/${sessionId}/accept`, { meetingLink: link });
      refreshSessions();
    } catch (err) {
      alert(err.response?.data?.message || err.response?.data || 'Failed to accept session');
    }
  };

  const handleCompleteSession = async (sessionId) => {
    try {
      await api.post(`/session-service/sessions/${sessionId}/complete`);
      refreshSessions();
    } catch (err) {
      alert(err.response?.data?.message || err.response?.data || 'Failed to complete session');
    }
  };

  const refreshSessions = () => {
    if (role === 'ROLE_LEARNER') {
      api.get(`/review-service/reviews/learner/${userId}`)
        .then(res => {
          const reviewIds = new Set(res.data.map(r => r.sessionId));
          setReviewedSessionIds(reviewIds);
        }).catch(() => {});
    }
    const endpoint = role === 'ROLE_MENTOR' ? `/session-service/sessions/mentor/${userId}` : `/session-service/sessions/learner/${userId}`;
    api.get(endpoint).then(res => setSessions(res.data || []));
  };

  const handleRejectSession = async (sessionId) => {
    const reason = window.prompt("Please enter a reason for rejecting this session:");
    if (reason === null) return; // User cancelled
    if (!reason.trim()) {
      alert("A reason is required to reject the session.");
      return;
    }
    try {
      await api.post(`/session-service/sessions/${sessionId}/reject`, { reason });
      refreshSessions();
    } catch (err) {
      alert(err.response?.data?.message || err.response?.data || 'Failed to reject session');
    }
  };

  const [reviewModal, setReviewModal] = useState({ isOpen: false, sessionId: null });
  const [reviewData, setReviewData] = useState({ rating: 5, comment: '' });
  const [submittingReview, setSubmittingReview] = useState(false);

  const handleSubmitReview = async () => {
    if (!reviewData.comment.trim()) {
      alert('Please enter a comment for the review.');
      return;
    }
    setSubmittingReview(true);
    try {
      await api.post('/review-service/reviews', {
        sessionId: reviewModal.sessionId,
        rating: reviewData.rating,
        comment: reviewData.comment
      });
      alert('Review submitted successfully!');
      setReviewModal({ isOpen: false, sessionId: null });
      setReviewData({ rating: 5, comment: '' });
      refreshSessions();
    } catch (err) {
      alert(`Debug Error: ${err.message}. Data: ${JSON.stringify(err.response?.data)}`);
    } finally {
      setSubmittingReview(false);
    }
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h2>My Sessions</h2>
          <p className="date-subtitle">Manage all your requested, upcoming, and completed sessions here.</p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <span style={{ fontSize: '0.9rem', color: '#6b7280', fontWeight: 600 }}>Filter by Status:</span>
          <select 
            value={statusFilter} 
            onChange={(e) => setStatusFilter(e.target.value)}
            style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #d1d5db', outline: 'none', backgroundColor: '#fff', cursor: 'pointer' }}
          >
            <option value="ALL">All Sessions</option>
            <option value="REQUESTED">Pending (Requested)</option>
            <option value="ACCEPTED">Accepted</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </div>
      </div>

      <div className="sessions-table-container">
        {sessions.filter(s => statusFilter === 'ALL' || s.status === statusFilter).length === 0 ? (
          <p style={{padding: '1rem'}}>You have no sessions matching the selected filter.</p>
        ) : (
          <table className="sessions-table">
            <thead>
              <tr>
                <th>START DATE</th>
                <th>START TIME</th>
                <th>END TIME</th>
                <th>{role === 'ROLE_MENTOR' ? 'LEARNER' : 'MENTOR'}</th>
                <th>TOPIC</th>
                <th>STATUS</th>
                <th>INFO</th>
                {sessions.some(s => 
                  (role === 'ROLE_MENTOR' && s.status === 'REQUESTED') ||
                  (role === 'ROLE_LEARNER' && s.status === 'COMPLETED')
                ) && <th>ACTIONS</th>}
              </tr>
            </thead>
            <tbody>
              {sessions.filter(s => statusFilter === 'ALL' || s.status === statusFilter).map(session => {
                const startTime = new Date(session.sessionTime);
                const endTime = new Date(startTime.getTime() + session.durationMinutes * 60000);
                return (
                <tr key={session.id}>
                  <td>📅 {startTime.toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' })}</td>
                  <td>{startTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</td>
                  <td>{endTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</td>
                  <td style={{ fontWeight: 'bold' }}>
                    {role === 'ROLE_MENTOR' 
                      ? (userNames[session.learnerId] || `Learner #${session.learnerId}`) 
                      : (userNames[`mentor_${session.mentorId}`] || `Mentor #${session.mentorId}`)}
                  </td>
                  <td>{session.topic || 'General Guidance'}</td>
                  <td>
                    <span className={`status-badge ${session.status.toLowerCase()}`} style={{
                      backgroundColor: session.status === 'ACCEPTED' ? '#dcfce7' : (session.status === 'REQUESTED' ? '#fef3c7' : (session.status === 'COMPLETED' ? '#e0e7ff' : '#fee2e2')),
                      color: session.status === 'ACCEPTED' ? '#166534' : (session.status === 'REQUESTED' ? '#d97706' : (session.status === 'COMPLETED' ? '#4f46e5' : '#991b1b')),
                      padding: '4px 12px', borderRadius: '20px', fontWeight: 600, fontSize: '0.85rem', display: 'inline-block'
                    }}>
                      {session.status === 'REQUESTED' ? 'Pending' : session.status.charAt(0).toUpperCase() + session.status.slice(1).toLowerCase()}
                    </span>
                  </td>
                  <td>
                    {session.status === 'ACCEPTED' && session.meetingLink && role === 'ROLE_LEARNER' && (
                      <a href={session.meetingLink} target="_blank" rel="noreferrer" style={{color: '#2563eb', textDecoration: 'underline', fontSize: '0.85rem'}}>Join Meeting</a>
                    )}
                    {session.status === 'ACCEPTED' && session.meetingLink && role === 'ROLE_MENTOR' && (
                      <span style={{fontSize: '0.85rem', color: '#6b7280'}}>Link Sent</span>
                    )}
                    {session.status === 'REJECTED' && session.rejectionReason && (
                      <span style={{fontSize: '0.85rem', color: '#dc2626'}}>Reason: {session.rejectionReason}</span>
                    )}
                    {(!['ACCEPTED', 'REJECTED'].includes(session.status) || (session.status === 'ACCEPTED' && !session.meetingLink) || (session.status === 'REJECTED' && !session.rejectionReason)) && (
                      <span style={{fontSize: '1.25rem', color: '#9ca3af', fontWeight: 'bold'}}>&mdash;</span>
                    )}
                  </td>
                  {sessions.some(s => 
                    (role === 'ROLE_MENTOR' && s.status === 'REQUESTED') ||
                    (role === 'ROLE_LEARNER' && s.status === 'COMPLETED')
                  ) && (
                    <td>
                      {role === 'ROLE_MENTOR' ? (
                        <>
                          {session.status === 'REQUESTED' ? (
                            <div style={{display: 'flex', gap: '0.5rem'}}>
                              <button 
                                onClick={() => handleAcceptSession(session.id)}
                                style={{backgroundColor: '#dcfce7', color: '#166534', border: 'none', padding: '4px 12px', borderRadius: '4px', cursor: 'pointer', fontWeight: 600}}>
                                Approve
                              </button>
                              <button 
                                onClick={() => handleRejectSession(session.id)}
                                style={{backgroundColor: '#fee2e2', color: '#dc2626', border: 'none', padding: '4px 12px', borderRadius: '4px', cursor: 'pointer', fontWeight: 600}}>
                                Reject
                              </button>
                            </div>
                          ) : (
                            <span style={{ color: '#9ca3af', fontWeight: 'bold', fontSize: '1.25rem' }}>&mdash;</span>
                          )}
                        </>
                      ) : (
                        <>
                          {session.status === 'COMPLETED' ? (
                            reviewedSessionIds.has(session.id) ? (
                              <span style={{ color: '#059669', fontWeight: 600, fontSize: '0.9rem' }}>Reviewed ✓</span>
                            ) : (
                              <button 
                                onClick={() => setReviewModal({ isOpen: true, sessionId: session.id })}
                                style={{backgroundColor: '#fef3c7', color: '#d97706', border: 'none', padding: '4px 12px', borderRadius: '4px', cursor: 'pointer', fontWeight: 600}}>
                                Write Review
                              </button>
                            )
                          ) : (
                            <span style={{ color: '#9ca3af', fontWeight: 'bold', fontSize: '1.25rem' }}>&mdash;</span>
                          )}
                        </>
                      )}
                    </td>
                  )}
                </tr>
              )})}
            </tbody>
          </table>
        )}
      </div>

      {reviewModal.isOpen && (
        <div style={{position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', justifyContent: 'center', alignItems: 'center'}}>
          <div style={{backgroundColor: '#fff', padding: '2rem', borderRadius: '12px', width: '90%', maxWidth: '400px', boxShadow: '0 10px 25px rgba(0,0,0,0.2)'}}>
            <h3 style={{marginTop: 0, marginBottom: '1.5rem'}}>Leave a Review</h3>
            
            <div style={{marginBottom: '1.5rem'}}>
              <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 600}}>Rating (1-5)</label>
              <div style={{display: 'flex', gap: '0.5rem'}}>
                {[1, 2, 3, 4, 5].map(star => (
                  <span 
                    key={star} 
                    onClick={() => setReviewData({...reviewData, rating: star})}
                    style={{fontSize: '2rem', cursor: 'pointer', color: star <= reviewData.rating ? '#fbbf24' : '#e5e7eb'}}
                  >
                    ★
                  </span>
                ))}
              </div>
            </div>

            <div style={{marginBottom: '1.5rem'}}>
              <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 600}}>Comment</label>
              <textarea 
                value={reviewData.comment} 
                onChange={(e) => setReviewData({...reviewData, comment: e.target.value})}
                rows="4" 
                placeholder="How was the session?" 
                style={{width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', boxSizing: 'border-box', fontFamily: 'inherit'}}
              ></textarea>
            </div>

            <div style={{display: 'flex', justifyContent: 'flex-end', gap: '1rem'}}>
              <button 
                onClick={() => setReviewModal({ isOpen: false, sessionId: null })}
                style={{backgroundColor: '#fff', color: '#4b5563', border: '1px solid #d1d5db', padding: '0.5rem 1rem', borderRadius: '6px', fontWeight: 600, cursor: 'pointer'}}
              >
                Cancel
              </button>
              <button 
                onClick={handleSubmitReview}
                disabled={submittingReview}
                style={{backgroundColor: '#e11d48', color: '#fff', border: 'none', padding: '0.5rem 1rem', borderRadius: '6px', fontWeight: 600, cursor: 'pointer'}}
              >
                {submittingReview ? 'Submitting...' : 'Submit Review'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MySessions;
