import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { GalleryComponent } from './gallery/gallery.component';
import { OurProcessComponent } from './our-process/our-process.component';
import { ContactComponent } from './contact/contact.component';
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, GalleryComponent, OurProcessComponent],
  templateUrl: './home.component.html'
})
export class HomeComponent {

  // Data for the Features Section
  features = [
    {
      icon: '🏗️',
      title: 'Premium Quality',
      description: 'Manufactured with high-grade raw materials and fired at optimal temperatures for maximum structural durability and load-bearing strength.'
    },
    {
      icon: '🚚',
      title: 'Fast Delivery',
      description: 'Our dedicated logistics fleet ensures your materials arrive on-site, exactly on schedule, keeping your project timeline completely uninterrupted.'
    },
    {
      icon: '💰',
      title: 'Factory-Direct Pricing',
      description: 'By cutting out the middleman, we provide highly competitive market rates without ever compromising on dimensions, finish, or quality.'
    }
  ];

  // Data for the Trust Metrics Section
  stats = [
    { number: '15+', label: 'Years Experience' },
    { number: '50k+', label: 'Bricks Daily Output' },
    { number: '1,200+', label: 'Projects Completed' },
    { number: '100%', label: 'Quality Guarantee' }
  ];
}
