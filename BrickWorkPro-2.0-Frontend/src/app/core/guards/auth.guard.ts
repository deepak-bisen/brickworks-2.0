import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  if (authService.isAuthenticated()) return true;
  router.navigate(['/login']);
  return false;
};

export const adminGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  if (authService.isAuthenticated() && authService.isAdminRole()) return true;
  router.navigate([authService.isAuthenticated() ? '/' : '/login']);
  return false;
};

export const customerGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  if (authService.isAuthenticated() && authService.getRole() === 'CUSTOMER') return true;
  router.navigate([authService.isAuthenticated() ? '/' : '/login']);
  return false;
};

export const staffGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const role = authService.getRole();
  if (authService.isAuthenticated() && role === 'STAFF') return true;
  router.navigate([authService.isAuthenticated() ? '/' : '/login']);
  return false;
};