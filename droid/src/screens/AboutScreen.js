import React from 'react';
import { StyleSheet, View, Text, ScrollView } from 'react-native';

export default function AboutScreen() {
  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      <View style={styles.aboutBox}>
        <Text style={styles.title}>About Technet7</Text>
        
        <Text style={styles.sectionTitle}>Who We Are</Text>
        <Text style={styles.description}>
          Technet7 is a leading technology solutions provider specializing in AI, cloud infrastructure, and custom software development.
        </Text>

        <Text style={styles.sectionTitle}>Our Mission</Text>
        <Text style={styles.description}>
          To empower businesses with innovative technology solutions that drive growth, efficiency, and digital transformation.
        </Text>

        <Text style={styles.sectionTitle}>Our Services</Text>
        <Text style={styles.description}>
          • AI & Machine Learning Solutions{'\n'}
          • Cloud Infrastructure & DevOps{'\n'}
          • Mobile & Web Development{'\n'}
          • Custom Software Development{'\n'}
          • Data Analytics & Modeling
        </Text>

        <Text style={styles.sectionTitle}>Why Choose Us?</Text>
        <Text style={styles.description}>
          • Expert team with 15+ years of experience{'\n'}
          • Proven track record with Fortune 500 companies{'\n'}
          • Cutting-edge technology and best practices{'\n'}
          • 24/7 Support and maintenance
        </Text>

        <Text style={styles.sectionTitle}>Contact Information</Text>
        <Text style={styles.description}>
          Email: info@technet7.com{'\n'}
          Phone: +1 (555) 123-4567{'\n'}
          Address: 123 Tech Street, San Francisco, CA 94105
        </Text>
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
  aboutBox: {
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
    marginBottom: 24,
    textAlign: 'center',
  },
  sectionTitle: {
    color: '#ffd700',
    fontSize: 18,
    fontWeight: '600',
    marginTop: 16,
    marginBottom: 8,
  },
  description: {
    color: '#fff',
    fontSize: 14,
    lineHeight: 20,
    marginBottom: 12,
  },
});
