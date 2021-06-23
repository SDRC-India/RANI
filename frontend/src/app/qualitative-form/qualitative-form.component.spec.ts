import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QualitativeFormComponent } from './qualitative-form.component';

describe('QualitativeFormComponent', () => {
  let component: QualitativeFormComponent;
  let fixture: ComponentFixture<QualitativeFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ QualitativeFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QualitativeFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
