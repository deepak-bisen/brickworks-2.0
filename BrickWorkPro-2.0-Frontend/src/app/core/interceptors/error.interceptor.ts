import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';
import { extractApiErrorMessage } from '../../shared/utils/api-error.util';

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
        const apiMessage = extractApiErrorMessage(error);

        // Handled inline: optional payment prefetch and invoice actions.
        if (req.url.includes('/api/finance/payments/order/')) {
          if (error.status === 404 || error.status === 403) {
            return throwError(() => error);
          }
        }
        if (req.url.includes('/api/finance/invoice/')) {
          return throwError(() => error);
        }

        // Payment *write* actions during checkout (COD, UTR submit, Razorpay create/verify).
        // These are now resilient on the backend (payment record is saved even if order sync lags).
        // Let the CheckoutComponent decide the user-facing message (often a soft/recoverable error
        // with the orderId so the user isn't scared by a global toast when the important data exists).
        if (req.url.includes('/api/finance/payments/cod/select') ||
            req.url.includes('/api/finance/payments/utr/submit') ||
            req.url.includes('/api/finance/payments/create-order') ||
            req.url.includes('/api/finance/payments/verify')) {
          return throwError(() => error);
        }

        switch (error.status) {
          case 401:
            if (req.url.includes('/api/auth/login')) {
              errorMessage =
                apiMessage ||
                (typeof error.error === 'string' ? error.error : null) ||
                'Invalid username or password.';
            } else {
              errorMessage = 'Your session has expired. Please log in again.';
              authService.logout();
              router.navigate(['/login']);
            }
            break;
          case 403:
            errorMessage = apiMessage || 'You are not authorized to access this resource.';
            break;
          case 404:
            errorMessage = apiMessage || 'The requested resource was not found.';
            break;
          case 409:
            errorMessage = apiMessage || 'This action conflicts with current data. Please review and try again.';
            break;
          case 500:
            errorMessage = apiMessage || 'Server is experiencing issues. Please try again later.';
            break;
          default:
            errorMessage =
              apiMessage ||
              `Server returned error code ${error.status}.`;
        }
      }

      notification.error(errorMessage);
      return throwError(() => error);
    }),
  );
};