import React, { useState } from 'react';
import { StyleSheet, View, Image, Text, TouchableOpacity, Modal, ScrollView } from 'react-native';
import { AuthProvider } from './src/context/AuthContext';
import HomeScreen from './src/screens/HomeScreen';
import LoginScreen from './src/screens/LoginScreen';
import ChatbotScreen from './src/screens/ChatbotScreen';
import EmailListScreen from './src/screens/EmailListScreen';
import AboutScreen from './src/screens/AboutScreen';
import ContactScreen from './src/screens/ContactScreen';

export default function App() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState('home');

  const handleMenuNavigation = (page) => {
    setCurrentPage(page);
    setMenuOpen(false);
  };

  const renderPage = () => {
    switch (currentPage) {
      case 'login':
        return <LoginScreen onLoginSuccess={() => setCurrentPage('home')} />;
      case 'chatbot':
        return <ChatbotScreen />;
      case 'emails':
        return <EmailListScreen />;
      case 'about':
        return <AboutScreen />;
      case 'contact':
        return <ContactScreen />;
      case 'home':
      default:
        return <HomeScreen />;
    }
  };

  return (
    <AuthProvider>
      <View style={styles.container}>
        <Image source={require('./assets/cityscape.png')} style={styles.background} />
      
      {/* Menu Modal */}
      <Modal
        visible={menuOpen}
        transparent={true}
        animationType="fade"
        onRequestClose={() => setMenuOpen(false)}
      >
        <TouchableOpacity 
          style={styles.menuOverlay} 
          activeOpacity={1} 
          onPress={() => setMenuOpen(false)}
        >
          <View style={styles.menuPanel}>
            <TouchableOpacity 
              style={styles.closeButton}
              onPress={() => setMenuOpen(false)}
            >
              <Text style={styles.closeButtonText}>✕</Text>
            </TouchableOpacity>
            <Text style={styles.menuTitle}>Menu</Text>
            <TouchableOpacity 
              style={styles.menuItem}
              onPress={() => handleMenuNavigation('home')}
            >
              <Text style={styles.menuItemText}>Home</Text>
            </TouchableOpacity>
            <TouchableOpacity 
              style={styles.menuItem}
              onPress={() => handleMenuNavigation('login')}
            >
              <Text style={styles.menuItemText}>Login</Text>
            </TouchableOpacity>
            <TouchableOpacity 
              style={styles.menuItem}
              onPress={() => handleMenuNavigation('chatbot')}
            >
              <Text style={styles.menuItemText}>Chatbot</Text>
            </TouchableOpacity>
            <TouchableOpacity 
              style={styles.menuItem}
              onPress={() => handleMenuNavigation('emails')}
            >
              <Text style={styles.menuItemText}>Email Management</Text>
            </TouchableOpacity>
            <TouchableOpacity 
              style={styles.menuItem}
              onPress={() => handleMenuNavigation('about')}
            >
              <Text style={styles.menuItemText}>About</Text>
            </TouchableOpacity>
            <TouchableOpacity 
              style={styles.menuItem}
              onPress={() => handleMenuNavigation('contact')}
            >
              <Text style={styles.menuItemText}>Contact</Text>
            </TouchableOpacity>
          </View>
        </TouchableOpacity>
      </Modal>

      <View style={styles.header}>
        <TouchableOpacity 
          style={styles.hamburger}
          onPress={() => setMenuOpen(true)}
          activeOpacity={0.7}
        >
          <Text style={styles.hamburgerIcon}>☰</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Technet7 AI Services</Text>
      </View>

      <View style={styles.pageContainer}>
        {renderPage()}
      </View>
      </View>
    </AuthProvider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a2332',
    justifyContent: 'center',
    alignItems: 'center',
  },
  background: {
    position: 'absolute',
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
    zIndex: 0,
  },
  header: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    height: 64,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-end',
    paddingHorizontal: 16,
    zIndex: 2,
  },
  hamburger: {
    marginRight: 16,
    padding: 8,
  },
  hamburgerIcon: {
    fontSize: 32,
    color: '#ffd700',
  },
  headerTitle: {
    color: '#ffd700',
    fontSize: 20,
    fontWeight: 'bold',
    marginLeft: 8,
  },
  pageContainer: {
    flex: 1,
    width: '100%',
    zIndex: 1,
    marginTop: 64,
  },
  horizontalScrollContent: {
    paddingRight: 20,
  },
  menuOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    justifyContent: 'flex-start',
  },
  menuPanel: {
    backgroundColor: 'rgba(26, 35, 50, 0.95)',
    width: 200,
    maxHeight: 320,
    paddingTop: 20,
    paddingHorizontal: 12,
    borderRightWidth: 2,
    borderRightColor: '#ffd700',
  },
  closeButton: {
    padding: 4,
    alignSelf: 'flex-end',
  },
  closeButtonText: {
    color: '#ffd700',
    fontSize: 22,
    fontWeight: 'bold',
  },
  menuTitle: {
    color: '#ffd700',
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 16,
    marginTop: 12,
  },
  menuItem: {
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(74, 85, 104, 0.5)',
  },
  menuItemText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '500',
  },
});
