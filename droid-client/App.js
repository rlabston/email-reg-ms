import React, { useState, useEffect } from 'react';
import { View, Text, ImageBackground, TouchableOpacity, Modal, StyleSheet, ActivityIndicator, FlatList } from 'react-native';
import { fetchServices } from './api';

export default function App() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [showLogin, setShowLogin] = useState(false);
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchServices()
      .then(data => { setServices(data); setLoading(false); })
      .catch(err => { setError(err.message); setLoading(false); });
  }, []);

  return (
    <ImageBackground source={require('./assets/cityscape.png')} style={styles.bg}>
      <View style={styles.header}>
        <TouchableOpacity style={styles.hamburger} onPress={() => setMenuOpen(!menuOpen)}>
          <Text style={styles.hamburgerText}>☰</Text>
        </TouchableOpacity>
        <Text style={styles.title}>Droid Client</Text>
        {menuOpen && (
          <View style={styles.menuDropdown}>
            <TouchableOpacity onPress={() => setMenuOpen(false)}><Text style={styles.menuItem}>Home</Text></TouchableOpacity>
            <TouchableOpacity onPress={() => setMenuOpen(false)}><Text style={styles.menuItem}>Services</Text></TouchableOpacity>
            <TouchableOpacity onPress={() => { setShowLogin(true); setMenuOpen(false); }}><Text style={styles.menuItem}>Login</Text></TouchableOpacity>
          </View>
        )}
      </View>
      <View style={styles.mainContent}>
        <View style={styles.servicesCard}>
          <Text style={styles.welcome}>Welcome to the Services Page</Text>
          {loading ? (
            <ActivityIndicator size="large" color="#ffd700" />
          ) : error ? (
            <Text style={{ color: 'red' }}>{error}</Text>
          ) : (
            <FlatList
              data={services}
              keyExtractor={item => item.id?.toString() || item.name}
              renderItem={({ item }) => (
                <View style={styles.serviceItem}>
                  <Text style={styles.serviceName}>{item.name}</Text>
                  <Text style={styles.serviceDesc}>{item.description}</Text>
                </View>
              )}
            />
          )}
        </View>
      </View>
      <Modal visible={showLogin} transparent animationType="fade">
        <View style={styles.modalOverlay}>
          <View style={styles.loginModal}>
            <Text style={styles.loginTitle}>Login</Text>
            <Text>Login modal placeholder.</Text>
            <TouchableOpacity onPress={() => setShowLogin(false)} style={styles.closeBtn}><Text>Close</Text></TouchableOpacity>
          </View>
        </View>
      </Modal>
    </ImageBackground>
  );
}

const styles = StyleSheet.create({
  bg: { flex: 1, resizeMode: 'cover', justifyContent: 'center' },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', padding: 24, backgroundColor: 'rgba(26,35,50,0.7)' },
  hamburger: { padding: 8 },
  hamburgerText: { fontSize: 32, color: '#ffd700' },
  title: { fontSize: 22, color: '#fff', fontWeight: 'bold' },
  menuDropdown: { position: 'absolute', right: 0, top: 48, backgroundColor: '#222c3a', borderRadius: 8, padding: 8, zIndex: 10 },
  menuItem: { color: '#ffd700', padding: 12, fontSize: 16 },
  mainContent: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  servicesCard: { backgroundColor: 'rgba(0,0,0,0.7)', padding: 32, borderRadius: 16, alignItems: 'center', width: '90%' },
  welcome: { color: '#fff', fontSize: 20, fontWeight: 'bold', marginBottom: 8 },
  serviceItem: { marginBottom: 16, backgroundColor: '#222c3a', padding: 12, borderRadius: 8 },
  serviceName: { color: '#ffd700', fontSize: 16, fontWeight: 'bold' },
  serviceDesc: { color: '#fff', fontSize: 14 },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'center', alignItems: 'center' },
  loginModal: { backgroundColor: '#fff', padding: 32, borderRadius: 12, minWidth: 300, alignItems: 'center' },
  loginTitle: { fontSize: 20, fontWeight: 'bold', marginBottom: 12 },
  closeBtn: { marginTop: 16, padding: 8, backgroundColor: '#eee', borderRadius: 8 }
});
