import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import api from '../services/api';
import './BookSession.css';

const BookSession = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const mentor = location.state?.mentor || {
    id: 1, // Fallback mentor Profile ID
    userId: 1, // Fallback mentor User ID
    name: 'Priya Sharma',
    hourlyRate: 800,
    role: 'Platform Mentor',
    rating: 4.9,
    initials: 'PS',
    avatarColor: '#e11d48'
  };

  const [selectedDuration, setSelectedDuration] = useState(60);
  const [selectedTime, setSelectedTime] = useState('');
  const [selectedDate, setSelectedDate] = useState(new Date().getDate());
  const [topic, setTopic] = useState('');
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState(1);
  const [existingSessions, setExistingSessions] = useState([]);
  const [timeTab, setTimeTab] = useState('morning'); // morning, afternoon, evening

  // Get current month info
  const currentDate = new Date();
  const currentYear = currentDate.getFullYear();
  const currentMonth = currentDate.getMonth();
  const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
  const todayDate = currentDate.getDate();

  useEffect(() => {
    // Fetch mentor's existing sessions using their userId
    if (mentor.userId) {
      api.get(`/session-service/sessions/mentor/${mentor.userId}`)
        .then(res => {
          // Filter out rejected or cancelled sessions to reopen those slots
          const activeSessions = res.data.filter(s => s.status !== 'REJECTED' && s.status !== 'CANCELLED');
          setExistingSessions(activeSessions);
        })
        .catch(err => console.error('Failed to fetch existing sessions', err));
    }
  }, [mentor.userId]);

  const generateTimeSlots = (startHour, endHour, endMinExcl) => {
    const slots = [];
    for (let h = startHour; h <= endHour; h++) {
      for (let m = 0; m < 60; m += 15) {
        if (h === endHour && m >= endMinExcl) break;

        let displayHour = h;
        let ampm = 'AM';

        if (h >= 12) {
          ampm = 'PM';
          if (h > 12) displayHour = h - 12;
        }
        if (h === 0) displayHour = 12; // Midnight edge case

        const formattedHour = displayHour.toString().padStart(2, '0');
        const formattedMin = m.toString().padStart(2, '0');
        slots.push(`${formattedHour}:${formattedMin} ${ampm}`);
      }
    }
    return slots;
  };

  // Morning: 6:00 AM to 11:45 AM
  const morningSlots = generateTimeSlots(6, 11, 60);
  // Afternoon: 12:00 PM to 5:45 PM
  const afternoonSlots = generateTimeSlots(12, 17, 60);
  // Evening: 6:00 PM to 11:45 PM
  const eveningSlots = generateTimeSlots(18, 23, 60);

  const getSlotsForTab = () => {
    if (timeTab === 'morning') return morningSlots;
    if (timeTab === 'afternoon') return afternoonSlots;
    return eveningSlots;
  };

  const parseTimeToDate = (timeStr, day) => {
    const [time, period] = timeStr.split(' ');
    let [hours, minutes] = time.split(':').map(Number);
    if (period === 'PM' && hours !== 12) hours += 12;
    if (period === 'AM' && hours === 12) hours = 0;

    // Create Date object in local time
    return new Date(currentYear, currentMonth, day, hours, minutes, 0);
  };

  const isSlotAvailable = (timeStr) => {
    const reqStart = parseTimeToDate(timeStr, selectedDate);
    const reqEnd = new Date(reqStart.getTime() + selectedDuration * 60000);

    // Block slots less than 30 mins from current time
    const nowWithBuffer = new Date(new Date().getTime() + 30 * 60000);
    if (reqStart < nowWithBuffer) {
      return false;
    }

    for (const session of existingSessions) {
      const sessionStart = new Date(session.sessionTime);
      const sessionEnd = new Date(sessionStart.getTime() + session.durationMinutes * 60000);

      // Add a 15-minute buffer between sessions to prevent consecutive bookings
      const sessionEndWithBuffer = new Date(sessionEnd.getTime() + 15 * 60000);
      const sessionStartWithBuffer = new Date(sessionStart.getTime() - 15 * 60000);

      if (reqStart < sessionEndWithBuffer && reqEnd > sessionStartWithBuffer) {
        return false; // Overlaps or doesn't have a 15min gap
      }
    }
    return true; // Available
  };

  const calculateTotal = () => {
    return (mentor.hourlyRate * (selectedDuration / 60)).toFixed(0);
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

  const handleContinueToPayment = () => {
    if (!selectedTime) {
      alert('Please select a time slot.');
      return;
    }
    if (!topic || !topic.trim()) {
      alert('Please enter a session topic.');
      return;
    }
    if (!isSlotAvailable(selectedTime)) {
      alert('The selected time and duration overlaps with an existing booked session. Please choose another slot.');
      return;
    }
    setStep(2);
  };

  const handleBooking = async () => {
    setLoading(true);
    try {
      const selectedDateTime = parseTimeToDate(selectedTime, selectedDate);

      const formattedMonth = (selectedDateTime.getMonth() + 1).toString().padStart(2, '0');
      const formattedDate = selectedDateTime.getDate().toString().padStart(2, '0');
      const formattedHours = selectedDateTime.getHours().toString().padStart(2, '0');
      const formattedMins = selectedDateTime.getMinutes().toString().padStart(2, '0');

      const sessionTime = `${selectedDateTime.getFullYear()}-${formattedMonth}-${formattedDate}T${formattedHours}:${formattedMins}:00`;

      const payload = {
        mentorId: mentor.id,
        sessionTime: sessionTime,
        durationMinutes: selectedDuration,
        topic: topic
      };

      await api.post('/session-service/sessions/requestDirect', payload);
      alert('Session requested successfully! Waiting for mentor approval.');
      navigate('/app/dashboard');
    } catch (err) {
      console.error('Booking failed. Full error:', err.response?.data || err);
      let errorMsg = 'Failed to request session. Mentor might already be booked.';
      if (err.response?.data) {
        if (typeof err.response.data === 'string') {
          errorMsg = err.response.data;
        } else if (err.response.data.message) {
          errorMsg = err.response.data.message;
        } else if (err.response.data.error) {
          errorMsg = `Server Error: ${err.response.data.error}`;
        }
      }
      alert(`Error: ${errorMsg}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="book-session-container">
      <div className="back-nav">
        <button className="back-btn" onClick={() => navigate('/app/mentors')}>← Back to Mentors</button>
      </div>

      <div className="book-header">
        <h2>Book a Session</h2>
        <p>Schedule a 1-on-1 session with {mentor.name}</p>
      </div>

      <div className="wizard-progress">
        <div className={`wizard-step ${step > 1 ? 'completed' : 'active'}`}>
          <div className="step-circle">{step > 1 ? '✓' : '1'}</div>
          <span style={{ color: step === 1 ? '#e11d48' : '#111827' }}>Choose Slot</span>
        </div>
        <div className={`wizard-line ${step > 1 ? 'active' : ''}`}></div>
        <div className={`wizard-step ${step === 2 ? 'active' : 'pending'}`}>
          <div className="step-circle">2</div>
          <span style={{ color: step === 2 ? '#e11d48' : '#9ca3af' }}>Confirm & Pay</span>
        </div>
      </div>

      <div className="book-content">
        <div className="book-left">
          {step === 1 && (
            <div className="selection-card">
            <h3>📅 Select Date & Time Slot</h3>

            <div className="calendar-mock">
              <div className="calendar-header">
                <h4>{currentDate.toLocaleString('default', { month: 'long' })} {currentYear}</h4>
              </div>
              <div className="calendar-grid">
                <div className="day-name">Su</div><div className="day-name">Mo</div><div className="day-name">Tu</div>
                <div className="day-name">We</div><div className="day-name">Th</div><div className="day-name">Fr</div>
                <div className="day-name">Sa</div>

                {Array.from({ length: daysInMonth }, (_, i) => i + 1).map(day => (
                  <div
                    key={day}
                    className={`day ${day < todayDate ? 'disabled' : ''} ${selectedDate === day ? 'active' : ''}`}
                    onClick={() => {
                      if (day >= todayDate) {
                        setSelectedDate(day);
                        setSelectedTime(''); // Reset time when date changes
                      }
                    }}
                    style={{ cursor: day >= todayDate ? 'pointer' : 'default' }}
                  >
                    {day}
                  </div>
                ))}
              </div>
            </div>

            <div className="duration-section">
              <h4>Session Duration</h4>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginTop: '0.5rem' }}>
                <button
                  onClick={() => setSelectedDuration(Math.max(15, selectedDuration - 15))}
                  style={{ width: '40px', height: '40px', borderRadius: '50%', border: '1px solid #e11d48', backgroundColor: 'white', color: '#e11d48', fontSize: '1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', outline: 'none' }}
                >
                  -
                </button>
                <div style={{ fontSize: '1.2rem', fontWeight: 'bold', width: '80px', textAlign: 'center' }}>
                  {selectedDuration} min
                </div>
                <button
                  onClick={() => setSelectedDuration(Math.min(240, selectedDuration + 15))}
                  style={{ width: '40px', height: '40px', borderRadius: '50%', border: '1px solid #e11d48', backgroundColor: '#e11d48', color: 'white', fontSize: '1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', outline: 'none' }}
                >
                  +
                </button>
              </div>
            </div>

            <div className="slots-section">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h4 style={{ margin: 0 }}>Available slots on {currentDate.toLocaleString('default', { month: 'short' })} {selectedDate}</h4>
              </div>

              {/* Time Group Tabs */}
              <div style={{ display: 'flex', backgroundColor: '#f3f4f6', padding: '4px', borderRadius: '8px', marginBottom: '1.5rem' }}>
                <button
                  onClick={() => setTimeTab('morning')}
                  style={{ flex: 1, padding: '8px', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 600, fontSize: '0.9rem', backgroundColor: timeTab === 'morning' ? '#fff' : 'transparent', color: timeTab === 'morning' ? '#e11d48' : '#4b5563', boxShadow: timeTab === 'morning' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none' }}
                >
                  🌅 Morning
                </button>
                <button
                  onClick={() => setTimeTab('afternoon')}
                  style={{ flex: 1, padding: '8px', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 600, fontSize: '0.9rem', backgroundColor: timeTab === 'afternoon' ? '#fff' : 'transparent', color: timeTab === 'afternoon' ? '#e11d48' : '#4b5563', boxShadow: timeTab === 'afternoon' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none' }}
                >
                  ☀️ Afternoon
                </button>
                <button
                  onClick={() => setTimeTab('evening')}
                  style={{ flex: 1, padding: '8px', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 600, fontSize: '0.9rem', backgroundColor: timeTab === 'evening' ? '#fff' : 'transparent', color: timeTab === 'evening' ? '#e11d48' : '#4b5563', boxShadow: timeTab === 'evening' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none' }}
                >
                  🌙 Evening
                </button>
              </div>

              <div className="slots-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(100px, 1fr))', gap: '10px' }}>
                {getSlotsForTab().map(time => {
                  const available = isSlotAvailable(time);
                  return (
                    <button
                      key={time}
                      className={`slot-btn ${selectedTime === time ? 'active' : ''} ${!available ? 'disabled' : ''}`}
                      onClick={() => available && setSelectedTime(time)}
                      disabled={!available}
                      style={{
                        padding: '10px',
                        borderRadius: '8px',
                        border: selectedTime === time ? '2px solid #e11d48' : '1px solid #e5e7eb',
                        backgroundColor: selectedTime === time ? '#fff1f2' : (available ? '#fff' : '#f9fafb'),
                        color: selectedTime === time ? '#e11d48' : (available ? '#374151' : '#9ca3af'),
                        fontWeight: selectedTime === time ? 'bold' : 'normal',
                        opacity: available ? 1 : 0.5,
                        cursor: available ? 'pointer' : 'not-allowed',
                        textDecoration: !available ? 'line-through' : 'none',
                        transition: 'all 0.2s'
                      }}
                    >
                      {time}
                    </button>
                  );
                })}
              </div>
            </div>

            <div className="topic-section" style={{ marginTop: '2rem' }}>
              <h4>Session Topic <span style={{ color: '#e11d48' }}>*</span></h4>
              <input
                type="text"
                className="topic-input"
                placeholder="What do you want to learn? (Required)"
                value={topic}
                onChange={(e) => setTopic(e.target.value)}
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '6px',
                  border: '1px solid #d1d5db',
                  outline: 'none',
                  fontSize: '1rem',
                  boxSizing: 'border-box'
                }}
              />
              {!topic.trim() && (
                <div style={{ color: '#6b7280', fontSize: '0.85rem', marginTop: '0.5rem' }}>
                  A session topic is required to proceed with booking.
                </div>
              )}
            </div>

            <div className="action-buttons">
              <button className="outline-btn" onClick={() => navigate('/app/mentors')}>Cancel</button>
              <button 
                className="primary-btn" 
                onClick={handleContinueToPayment} 
                disabled={!selectedTime || !isSlotAvailable(selectedTime) || !topic.trim()}
                style={{
                  opacity: (!selectedTime || !isSlotAvailable(selectedTime) || !topic.trim()) ? 0.5 : 1,
                  cursor: (!selectedTime || !isSlotAvailable(selectedTime) || !topic.trim()) ? 'not-allowed' : 'pointer',
                  backgroundColor: '#e11d48',
                  color: '#fff',
                  border: 'none',
                  padding: '0.75rem 1.5rem',
                  borderRadius: '6px',
                  fontWeight: 'bold'
                }}
              >
                Continue to Payment
              </button>
              {selectedTime && !isSlotAvailable(selectedTime) && (
                <div style={{ color: '#e11d48', fontSize: '0.85rem', marginTop: '0.5rem', textAlign: 'center' }}>
                  Please select a valid future time slot that does not overlap with existing bookings.
                </div>
              )}
            </div>
          </div>
          )}

          {step === 2 && (
            <div className="payment-card" style={{ backgroundColor: '#fff', padding: '2rem', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
              <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', margin: '0 0 1rem 0' }}>
                <span style={{ color: '#2563eb' }}>💳</span> Pay via UPI
              </h3>
              <p style={{ color: '#6b7280', fontSize: '0.9rem', marginBottom: '1.5rem' }}>Scan the QR with any UPI app — GPay, PhonePe, Paytm, etc.</p>
              
              <div style={{ border: '2px dashed #e5e7eb', borderRadius: '12px', padding: '2rem', display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '1.5rem' }}>
                {mentor.qrCodeImage ? (
                  <img 
                    src={mentor.qrCodeImage} 
                    alt="Mentor Custom QR Code" 
                    style={{ width: '200px', height: '200px', marginBottom: '1rem', objectFit: 'contain' }}
                  />
                ) : (
                  <img 
                    src={`https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=upi://pay?pa=${mentor.upiId}&pn=${encodeURIComponent(mentor.name)}&am=${calculateTotal()}&cu=INR`} 
                    alt="Auto-generated QR Code" 
                    style={{ width: '200px', height: '200px', marginBottom: '1rem', objectFit: 'contain' }}
                  />
                )}
                <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#c2410c' }}>₹{calculateTotal()}</div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', backgroundColor: '#f9fafb', border: '1px solid #e5e7eb', borderRadius: '6px', overflow: 'hidden', marginBottom: '1.5rem' }}>
                <div style={{ padding: '0.75rem 1rem', color: '#6b7280', fontSize: '0.9rem', borderRight: '1px solid #e5e7eb' }}>UPI ID</div>
                <div style={{ padding: '0.75rem 1rem', flex: 1, fontWeight: 'bold', color: '#111827', fontSize: '0.95rem' }}>{mentor.upiId}</div>
              </div>

              <p style={{ color: '#6b7280', fontSize: '0.85rem', textAlign: 'center', marginBottom: '1.5rem' }}>After payment, click the button below to confirm your booking.</p>

              <button 
                onClick={handleBooking} 
                disabled={loading}
                style={{
                  width: '100%',
                  padding: '1rem',
                  backgroundColor: '#c2410c',
                  color: '#fff',
                  border: 'none',
                  borderRadius: '6px',
                  fontWeight: 'bold',
                  fontSize: '1rem',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  opacity: loading ? 0.7 : 1,
                  marginBottom: '1rem'
                }}
              >
                {loading ? 'Confirming...' : "I've Paid – Confirm Booking"}
              </button>

              <button 
                onClick={() => setStep(1)} 
                disabled={loading}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  backgroundColor: 'transparent',
                  color: '#6b7280',
                  border: '1px solid #e5e7eb',
                  borderRadius: '6px',
                  fontWeight: '600',
                  cursor: loading ? 'not-allowed' : 'pointer'
                }}
              >
                ← Back to Slot Selection
              </button>
            </div>
          )}
        </div>

        <div className="book-right">
          <div className="summary-card">
            <h3>Session Summary</h3>

            <div className="mentor-summary">
              <div className="mentor-avatar" style={{ backgroundColor: mentor.avatarColor }}>{mentor.initials}</div>
              <div className="mentor-info">
                <h4>{mentor.name}</h4>
                <p>{mentor.role}</p>
                <div className="mentor-rating" style={{ marginBottom: 0, display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                  {renderStars(mentor.rating || 0)}
                  <span className="rating-score" style={{ marginLeft: '0.25rem' }}>{(mentor.rating || 0).toFixed(1)}</span>
                </div>
              </div>
            </div>

            <div className="summary-details">
              <div className="summary-row">
                <span className="label">Date</span>
                <span className="value">{currentDate.toLocaleString('default', { month: 'long' })} {selectedDate}, {currentYear}</span>
              </div>
              <div className="summary-row">
                <span className="label">Start Time</span>
                <span className="value">{selectedTime || 'Not selected'}</span>
              </div>
              <div className="summary-row">
                <span className="label">Duration</span>
                <span className="value">{selectedDuration} minutes</span>
              </div>
              <div className="summary-row">
                <span className="label">Format</span>
                <span className="value">Video Call</span>
              </div>
              <div className="summary-row">
                <span className="label">Rate</span>
                <span className="value">₹{mentor.hourlyRate}/hr</span>
              </div>
              <div className="summary-row total-row">
                <span className="label">Total</span>
                <span className="value total-price">₹{calculateTotal()}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookSession;
