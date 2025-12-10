import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-about',
  imports: [CommonModule, RouterModule],
  template: `
    <div class="about-container">
      <div class="about-box">
        <h1 class="title">About Technet7</h1>
        
        <h2 class="section-title">Who We Are</h2>
        <p class="description">
          Technet7 is a leading technology solutions provider specializing in AI, cloud infrastructure, and custom software development.
        </p>

        <h2 class="section-title">Our Mission</h2>
        <p class="description">
          To empower businesses with innovative technology solutions that drive growth, efficiency, and digital transformation.
        </p>

        <h2 class="section-title">Our Services</h2>
        <p class="description">
          • AI & Machine Learning Solutions<br>
          • Cloud Infrastructure & DevOps<br>
          • Mobile & Web Development<br>
          • Custom Software Development<br>
          • Data Analytics & Modeling
        </p>

        <h2 class="section-title">Why Choose Us?</h2>
        <p class="description">
          • Expert team with 15+ years of experience<br>
          • Proven track record with Fortune 500 companies<br>
          • Cutting-edge technology and best practices<br>
          • 24/7 Support and maintenance
        </p>

        <h2 class="section-title">Contact Information</h2>
        <p class="description">
          Email: info@technet7.com<br>
          Phone: +1 (555) 123-4567<br>
          Address: 123 Tech Street, San Francisco, CA 94105
        </p>

        <div class="back-button-container">
          <a routerLink="/home" class="back-button">← Back to Home</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .about-container {
      min-height: 100vh;
      padding: 20px;
      background: url('/assets/cityscape.png') center/cover no-repeat;
      background-attachment: fixed;
      background-color: #1a2332;
    }

    .about-box {
      max-width: 800px;
      margin: 0 auto;
      background: rgba(34, 44, 58, 0.9);
      border-radius: 12px;
      padding: 40px;
      border: 1px solid #4a5568;
    }

    .title {
      color: #ffd700;
      font-size: 32px;
      font-weight: bold;
      margin-bottom: 24px;
      text-align: center;
    }

    .section-title {
      color: #ffd700;
      font-size: 18px;
      font-weight: 600;
      margin-top: 24px;
      margin-bottom: 12px;
    }

    .description {
      color: #fff;
      font-size: 14px;
      line-height: 24px;
      margin-bottom: 12px;
    }

    .back-button-container {
      margin-top: 32px;
      text-align: center;
    }

    .back-button {
      display: inline-block;
      padding: 12px 24px;
      background: #ffd700;
      color: #1a2332;
      text-decoration: none;
      border-radius: 6px;
      font-weight: 600;
      transition: all 0.3s;
    }

    .back-button:hover {
      background: #ffed4e;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3);
    }

    @media (max-width: 768px) {
      .about-box {
        padding: 20px;
      }

      .title {
        font-size: 24px;
      }

      .section-title {
        font-size: 16px;
      }
    }
  `]
})
export class AboutComponent {}
