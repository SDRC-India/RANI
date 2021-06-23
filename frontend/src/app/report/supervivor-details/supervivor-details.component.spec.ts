import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SupervivorDetailsComponent } from './supervivor-details.component';

describe('SupervivorDetailsComponent', () => {
  let component: SupervivorDetailsComponent;
  let fixture: ComponentFixture<SupervivorDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SupervivorDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SupervivorDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
