import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { HomeComponent } from './home/home.component';
import { RegistrationComponent } from './registration/registration.component';
import { EmailListComponent } from './email-list/email-list.component';
import { authGuard, authMatchGuard } from './guards/auth.guard';
import { adminGuard, adminMatchGuard } from './guards/admin.guard';
import { ChatbotComponent } from './chatbot/chatbot.component';
import { AboutComponent } from './about/about.component';
import { ContactComponent } from './contact/contact.component';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: HomeComponent
  },
  {
    path: 'home',
    component: HomeComponent
  },
  {
    path: 'about',
    component: AboutComponent
  },
  {
    path: 'contact',
    component: ContactComponent
  },
  {
    path: 'register',
    component: RegistrationComponent
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'emails',
    component: EmailListComponent,
    canMatch: [adminMatchGuard],
    canActivate: [adminGuard]
  },
  {
    path: 'chatbot',
    component: ChatbotComponent
  }
];
