import React, { useEffect, useState } from 'react';
import { StyleSheet, View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { useAuth } from '../context/AuthContext';

export default function HomeScreen() {
  const [homeData, setHomeData] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const { username, userEmail, isAuthenticated } = useAuth();

  // Featured Services
  const featuredServices = [
    { 
      id: 1, 
      title: 'AI & Machine Learning Solutions', 
      description: 'Custom AI models, chatbots with natural language processing, and intelligent automation systems.',
      icon: '🤖'
    },
    { 
      id: 2, 
      title: 'Cloud Infrastructure & DevOps', 
      description: 'Scalable cloud architecture, CI/CD pipelines, and modern deployment strategies for enterprise applications.',
      icon: '☁️'
    },
    { 
      id: 3, 
      title: 'Mobile & Web Development', 
      description: 'Full-stack development with modern frameworks, responsive design, and cross-platform mobile solutions.',
      icon: '📱'
    },
  ];

  // Service Categories
  const categories = {
    'AI & Data Science': [
      { title: 'Machine Learning Models', icon: '🧠', description: 'Custom ML models for classification, regression, and clustering.' },
      { title: 'Natural Language Processing', icon: '💬', description: 'Text analysis, sentiment analysis, and conversational AI.' },
      { title: 'Data Modeling & Analytics', icon: '📊', description: 'Advanced data modeling and predictive analytics.' },
    ],
    'Cloud & Infrastructure': [
      { title: 'AWS Cloud Solutions', icon: '🔶', description: 'Complete AWS infrastructure setup and migration.' },
      { title: 'Azure Solutions', icon: '🔷', description: 'Microsoft Azure cloud services and infrastructure.' },
      { title: 'Kubernetes Orchestration', icon: '⚙️', description: 'Container orchestration and microservices architecture.' },
    ],
    'Web & Mobile Development': [
      { title: 'Web Application Development', icon: '🌐', description: 'Modern responsive web applications.' },
      { title: 'Android Development', icon: '📲', description: 'Native Android applications with Kotlin.' },
      { title: 'iOS Development', icon: '🍎', description: 'Native iOS applications with Swift.' },
    ],
  };

  useEffect(() => {
    // TODO: Load home data from API
    setIsLoading(false);
  }, []);

  if (error) {
    return (
      <View style={styles.errorContainer}>
        <Text style={styles.errorText}>{error}</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      <Text style={styles.welcomeTitle}>Welcome to Technet7 AI Services</Text>
      <Text style={styles.welcomeSubtitle}>Innovative Technology Solutions for Modern Businesses</Text>
      {isAuthenticated() && (username || userEmail) && (
        <Text style={styles.welcomeMessage}>
          Welcome back, {username || userEmail}!
        </Text>
      )}
      
      <Text style={styles.sectionTitle}>Featured Services</Text>
      <ScrollView 
        horizontal 
        showsHorizontalScrollIndicator={false}
        style={styles.horizontalScroll}
        contentContainerStyle={styles.horizontalScrollContent}
      >
        {featuredServices.map(service => (
          <TouchableOpacity key={service.id} style={styles.featuredCard}>
            <Text style={styles.serviceIcon}>{service.icon}</Text>
            <Text style={styles.serviceTitle}>{service.title}</Text>
            <Text style={styles.serviceDescription}>{service.description}</Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      {Object.keys(categories).map(categoryName => (
        <View key={categoryName} style={styles.categorySection}>
          <Text style={styles.sectionTitle}>{categoryName}</Text>
          <ScrollView 
            horizontal 
            showsHorizontalScrollIndicator={false}
            style={styles.horizontalScroll}
            contentContainerStyle={styles.horizontalScrollContent}
          >
            {categories[categoryName].map((service, idx) => (
              <TouchableOpacity key={idx} style={styles.serviceCard}>
                <Text style={styles.serviceIcon}>{service.icon}</Text>
                <Text style={styles.serviceTitle}>{service.title}</Text>
                <Text style={styles.serviceDescription}>{service.description}</Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  contentContainer: {
    padding: 20,
    paddingTop: 0,
    alignItems: 'center',
  },
  welcomeTitle: {
    color: '#ffd700',
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 8,
    textAlign: 'center',
    textShadowColor: '#222c3a',
    textShadowOffset: { width: 1, height: 1 },
    textShadowRadius: 2,
  },
  welcomeSubtitle: {
    color: '#fff',
    fontSize: 16,
    marginBottom: 16,
    textAlign: 'center',
    textShadowColor: '#222c3a',
    textShadowOffset: { width: 1, height: 1 },
    textShadowRadius: 2,
  },
  welcomeMessage: {
    fontSize: 18,
    color: '#ffd700',
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 16,
    paddingVertical: 12,
    paddingHorizontal: 20,
    backgroundColor: 'rgba(255, 215, 0, 0.1)',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#ffd700',
  },
  sectionTitle: {
    color: '#ffd700',
    fontSize: 22,
    fontWeight: 'bold',
    marginTop: 16,
    marginBottom: 16,
    textShadowColor: '#222c3a',
    textShadowOffset: { width: 1, height: 1 },
    textShadowRadius: 2,
  },
  categorySection: {
    width: '100%',
    marginBottom: 24,
  },
  horizontalScroll: {
    width: '100%',
    marginBottom: 16,
  },
  horizontalScrollContent: {
    paddingRight: 20,
  },
  featuredCard: {
    backgroundColor: 'rgba(34, 44, 58, 0.9)',
    borderRadius: 12,
    padding: 20,
    marginRight: 16,
    borderWidth: 2,
    borderColor: '#ffd700',
    width: 280,
    minHeight: 180,
  },
  serviceCard: {
    backgroundColor: 'rgba(34, 44, 58, 0.85)',
    borderRadius: 12,
    padding: 16,
    marginRight: 16,
    borderWidth: 1,
    borderColor: '#4a5568',
    width: 260,
    minHeight: 160,
  },
  serviceIcon: {
    fontSize: 32,
    marginBottom: 8,
  },
  serviceTitle: {
    color: '#ffd700',
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  serviceDescription: {
    color: '#fff',
    fontSize: 14,
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  errorText: {
    color: '#ff6b6b',
    fontSize: 16,
    textAlign: 'center',
  },
});
