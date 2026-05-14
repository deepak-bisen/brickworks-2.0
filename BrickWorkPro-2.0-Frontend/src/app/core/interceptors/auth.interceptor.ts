import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // 1. Get the token from local storage
  const token = localStorage.getItem('adminToken');

  // 2. If token exists, clone the request and add the Authorization header
  // Your backend JwtRequestFilter expects "Bearer <token>"
  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }

  // 3. If no token, just pass the request through (needed for Login)
  return next(req);
};
