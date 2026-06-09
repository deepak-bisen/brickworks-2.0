import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// FIX: authGuard was not used on ANY route in app.routes.ts.
// This file keeps the guard ready; see app.routes.ts for the route-level fix.
export const authGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  if (authService.isAuthenticated()) return true;
  router.navigate(['/login']);
  return false;
};

// Role-specific guards
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
  if (authService.isAuthenticated() && (role === 'STAFF' || role === 'MANAGER')) return true;
  router.navigate([authService.isAuthenticated() ? '/' : '/login']);
  return false;
};
