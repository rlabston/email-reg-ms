import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { App } from './app';
import { authGuard, authMatchGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: '',
    pathMatch: 'full',
    component: App,
    // Use canMatch to ensure redirect happens even during initial navigation/hydration
    canMatch: [authMatchGuard],
    // Keep canActivate as a secondary check for subsequent navigations
    canActivate: [authGuard]
  }
];
