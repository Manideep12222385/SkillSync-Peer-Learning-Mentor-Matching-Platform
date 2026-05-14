import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import './Login.css';

const ForgotPassword = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSendOtp = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);
    
    try {
      const response = await api.post('/auth-service/auth/forgot-password', {
        email: email
      });
      
      setMessage(typeof response.data === 'string' ? response.data : 'OTP sent to your email.');
      setStep(2); // Move to OTP verification step
    } catch (err) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.data && typeof err.response.data === 'string') {
        setError(err.response.data);
      } else {
        setError('Failed to process request. Please try again later.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (newPassword !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setLoading(true);
    
    try {
      const response = await api.post('/auth-service/auth/reset-password', {
        email: email,
        otp: otp,
        newPassword: newPassword
      });
      
      setMessage(typeof response.data === 'string' ? response.data : 'Password reset successful!');
      
      // Redirect to login after a short delay
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.data && typeof err.response.data === 'string') {
        setError(err.response.data);
      } else {
        setError('Failed to reset password. Please try again later.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-left">
        <div className="login-brand">
          <Link to="/" style={{ textDecoration: 'none', color: 'inherit' }}>
            <h1>Skill<span>Sync</span></h1>
          </Link>
          <p>Peer Learning & Mentor Matching Platform</p>
        </div>
        <div className="login-features">
          <div className="feature-item">
            <span className="icon" style={{backgroundColor: '#ef4444'}}>🎯</span>
            <span>Find expert mentors matched to your goals</span>
          </div>
          <div className="feature-item">
            <span className="icon" style={{backgroundColor: '#3b82f6'}}>📅</span>
            <span>Book 1-on-1 sessions at your convenience</span>
          </div>
          <div className="feature-item">
            <span className="icon" style={{backgroundColor: '#f59e0b'}}>⭐</span>
            <span>Track your growth with ratings & reviews</span>
          </div>
        </div>
      </div>
      <div className="login-right">
        <div className="login-form-container">
          <h2>Reset Password 🔒</h2>
          <p className="subtitle">
            {step === 1 ? 'Enter your email to receive reset instructions' : 'Enter the OTP and your new password'}
          </p>
          
          {error && <div className="error-message" style={{color: '#e11d48', marginBottom: '1rem', padding: '0.5rem', backgroundColor: '#ffe4e6', borderRadius: '4px', fontSize: '0.9rem'}}>{error}</div>}
          {message && <div className="success-message" style={{color: '#166534', marginBottom: '1rem', padding: '0.5rem', backgroundColor: '#dcfce7', borderRadius: '4px', fontSize: '0.9rem'}}>{message}</div>}
          
          {step === 1 ? (
            <form onSubmit={handleSendOtp}>
              <div className="form-group">
                <label>Email Address</label>
                <input type="email" placeholder="rahul.sharma@example.com" value={email} onChange={(e) => setEmail(e.target.value)} required />
              </div>
              
              <button type="submit" className="login-btn" disabled={loading}>
                {loading ? 'SENDING INSTRUCTIONS...' : 'SEND OTP →'}
              </button>
            </form>
          ) : (
            <form onSubmit={handleResetPassword}>
              <div className="form-group">
                <label>Email Address</label>
                <input type="email" value={email} disabled style={{ backgroundColor: '#f3f4f6', color: '#6b7280' }} />
              </div>
              <div className="form-group">
                <label>Enter OTP</label>
                <input type="text" placeholder="123456" value={otp} onChange={(e) => setOtp(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>New Password</label>
                <input type="password" placeholder="••••••••••••" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Confirm Password</label>
                <input type="password" placeholder="••••••••••••" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />
              </div>
              
              <button type="submit" className="login-btn" disabled={loading}>
                {loading ? 'RESETTING PASSWORD...' : 'RESET PASSWORD →'}
              </button>
            </form>
          )}

          <div className="form-actions" style={{justifyContent: 'center', marginTop: '1.5rem'}}>
            <Link to="/login" style={{color: '#6b7280', textDecoration: 'none', fontSize: '0.9rem'}}>← Back to Login</Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
