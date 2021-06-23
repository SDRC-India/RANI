import { TestBed } from '@angular/core/testing';

import { PlanningServiceService } from './planning-service.service';

describe('PlanningServiceService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: PlanningServiceService = TestBed.get(PlanningServiceService);
    expect(service).toBeTruthy();
  });
});
