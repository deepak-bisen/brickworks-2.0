import { ApplicationConfig } from '@angular/core';
import { provideRouter, withComponentInputBinding, withDebugTracing } from '@angular/router'; // Added helper functions
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(
      routes,
      withComponentInputBinding(), // Allows components to receive route params as @Input
      withDebugTracing()           // Logs routing events to the console for debugging
    ),
    provideHttpClient(
      withInterceptors([authInterceptor])
    )
  ]
};
