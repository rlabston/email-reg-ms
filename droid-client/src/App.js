import React, { useState } from "react";
import "./App.css";
import cityscape from "./cityscape.png";

function App() {
  const [showLogin, setShowLogin] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className="app-bg">
      <header className="app-header">
        <div className="menu-container">
          <button
            className="hamburger"
            onClick={() => setMenuOpen(!menuOpen)}
            aria-label="Open menu"
          >
            &#9776;
          </button>
          {menuOpen && (
            <nav className="menu-dropdown">
              <button onClick={() => setMenuOpen(false)}>Home</button>
              <button onClick={() => setMenuOpen(false)}>Services</button>
              <button onClick={() => { setShowLogin(true); setMenuOpen(false); }}>Login</button>
            </nav>
          )}
        </div>
        <h1>Droid Client</h1>
      </header>
      <main className="main-content">
        <div className="services-card">
          <h2>Welcome to the Services Page</h2>
          <p>This is the minimalist React version of ai-catalog.</p>
        </div>
      </main>
      {showLogin && (
        <div className="modal-overlay" onClick={() => setShowLogin(false)}>
          <div className="login-modal" onClick={e => e.stopPropagation()}>
            <h2>Login</h2>
            <p>Login modal placeholder.</p>
            <button onClick={() => setShowLogin(false)}>Close</button>
          </div>
        </div>
      )}
      <img src={cityscape} alt="Cityscape background" className="cityscape-bg" />
    </div>
  );
}

export default App;
