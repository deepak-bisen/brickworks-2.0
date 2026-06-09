import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrickCalculator } from './brick-calculator';

describe('BrickCalculator', () => {
  let component: BrickCalculator;
  let fixture: ComponentFixture<BrickCalculator>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BrickCalculator],
    }).compileComponents();

    fixture = TestBed.createComponent(BrickCalculator);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
