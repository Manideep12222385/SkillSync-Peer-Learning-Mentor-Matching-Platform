import React, { useState, useEffect } from 'react';
import api from '../services/api';
import './Dashboard.css';

const Reviews = () => {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editModal, setEditModal] = useState({ isOpen: false, review: null });
  const [editData, setEditData] = useState({ rating: 5, comment: '' });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [filter, setFilter] = useState('newest');
  const role = localStorage.getItem('role');
  const userId = localStorage.getItem('userId');

  const fetchReviews = async () => {
    try {
      let res;
      if (role === 'ROLE_MENTOR') {
        const mentorRes = await api.get(`/mentor-service/mentors/by-user/${userId}`);
        const mentorProfileId = mentorRes.data;
        res = await api.get(`/review-service/reviews/mentor/${mentorProfileId}`);
        const reviewsWithNames = await Promise.all((res.data || []).map(async (review) => {
          try {
            const nameRes = await api.get(`/auth-service/auth/internal/users/${review.learnerId}/name`);
            return { ...review, learnerName: nameRes.data };
          } catch (err) {
            return { ...review, learnerName: 'Learner' };
          }
        }));
        setReviews(reviewsWithNames);
      } else {
        res = await api.get(`/review-service/reviews/learner/${userId}`);
        const reviewsWithMentorNames = await Promise.all((res.data || []).map(async (review) => {
          try {
            const idRes = await api.get(`/mentor-service/mentors/internal/${review.mentorId}/userid`);
            const nameRes = await api.get(`/auth-service/auth/internal/users/${idRes.data}/name`);
            return { ...review, targetName: nameRes.data };
          } catch (err) {
            return { ...review, targetName: `Mentor #${review.mentorId}` };
          }
        }));
        setReviews(reviewsWithMentorNames);
      }
    } catch (err) {
      console.error('Failed to fetch reviews', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (userId) {
      fetchReviews();
    } else {
      setLoading(false);
    }
  }, [userId, role]);

  const renderStars = (rating, interactive = false, onRatingChange = null) => {
    return (
      <div style={{ display: 'flex', color: '#fbbf24', fontSize: '1.2rem', cursor: interactive ? 'pointer' : 'default' }}>
        {[...Array(5)].map((_, i) => (
          <span key={i} onClick={() => interactive && onRatingChange(i + 1)}>{i < rating ? '★' : '☆'}</span>
        ))}
      </div>
    );
  };

  const handleEditSubmit = async () => {
    if (!editData.comment.trim()) {
      alert('Please enter a comment.');
      return;
    }
    setIsSubmitting(true);
    try {
      await api.put(`/review-service/reviews/${editModal.review.id}`, {
        sessionId: editModal.review.sessionId,
        rating: editData.rating,
        comment: editData.comment
      });
      alert('Review updated successfully!');
      setEditModal({ isOpen: false, review: null });
      fetchReviews();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update review');
    } finally {
      setIsSubmitting(false);
    }
  };

  const filteredReviews = (() => {
    let result = [...reviews];

    if (filter === 'newest') {
      result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    } else if (filter === 'highest') {
      result.sort((a, b) => b.rating - a.rating);
    } else if (filter === 'lowest') {
      result.sort((a, b) => a.rating - b.rating);
    } else if (['5', '4', '3', '2', '1'].includes(filter)) {
      result = result.filter(r => r.rating === parseInt(filter));
      result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    }

    return result;
  })();

  return (
    <div className="dashboard-container">
      <div className="dashboard-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1>{role === 'ROLE_MENTOR' ? 'My Reviews' : 'Reviews Given'}</h1>
          <p>Manage and view your session reviews.</p>
        </div>
        {!loading && reviews.length > 0 && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <label style={{ fontSize: '0.9rem', color: '#4b5563', fontWeight: 500 }}>Filter by:</label>
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              style={{
                padding: '0.5rem',
                borderRadius: '8px',
                border: '1px solid #d1d5db',
                backgroundColor: '#fff',
                fontSize: '0.9rem',
                color: '#111827',
                cursor: 'pointer',
                outline: 'none'
              }}
            >
              <option value="newest">Newest First</option>
              <option value="highest">Highest Rated</option>
              <option value="lowest">Lowest Rated</option>
              <option value="5">5 Stars</option>
              <option value="4">4 Stars</option>
              <option value="3">3 Stars</option>
              <option value="2">2 Stars</option>
              <option value="1">1 Star</option>
            </select>
          </div>
        )}
      </div>

      <div className="dashboard-content">
        {loading ? (
          <div style={{ padding: '2rem', textAlign: 'center' }}>Loading reviews...</div>
        ) : reviews.length === 0 ? (
          <div className="empty-state-container" style={{
            backgroundColor: '#fff',
            borderRadius: '12px',
            padding: '4rem 2rem',
            textAlign: 'center',
            boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05)',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            maxWidth: '600px',
            margin: '0 auto',
            marginTop: '2rem'
          }}>
            <div style={{
              width: '80px',
              height: '80px',
              backgroundColor: '#fef3c7',
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              marginBottom: '1.5rem',
              fontSize: '2.5rem'
            }}>
              ⭐
            </div>
            <h3 style={{ fontSize: '1.5rem', color: '#111827', marginBottom: '0.5rem' }}>No Reviews Yet</h3>
            <p style={{ color: '#6b7280', fontSize: '1.1rem', maxWidth: '400px', lineHeight: '1.5' }}>
              {role === 'ROLE_MENTOR'
                ? "You haven't received any reviews yet. Complete more sessions and ask your learners to leave feedback!"
                : "You haven't written any reviews yet. After completing a session, share your experience to help others."}
            </p>
          </div>
        ) : (
          <div style={{ display: 'grid', gap: '1.5rem', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', marginTop: '1rem' }}>
            {filteredReviews.length === 0 ? (
              <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '3rem', color: '#6b7280' }}>
                No reviews match the selected filter.
              </div>
            ) : (
              filteredReviews.map(review => (
                <div key={review.id} style={{
                  backgroundColor: '#fff',
                  borderRadius: '12px',
                  padding: '1.5rem',
                  boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
                  border: '1px solid #f3f4f6'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                    {renderStars(review.rating)}
                    <span style={{ color: '#9ca3af', fontSize: '0.85rem' }}>
                      {new Date(review.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                  <p style={{ color: '#374151', fontSize: '1rem', lineHeight: '1.5', fontStyle: 'italic' }}>
                    "{review.comment}"
                  </p>
                  <div style={{ marginTop: '1rem', paddingTop: '1rem', borderTop: '1px solid #f3f4f6', fontSize: '0.85rem', color: '#6b7280', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span>
                      Session ID: #{review.sessionId}
                      {role === 'ROLE_MENTOR' && review.learnerName && ` • By: ${review.learnerName}`}
                      {role === 'ROLE_LEARNER' && review.targetName && ` • For: ${review.targetName}`}
                    </span>
                    {role === 'ROLE_LEARNER' && (
                      <button
                        onClick={() => {
                          setEditData({ rating: review.rating, comment: review.comment });
                          setEditModal({ isOpen: true, review });
                        }}
                        style={{ background: 'none', border: 'none', color: '#3b82f6', cursor: 'pointer', fontWeight: 600, padding: 0 }}
                      >
                        Edit Review
                      </button>
                    )}
                  </div>
                </div>
              )))}
          </div>
        )}
      </div>

      {editModal.isOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
          <div style={{ backgroundColor: '#fff', padding: '2rem', borderRadius: '12px', width: '90%', maxWidth: '400px', boxShadow: '0 10px 25px rgba(0,0,0,0.2)' }}>
            <h3 style={{ marginTop: 0, marginBottom: '1.5rem', color: '#111827', fontSize: '1.2rem' }}>Edit Review</h3>

            <div style={{ marginBottom: '1.5rem' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', color: '#374151', fontWeight: 600 }}>Rating (1-5)</label>
              {renderStars(editData.rating, true, (rating) => setEditData({ ...editData, rating }))}
            </div>

            <div style={{ marginBottom: '1.5rem' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', color: '#374151', fontWeight: 600 }}>Comment</label>
              <textarea
                value={editData.comment}
                onChange={(e) => setEditData({ ...editData, comment: e.target.value })}
                style={{ width: '100%', minHeight: '100px', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px', resize: 'vertical', fontFamily: 'inherit', boxSizing: 'border-box' }}
                placeholder="Share your experience..."
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
              <button
                onClick={() => setEditModal({ isOpen: false, review: null })}
                style={{ backgroundColor: '#fff', color: '#4b5563', border: '1px solid #d1d5db', padding: '0.5rem 1rem', borderRadius: '6px', fontWeight: 600, cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                onClick={handleEditSubmit}
                disabled={isSubmitting}
                style={{ backgroundColor: '#e11d48', color: '#fff', border: 'none', opacity: isSubmitting ? 0.7 : 1, padding: '0.5rem 1rem', borderRadius: '6px', fontWeight: 600, cursor: isSubmitting ? 'not-allowed' : 'pointer' }}
              >
                {isSubmitting ? 'Updating...' : 'Update Review'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Reviews;
