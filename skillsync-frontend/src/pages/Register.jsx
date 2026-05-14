import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import './Login.css'; // Reusing Login styles for consistency

const Register = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'ROLE_LEARNER'
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    
    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      setLoading(false);
      return;
    }

    try {
      // The API endpoint handles user registration via Gateway
      await api.post('/auth-service/auth/register', formData);
      
      // On success, redirect to login page
      navigate('/login');
    } catch (err) {
      if (!err.response) {
        setError('Cannot connect to the server. Please check if the backend is running and you have restarted the API Gateway.');
      } else if (err.response?.data?.messages && err.response.data.messages.length > 0) {
        setError(err.response.data.messages[0]);
      } else {
        setError(err.response?.data?.message || err.response?.data?.error || 'Registration failed. Please try again.');
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
            <span className="icon" style={{backgroundColor: '#ef4444'}}>🚀</span>
            <span>Accelerate your career with expert guidance</span>
          </div>
          <div className="feature-item">
            <span className="icon" style={{backgroundColor: '#10b981'}}>🤝</span>
            <span>Connect with top industry professionals</span>
          </div>
          <div className="feature-item">
            <span className="icon" style={{backgroundColor: '#8b5cf6'}}>📈</span>
            <span>Achieve your learning goals faster</span>
          </div>
        </div>
      </div>
      <div className="login-right">
        <div className="login-form-container">
          <h2>Create an account ✨</h2>
          <p className="subtitle">Join SkillSync to start learning</p>
          
          <form onSubmit={handleRegister}>
            {error && <div className="error-message" style={{color: '#e11d48', marginBottom: '1rem', padding: '0.5rem', backgroundColor: '#ffe4e6', borderRadius: '4px', fontSize: '0.9rem'}}>{error}</div>}
            
            <div className="form-group">
              <label>Full Name</label>
              <input type="text" name="username" placeholder="Rahul Sharma" value={formData.username} onChange={handleChange} required />
            </div>

            <div className="form-group">
              <label>Email Address</label>
              <input type="email" name="email" placeholder="rahul@example.com" value={formData.email} onChange={handleChange} required />
            </div>
            
            <div className="form-group">
              <label>Password</label>
              <div style={{position: 'relative'}}>
                <input type={showPassword ? "text" : "password"} name="password" placeholder="••••••••••••" value={formData.password} onChange={handleChange} required minLength="8" title="Password must contain uppercase, lowercase, digit and special character" style={{width: '100%', paddingRight: '2.5rem'}} />
                <button type="button" onClick={() => setShowPassword(!showPassword)} style={{position: 'absolute', right: '0.75rem', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: '#6b7280', fontSize: '1.2rem', padding: 0}}>
                  {showPassword ? '👁️' : '👁️‍🗨️'}
                </button>
              </div>
              <small style={{color: '#6b7280', fontSize: '0.75rem'}}>Min 8 chars. Must include uppercase, lowercase, number & special char.</small>
            </div>

            <div className="form-group">
              <label>Confirm Password</label>
              <div style={{position: 'relative'}}>
                <input type={showConfirmPassword ? "text" : "password"} name="confirmPassword" placeholder="••••••••••••" value={formData.confirmPassword} onChange={handleChange} required style={{width: '100%', paddingRight: '2.5rem'}} />
                <button type="button" onClick={() => setShowConfirmPassword(!showConfirmPassword)} style={{position: 'absolute', right: '0.75rem', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: '#6b7280', fontSize: '1.2rem', padding: 0}}>
                  {showConfirmPassword ? '👁️' : '👁️‍🗨️'}
                </button>
              </div>
            </div>

            <div className="form-group">
              <label>I want to join as a:</label>
              <select name="role" value={formData.role} onChange={handleChange} style={{width: '100%', padding: '0.75rem 1rem', border: '1px solid #d1d5db', borderRadius: '8px', fontFamily: 'inherit', fontSize: '1rem', backgroundColor: 'white'}}>
                <option value="ROLE_LEARNER">Student / Learner</option>
                <option value="ROLE_MENTOR">Mentor / Expert</option>
              </select>
            </div>

            <button type="submit" className="login-btn" style={{marginTop: '1rem'}} disabled={loading}>
              {loading ? 'CREATING ACCOUNT...' : 'CREATE ACCOUNT →'}
            </button>
          </form>

          <div className="divider">
            <span>or</span>
          </div>

          <p className="register-link">
            Already have an account? <Link to="/login">Sign in here</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;
