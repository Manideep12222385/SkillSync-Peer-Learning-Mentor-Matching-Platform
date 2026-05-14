import React, { useState } from 'react';

const Settings = () => {
  const [activeTab, setActiveTab] = useState('account');
  const role = localStorage.getItem('role') || 'ROLE_LEARNER';

  // Temporary state for the UI, committed on Save
  const [tempTheme, setTempTheme] = useState(localStorage.getItem('theme') || 'light');
  const [settings, setSettings] = useState({
    twoFactor: localStorage.getItem('setting_2fa') === 'true',
    profileVisibility: localStorage.getItem('setting_visibility') !== 'false', // Default true
    emailNotif: localStorage.getItem('setting_email_notif') !== 'false', // Default true
    pushNotif: localStorage.getItem('setting_push_notif') === 'true',
    sessionReminders: localStorage.getItem('setting_reminders') !== 'false', // Default true
    marketingEmails: localStorage.getItem('setting_marketing') === 'true'
  });

  const handleSettingToggle = (key) => {
    setSettings({ ...settings, [key]: !settings[key] });
  };

  const handleSavePreferences = () => {
    // Apply and Save Theme
    localStorage.setItem('theme', tempTheme);
    if (tempTheme === 'dark') {
      document.body.classList.add('dark-theme');
    } else {
      document.body.classList.remove('dark-theme');
    }

    // Save Settings
    localStorage.setItem('setting_2fa', settings.twoFactor);
    localStorage.setItem('setting_visibility', settings.profileVisibility);
    localStorage.setItem('setting_email_notif', settings.emailNotif);
    localStorage.setItem('setting_push_notif', settings.pushNotif);
    localStorage.setItem('setting_reminders', settings.sessionReminders);
    localStorage.setItem('setting_marketing', settings.marketingEmails);

    alert('Your preferences have been successfully saved!');
  };

  const renderTabContent = () => {
    switch (activeTab) {
      case 'account':
        return (
          <div className="settings-section">
            <h3 style={{borderBottom: '1px solid #f0f0f0', paddingBottom: '1rem', marginBottom: '1.5rem', fontSize: '1.2rem'}}>Account Preferences</h3>
            <div style={{marginBottom: '1.5rem'}}>
              <label style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer', padding: '1rem', backgroundColor: '#f9fafb', borderRadius: '8px', border: '1px solid #e5e7eb'}}>
                <div>
                  <span style={{fontWeight: 600, display: 'block', color: '#111827'}}>Two-Factor Authentication (2FA)</span>
                  <span style={{fontSize: '0.85rem', color: '#6b7280'}}>Add an extra layer of security to your account.</span>
                </div>
                <input type="checkbox" checked={settings.twoFactor} onChange={() => handleSettingToggle('twoFactor')} style={{width: '18px', height: '18px', cursor: 'pointer'}} />
              </label>
            </div>
            <div style={{marginBottom: '1.5rem'}}>
              <label style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer', padding: '1rem', backgroundColor: '#f9fafb', borderRadius: '8px', border: '1px solid #e5e7eb'}}>
                <div>
                  <span style={{fontWeight: 600, display: 'block', color: '#111827'}}>Profile Visibility</span>
                  <span style={{fontSize: '0.85rem', color: '#6b7280'}}>Make your profile visible to other users on the platform.</span>
                </div>
                <input type="checkbox" checked={settings.profileVisibility} onChange={() => handleSettingToggle('profileVisibility')} style={{width: '18px', height: '18px', cursor: 'pointer'}} />
              </label>
            </div>
            {role !== 'ROLE_ADMIN' && (
              <div style={{marginTop: '3rem', paddingTop: '1.5rem', borderTop: '1px solid #fee2e2'}}>
                <h4 style={{color: '#dc2626', margin: '0 0 0.5rem 0'}}>Danger Zone</h4>
                <p style={{fontSize: '0.85rem', color: '#6b7280', marginBottom: '1rem'}}>Once you delete your account, there is no going back. Please be certain.</p>
                <button style={{backgroundColor: '#fff', color: '#dc2626', border: '1px solid #dc2626', padding: '0.5rem 1rem', borderRadius: '6px', fontWeight: 600, cursor: 'pointer'}}>
                  Delete Account
                </button>
              </div>
            )}
          </div>
        );
      case 'notifications':
        return (
          <div className="settings-section">
            <h3 style={{borderBottom: '1px solid #f0f0f0', paddingBottom: '1rem', marginBottom: '1.5rem', fontSize: '1.2rem'}}>Notification Preferences</h3>
            <div style={{display: 'flex', flexDirection: 'column', gap: '1rem'}}>
              <label style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer', padding: '1rem', backgroundColor: '#fff', borderRadius: '8px', border: '1px solid #e5e7eb'}}>
                <div>
                  <span style={{fontWeight: 600, display: 'block', color: '#111827'}}>Email Notifications</span>
                  <span style={{fontSize: '0.85rem', color: '#6b7280'}}>Receive updates and reminders via email.</span>
                </div>
                <input type="checkbox" checked={settings.emailNotif} onChange={() => handleSettingToggle('emailNotif')} style={{width: '18px', height: '18px', cursor: 'pointer', accentColor: '#4f46e5'}} />
              </label>
              
              <label style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer', padding: '1rem', backgroundColor: '#fff', borderRadius: '8px', border: '1px solid #e5e7eb'}}>
                <div>
                  <span style={{fontWeight: 600, display: 'block', color: '#111827'}}>Push Notifications</span>
                  <span style={{fontSize: '0.85rem', color: '#6b7280'}}>Receive alerts directly in your browser.</span>
                </div>
                <input type="checkbox" checked={settings.pushNotif} onChange={() => handleSettingToggle('pushNotif')} style={{width: '18px', height: '18px', cursor: 'pointer', accentColor: '#4f46e5'}} />
              </label>
              
              <label style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer', padding: '1rem', backgroundColor: '#fff', borderRadius: '8px', border: '1px solid #e5e7eb'}}>
                <div>
                  <span style={{fontWeight: 600, display: 'block', color: '#111827'}}>Session Reminders</span>
                  <span style={{fontSize: '0.85rem', color: '#6b7280'}}>Get notified 30 minutes before a session starts.</span>
                </div>
                <input type="checkbox" checked={settings.sessionReminders} onChange={() => handleSettingToggle('sessionReminders')} style={{width: '18px', height: '18px', cursor: 'pointer', accentColor: '#4f46e5'}} />
              </label>

              <label style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer', padding: '1rem', backgroundColor: '#fff', borderRadius: '8px', border: '1px solid #e5e7eb'}}>
                <div>
                  <span style={{fontWeight: 600, display: 'block', color: '#111827'}}>Marketing Emails</span>
                  <span style={{fontSize: '0.85rem', color: '#6b7280'}}>Receive news, tips, and promotional offers.</span>
                </div>
                <input type="checkbox" checked={settings.marketingEmails} onChange={() => handleSettingToggle('marketingEmails')} style={{width: '18px', height: '18px', cursor: 'pointer', accentColor: '#4f46e5'}} />
              </label>
            </div>
          </div>
        );
      case 'appearance':
        return (
          <div className="settings-section">
            <h3 style={{borderBottom: '1px solid #f0f0f0', paddingBottom: '1rem', marginBottom: '1.5rem', fontSize: '1.2rem'}}>Appearance</h3>
            <div style={{marginBottom: '2rem'}}>
              <label style={{display: 'block', fontWeight: 600, marginBottom: '1rem'}}>Theme Preference</label>
              <div style={{display: 'flex', gap: '1rem'}}>
                <div onClick={() => setTempTheme('light')} style={{flex: 1, padding: '1.5rem', border: tempTheme === 'light' ? '2px solid #4f46e5' : '1px solid #e5e7eb', borderRadius: '8px', textAlign: 'center', cursor: 'pointer', backgroundColor: '#f8fafc'}}>
                  <span style={{fontSize: '2rem', display: 'block', marginBottom: '0.5rem'}}>☀️</span>
                  <span style={{fontWeight: 600}}>Light Mode</span>
                </div>
                <div onClick={() => setTempTheme('dark')} style={{flex: 1, padding: '1.5rem', border: tempTheme === 'dark' ? '2px solid #4f46e5' : '1px solid #e5e7eb', borderRadius: '8px', textAlign: 'center', cursor: 'pointer', backgroundColor: '#1f2937', color: '#fff'}}>
                  <span style={{fontSize: '2rem', display: 'block', marginBottom: '0.5rem'}}>🌙</span>
                  <span style={{fontWeight: 500}}>Dark Mode</span>
                </div>
              </div>
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h2>Settings ⚙️</h2>
        <p className="date-subtitle">Customize your platform experience and preferences.</p>
      </div>

      <div style={{display: 'flex', gap: '2rem', marginTop: '1.5rem'}}>
        {/* Settings Sidebar */}
        <div style={{width: '250px', flexShrink: 0}}>
          <div style={{backgroundColor: '#fff', borderRadius: '12px', overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.05)'}}>
            <button 
              onClick={() => setActiveTab('account')}
              style={{
                width: '100%', textAlign: 'left', padding: '1rem 1.5rem', border: 'none', background: activeTab === 'account' ? '#f5f3ff' : '#fff',
                color: activeTab === 'account' ? '#4f46e5' : '#4b5563', borderLeft: activeTab === 'account' ? '4px solid #4f46e5' : '4px solid transparent',
                fontWeight: activeTab === 'account' ? 600 : 500, cursor: 'pointer', fontSize: '0.95rem', transition: 'all 0.2s'
              }}
            >
              🔒 Account Security
            </button>
            <button 
              onClick={() => setActiveTab('notifications')}
              style={{
                width: '100%', textAlign: 'left', padding: '1rem 1.5rem', border: 'none', background: activeTab === 'notifications' ? '#f5f3ff' : '#fff',
                color: activeTab === 'notifications' ? '#4f46e5' : '#4b5563', borderLeft: activeTab === 'notifications' ? '4px solid #4f46e5' : '4px solid transparent',
                fontWeight: activeTab === 'notifications' ? 600 : 500, cursor: 'pointer', fontSize: '0.95rem', transition: 'all 0.2s'
              }}
            >
              📱 Notifications
            </button>
            <button 
              onClick={() => setActiveTab('appearance')}
              style={{
                width: '100%', textAlign: 'left', padding: '1rem 1.5rem', border: 'none', background: activeTab === 'appearance' ? '#f5f3ff' : '#fff',
                color: activeTab === 'appearance' ? '#4f46e5' : '#4b5563', borderLeft: activeTab === 'appearance' ? '4px solid #4f46e5' : '4px solid transparent',
                fontWeight: activeTab === 'appearance' ? 600 : 500, cursor: 'pointer', fontSize: '0.95rem', transition: 'all 0.2s'
              }}
            >
              🎨 Appearance
            </button>
          </div>
        </div>

        {/* Settings Content */}
        <div style={{flex: 1, backgroundColor: '#fff', borderRadius: '12px', padding: '2rem', boxShadow: '0 2px 8px rgba(0,0,0,0.05)'}}>
          {renderTabContent()}
          
          <div style={{display: 'flex', justifyContent: 'flex-end', marginTop: '2rem', paddingTop: '1.5rem', borderTop: '1px solid #f0f0f0'}}>
            <button onClick={handleSavePreferences} style={{backgroundColor: '#4f46e5', color: '#fff', border: 'none', padding: '0.75rem 1.5rem', borderRadius: '6px', fontWeight: 600, cursor: 'pointer'}}>
              Save Preferences
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Settings;
