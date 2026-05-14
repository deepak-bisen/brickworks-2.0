import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app'; // Change 'App' to 'AppComponent'

bootstrapApplication(AppComponent, appConfig) // Use the correct class name here
  .catch((err) => console.error(err));
