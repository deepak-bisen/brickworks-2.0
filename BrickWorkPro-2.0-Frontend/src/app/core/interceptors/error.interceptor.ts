import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service'; // Apna actual path check kar lein

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // NOTE: Agar aap ngx-toastr ya Angular Material use kar rahe hain,
  // toh yahan apna Toast Service inject karein.
  // const toast = inject(ToastrService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Something Went Wrong. Please Try again later!';0

      // Client-side ya network error
      if (error.error instanceof ErrorEvent) {
        errorMessage = `Network Error: ${error.error.message}`;
      } else {
        // Backend (Server-side) se aaya hua error
        switch (error.status) {
          case 401: // Unauthorized (Token Expired ya Invalid)
            errorMessage = 'Your session has expired. Please login again.';
            authService.logout(); // Yeh function localStorage se token hatayega
            router.navigate(['/login']);
            break;

          case 403: // Forbidden
            errorMessage = 'You are not authorized to access this resource.';
            break;

          case 404: // Not Found
            errorMessage = 'The requested resource was not found.';
            break;

          case 500: // Server Error
            errorMessage = 'Server is experiencing issues (500 Internal Server Error).';
            break;

          default:
            // Backend se aane wala custom error message agar available ho
            errorMessage = error.error?.message || `Server returned error code ${error.status}.`;
        }
      }

      // Yahan apna Toast/Notification fire karein
      // toast.error(errorMessage);
      console.error('Interceptor Caught Error:', errorMessage);

      // Error ko aage pass karein taaki specific component bhi catch kar sake agar zaroorat ho
      return throwError(() => new Error(errorMessage));
    })
  );
};
