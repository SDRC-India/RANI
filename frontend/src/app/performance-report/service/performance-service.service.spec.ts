import { TestBed } from '@angular/core/testing';

import { PerformanceServiceService } from './performance-service.service';

describe('PerformanceServiceService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: PerformanceServiceService = TestBed.get(PerformanceServiceService);
    expect(service).toBeTruthy();
  });
});
