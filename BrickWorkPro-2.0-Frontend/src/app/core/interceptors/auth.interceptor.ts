import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

// FIX: Must read from the same canonical key the AuthService writes to.
const TOKEN_KEY = 'brickworks_token';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const platformId = inject(PLATFORM_ID);

  if (isPlatformBrowser(platformId)) {
    // FIX: Check canonical key first, then old keys for backward compat
    const token =
      localStorage.getItem(TOKEN_KEY) ??
      localStorage.getItem('token') ??
      localStorage.getItem('adminToken');

    if (token) {
      const authReq = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` },
      });
      return next(authReq);
    }
  }

  return next(req);
};
