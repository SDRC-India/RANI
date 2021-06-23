import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HorizonatlBarChartComponent } from './horizonatl-bar-chart.component';

describe('HorizonatlBarChartComponent', () => {
  let component: HorizonatlBarChartComponent;
  let fixture: ComponentFixture<HorizonatlBarChartComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HorizonatlBarChartComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HorizonatlBarChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
