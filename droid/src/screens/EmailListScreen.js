import React, { useEffect, useState } from 'react';
import { StyleSheet, View, Text, TextInput, TouchableOpacity, ScrollView, FlatList, ActivityIndicator } from 'react-native';

export default function EmailListScreen() {
  const [mode, setMode] = useState('list'); // 'list' | 'register'
  const [emails, setEmails] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({ email: '', username: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);

  useEffect(() => {
    // Load emails on mount
    loadEmails();
  }, []);

  const loadEmails = async () => {
    setLoading(true);
    try {
      // TODO: Call API to fetch emails
      // const response = await ApiService.getRegisteredEmails();
      // setEmails(response);
      setError(null);
    } catch (err) {
      setError(err.message || 'Failed to load emails');
    } finally {
      setLoading(false);
    }
  };

  const handleRegisterEmail = async () => {
    if (!formData.email || !formData.username || !formData.password) {
      setError('Please fill in all fields');
      return;
    }

    setLoading(true);
    try {
      // TODO: Call API to register email
      // const response = await ApiService.registerEmail(formData);
      // if (response.success) {
      //   setMode('list');
      //   loadEmails();
      // }
      setError(null);
      setFormData({ email: '', username: '', password: '' });
      setMode('list');
    } catch (err) {
      setError(err.message || 'Failed to register email');
    } finally {
      setLoading(false);
    }
  };

  const EmailListView = () => (
    <>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Registered Emails</Text>
        <TouchableOpacity
          style={styles.addButton}
          onPress={() => setMode('register')}
        >
          <Text style={styles.addButtonText}>+ Add Email</Text>
        </TouchableOpacity>
      </View>

      {loading && <ActivityIndicator size="large" color="#ffd700" style={styles.loader} />}

      <FlatList
        data={emails}
        renderItem={({ item }) => (
          <View style={styles.emailItem}>
            <Text style={styles.emailText}>{item.email}</Text>
            <Text style={styles.usernameText}>{item.username}</Text>
            <TouchableOpacity style={styles.deleteButton}>
              <Text style={styles.deleteButtonText}>Delete</Text>
            </TouchableOpacity>
          </View>
        )}
        keyExtractor={item => item.id}
        contentContainerStyle={styles.emailsList}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyText}>No emails registered yet</Text>
          </View>
        }
      />
    </>
  );

  const RegisterEmailView = () => (
    <ScrollView style={styles.registerContainer} contentContainerStyle={styles.registerContent}>
      <View style={styles.registerBox}>
        <Text style={styles.registerTitle}>Register Email</Text>
        <Text style={styles.registerSubtitle}>Add a new email address</Text>

        <View style={styles.formContainer}>
          <Text style={styles.label}>Email</Text>
          <TextInput
            style={styles.input}
            placeholder="Enter email address"
            placeholderTextColor="#999"
            value={formData.email}
            onChangeText={(text) => setFormData({...formData, email: text})}
            keyboardType="email-address"
            editable={!loading}
          />

          <Text style={styles.label}>Username</Text>
          <TextInput
            style={styles.input}
            placeholder="Enter username"
            placeholderTextColor="#999"
            value={formData.username}
            onChangeText={(text) => setFormData({...formData, username: text})}
            editable={!loading}
          />

          <Text style={styles.label}>Password</Text>
          <View style={styles.passwordContainer}>
            <TextInput
              style={styles.passwordInput}
              placeholder="Enter password"
              placeholderTextColor="#999"
              value={formData.password}
              onChangeText={(text) => setFormData({...formData, password: text})}
              secureTextEntry={!showPassword}
              editable={!loading}
            />
            <TouchableOpacity 
              onPress={() => setShowPassword(!showPassword)}
              style={styles.eyeIcon}
            >
              <Text>{showPassword ? '👁️' : '👁️‍🗨️'}</Text>
            </TouchableOpacity>
          </View>

          {error && <Text style={styles.errorText}>{error}</Text>}

          <TouchableOpacity
            style={[styles.registerButton, loading && styles.registerButtonDisabled]}
            onPress={handleRegisterEmail}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color="#1a2332" />
            ) : (
              <Text style={styles.registerButtonText}>Register</Text>
            )}
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.cancelButton}
            onPress={() => setMode('list')}
            disabled={loading}
          >
            <Text style={styles.cancelButtonText}>Cancel</Text>
          </TouchableOpacity>
        </View>
      </View>
    </ScrollView>
  );

  return (
    <View style={styles.container}>
      {mode === 'list' ? <EmailListView /> : <RegisterEmailView />}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a2332',
  },
  header: {
    backgroundColor: 'rgba(34, 44, 58, 0.9)',
    paddingHorizontal: 16,
    paddingVertical: 16,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderBottomWidth: 1,
    borderBottomColor: '#ffd700',
  },
  headerTitle: {
    color: '#ffd700',
    fontSize: 22,
    fontWeight: 'bold',
  },
  addButton: {
    backgroundColor: '#ffd700',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 6,
  },
  addButtonText: {
    color: '#1a2332',
    fontWeight: 'bold',
  },
  loader: {
    marginVertical: 20,
  },
  emailsList: {
    padding: 16,
  },
  emailItem: {
    backgroundColor: 'rgba(34, 44, 58, 0.9)',
    borderRadius: 8,
    padding: 12,
    marginBottom: 12,
    borderLeftWidth: 3,
    borderLeftColor: '#ffd700',
  },
  emailText: {
    color: '#ffd700',
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 4,
  },
  usernameText: {
    color: '#fff',
    fontSize: 14,
    marginBottom: 8,
  },
  deleteButton: {
    alignSelf: 'flex-start',
    paddingVertical: 4,
    paddingHorizontal: 8,
    backgroundColor: '#ff6b6b',
    borderRadius: 4,
  },
  deleteButtonText: {
    color: '#fff',
    fontSize: 12,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 40,
  },
  emptyText: {
    color: '#999',
    fontSize: 16,
  },
  registerContainer: {
    flex: 1,
  },
  registerContent: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 16,
  },
  registerBox: {
    backgroundColor: 'rgba(34, 44, 58, 0.95)',
    borderRadius: 12,
    padding: 24,
    borderWidth: 2,
    borderColor: '#ffd700',
  },
  registerTitle: {
    color: '#ffd700',
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 8,
    textAlign: 'center',
  },
  registerSubtitle: {
    color: '#fff',
    fontSize: 14,
    marginBottom: 20,
    textAlign: 'center',
  },
  formContainer: {
    gap: 12,
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
  passwordContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderRadius: 8,
  },
  passwordInput: {
    flex: 1,
    paddingHorizontal: 12,
    paddingVertical: 10,
    color: '#000',
    fontSize: 16,
  },
  eyeIcon: {
    paddingRight: 12,
  },
  errorText: {
    color: '#ff6b6b',
    fontSize: 14,
  },
  registerButton: {
    backgroundColor: '#ffd700',
    borderRadius: 8,
    paddingVertical: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 8,
  },
  registerButtonDisabled: {
    opacity: 0.6,
  },
  registerButtonText: {
    color: '#1a2332',
    fontSize: 16,
    fontWeight: 'bold',
  },
  cancelButton: {
    paddingVertical: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 8,
  },
  cancelButtonText: {
    color: '#ffd700',
    fontSize: 16,
    fontWeight: '600',
  },
});
