import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanningSideMenuComponent } from './planning-side-menu.component';

describe('PlanningSideMenuComponent', () => {
  let component: PlanningSideMenuComponent;
  let fixture: ComponentFixture<PlanningSideMenuComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PlanningSideMenuComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlanningSideMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
