import { Component } from '@angular/core';

@Component({
  selector: 'app-gallery',
  standalone: true,
  templateUrl: './gallery.component.html'
})
export class GalleryComponent {
  // Using the exact image names from your Phase 1 repository
  images = [
    { src: 'assets/images/projects/modern-home.jpg', alt: 'Modern Home Construction' },
    { src: 'assets/images/projects/3-Floor-House.jpg', alt: '3 Floor Apartment' },
    { src: 'assets/images/projects/home-2.jpg', alt: 'Residential Project' },
    { src: 'assets/images/projects/cement-home.jpg', alt: 'Cement Brick Structure' },
    { src: 'assets/images/projects/home.jpg', alt: 'Our Own House' },
    { src: 'assets/images/projects/kiln.jpg', alt: 'Kiln for Brick Firing' },
    {src: 'assets/images/products/red_backed.png', alt: 'Bricks for Construction' },
    {src: 'assets/images/products/brick_unbacked.png', alt: 'Unbacked Bricks In Process' },
  ];
}
