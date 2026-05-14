import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/auth-service/auth/login', {
        email: email,
        password: password
      });

      // Store token correctly
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('username', response.data.username);
      localStorage.setItem('email', response.data.email);
      localStorage.setItem('role', response.data.role);
      localStorage.setItem('userId', response.data.userId);

      navigate('/app/dashboard');
    } catch (err) {
      if (!err.response) {
        setError('Cannot connect to the server. Please check if the backend is running and you have restarted the API Gateway.');
      } else if (err.response?.data?.messages && err.response.data.messages.length > 0) {
        setError(err.response.data.messages[0]);
      } else {
        setError(err.response?.data?.message || 'Login failed. Please check your credentials.');
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
            <span className="icon" style={{ backgroundColor: '#ef4444' }}>🎯</span>
            <span>Find expert mentors matched to your goals</span>
          </div>
          <div className="feature-item">
            <span className="icon" style={{ backgroundColor: '#3b82f6' }}>📅</span>
            <span>Book 1-on-1 sessions at your convenience</span>
          </div>
          <div className="feature-item">
            <span className="icon" style={{ backgroundColor: '#f59e0b' }}>⭐</span>
            <span>Track your growth with ratings & reviews</span>
          </div>
        </div>
      </div>
      <div className="login-right">
        <div className="login-form-container">
          <h2>Welcome back 👋</h2>
          <p className="subtitle">Sign in to your SkillSync account</p>

          <form onSubmit={handleLogin}>
            {error && <div className="error-message" style={{ color: '#e11d48', marginBottom: '1rem', padding: '0.5rem', backgroundColor: '#ffe4e6', borderRadius: '4px', fontSize: '0.9rem' }}>{error}</div>}
            <div className="form-group">
              <label>Email Address</label>
              <input type="email" placeholder="rahul.sharma@example.com" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <div className="form-group">
              <label>Password</label>
              <div style={{ position: 'relative' }}>
                <input type={showPassword ? "text" : "password"} placeholder="••••••••••••" value={password} onChange={(e) => setPassword(e.target.value)} required style={{ width: '100%', paddingRight: '2.5rem' }} />
                <button type="button" onClick={() => setShowPassword(!showPassword)} style={{ position: 'absolute', right: '0.75rem', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: '#6b7280', fontSize: '1.2rem', padding: 0 }}>
                  {showPassword ? '👁️' : '👁️‍🗨️'}
                </button>
              </div>
            </div>
            <div className="form-actions">
              <label className="checkbox-label">
                <input type="checkbox" defaultChecked /> Remember me
              </label>
              <Link to="/forgot-password" className="forgot-password">Forgot Password?</Link>
            </div>
            <button type="submit" className="login-btn" disabled={loading}>
              {loading ? 'SIGNING IN...' : 'SIGN IN →'}
            </button>
          </form>


          <p className="register-link">
            Don't have an account? <Link to="/register">Create one free</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
