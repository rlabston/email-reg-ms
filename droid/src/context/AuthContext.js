import React, { createContext, useState, useContext } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(null);
  const [userEmail, setUserEmail] = useState(null);
  const [username, setUsername] = useState(null);

  const saveToken = (newToken, email = null, name = null) => {
    setToken(newToken);
    if (email) {
      setUserEmail(email);
    }
    if (name) {
      setUsername(name);
    }
  };

  const clearToken = () => {
    setToken(null);
    setUserEmail(null);
    setUsername(null);
  };

  const isAuthenticated = () => {
    return token !== null;
  };

  return (
    <AuthContext.Provider value={{ token, userEmail, username, saveToken, clearToken, isAuthenticated }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
