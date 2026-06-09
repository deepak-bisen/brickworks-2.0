import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-our-process',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './our-process.component.html'
})
export class OurProcessComponent {
  processes = [
    { step: '01', title: 'Clay Preparation', desc: 'We source the finest local soil and blend it to the perfect consistency for maximum durability.' },
    { step: '02', title: 'Molding & Shaping', desc: 'Using advanced machinery alongside traditional techniques, bricks are pressed into precise dimensions.' },
    { step: '03', title: 'Sun Drying', desc: 'Raw bricks are naturally sun-dried to remove excess moisture, preventing structural cracking.' },
    { step: '04', title: 'Kiln Firing', desc: 'Bricks are baked at extreme temperatures in our high-capacity kilns to achieve their signature strength and red color.' },
    { step: '05', title: 'Quality Check & Dispatch', desc: 'Every batch undergoes rigorous quality testing before being loaded onto trucks for delivery to your site.' }
  ];
}
