import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface ServiceItem {
  title: string;
  description: string;
  icon: string;
  category: string;
  features: string[];
  ctaText: string;
  ctaUrl: string;
  isHighlighted: boolean;
}

interface ContactInfo {
  email: string;
  phone: string;
  website: string;
  address: string;
  linkedIn: string;
  github: string;
}

interface HomeScreenData {
  welcomeTitle: string;
  welcomeSubtitle: string;
  heroImageUrl: string;
  featuredServices: ServiceItem[];
  servicesByCategory: { [key: string]: ServiceItem[] };
  supportedTechnologies: string[];
  contactInfo: ContactInfo;
  lastUpdated: number;
}

@Component({
  selector: 'app-home',
  imports: [CommonModule],
  template: `
    <div class="home-container">
      <div class="top-bar">
        <div class="spacer"></div>
        <div class="menu-wrapper">
          <button class="hamburger-menu" (click)="menuOpen = !menuOpen" aria-label="Open menu">
            &#9776;
          </button>
          <div class="dropdown-menu" *ngIf="menuOpen">
            <a (click)="menuOpen = false">Home</a>
            <a routerLink="/registration" (click)="menuOpen = false">Email Registration</a>
            <a *ngIf="!isLoggedIn()" (click)="showLoginModal = true; menuOpen = false">Login</a>
            <a routerLink="/chatbot" *ngIf="isLoggedIn()" (click)="menuOpen = false">AI Chatbot</a>
            <a routerLink="/email-list" *ngIf="isAdmin()" (click)="menuOpen = false">View All Emails</a>
            <a (click)="logout(); menuOpen = false" *ngIf="isLoggedIn()">Logout</a>
          </div>
        </div>
      </div>
      <!-- ...existing code... -->
      @if (isLoading()) {
        <div class="loading-card">
          <div class="spinner"></div>
          <p>Loading services...</p>
        </div>
      } @else if (homeData()) {
        <!-- ...existing code... -->
      } @else {
        <div class="error-card">
          <h2>Error loading services</h2>
          <p>{{ error() }}</p>
          <button (click)="loadHomeData()">Retry</button>
        </div>
      }
      <!-- Login Modal Overlay -->
      <div class="login-modal-overlay" *ngIf="showLoginModal">
        <app-login (close)="showLoginModal = false" (loggedIn)="showLoginModal = false; reloadHomeData()"></app-login>
      </div>
    </div>
  `,
  styles: [`
    .home-container {
      padding: 20px;
      max-width: 1400px;
      margin: 0 auto;
      background: url('/assets/cityscape.png') center/cover no-repeat;
      background-attachment: fixed;
      min-height: 100vh;
    }

    .loading-card, .error-card {
      background: rgba(0, 0, 0, 0.7);
      border-radius: 12px;
      padding: 40px;
      text-align: center;
      color: white;
      margin: 40px auto;
      max-width: 500px;
    }

    .spinner {
      border: 4px solid rgba(255, 255, 255, 0.3);
      border-top: 4px solid white;
      border-radius: 50%;
      width: 50px;
      height: 50px;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .welcome-section {
      background: transparent;
      border-radius: 12px;
      padding: 40px;
      text-align: center;
      margin-bottom: 24px;
      color: white;
    }

    .welcome-section h1 {
      font-size: 2.5rem;
      font-weight: bold;
      margin-bottom: 12px;
    }

    .subtitle {
      font-size: 1.25rem;
      color: #aaa;
    }

    .services-section {
      margin-bottom: 24px;
    }

    .section-title {
      color: white;
      font-size: 1.75rem;
      font-weight: bold;
      margin-bottom: 16px;
      padding: 0 8px;
    }

    .horizontal-scroll {
      display: flex;
      gap: 16px;
      overflow-x: auto;
      padding: 8px 8px 16px;
      scrollbar-width: thin;
      scrollbar-color: rgba(255, 255, 255, 0.3) transparent;
    }

    .horizontal-scroll::-webkit-scrollbar {
      height: 8px;
    }

    .horizontal-scroll::-webkit-scrollbar-track {
      background: transparent;
    }

    .horizontal-scroll::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.3);
      border-radius: 4px;
    }
  styles: [`
    .home-container {
      padding: 20px;
      max-width: 1400px;
      margin: 0 auto;
      background: url('/assets/cityscape.png') center/cover no-repeat;
      background-attachment: fixed;
      min-height: 100vh;
      position: relative;
    }
    .login-modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background: rgba(30,33,50,0.85);
      z-index: 1000;
      display: flex;
      align-items: center;
      justify-content: center;
      backdrop-filter: blur(8px);
    }
    // ...existing code...
  `]
      color: white;
      padding: 10px 20px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 0.85rem;
      transition: all 0.2s;
    }

    .cta-button:hover {
      background: white;
      color: #1a1a2e;
    }

    .cta-button-small {
      background: transparent;
      border: 1px solid white;
      color: white;
      padding: 8px 16px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 0.8rem;
      transition: all 0.2s;
    }

    .cta-button-small:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    .technologies-section {
      background: rgba(0, 0, 0, 0.3);
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 24px;
    }

    .technologies-section .section-title {
      padding: 0;
      margin-bottom: 16px;
    }

    .tech-pills {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
    }

    .tech-pill {
      background: rgba(255, 255, 255, 0.1);
      color: white;
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 0.9rem;
      display: inline-block;
    }

    .contact-section {
      background: rgba(0, 0, 0, 0.3);
      border-radius: 12px;
      padding: 24px;
      color: white;
    }

    .contact-section .section-title {
      padding: 0;
      margin-bottom: 16px;
    }

    .contact-section p {
      color: #aaa;
      margin: 8px 0;
      font-size: 1rem;
    }

    .error-card button {
      background: #007bff;
      color: white;
      border: none;
      padding: 12px 24px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 1rem;
      margin-top: 16px;
    }

    .error-card button:hover {
      background: #0056b3;
    }
  `]
})
export class HomeComponent implements OnInit {
  homeData = signal<HomeScreenData | null>(null);
  isLoading = signal(true);
  error = signal<string | null>(null);

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadHomeData();
  }

  loadHomeData() {
    this.isLoading.set(true);
    this.error.set(null);

    // Try to load from backend API, fallback to mock data
    this.http.get<HomeScreenData>('/api/home/data').subscribe({
      next: (data) => {
        this.homeData.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        // Use rich mock data based on Technet7 services
        this.homeData.set(this.createTechnet7ServicesData());
        this.isLoading.set(false);
      }
    });
  }

  getCategories(): string[] {
    return Object.keys(this.homeData()?.servicesByCategory || {});
  }

  private createTechnet7ServicesData(): HomeScreenData {
    const featuredServices: ServiceItem[] = [
      {
        title: "AI & Machine Learning Solutions",
        description: "Custom AI models, chatbots with natural language processing, and intelligent automation systems designed to transform your business processes.",
        icon: "🤖",
        category: "AI/ML",
        features: ["Custom AI Model Development", "ChatGPT Integration", "Natural Language Processing"],
        ctaText: "Explore AI Solutions",
        ctaUrl: "/services/ai-ml",
        isHighlighted: true
      },
      {
        title: "Cloud Infrastructure & DevOps",
        description: "Scalable cloud architecture, CI/CD pipelines, and modern deployment strategies for enterprise applications on AWS, Azure, and GCP.",
        icon: "☁️",
        category: "Cloud",
        features: ["AWS/Azure/GCP Setup", "Kubernetes Orchestration", "CI/CD Pipeline Design"],
        ctaText: "View Cloud Services",
        ctaUrl: "/services/cloud",
        isHighlighted: true
      },
      {
        title: "Mobile & Web Development",
        description: "Full-stack development with modern frameworks, responsive design, and cross-platform mobile solutions for Android and iOS.",
        icon: "📱",
        category: "Development",
        features: ["React/Angular Applications", "Android/iOS Development", "Progressive Web Apps"],
        ctaText: "Start Your Project",
        ctaUrl: "/services/development",
        isHighlighted: true
      }
    ];

    const servicesByCategory = {
      "AI & Data Science": [
        { title: "Machine Learning Models", description: "Custom ML models for classification, regression, clustering, and recommendation systems tailored to your business needs.", icon: "🧠", category: "AI/ML", features: [], ctaText: "Learn More", ctaUrl: "/ai/ml-models", isHighlighted: false },
        { title: "Natural Language Processing", description: "Text analysis, sentiment analysis, language translation, and conversational AI solutions for enhanced customer interaction.", icon: "💬", category: "AI/ML", features: [], ctaText: "Explore NLP", ctaUrl: "/ai/nlp", isHighlighted: false },
        { title: "Data Modeling & Analytics", description: "Advanced data modeling, predictive analytics, and business intelligence solutions to unlock insights from your data.", icon: "📊", category: "Data", features: [], ctaText: "View Analytics", ctaUrl: "/ai/analytics", isHighlighted: false }
      ],
      "Cloud & Infrastructure": [
        { title: "AWS Cloud Solutions", description: "Complete AWS infrastructure setup, migration, and optimization for scalable, cost-effective cloud applications.", icon: "🔶", category: "Cloud", features: [], ctaText: "AWS Services", ctaUrl: "/cloud/aws", isHighlighted: false },
        { title: "Azure Solutions", description: "Microsoft Azure cloud services including Azure Functions, App Services, and enterprise-grade cloud infrastructure.", icon: "🔷", category: "Cloud", features: [], ctaText: "Azure Services", ctaUrl: "/cloud/azure", isHighlighted: false },
        { title: "Kubernetes Orchestration", description: "Container orchestration, microservices architecture, and automated scaling solutions for modern applications.", icon: "⚙️", category: "Cloud", features: [], ctaText: "K8s Solutions", ctaUrl: "/cloud/kubernetes", isHighlighted: false }
      ],
      "Web & Mobile Development": [
        { title: "Web Application Development", description: "Modern responsive web applications using React, Angular, Vue.js, and Spring Boot for robust enterprise solutions.", icon: "🌐", category: "Development", features: [], ctaText: "Web Apps", ctaUrl: "/dev/web", isHighlighted: false },
        { title: "Android Development", description: "Native Android applications with Kotlin and Jetpack Compose for exceptional mobile user experiences.", icon: "📲", category: "Development", features: [], ctaText: "Android Apps", ctaUrl: "/dev/android", isHighlighted: false },
        { title: "iOS Development", description: "Native iOS applications with Swift and SwiftUI for seamless Apple ecosystem integration.", icon: "🍎", category: "Development", features: [], ctaText: "iOS Apps", ctaUrl: "/dev/ios", isHighlighted: false }
      ]
    };

    const technologies = [
      "Java", "Spring Boot", "Python", "TensorFlow", "React", "Angular", "Node.js",
      "Kotlin", "Swift", "Flutter", "AWS", "Azure", "Docker", "Kubernetes", 
      "PostgreSQL", "MongoDB", "Redis", "Elasticsearch"
    ];

    const contactInfo: ContactInfo = {
      email: "info@technet7.com",
      phone: "+1 (555) 123-4567",
      website: "https://technet7.com",
      address: "San Francisco, CA",
      linkedIn: "https://linkedin.com/company/technet7",
      github: "https://github.com/technet7"
    };

    return {
      welcomeTitle: "Welcome to Technet7 AI Services",
      welcomeSubtitle: "Innovative Technology Solutions for Modern Businesses",
      heroImageUrl: "https://technet7.com/images/hero-background.jpg",
      featuredServices,
      servicesByCategory,
      supportedTechnologies: technologies,
      contactInfo,
      lastUpdated: Date.now()
    };
  }
}
