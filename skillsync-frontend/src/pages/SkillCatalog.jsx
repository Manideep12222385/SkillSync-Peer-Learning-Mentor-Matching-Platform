import React, { useState, useEffect } from 'react';
import api from '../services/api';

const SkillCatalog = () => {
  const [skills, setSkills] = useState([]);
  const [newSkillName, setNewSkillName] = useState('');
  const [newSkillDesc, setNewSkillDesc] = useState('');
  const [loading, setLoading] = useState(false);

  const fetchSkills = async () => {
    try {
      setLoading(true);
      const response = await api.get('/skill-service/skills?size=100');
      if (response.data && response.data.content) {
        setSkills(response.data.content);
      }
    } catch (err) {
      console.error('Failed to fetch skills', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSkills();
  }, []);

  const handleAddSkill = async (e) => {
    e.preventDefault();
    if (!newSkillName.trim()) return;

    try {
      setLoading(true);
      await api.post('/skill-service/skills', {
        skillName: newSkillName,
        category: newSkillDesc
      });
      setNewSkillName('');
      setNewSkillDesc('');
      fetchSkills();
      alert('Skill added successfully');
    } catch (err) {
      alert('Failed to add skill: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteSkill = async (id) => {
    if (!window.confirm('Are you sure you want to delete this skill?')) return;
    
    try {
      setLoading(true);
      await api.delete(`/skill-service/skills/${id}`);
      fetchSkills();
    } catch (err) {
      alert('Failed to delete skill');
      setLoading(false);
    }
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h2>Skill Catalog 🛠️</h2>
        <p className="date-subtitle">Manage global skills available on the platform.</p>
      </div>

      <div style={{ display: 'flex', gap: '2rem', marginTop: '2rem' }}>
        
        {/* Left Column: Add Skill */}
        <div style={{ flex: '1', backgroundColor: '#fff', padding: '2rem', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)', height: 'fit-content' }}>
          <h3 style={{ borderBottom: '1px solid #f0f0f0', paddingBottom: '1rem', marginBottom: '1.5rem' }}>Add New Skill</h3>
          
          <form onSubmit={handleAddSkill}>
            <div style={{ marginBottom: '1.5rem' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem' }}>Skill Name</label>
              <input 
                type="text" 
                value={newSkillName}
                onChange={(e) => setNewSkillName(e.target.value)}
                placeholder="e.g. React.js"
                style={{ width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', boxSizing: 'border-box' }} 
                required
              />
            </div>

            <div style={{ marginBottom: '1.5rem' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem' }}>Description</label>
              <textarea 
                value={newSkillDesc}
                onChange={(e) => setNewSkillDesc(e.target.value)}
                placeholder="Brief description of the skill..."
                rows="3"
                style={{ width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', boxSizing: 'border-box', fontFamily: 'inherit' }} 
              />
            </div>
            
            <button 
              type="submit" 
              disabled={loading}
              style={{ width: '100%', backgroundColor: '#4f46e5', color: '#fff', border: 'none', padding: '0.75rem 1.5rem', borderRadius: '6px', fontWeight: 600, cursor: 'pointer' }}
            >
              {loading ? 'Adding...' : 'Add Skill'}
            </button>
          </form>
        </div>

        {/* Right Column: Skill List */}
        <div style={{ flex: '2', backgroundColor: '#fff', padding: '2rem', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
          <h3 style={{ borderBottom: '1px solid #f0f0f0', paddingBottom: '1rem', marginBottom: '1.5rem' }}>Platform Skills ({skills.length})</h3>
          
          {loading && skills.length === 0 ? (
            <p>Loading skills...</p>
          ) : skills.length === 0 ? (
            <p style={{ color: '#6b7280' }}>No skills found. Add your first skill to the platform.</p>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '1rem' }}>
              {skills.map(skill => (
                <div key={skill.skillId} style={{ border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <strong style={{ display: 'block', fontSize: '1rem' }}>{skill.skillName}</strong>
                    {skill.category && <span style={{ fontSize: '0.8rem', color: '#6b7280' }}>{skill.category}</span>}
                  </div>
                  <button 
                    onClick={() => handleDeleteSkill(skill.skillId)}
                    style={{ background: '#fee2e2', color: '#dc2626', border: 'none', borderRadius: '4px', width: '30px', height: '30px', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}
                    title="Delete Skill"
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

      </div>
    </div>
  );
};

export default SkillCatalog;
