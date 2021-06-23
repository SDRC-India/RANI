import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ThreeDBarChartComponent } from './three-d-bar-chart.component';

describe('ThreeDBarChartComponent', () => {
  let component: ThreeDBarChartComponent;
  let fixture: ComponentFixture<ThreeDBarChartComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ThreeDBarChartComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ThreeDBarChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
