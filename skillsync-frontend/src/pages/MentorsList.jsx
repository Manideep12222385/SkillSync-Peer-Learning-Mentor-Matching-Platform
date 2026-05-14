import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import './MentorsList.css';

const MentorsList = () => {
  const navigate = useNavigate();
  const [mentors, setMentors] = useState([]);
  const [loading, setLoading] = useState(true);

  const [viewMentorModal, setViewMentorModal] = useState({ isOpen: false, mentor: null });
  const [mentorReviews, setMentorReviews] = useState([]);
  const [loadingReviews, setLoadingReviews] = useState(false);
  const [reviewFilter, setReviewFilter] = useState('newest');

  const [activeFilters, setActiveFilters] = useState({
    searchTerm: '',
    selectedSkills: [],
    minRating: null,
    maxPrice: null,
    minExperience: null,
    isAvailable: null
  });

  const [draftFilters, setDraftFilters] = useState({ ...activeFilters });
  const [isFilterModalOpen, setIsFilterModalOpen] = useState(false);
  const [activeFilterCategory, setActiveFilterCategory] = useState('Skill');
  const [allSkills, setAllSkills] = useState([]);

  useEffect(() => {
    fetchMentors();
  }, [activeFilters]);

  useEffect(() => {
    api.get('/skill-service/skills?size=100')
      .then(res => setAllSkills(res.data?.content || []))
      .catch(err => console.error('Failed to load skills', err));
  }, []);

  const fetchMentors = async () => {
    setLoading(true);
    try {
      let query = `/mentor-service/mentors/search?page=0&size=50`;
      if (activeFilters.maxPrice) query += `&maxPrice=${activeFilters.maxPrice}`;
      if (activeFilters.minRating) query += `&rating=${activeFilters.minRating}`;
      if (activeFilters.isAvailable !== null) query += `&available=${activeFilters.isAvailable}`;

      const response = await api.get(query);
      const mentorsList = response.data.content || [];

      // Fetch names for all mentors
      let mentorsWithNames = await Promise.all(mentorsList.map(async (mentor) => {
        try {
          const nameRes = await api.get(`/auth-service/auth/internal/users/${mentor.userId}/name`);
          return { ...mentor, username: nameRes.data };
        } catch (e) {
          return { ...mentor, username: `Mentor #${mentor.userId}` };
        }
      }));

      // Local filtering for name/skills and experience
      if (activeFilters.searchTerm) {
        mentorsWithNames = mentorsWithNames.filter(m =>
          m.username?.toLowerCase().includes(activeFilters.searchTerm.toLowerCase())
        );
      }
      if (activeFilters.minExperience) {
        mentorsWithNames = mentorsWithNames.filter(m => (m.experienceYears || 0) >= activeFilters.minExperience);
      }
      if (activeFilters.selectedSkills.length > 0) {
        // Find skill IDs for selected skill names
        const selectedSkillIds = activeFilters.selectedSkills.map(name => {
          const skill = allSkills.find(s => s.skillName === name);
          return skill ? skill.skillId : null;
        }).filter(id => id !== null);

        if (selectedSkillIds.length > 0) {
          mentorsWithNames = mentorsWithNames.filter(m =>
            selectedSkillIds.every(id => m.mentorSkills && m.mentorSkills.some(ms => ms.skillId === id))
          );
        }
      }

      setMentors(mentorsWithNames);
    } catch (err) {
      console.error('Failed to fetch mentors', err);
    } finally {
      setLoading(false);
    }
  };

  const handleBook = (mentor) => {
    const formattedMentor = {
      id: mentor.mentorId, // Pass mentorId for booking endpoint
      userId: mentor.userId,
      name: mentor.username || `Mentor ${mentor.userId}`,
      hourlyRate: mentor.hourlyRate || 500,
      role: 'Platform Mentor',
      rating: mentor.averageRating || 0,
      initials: mentor.username ? mentor.username.substring(0, 2).toUpperCase() : `M${mentor.userId}`,
      avatarColor: '#e11d48',
      upiId: mentor.upiId || '6281149165@ibl',
      qrCodeImage: mentor.qrCodeImage || null
    };
    navigate('/app/book', { state: { mentor: formattedMentor } });
  };

  const renderStars = (rating) => {
    const percentage = (rating / 5) * 100;
    return (
      <div style={{ position: 'relative', display: 'inline-block', color: '#e5e7eb', letterSpacing: '2px' }}>
        ★★★★★
        <div style={{
          position: 'absolute',
          top: 0,
          left: 0,
          overflow: 'hidden',
          width: `${percentage}%`,
          color: '#fbbf24',
          whiteSpace: 'nowrap'
        }}>
          ★★★★★
        </div>
      </div>
    );
  };

  const skillsByCategory = allSkills.reduce((acc, skill) => {
    const cat = skill.category || 'Other';
    if (!acc[cat]) acc[cat] = [];
    acc[cat].push(skill);
    return acc;
  }, {});

  useEffect(() => {
    if (viewMentorModal.isOpen && viewMentorModal.mentor) {
      setLoadingReviews(true);
      api.get(`/review-service/reviews/mentor/${viewMentorModal.mentor.mentorId}`)
        .then(res => {
          setMentorReviews(res.data || []);
        })
        .catch(err => {
          console.error("Failed to fetch mentor reviews", err);
          setMentorReviews([]);
        })
        .finally(() => {
          setLoadingReviews(false);
        });
    } else {
      setMentorReviews([]);
      setReviewFilter('newest');
    }
  }, [viewMentorModal]);

  const filteredMentorReviews = (() => {
    let result = [...mentorReviews];
    if (reviewFilter === 'newest') {
      result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    } else if (reviewFilter === 'highest') {
      result.sort((a, b) => b.rating - a.rating);
    } else if (reviewFilter === 'lowest') {
      result.sort((a, b) => a.rating - b.rating);
    } else if (['5', '4', '3', '2', '1'].includes(reviewFilter)) {
      result = result.filter(r => r.rating === parseInt(reviewFilter));
      result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    }
    return result;
  })();

  return (
    <div className="mentors-page-container">
      <div className="mentors-header">
        <h2>Find a Mentor</h2>
        <p className="subtitle">Discover expert mentors matched to your learning goals</p>
      </div>

      <div className="search-filter-section" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '0.5rem 1rem', width: '100%', boxSizing: 'border-box' }}>
          <span style={{ marginRight: '0.5rem' }}>🔍</span>
          <input
            type="text"
            placeholder="Search skills, name..."
            value={activeFilters.searchTerm}
            onChange={(e) => setActiveFilters({ ...activeFilters, searchTerm: e.target.value })}
            style={{ border: 'none', outline: 'none', flex: 1, padding: '0.5rem' }}
          />
          <button onClick={() => { setDraftFilters(activeFilters); setIsFilterModalOpen(true); }} style={{ backgroundColor: '#e11d48', color: 'white', border: 'none', borderRadius: '20px', padding: '0.5rem 1.5rem', cursor: 'pointer', fontWeight: 600 }}>🔧 Filters</button>
        </div>

        {/* Dynamic Filter Chips */}
        {(activeFilters.selectedSkills.length > 0 || activeFilters.minRating || activeFilters.maxPrice || activeFilters.minExperience || activeFilters.isAvailable !== null) && (
          <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', alignItems: 'center' }}>
            {activeFilters.selectedSkills.map(skill => (
              <span key={skill} onClick={() => setActiveFilters({ ...activeFilters, selectedSkills: activeFilters.selectedSkills.filter(s => s !== skill) })} style={{ border: '1px solid #e11d48', color: '#e11d48', backgroundColor: '#fff', padding: '4px 12px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '500', cursor: 'pointer' }}>
                • {skill} ✕
              </span>
            ))}
            {activeFilters.minRating && (
              <span onClick={() => setActiveFilters({ ...activeFilters, minRating: null })} style={{ border: '1px solid #e11d48', color: '#e11d48', backgroundColor: '#fff', padding: '4px 12px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '500', cursor: 'pointer' }}>
                ★ {activeFilters.minRating}+ ✕
              </span>
            )}
            {activeFilters.maxPrice && (
              <span onClick={() => setActiveFilters({ ...activeFilters, maxPrice: null })} style={{ border: '1px solid #e11d48', color: '#e11d48', backgroundColor: '#fff', padding: '4px 12px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '500', cursor: 'pointer' }}>
                ₹ &lt;{activeFilters.maxPrice}/hr ✕
              </span>
            )}
            {activeFilters.minExperience && (
              <span onClick={() => setActiveFilters({ ...activeFilters, minExperience: null })} style={{ border: '1px solid #e11d48', color: '#e11d48', backgroundColor: '#fff', padding: '4px 12px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '500', cursor: 'pointer' }}>
                {activeFilters.minExperience}+ yrs exp ✕
              </span>
            )}
            {activeFilters.isAvailable && (
              <span onClick={() => setActiveFilters({ ...activeFilters, isAvailable: null })} style={{ border: '1px solid #e11d48', color: '#e11d48', backgroundColor: '#fff', padding: '4px 12px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '500', cursor: 'pointer' }}>
                Available Now ✕
              </span>
            )}

            <button onClick={() => setActiveFilters({ searchTerm: activeFilters.searchTerm, selectedSkills: [], minRating: null, maxPrice: null, minExperience: null, isAvailable: null })} style={{ border: '1px solid #e11d48', color: '#e11d48', backgroundColor: '#fff', padding: '6px 16px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '500', cursor: 'pointer', marginLeft: 'auto' }}>Clear All</button>
          </div>
        )}

        {/* Filter Modal */}
        {isFilterModalOpen && (
          <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
            <div style={{ backgroundColor: '#fff', width: '90%', maxWidth: '700px', height: '70vh', borderRadius: '12px', display: 'flex', flexDirection: 'column', overflow: 'hidden', boxShadow: '0 10px 25px rgba(0,0,0,0.2)' }}>

              <div style={{ padding: '1rem', borderBottom: '1px solid #e5e7eb', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3 style={{ margin: 0, fontSize: '1.2rem', color: '#111827' }}>Filters ({mentors.length} mentors)</h3>
                <button onClick={() => setIsFilterModalOpen(false)} style={{ border: 'none', background: 'none', fontSize: '1.5rem', cursor: 'pointer', color: '#4b5563' }}>✕</button>
              </div>

              <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
                <div style={{ width: '35%', backgroundColor: '#f9fafb', borderRight: '1px solid #e5e7eb', overflowY: 'auto' }}>
                  {['Skill', 'Price', 'Rating', 'Experience', 'Availability'].map(cat => (
                    <div
                      key={cat}
                      onClick={() => setActiveFilterCategory(cat)}
                      style={{
                        padding: '1.2rem 1rem',
                        cursor: 'pointer',
                        backgroundColor: activeFilterCategory === cat ? '#fff' : 'transparent',
                        fontWeight: activeFilterCategory === cat ? '600' : '400',
                        color: activeFilterCategory === cat ? '#111827' : '#4b5563',
                        borderLeft: activeFilterCategory === cat ? '4px solid #111827' : '4px solid transparent',
                        borderBottom: '1px solid #e5e7eb'
                      }}
                    >
                      {cat}
                    </div>
                  ))}
                </div>

                <div style={{ flex: 1, padding: '1.5rem', overflowY: 'auto' }}>
                  {activeFilterCategory === 'Skill' && (
                    <div>
                      {Object.entries(skillsByCategory).map(([category, skills]) => (
                        <div key={category} style={{ marginBottom: '1.5rem' }}>
                          <h4 style={{ margin: '0 0 0.8rem 0', color: '#111827', fontSize: '1rem' }}>{category}</h4>
                          {skills.map(s => (
                            <label key={s.skillId} style={{ display: 'flex', alignItems: 'center', marginBottom: '0.8rem', cursor: 'pointer', color: '#4b5563' }}>
                              <input type="checkbox" checked={draftFilters.selectedSkills.includes(s.skillName)} onChange={(e) => {
                                if (e.target.checked) setDraftFilters({ ...draftFilters, selectedSkills: [...draftFilters.selectedSkills, s.skillName] });
                                else setDraftFilters({ ...draftFilters, selectedSkills: draftFilters.selectedSkills.filter(sk => sk !== s.skillName) });
                              }} style={{ marginRight: '0.8rem', width: '18px', height: '18px' }} /> {s.skillName}
                            </label>
                          ))}
                        </div>
                      ))}
                    </div>
                  )}

                  {activeFilterCategory === 'Rating' && (
                    <div>
                      {[4, 3, 2, 1].map(rating => (
                        <label key={rating} style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem', cursor: 'pointer', color: '#4b5563' }}>
                          <input type="radio" name="rating" checked={draftFilters.minRating === rating} onChange={() => setDraftFilters({ ...draftFilters, minRating: rating })} style={{ marginRight: '0.8rem', width: '18px', height: '18px' }} />
                          {rating} ★ & Above
                        </label>
                      ))}
                    </div>
                  )}

                  {activeFilterCategory === 'Price' && (
                    <div>
                      {[500, 1000, 1500, 2000, 5000].map(price => (
                        <label key={price} style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem', cursor: 'pointer', color: '#4b5563' }}>
                          <input type="radio" name="price" checked={draftFilters.maxPrice === price} onChange={() => setDraftFilters({ ...draftFilters, maxPrice: price })} style={{ marginRight: '0.8rem', width: '18px', height: '18px' }} />
                          Below Rs.{price}
                        </label>
                      ))}
                    </div>
                  )}

                  {activeFilterCategory === 'Experience' && (
                    <div>
                      {[1, 2, 5, 10].map(exp => (
                        <label key={exp} style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem', cursor: 'pointer', color: '#4b5563' }}>
                          <input type="radio" name="exp" checked={draftFilters.minExperience === exp} onChange={() => setDraftFilters({ ...draftFilters, minExperience: exp })} style={{ marginRight: '0.8rem', width: '18px', height: '18px' }} />
                          {exp} Years & Above
                        </label>
                      ))}
                    </div>
                  )}

                  {activeFilterCategory === 'Availability' && (
                    <div>
                      <label style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem', cursor: 'pointer', color: '#4b5563' }}>
                        <input type="checkbox" checked={draftFilters.isAvailable === true} onChange={(e) => setDraftFilters({ ...draftFilters, isAvailable: e.target.checked ? true : null })} style={{ marginRight: '0.8rem', width: '18px', height: '18px' }} />
                        Include only available mentors
                      </label>
                    </div>
                  )}
                </div>
              </div>

              <div style={{ padding: '1rem', borderTop: '1px solid #e5e7eb', display: 'flex', justifyContent: 'space-between' }}>
                <button onClick={() => setDraftFilters({ searchTerm: activeFilters.searchTerm, selectedSkills: [], minRating: null, maxPrice: null, minExperience: null, isAvailable: null })} style={{ padding: '0.75rem 2rem', backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '6px', fontWeight: 600, cursor: 'pointer', color: '#111827' }}>Reset</button>
                <button onClick={() => { setActiveFilters(draftFilters); setIsFilterModalOpen(false); }} style={{ padding: '0.75rem 2rem', backgroundColor: '#111827', color: '#fff', border: 'none', borderRadius: '6px', fontWeight: 600, cursor: 'pointer' }}>Apply Filter</button>
              </div>
            </div>
          </div>
        )}

        <div style={{ marginTop: '0.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ color: '#6b7280' }}>Showing <strong style={{ color: '#111827' }}>{mentors.length} mentors</strong> matching your criteria</span>
        </div>
      </div>

      <div className="mentors-grid">
        {loading ? (
          <div style={{ padding: '2rem' }}>Loading mentors...</div>
        ) : mentors.length === 0 ? (
          <div style={{ padding: '2rem' }}>No mentors available right now.</div>
        ) : (
          mentors.map(mentor => (
            <div className="mentor-card" key={mentor.mentorId}>
              <div className={`availability-badge ${mentor.available ? 'available' : 'unavailable'}`}>
                {mentor.available ? '● Available' : '● Unavailable'}
              </div>
              <div className="mentor-header">
                <div className="mentor-avatar" style={{ backgroundColor: '#e11d48' }}>
                  {mentor.username ? mentor.username.substring(0, 2).toUpperCase() : `M${mentor.userId}`}
                </div>
                <div className="mentor-info">
                  <h4>{mentor.username || `Mentor User #${mentor.userId}`}</h4>
                  <p>{mentor.experienceYears || 0} yrs experience</p>
                </div>
              </div>
              <div className="mentor-rating" style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                {renderStars(mentor.averageRating || 0)}
                <span className="rating-score" style={{ marginLeft: '0.25rem' }}>{(mentor.averageRating || 0).toFixed(1)}</span>
                <span className="review-count">({mentor.totalSessions || 0} reviews)</span>
              </div>
              <div className="mentor-skills">
                {(mentor.mentorSkills || []).slice(0, 3).map(skill => (
                  <span className="skill-tag" key={skill.id}>{allSkills.find(s => s.skillId === skill.skillId)?.skillName || `Skill ${skill.skillId}`}</span>
                ))}
              </div>
              <div className="mentor-footer">
                <div className="mentor-price" style={{ color: '#e11d48', fontWeight: 'bold' }}>₹{mentor.hourlyRate || 500}/hr</div>
                <div className="mentor-actions" style={{ display: 'flex', gap: '0.5rem' }}>
                  <button
                    onClick={() => setViewMentorModal({ isOpen: true, mentor })}
                    style={{ backgroundColor: '#fff', color: '#e11d48', border: '1px solid #e11d48', padding: '0.5rem 1rem', borderRadius: '6px', fontWeight: 600, cursor: 'pointer' }}>
                    View
                  </button>
                  <button className="book-btn" disabled={!mentor.available} style={{ backgroundColor: '#e11d48', color: '#fff', border: 'none', opacity: mentor.available ? 1 : 0.5, cursor: mentor.available ? 'pointer' : 'not-allowed', padding: '0.5rem 1rem', borderRadius: '6px', fontWeight: 600 }} onClick={() => handleBook(mentor)}>
                    Book
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {viewMentorModal.isOpen && viewMentorModal.mentor && (
        <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
          <div style={{ backgroundColor: '#fff', padding: '2rem', borderRadius: '12px', width: '90%', maxWidth: '500px', boxShadow: '0 10px 25px rgba(0,0,0,0.2)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h3 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: '#e11d48', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.2rem', fontWeight: 'bold' }}>
                  {viewMentorModal.mentor.username ? viewMentorModal.mentor.username.substring(0, 2).toUpperCase() : `M${viewMentorModal.mentor.userId}`}
                </div>
                {viewMentorModal.mentor.username || `Mentor #${viewMentorModal.mentor.userId}`}
              </h3>
              <button onClick={() => setViewMentorModal({ isOpen: false, mentor: null })} style={{ background: 'none', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: '#6b7280' }}>&times;</button>
            </div>

            <div style={{ marginBottom: '1.5rem', color: '#4b5563', lineHeight: '1.6' }}>
              <strong>Bio:</strong> {viewMentorModal.mentor.bio || "This mentor is highly experienced and ready to help you achieve your goals. They specialize in various industry-standard tools and practices."}
            </div>

            <div style={{ display: 'flex', gap: '2rem', marginBottom: '1.5rem' }}>
              <div>
                <strong style={{ display: 'block', marginBottom: '0.25rem', color: '#374151' }}>Experience</strong>
                <span style={{ color: '#6b7280' }}>{viewMentorModal.mentor.experienceYears || 0} Years</span>
              </div>
              <div>
                <strong style={{ display: 'block', marginBottom: '0.25rem', color: '#374151' }}>Hourly Rate</strong>
                <span style={{ color: '#6b7280', fontWeight: 'bold' }}>₹{viewMentorModal.mentor.hourlyRate || 500}/hr</span>
              </div>
              <div>
                <strong style={{ display: 'block', marginBottom: '0.25rem', color: '#374151' }}>Rating</strong>
                <span style={{ color: '#6b7280' }}>⭐ {(viewMentorModal.mentor.averageRating || 0).toFixed(1)} ({viewMentorModal.mentor.totalSessions || 0} reviews)</span>
              </div>
            </div>

            <div style={{ marginBottom: '2rem' }}>
              <strong style={{ display: 'block', marginBottom: '0.5rem', color: '#374151' }}>Skills</strong>
              <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                {(viewMentorModal.mentor.mentorSkills || []).map(skill => (
                  <span key={skill.id} style={{ backgroundColor: '#f3f4f6', padding: '4px 12px', borderRadius: '16px', fontSize: '0.85rem', color: '#4b5563' }}>
                    {allSkills.find(s => s.skillId === skill.skillId)?.skillName || `Skill ${skill.skillId}`}
                  </span>
                ))}
                {(!viewMentorModal.mentor.mentorSkills || viewMentorModal.mentor.mentorSkills.length === 0) && (
                  <span style={{ color: '#9ca3af', fontStyle: 'italic' }}>No specific skills listed</span>
                )}
              </div>
            </div>

            <div style={{ marginBottom: '2rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #e5e7eb', paddingBottom: '0.5rem', marginBottom: '0.75rem' }}>
                <strong style={{ color: '#374151' }}>Learner Reviews</strong>
                {mentorReviews.length > 0 && (
                  <select
                    value={reviewFilter}
                    onChange={(e) => setReviewFilter(e.target.value)}
                    style={{
                      padding: '0.25rem 0.5rem',
                      borderRadius: '6px',
                      border: '1px solid #d1d5db',
                      fontSize: '0.8rem',
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
                )}
              </div>
              <div style={{ maxHeight: '200px', overflowY: 'auto', paddingRight: '0.5rem' }}>
                {loadingReviews ? (
                  <div style={{ color: '#6b7280', fontStyle: 'italic', fontSize: '0.9rem' }}>Loading reviews...</div>
                ) : mentorReviews.length > 0 ? (
                  filteredMentorReviews.length === 0 ? (
                    <div style={{ color: '#6b7280', fontStyle: 'italic', fontSize: '0.9rem' }}>No reviews match the selected filter.</div>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                      {filteredMentorReviews.map(review => (
                        <div key={review.id} style={{ backgroundColor: '#f9fafb', padding: '1rem', borderRadius: '8px', border: '1px solid #f3f4f6' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                            <div style={{ color: '#fbbf24', fontSize: '0.9rem' }}>
                              {[...Array(5)].map((_, i) => <span key={i}>{i < review.rating ? '★' : '☆'}</span>)}
                            </div>
                            <div style={{ color: '#9ca3af', fontSize: '0.8rem' }}>
                              {new Date(review.createdAt).toLocaleDateString()}
                            </div>
                          </div>
                          <p style={{ margin: 0, color: '#4b5563', fontSize: '0.9rem', fontStyle: 'italic' }}>"{review.comment}"</p>
                        </div>
                      ))}
                    </div>
                  )
                ) : (
                  <div style={{ color: '#6b7280', fontStyle: 'italic', fontSize: '0.9rem' }}>No reviews yet for this mentor.</div>
                )}
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', borderTop: '1px solid #e5e7eb', paddingTop: '1.5rem' }}>
              <button
                onClick={() => setViewMentorModal({ isOpen: false, mentor: null })}
                style={{ backgroundColor: '#fff', color: '#4b5563', border: '1px solid #d1d5db', padding: '0.5rem 1rem', borderRadius: '6px', fontWeight: 600, cursor: 'pointer' }}
              >
                Close
              </button>
              <button
                onClick={() => {
                  handleBook(viewMentorModal.mentor);
                  setViewMentorModal({ isOpen: false, mentor: null });
                }}
                disabled={!viewMentorModal.mentor.available}
                style={{ backgroundColor: '#e11d48', color: '#fff', border: 'none', opacity: viewMentorModal.mentor.available ? 1 : 0.5, cursor: viewMentorModal.mentor.available ? 'pointer' : 'not-allowed', padding: '0.5rem 1rem', borderRadius: '6px', fontWeight: 600 }}
              >
                Book Session
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MentorsList;
