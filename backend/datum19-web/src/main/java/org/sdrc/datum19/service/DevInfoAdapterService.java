package org.sdrc.datum19.service;

import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public interface DevInfoAdapterService {
public String importDevinfoData(Integer sourceCode, String sector, String subsector, String sourcepath) throws InvalidFormatException, IOException;
}
