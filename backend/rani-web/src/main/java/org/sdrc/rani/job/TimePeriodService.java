package org.sdrc.rani.job;

import java.text.ParseException;

public interface TimePeriodService {

	void createMonthlyTimePeriod() throws ParseException;

	void schedulePlanning();

	void updateAggregatedData();

}
