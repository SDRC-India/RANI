import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HemocueComponent } from './hemocue.component';

describe('HemocueComponent', () => {
  let component: HemocueComponent;
  let fixture: ComponentFixture<HemocueComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HemocueComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HemocueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
