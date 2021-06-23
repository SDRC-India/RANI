package org.sdrc.rani.util;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;



/**
 * @author Subham Ashish(subham@sdrc.co.in)
 */
@Component
public class ExcelStyleSheet {

	/**
	 * style for color header
	 * 
	 * @param workbook
	 * @return
	 */
	public static CellStyle getStyleForColorHeader(Workbook workbook) {

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setWrapText(true);
		headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setFont(getFontForNoteHeader(workbook));
		return headerStyle;
	}

	/**
	 * font for header
	 * 
	 * @param workbook
	 * @return
	 */
	public static Font getFontForHeader(Workbook workbook) {

		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setBold(true);

		return font;
	}
	
	
	/**
	 * font for header
	 * 
	 * @param workbook
	 * @return
	 */
	public static Font getFontForHeaderThematicView(Workbook workbook) {

		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) 13);
		font.setBold(true);

		return font;
	}
	
	public static Font getFontForNoteHeader(Workbook workbook) {

		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setBold(true);
		font.setColor((short) 0xa);
		return font;
	}

	/**
	 * Font for Hyperlink
	 * 
	 * @param workBook
	 * @return
	 */
	public static Font hyperLinkFont(Workbook workBook) {

		Font font = workBook.createFont();
		font.setUnderline(XSSFFont.U_SINGLE);
		font.setColor(HSSFColor.BLUE.index);
		return font;

	}

	// odd cell
	public static CellStyle getStyleForOddHyperLink(Workbook workbook) {
		CellStyle colStyleOdd = workbook.createCellStyle();
		colStyleOdd.setBorderBottom(BorderStyle.THIN);
		colStyleOdd.setBorderTop(BorderStyle.THIN);
		colStyleOdd.setBorderLeft(BorderStyle.THIN);
		colStyleOdd.setBorderRight(BorderStyle.THIN);
		colStyleOdd.setAlignment(HorizontalAlignment.CENTER);
		colStyleOdd.setWrapText(true);
		colStyleOdd.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		colStyleOdd.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		colStyleOdd.setFont(hyperLinkFont(workbook));
		return colStyleOdd;
	}

	public static CellStyle getStyleForEvenHyperLink(Workbook workbook) {

		CellStyle colStyleEven = workbook.createCellStyle();
		colStyleEven.setBorderBottom(BorderStyle.THIN);
		colStyleEven.setBorderTop(BorderStyle.THIN);
		colStyleEven.setBorderLeft(BorderStyle.THIN);
		colStyleEven.setBorderRight(BorderStyle.THIN);
		colStyleEven.setWrapText(true);
		colStyleEven.setAlignment(HorizontalAlignment.CENTER);
		colStyleEven.setFont(hyperLinkFont(workbook));
		return colStyleEven;

	}

	// odd cell
	public static CellStyle getStyleForOddCell(Workbook workbook,Boolean lockCheck) {
		CellStyle colStyleOdd = workbook.createCellStyle();
		colStyleOdd.setBorderBottom(BorderStyle.THIN);
		colStyleOdd.setBorderTop(BorderStyle.THIN);
		colStyleOdd.setBorderLeft(BorderStyle.THIN);
		colStyleOdd.setBorderRight(BorderStyle.THIN);
		colStyleOdd.setAlignment(HorizontalAlignment.CENTER);
		colStyleOdd.setWrapText(true);
		colStyleOdd.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		colStyleOdd.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		colStyleOdd.setLocked(false);
		
		if(lockCheck){
			colStyleOdd.setLocked(true);
		}
		return colStyleOdd;
	}

	public static CellStyle getStyleForEvenCell(Workbook workbook,Boolean lockCheck) {

		CellStyle colStyleEven = workbook.createCellStyle();
		colStyleEven.setBorderBottom(BorderStyle.THIN);
		colStyleEven.setBorderTop(BorderStyle.THIN);
		colStyleEven.setBorderLeft(BorderStyle.THIN);
		colStyleEven.setBorderRight(BorderStyle.THIN);
		colStyleEven.setWrapText(true);
		colStyleEven.setAlignment(HorizontalAlignment.CENTER);
		colStyleEven.setLocked(false);
		if(lockCheck){
			colStyleEven.setLocked(true);
		}
		return colStyleEven;

	}


	/**
	 * @Description This method merge column and sets border and color
	 * @param rowIndex
	 * @param columnIndex
	 * @param rowSpan
	 * @param columnSpan
	 * @param sheet
	 * @return
	 */
	public static HSSFSheet doMerge(int rowIndex, int columnIndex, int rowSpan, int columnSpan, HSSFSheet sheet) {

		Cell cell = sheet.getRow(rowIndex).getCell(rowSpan);
		CellRangeAddress range = new CellRangeAddress(rowIndex, columnIndex, rowSpan, columnSpan);

		sheet.addMergedRegion(range);

		RegionUtil.setBorderBottom(BorderStyle.THIN, range, sheet);
		RegionUtil.setBorderTop(BorderStyle.THIN, range, sheet);
		RegionUtil.setBorderLeft(BorderStyle.THIN, range, sheet);
		RegionUtil.setBorderRight(BorderStyle.THIN, range, sheet);

		RegionUtil.setBottomBorderColor(cell.getCellStyle().getBottomBorderColor(), range, sheet);
		RegionUtil.setTopBorderColor(cell.getCellStyle().getTopBorderColor(), range, sheet);
		RegionUtil.setLeftBorderColor(cell.getCellStyle().getLeftBorderColor(), range, sheet);
		RegionUtil.setRightBorderColor(cell.getCellStyle().getRightBorderColor(), range, sheet);

		return sheet;
	}

	public static CellStyle getStyleForHeading(HSSFWorkbook workbook) {

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setAlignment(HorizontalAlignment.LEFT);
		headerStyle.setWrapText(true);
		headerStyle.setFillForegroundColor(getCustomColorForHeader(workbook));
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setFont(getFontForHeader(workbook));
		return headerStyle;
	}

	public static CellStyle getStyleForHeading(XSSFWorkbook workbook) {

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setAlignment(HorizontalAlignment.LEFT);
		headerStyle.setWrapText(true);
		headerStyle.setFillForegroundColor((short) 31);
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setFont(getFontForHeader(workbook));
		return headerStyle;
	}
	
	public static CellStyle getStyleForPlanningModuleHeading(HSSFWorkbook workbook) {

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setWrapText(true);
		headerStyle.setFillForegroundColor(getCustomColorForHeader(workbook));
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setFont(getFontForHeader(workbook));
		return headerStyle;
	}
	
	
	public static CellStyle getStyleForPlanningModuleNoteHeading(HSSFWorkbook workbook) {

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setWrapText(true);
		headerStyle.setFillForegroundColor(getCustomColorForHeader(workbook));
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setFont(getFontForNoteHeader(workbook));
		return headerStyle;
	}
	
	
	public static CellStyle getStyleForThematicDownload(XSSFWorkbook workbook,Integer r,Integer g,Integer b) {

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setWrapText(true);
		
		((XSSFCellStyle) headerStyle).setFillForegroundColor(new XSSFColor(new java.awt.Color(r, g, b)));
		
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setFont(getFontForNoteHeader(workbook));
		return headerStyle;
	}
	
	
	private static short getCustomColorForHeader(HSSFWorkbook workbook) {

		HSSFPalette palette = workbook.getCustomPalette();
		// get the color which most closely matches the color you want to use
		HSSFColor myColor = palette.findSimilarColor(197, 217, 241);
		// get the palette index of that color
		short palIndex = myColor.getIndex();

		return palIndex;

	}

	
	public static CellStyle getStyleForHeadingWithoutBorder(XSSFWorkbook workbook) {

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setAlignment(HorizontalAlignment.LEFT);
		headerStyle.setWrapText(true);
		headerStyle.setFont(getFontForHeader(workbook));
		return headerStyle;
	}
	
	public static CellStyle getStyleForHeadingWithBorder(XSSFWorkbook workbook,Boolean value) {

		CellStyle headerStyle = workbook.createCellStyle();
		
		if (value!=null && value==true) {
			headerStyle.setBorderBottom(BorderStyle.THIN);
			headerStyle.setBorderTop(BorderStyle.THIN);
			headerStyle.setBorderLeft(BorderStyle.THIN);
			headerStyle.setBorderRight(BorderStyle.THIN);
			headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			headerStyle.setAlignment(HorizontalAlignment.LEFT);
		}else {
			headerStyle.setBorderBottom(BorderStyle.THIN);
			headerStyle.setFont(getFontForHeaderThematicView(workbook));
		}
		
		if(value==null) {
			headerStyle.setFont(getFontForHeader(workbook));
		}
		
		headerStyle.setWrapText(true);
		return headerStyle;
	}
	
	
	public static XSSFSheet doMergeThematicView(int rowIndex, int columnIndex, int rowSpan, int columnSpan, XSSFSheet sheet,boolean value) {

		Cell cell = sheet.getRow(rowIndex).getCell(rowSpan);
		CellRangeAddress range = new CellRangeAddress(rowIndex, columnIndex, rowSpan, columnSpan);

		sheet.addMergedRegion(range);

		if (value)
		RegionUtil.setBorderBottom(BorderStyle.THIN, range, sheet);

		RegionUtil.setBottomBorderColor(cell.getCellStyle().getBottomBorderColor(), range, sheet);
		RegionUtil.setTopBorderColor(cell.getCellStyle().getTopBorderColor(), range, sheet);
		RegionUtil.setLeftBorderColor(cell.getCellStyle().getLeftBorderColor(), range, sheet);
		RegionUtil.setRightBorderColor(cell.getCellStyle().getRightBorderColor(), range, sheet);

		return sheet;
	}
}
