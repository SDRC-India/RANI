package org.sdrc.rani.util;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class RANIUtil {

	public static XSSFCellStyle getStyleForLeftMiddle(XSSFWorkbook workbook) {
		
		XSSFCellStyle styleForLeftMiddle = workbook.createCellStyle();
		styleForLeftMiddle.setVerticalAlignment(VerticalAlignment.CENTER);
		styleForLeftMiddle.setAlignment(HorizontalAlignment.CENTER);
		styleForLeftMiddle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleForLeftMiddle.setFillForegroundColor(new XSSFColor(new java.awt.Color(194, 107, 97)));
		styleForLeftMiddle.setFont(getStyleForFont(workbook).getFont());
		return styleForLeftMiddle;
	}
public static XSSFCellStyle getStyleForLeftMiddle1(XSSFWorkbook workbook) {
		
		XSSFCellStyle styleForLeftMiddle = workbook.createCellStyle();
		styleForLeftMiddle.setVerticalAlignment(VerticalAlignment.CENTER);
		styleForLeftMiddle.setAlignment(HorizontalAlignment.CENTER);
		styleForLeftMiddle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleForLeftMiddle.setFillForegroundColor(new XSSFColor(new java.awt.Color(243, 115, 97)));
		styleForLeftMiddle.setFont(getStyleForFont(workbook).getFont());
		return styleForLeftMiddle;
	}
public static XSSFCellStyle getStyleForLeftMiddle2(XSSFWorkbook workbook) {
	
	XSSFCellStyle styleForLeftMiddle = workbook.createCellStyle();
	styleForLeftMiddle.setVerticalAlignment(VerticalAlignment.CENTER);
	styleForLeftMiddle.setAlignment(HorizontalAlignment.CENTER);
	styleForLeftMiddle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	styleForLeftMiddle.setFillForegroundColor(new XSSFColor(new java.awt.Color(244, 167, 117)));
	styleForLeftMiddle.setFont(getStyleForFont(workbook).getFont());
	return styleForLeftMiddle;
}
public static XSSFCellStyle getStyleForLeftMiddle3(XSSFWorkbook workbook) {
	
	XSSFCellStyle styleForLeftMiddle = workbook.createCellStyle();
	styleForLeftMiddle.setVerticalAlignment(VerticalAlignment.CENTER);
	styleForLeftMiddle.setAlignment(HorizontalAlignment.CENTER);
	styleForLeftMiddle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	styleForLeftMiddle.setFillForegroundColor(new XSSFColor(new java.awt.Color(244, 198, 103)));
	styleForLeftMiddle.setFont(getStyleForFont(workbook).getFont());
	return styleForLeftMiddle;
}
	
	public static XSSFCellStyle getStyleForFont(XSSFWorkbook workbook) {
		
		//Create a new font and alter it.
       XSSFFont font = workbook.createFont();
       font.setFontHeightInPoints((short) 12);
       font.setBold(true);
		
		XSSFCellStyle tyleForWrapFont = workbook.createCellStyle();
		tyleForWrapFont.setFont(font);
		
		return tyleForWrapFont;
	}
	
public static XSSFCellStyle getStyleForSectorFont(XSSFWorkbook workbook) {
		
		//Create a new font and alter it.
       XSSFFont font = workbook.createFont();
       font.setFontHeightInPoints((short) 15);
       font.setBold(true);
		
		XSSFCellStyle tyleForWrapFont = workbook.createCellStyle();
		tyleForWrapFont.setFont(font);
		
		return tyleForWrapFont;
	}
	
	
public static XSSFCellStyle getStyleForFontDatavalue(XSSFWorkbook workbook) {
	
	//Create a new font and alter it.
   XSSFFont font = workbook.createFont();
   font.setFontHeightInPoints((short) 12);
   font.setBold(true);
  
	XSSFCellStyle tyleForWrapFont = workbook.createCellStyle();
	tyleForWrapFont.setFont(font);
	tyleForWrapFont.setAlignment(HorizontalAlignment.LEFT);
	tyleForWrapFont.setVerticalAlignment(VerticalAlignment.CENTER);
	return tyleForWrapFont;
}
	

}
