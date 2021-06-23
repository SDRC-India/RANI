import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SdrcLoaderComponent } from './sdrc-loader.component';

describe('SdrcLoaderComponent', () => {
  let component: SdrcLoaderComponent;
  let fixture: ComponentFixture<SdrcLoaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SdrcLoaderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SdrcLoaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
