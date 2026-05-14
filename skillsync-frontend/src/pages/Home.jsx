import React from 'react';
import '../App.css'; // We'll keep using App.css for now, or move it to Home.css
import { Link } from 'react-router-dom';

function Home() {
  return (
    <div className="app-container">
      {/* Background Orbs */}
      <div className="bg-orb orb-1"></div>
      <div className="bg-orb orb-2"></div>

      {/* Hero Section */}
      <main className="hero">
        <div className="hero-badge">
          <span role="img" aria-label="sparkles">✨</span>
          The Ultimate Peer Learning Platform
        </div>

        <h1 className="hero-title text-gradient">
          Elevate your skills with <br />
          <span className="text-accent">Peer-to-Peer</span> Mentorship
        </h1>

        <p className="hero-subtitle">
          Connect with industry experts, book 1-on-1 sessions, and accelerate your career growth through personalized learning experiences.
        </p>

        <div className="hero-actions">
          <Link to="/mentors" className="btn-primary btn-large">Find a Mentor</Link>
          <Link to="/register" className="btn-outline btn-large">Become a Mentor</Link>
        </div>
      </main>

      {/* Features Grid */}
      <section id="features" className="features-grid">
        <div className="feature-card">
          <div className="feature-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
          </div>
          <h3 className="feature-title">Smart Matching</h3>
          <p className="feature-desc">Our intelligent algorithm connects you with the perfect mentor based on your skills, goals, and learning style.</p>
        </div>

        <div className="feature-card">
          <div className="feature-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
          </div>
          <h3 className="feature-title">Seamless Scheduling</h3>
          <p className="feature-desc">Easily book sessions that fit your timeline. Real-time availability syncing ensures zero double-bookings.</p>
        </div>

        <div className="feature-card">
          <div className="feature-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
          </div>
          <h3 className="feature-title">Real-time Notifications</h3>
          <p className="feature-desc">Stay updated on your upcoming sessions, messages, and feedback with our reliable notification system.</p>
        </div>
      </section>
    </div>
  );
}

export default Home;
