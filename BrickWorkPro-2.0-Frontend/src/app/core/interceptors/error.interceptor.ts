import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const notification = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Something went wrong. Please try again later.';

      if (error.error instanceof ErrorEvent) {
        errorMessage = `Network error: ${error.error.message}`;
      } else {
        // Handled inline: optional payment prefetch and invoice actions.
        if (req.url.includes('/api/finance/payments/order/')) {
          if (error.status === 404 || error.status === 403) {
            return throwError(() => error);
          }
        }
        if (req.url.includes('/api/finance/invoice/')) {
          return throwError(() => error);
        }

        switch (error.status) {
          case 401:
            if (req.url.includes('/api/auth/login')) {
              errorMessage =
                (typeof error.error === 'string' ? error.error : null) ||
                'Invalid username or password.';
            } else {
              errorMessage = 'Your session has expired. Please log in again.';
              authService.logout();
              router.navigate(['/login']);
            }
            break;
          case 403:
            errorMessage = 'You are not authorized to access this resource.';
            break;
          case 404:
            errorMessage = 'The requested resource was not found.';
            break;
          case 500:
            errorMessage = 'Server is experiencing issues. Please try again later.';
            break;
          default:
            errorMessage =
              error.error?.message ||
              (typeof error.error === 'string' ? error.error : null) ||
              `Server returned error code ${error.status}.`;
        }
      }

      notification.error(errorMessage);
      return throwError(() => new Error(errorMessage));
    }),
  );
};