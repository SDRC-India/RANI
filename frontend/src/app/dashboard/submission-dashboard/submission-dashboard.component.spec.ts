import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SubmissionDashboardComponent } from './submission-dashboard.component';

describe('SubmissionDashboardComponent', () => {
  let component: SubmissionDashboardComponent;
  let fixture: ComponentFixture<SubmissionDashboardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SubmissionDashboardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SubmissionDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
