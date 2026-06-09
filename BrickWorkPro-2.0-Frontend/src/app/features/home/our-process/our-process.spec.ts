import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OurProcess } from './our-process';

describe('OurProcess', () => {
  let component: OurProcess;
  let fixture: ComponentFixture<OurProcess>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OurProcess],
    }).compileComponents();

    fixture = TestBed.createComponent(OurProcess);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
