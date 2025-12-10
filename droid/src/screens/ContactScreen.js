import React, { useState } from 'react';
import { StyleSheet, View, Text, TextInput, TouchableOpacity, ScrollView, ActivityIndicator } from 'react-native';

export default function ContactScreen() {
  const [formData, setFormData] = useState({ name: '', email: '', message: '' });
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState(null);

  const handleSendMessage = async () => {
    if (!formData.name || !formData.email || !formData.message) {
      setError('Please fill in all fields');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // TODO: Call API to send contact message
      // const response = await ApiService.sendContactMessage(formData);
      // if (response.success) {
      setSent(true);
      setFormData({ name: '', email: '', message: '' });
      setTimeout(() => setSent(false), 3000);
      // }
    } catch (err) {
      setError(err.message || 'Failed to send message');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      <View style={styles.contactBox}>
        <Text style={styles.title}>Contact Us</Text>
        <Text style={styles.subtitle}>We'd love to hear from you</Text>

        {sent && (
          <View style={styles.successMessage}>
            <Text style={styles.successText}>✓ Message sent successfully!</Text>
          </View>
        )}

        <View style={styles.formContainer}>
          <Text style={styles.label}>Name</Text>
          <TextInput
            style={styles.input}
            placeholder="Your name"
            placeholderTextColor="#999"
            value={formData.name}
            onChangeText={(text) => setFormData({...formData, name: text})}
            editable={!loading}
          />

          <Text style={styles.label}>Email</Text>
          <TextInput
            style={styles.input}
            placeholder="Your email"
            placeholderTextColor="#999"
            value={formData.email}
            onChangeText={(text) => setFormData({...formData, email: text})}
            keyboardType="email-address"
            editable={!loading}
          />

          <Text style={styles.label}>Message</Text>
          <TextInput
            style={[styles.input, styles.messageInput]}
            placeholder="Your message"
            placeholderTextColor="#999"
            value={formData.message}
            onChangeText={(text) => setFormData({...formData, message: text})}
            multiline
            editable={!loading}
          />

          {error && <Text style={styles.errorText}>{error}</Text>}

          <TouchableOpacity
            style={[styles.sendButton, loading && styles.sendButtonDisabled]}
            onPress={handleSendMessage}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color="#1a2332" />
            ) : (
              <Text style={styles.sendButtonText}>Send Message</Text>
            )}
          </TouchableOpacity>
        </View>

        <View style={styles.contactInfoContainer}>
          <Text style={styles.contactInfoTitle}>Direct Contact</Text>
          
          <View style={styles.contactInfo}>
            <Text style={styles.contactInfoLabel}>Email:</Text>
            <Text style={styles.contactInfoValue}>info@technet7.com</Text>
          </View>

          <View style={styles.contactInfo}>
            <Text style={styles.contactInfoLabel}>Phone:</Text>
            <Text style={styles.contactInfoValue}>+1 (555) 123-4567</Text>
          </View>

          <View style={styles.contactInfo}>
            <Text style={styles.contactInfoLabel}>Address:</Text>
            <Text style={styles.contactInfoValue}>123 Tech Street{'\n'}San Francisco, CA 94105</Text>
          </View>

          <View style={styles.contactInfo}>
            <Text style={styles.contactInfoLabel}>Hours:</Text>
            <Text style={styles.contactInfoValue}>Mon - Fri: 9:00 AM - 6:00 PM{'\n'}Sat - Sun: Closed</Text>
          </View>
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a2332',
  },
  contentContainer: {
    padding: 20,
    paddingTop: 0,
  },
  contactBox: {
    backgroundColor: 'rgba(34, 44, 58, 0.9)',
    borderRadius: 12,
    padding: 20,
    borderWidth: 1,
    borderColor: '#4a5568',
  },
  title: {
    color: '#ffd700',
    fontSize: 32,
    fontWeight: 'bold',
    marginBottom: 8,
    textAlign: 'center',
  },
  subtitle: {
    color: '#fff',
    fontSize: 16,
    marginBottom: 20,
    textAlign: 'center',
  },
  successMessage: {
    backgroundColor: '#4caf50',
    borderRadius: 8,
    padding: 12,
    marginBottom: 16,
  },
  successText: {
    color: '#fff',
    fontSize: 14,
    textAlign: 'center',
  },
  formContainer: {
    gap: 12,
    marginBottom: 20,
  },
  label: {
    color: '#ffd700',
    fontSize: 14,
    fontWeight: '600',
  },
  input: {
    backgroundColor: '#fff',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    color: '#000',
    fontSize: 16,
  },
  messageInput: {
    minHeight: 100,
    textAlignVertical: 'top',
  },
  errorText: {
    color: '#ff6b6b',
    fontSize: 14,
  },
  sendButton: {
    backgroundColor: '#ffd700',
    borderRadius: 8,
    paddingVertical: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  sendButtonDisabled: {
    opacity: 0.6,
  },
  sendButtonText: {
    color: '#1a2332',
    fontSize: 16,
    fontWeight: 'bold',
  },
  contactInfoContainer: {
    borderTopWidth: 1,
    borderTopColor: '#4a5568',
    paddingTop: 20,
  },
  contactInfoTitle: {
    color: '#ffd700',
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 16,
  },
  contactInfo: {
    marginBottom: 16,
  },
  contactInfoLabel: {
    color: '#ffd700',
    fontSize: 14,
    fontWeight: '600',
    marginBottom: 4,
  },
  contactInfoValue: {
    color: '#fff',
    fontSize: 14,
    lineHeight: 20,
  },
});
