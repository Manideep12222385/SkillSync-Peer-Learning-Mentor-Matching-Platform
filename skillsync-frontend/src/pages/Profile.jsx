import React, { useState } from 'react';
import api from '../services/api';

const Profile = () => {
  const username = localStorage.getItem('username') || 'User';
  const email = localStorage.getItem('email') || 'user@example.com';
  const role = localStorage.getItem('role') || 'ROLE_LEARNER';
  const userId = localStorage.getItem('userId');

  const [mentorData, setMentorData] = useState({
    bio: '',
    experienceYears: 0,
    hourlyRate: 0.0,
    available: true,
    profileImage: null,
    upiId: '',
    qrCodeImage: null
  });
  const [userData, setUserData] = useState({
    fullName: username,
    headline: '',
    bio: '',
    phone: '',
    profileImage: null
  });
  const [profileImage, setProfileImage] = useState(null);
  
  const [skillId, setSkillId] = useState('');
  const [availableSkills, setAvailableSkills] = useState([]);
  const [existingSkills, setExistingSkills] = useState([]);
  const [loading, setLoading] = useState(false);

  React.useEffect(() => {
    if (role === 'ROLE_MENTOR') {
      api.get('/skill-service/skills?size=100')
        .then(res => setAvailableSkills(res.data?.content || []))
        .catch(err => console.error('Failed to load skills', err));

      api.get('/mentor-service/mentors/profile')
        .then(res => {
          if (res.data) {
            setMentorData({
              bio: res.data.bio || '',
              experienceYears: res.data.experienceYears || 0,
              hourlyRate: res.data.hourlyRate || 0.0,
              available: res.data.available !== undefined ? res.data.available : true,
              profileImage: res.data.profileImage || null,
              upiId: res.data.upiId || '',
              qrCodeImage: res.data.qrCodeImage || null
            });
            setExistingSkills(res.data.mentorSkills || []);
            if (res.data.profileImage) {
              setProfileImage(res.data.profileImage);
            }
          }
        })
        .catch(err => console.error('Profile not created yet or failed to load', err));
    } else {
      // Learner / General User profile
      api.get('/user-service/users/me')
        .then(res => {
          if (res.data) {
            setUserData({
              fullName: res.data.fullName || username,
              headline: res.data.headline || '',
              bio: res.data.bio || '',
              phone: res.data.phone || '',
              profileImage: res.data.profileImage || null
            });
            if (res.data.profileImage) {
              setProfileImage(res.data.profileImage);
            }
          }
        })
        .catch(err => console.error('User profile not created yet', err));
    }
  }, [role, username]);

  const getInitials = (name) => {
    if (!name) return 'U';
    return name.split(' ').map(part => part[0]).join('').toUpperCase().substring(0, 2);
  };

  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 1048576) { // 1MB limit for safety
        alert('Image is too large. Please select an image under 1MB.');
        return;
      }
      const reader = new FileReader();
      reader.onloadend = () => {
        const base64String = reader.result;
        setProfileImage(base64String);
        if (role === 'ROLE_MENTOR') {
          setMentorData(prev => ({ ...prev, profileImage: base64String }));
        } else {
          setUserData(prev => ({ ...prev, profileImage: base64String }));
        }
      };
      reader.readAsDataURL(file);
    }
  };

  const handleQRUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 1048576) { 
        alert('Image is too large. Please select an image under 1MB.');
        return;
      }
      const reader = new FileReader();
      reader.onloadend = () => {
        const base64String = reader.result;
        setMentorData(prev => ({ ...prev, qrCodeImage: base64String }));
      };
      reader.readAsDataURL(file);
    }
  };

  const handleProfileUpdate = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    if (role === 'ROLE_MENTOR') {
      try {
        await api.put('/mentor-service/mentors/profile', mentorData);
        alert('Mentor Profile updated successfully!');
      } catch (err) {
        if (err.response?.status === 404 || err.response?.status === 400) {
          try {
            await api.post('/mentor-service/mentors/profile', mentorData);
            alert('Mentor Profile created successfully!');
          } catch (createErr) {
            alert('Failed to create mentor profile: ' + (createErr.response?.data?.message || 'Unknown error'));
          }
        } else {
          alert('Failed to update mentor profile: ' + (err.response?.data?.message || 'Unknown error'));
        }
      }
    } else {
      // User Profile Update
      try {
        await api.put('/user-service/users/profile', userData);
        alert('User Profile updated successfully!');
      } catch (err) {
        if (err.response?.status === 404 || err.response?.status === 400 || err.response?.status === 500) {
          try {
            await api.post('/user-service/users/profile', userData);
            alert('User Profile created successfully!');
          } catch (createErr) {
            alert('Failed to create user profile: ' + (createErr.response?.data?.message || 'Unknown error'));
          }
        } else {
          alert('Failed to update user profile: ' + (err.response?.data?.message || 'Unknown error'));
        }
      }
    }
    setLoading(false);
  };

  const handleAddSkill = async (e) => {
    e.preventDefault();
    if (!skillId) return;
    
    setLoading(true);
    try {
      await api.post(`/mentor-service/mentors/profile/skills/${skillId}`);
      alert('Skill added successfully!');
      setExistingSkills([...existingSkills, { skillId: parseInt(skillId) }]);
      setSkillId('');
    } catch (err) {
      alert('Failed to add skill: ' + (err.response?.data?.message || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveSkill = async (skillIdToRemove) => {
    setLoading(true);
    try {
      await api.delete(`/mentor-service/mentors/profile/skills/${skillIdToRemove}`);
      alert('Skill removed successfully!');
      setExistingSkills(existingSkills.filter(s => s.skillId !== skillIdToRemove));
    } catch (err) {
      alert('Failed to remove skill: ' + (err.response?.data?.message || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h2>My Profile 👤</h2>
        <p className="date-subtitle">Manage your personal information and settings.</p>
      </div>

      <div style={{display: 'flex', gap: '2rem', marginTop: '2rem'}}>
        {/* Left Column: Avatar & Basic Info */}
        <div style={{flex: '1', backgroundColor: '#fff', padding: '2rem', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)', textAlign: 'center'}}>
          <div style={{position: 'relative', width: '120px', height: '120px', margin: '0 auto 1.5rem auto'}}>
            {profileImage ? (
              <img src={profileImage} alt="Profile" style={{width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover'}} />
            ) : (
              <div style={{
                width: '100%', height: '100%', borderRadius: '50%', backgroundColor: '#4f46e5', 
                color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', 
                fontSize: '3rem', fontWeight: 'bold'
              }}>
                {getInitials(role === 'ROLE_MENTOR' ? username : userData.fullName)}
              </div>
            )}
            
            <label htmlFor="profile-upload" style={{
              position: 'absolute', bottom: '0', right: '0', backgroundColor: '#10b981', color: 'white',
              width: '32px', height: '32px', borderRadius: '50%', display: 'flex', alignItems: 'center',
              justifyContent: 'center', cursor: 'pointer', boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
            }} title="Upload Profile Image">
              📷
            </label>
            <input id="profile-upload" type="file" accept="image/*" style={{display: 'none'}} onChange={handleImageUpload} />
          </div>
          <h3 style={{margin: '0 0 0.5rem 0', fontSize: '1.5rem'}}>{role === 'ROLE_MENTOR' ? username : userData.fullName}</h3>
          <p style={{color: '#6b7280', margin: '0 0 1rem 0'}}>{role.replace('ROLE_', '')}</p>
          <span style={{
            display: 'inline-block', padding: '4px 12px', borderRadius: '20px', 
            backgroundColor: '#dcfce7', color: '#166534', fontSize: '0.85rem', fontWeight: 600
          }}>
            Active Account
          </span>
          
          <div style={{marginTop: '2rem', paddingTop: '2rem', borderTop: '1px solid #f0f0f0', textAlign: 'left'}}>
            <div style={{marginBottom: '1rem'}}>
              <p style={{margin: '0', fontSize: '0.85rem', color: '#6b7280', fontWeight: 600}}>User ID</p>
              <p style={{margin: '0.25rem 0 0 0', fontWeight: 500}}>#{userId}</p>
            </div>
            <div>
              <p style={{margin: '0', fontSize: '0.85rem', color: '#6b7280', fontWeight: 600}}>Member Since</p>
              <p style={{margin: '0.25rem 0 0 0', fontWeight: 500}}>April 2026</p>
            </div>
          </div>
        </div>

        {/* Right Column: Edit Details */}
        <div style={{flex: '2'}}>
          <div style={{backgroundColor: '#fff', padding: '2rem', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)', marginBottom: '2rem'}}>
            <h3 style={{borderBottom: '1px solid #f0f0f0', paddingBottom: '1rem', marginBottom: '1.5rem'}}>Personal Information</h3>
            
            <form onSubmit={handleProfileUpdate}>
              
              <div style={{marginBottom: '1.5rem'}}>
                <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem'}}>Email Address</label>
                <input type="email" defaultValue={email} readOnly style={{width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', backgroundColor: '#f9fafb', boxSizing: 'border-box', color: '#6b7280'}} />
              </div>

              {role !== 'ROLE_MENTOR' && (
                <>
                  <div style={{display: 'flex', gap: '1rem', marginBottom: '1.5rem'}}>
                    <div style={{flex: 1}}>
                      <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem'}}>Full Name</label>
                      <input type="text" value={userData.fullName} onChange={(e) => setUserData({...userData, fullName: e.target.value})} style={{width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', boxSizing: 'border-box'}} required />
                    </div>
                    <div style={{flex: 1}}>
                      <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem'}}>Phone</label>
                      <input type="text" value={userData.phone} onChange={(e) => setUserData({...userData, phone: e.target.value})} style={{width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', boxSizing: 'border-box'}} />
                    </div>
                  </div>
                  
                  <div style={{marginBottom: '1.5rem'}}>
                    <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem'}}>Headline</label>
                    <input type="text" value={userData.headline} onChange={(e) => setUserData({...userData, headline: e.target.value})} placeholder="E.g., Computer Science Student" style={{width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', boxSizing: 'border-box'}} />
                  </div>
                  
                  <div style={{marginBottom: '1.5rem'}}>
                    <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem'}}>Bio</label>
                    <textarea value={userData.bio} onChange={(e) => setUserData({...userData, bio: e.target.value})} rows="3" placeholder="Tell mentors about your goals..." style={{width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', boxSizing: 'border-box', fontFamily: 'inherit'}}></textarea>
                  </div>
                </>
              )}

              {role === 'ROLE_MENTOR' && (
                <>
                  <div style={{display: 'flex', gap: '1rem', marginBottom: '1.5rem'}}>
                    <div style={{flex: 1}}>
                      <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem'}}>Full Name</label>
                      <input type="text" defaultValue={username} readOnly style={{width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', backgroundColor: '#f9fafb', boxSizing: 'border-box'}} />
                    </div>
                  </div>

                  <div style={{marginBottom: '1.5rem'}}>
                    <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem'}}>Bio / Headline</label>
                    <textarea value={mentorData.bio} onChange={(e) => setMentorData({...mentorData, bio: e.target.value})} rows="3" placeholder="Tell students about your expertise..." style={{width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', boxSizing: 'border-box', fontFamily: 'inherit'}} required></textarea>
                  </div>
                  
                  <div style={{display: 'flex', gap: '1rem', marginBottom: '1.5rem'}}>
                    <div style={{flex: 1}}>
                      <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem'}}>Experience (Years)</label>
                      <input type="number" min="0" value={mentorData.experienceYears} onChange={(e) => setMentorData({...mentorData, experienceYears: parseInt(e.target.value)})} style={{width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', boxSizing: 'border-box'}} required />
                    </div>
                    <div style={{flex: 1}}>
                      <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem'}}>Hourly Rate ($)</label>
                      <input type="number" min="0" step="0.1" value={mentorData.hourlyRate} onChange={(e) => setMentorData({...mentorData, hourlyRate: parseFloat(e.target.value)})} style={{width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', boxSizing: 'border-box'}} required />
                    </div>
                  </div>

                  <div style={{marginBottom: '1.5rem'}}>
                    <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem'}}>UPI ID (Mandatory for receiving payments)</label>
                    <input type="text" value={mentorData.upiId} onChange={(e) => setMentorData({...mentorData, upiId: e.target.value})} placeholder="e.g. 9876543210@ybl" style={{width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', boxSizing: 'border-box'}} required />
                  </div>

                  <div style={{marginBottom: '1.5rem'}}>
                    <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem'}}>PhonePe/GooglePay QR Code (Optional)</label>
                    <input type="file" accept="image/*" onChange={handleQRUpload} style={{width: '100%', padding: '0.5rem', borderRadius: '6px', border: '1px solid #d1d5db', boxSizing: 'border-box'}} />
                    {mentorData.qrCodeImage && (
                      <div style={{marginTop: '1rem'}}>
                        <img src={mentorData.qrCodeImage} alt="QR Code" style={{maxWidth: '150px', border: '1px solid #e5e7eb', borderRadius: '8px'}} />
                      </div>
                    )}
                  </div>

                  <div style={{marginBottom: '1.5rem'}}>
                    <label style={{display: 'flex', alignItems: 'center', cursor: 'pointer'}}>
                      <input type="checkbox" checked={mentorData.available} onChange={(e) => setMentorData({...mentorData, available: e.target.checked})} style={{marginRight: '0.5rem'}} />
                      Available for Mentoring
                    </label>
                  </div>
                </>
              )}
              
              <div style={{display: 'flex', justifyContent: 'flex-end', marginTop: '2rem'}}>
                <button type="submit" disabled={loading} style={{backgroundColor: '#4f46e5', color: '#fff', border: 'none', padding: '0.75rem 1.5rem', borderRadius: '6px', fontWeight: 600, cursor: 'pointer', transition: 'background 0.2s'}}>
                  {loading ? 'Saving...' : 'Save Profile Changes'}
                </button>
              </div>
            </form>
          </div>

          {role === 'ROLE_MENTOR' && (
            <div style={{backgroundColor: '#fff', padding: '2rem', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)'}}>
              <h3 style={{borderBottom: '1px solid #f0f0f0', paddingBottom: '1rem', marginBottom: '1.5rem'}}>Manage Skills</h3>
              
              <div style={{marginBottom: '1.5rem'}}>
                <h4 style={{fontSize: '0.9rem', color: '#6b7280', marginBottom: '0.5rem'}}>Your Current Skills</h4>
                {existingSkills.length === 0 ? (
                  <p style={{fontSize: '0.9rem', color: '#9ca3af', fontStyle: 'italic'}}>No skills added yet.</p>
                ) : (
                  <div style={{display: 'flex', flexWrap: 'wrap', gap: '0.5rem'}}>
                    {existingSkills.map(skill => {
                      const skillName = availableSkills.find(s => s.skillId === skill.skillId)?.skillName || `Skill ${skill.skillId}`;
                      return (
                        <span key={skill.id || skill.skillId} style={{backgroundColor: '#f3f4f6', padding: '0.5rem 1rem', borderRadius: '20px', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.5rem'}}>
                          {skillName}
                          <button onClick={() => handleRemoveSkill(skill.skillId)} style={{background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer', fontSize: '1.1rem', padding: '0 0.25rem', display: 'flex', alignItems: 'center'}} title="Remove Skill">&times;</button>
                        </span>
                      );
                    })}
                  </div>
                )}
              </div>

              <form onSubmit={handleAddSkill} style={{display: 'flex', gap: '1rem', alignItems: 'flex-end', marginTop: '1.5rem', paddingTop: '1.5rem', borderTop: '1px dashed #e5e7eb'}}>
                <div style={{flex: 1}}>
                  <label style={{display: 'block', marginBottom: '0.5rem', fontWeight: 500, fontSize: '0.9rem'}}>Add New Skill</label>
                  <select 
                    value={skillId} 
                    onChange={(e) => setSkillId(e.target.value)} 
                    style={{width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #d1d5db', boxSizing: 'border-box'}} 
                    required
                  >
                    <option value="" disabled>-- Select a Skill --</option>
                    {availableSkills.map(skill => (
                      <option key={skill.skillId} value={skill.skillId}>{skill.skillName}</option>
                    ))}
                  </select>
                </div>
                <button type="submit" disabled={loading} style={{backgroundColor: '#10b981', color: '#fff', border: 'none', padding: '0.75rem 1.5rem', borderRadius: '6px', fontWeight: 600, cursor: 'pointer', transition: 'background 0.2s'}}>
                  Add Skill
                </button>
              </form>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Profile;
