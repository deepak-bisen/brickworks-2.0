import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductionStageManagementComponent } from './production-stage-management.component';

describe('ProductionStageManagementComponent', () => {
  let component: ProductionStageManagementComponent;
  let fixture: ComponentFixture<ProductionStageManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductionStageManagementComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductionStageManagementComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
