import React from 'react';
import { Link } from 'react-router-dom';
import '../App.css';

function Navbar() {
  return (
    <nav className="navbar">
      <Link to="/" className="nav-brand">Skill<span>Sync</span></Link>
      <div className="nav-links">
        <a href="#features" className="nav-link">Features</a>
        <Link to="/mentors" className="nav-link">Find Mentors</Link>
        <Link to="/sessions" className="nav-link">Sessions</Link>
      </div>
      <Link to="/login" className="btn-primary">Get Started</Link>
    </nav>
  );
}

export default Navbar;
