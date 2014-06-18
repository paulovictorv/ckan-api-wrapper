/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package util;


import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;

/**
 * Demonstrates <em>one</em> way to convert an Excel spreadsheet into a CSV
 * file. This class makes the following assumptions;
 * <list>
 * <li>1. Where the Excel workbook contains more that one worksheet, then a single
 *    CSV file will contain the data from all of the worksheets.</li>
 * <li>2. The data matrix contained in the CSV file will be square. This means that
 *    the number of fields in each record of the CSV file will match the number
 *    of cells in the longest row found in the Excel workbook. Any short records
 *    will be 'padded' with empty fields - an empty field is represented in the
 *    the CSV file in this way - ,,.</li>
 * <li>3. Empty fields will represent missing cells.</li>
 * <li>4. A record consisting of empty fields will be used to represent an empty row
 *    in the Excel workbook.</li>
 * </list>
 * Therefore, if the worksheet looked like this;
 *
 * <pre>
 *  ___________________________________________
 *     |       |       |       |       |       |
 *     |   A   |   B   |   C   |   D   |   E   |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   1 |   1   |   2   |   3   |   4   |   5   |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   2 |       |       |       |       |       |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   3 |       |   A   |       |   B   |       |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   4 |       |       |       |       |   Z   |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   5 | 1,400 |       |  250  |       |       |
 *  ___|_______|_______|_______|_______|_______|
 *
 * </pre>
 *
 * Then, the resulting CSV file will contain the following lines (records);
 * <pre>
 * 1,2,3,4,5
 * ,,,,
 * ,A,,B,
 * ,,,,Z
 * "1,400",,250,,
 * </pre><p>
 * Typically, the comma is used to separate each of the fields that, together,
 * constitute a single record or line within the CSV file. This is not however
 * a hard and fast rule and so this class allows the user to determine which
 * character is used as the field separator and assumes the comma if none other
 * is specified.
 * </p><p>
 * If a field contains the separator then it will be escaped. If the file should
 * obey Excel's CSV formatting rules, then the field will be surrounded with
 * speech marks whilst if it should obey UNIX conventions, each occurrence of
 * the separator will be preceded by the backslash character.
 * </p><p>
 * If a field contains an end of line (EOL) character then it too will be
 * escaped. If the file should obey Excel's CSV formatting rules then the field
 * will again be surrounded by speech marks. On the other hand, if the file
 * should follow UNIX conventions then a single backslash will precede the
 * EOL character. There is no single applicable standard for UNIX and some
 * appications replace the CR with \r and the LF with \n but this class will
 * not do so.
 * </p><p>
 * If the field contains double quotes then that character will be escaped. It
 * seems as though UNIX does not define a standard for this whilst Excel does.
 * Should the CSV file have to obey Excel's formmating rules then the speech
 * mark character will be escaped with a second set of speech marks. Finally, an
 * enclosing set of speah marks will also surround the entire field. Thus, if
 * the following line of text appeared in a cell - "Hello" he said - it would
 * look like this when converted into a field within a CSV file - """Hello"" he
 * said".
 * </p><p>
 * Finally, it is worth noting that talk of CSV 'standards' is really slightly
 * missleading as there is no such thing. It may well be that the code in this
 * class has to be modified to produce files to suit a specific application
 * or requirement.
 * </p>
 * @author Mark B
 * @version 1.00 9th April 2010
 *          1.10 13th April 2010 - Added support for processing all Excel
 *                                 workbooks in a folder along with the ability
 *                                 to specify a field separator character.
 *          2.00 14th April 2010 - Added support for embedded characters; the
 *                                 field separator, EOL and double quotes or
 *                                 speech marks. In addition, gave the client
 *                                 the ability to select how these are handled,
 *                                 either obeying Excel's or UNIX formatting
 *                                 conventions.
 */
public class ToCSV {

    private Workbook workbook = null;
    private ArrayList<ArrayList> csvData = null;
    private int maxRowWidth = 0;
    private int formattingConvention = 0;
    private DataFormatter formatter = null;
    private FormulaEvaluator evaluator = null;
    private String separator = null;

    private static final String DEFAULT_SEPARATOR = ",";

    public static final int EXCEL_STYLE_ESCAPING = 0;

    public static final int UNIX_STYLE_ESCAPING = 1;

    public InputStream convertToCSV(InputStream xlsStream) throws IOException, InvalidFormatException {
        this.separator = DEFAULT_SEPARATOR;
        this.openWorkbook(xlsStream);
        this.convertToCSV();
        String s = this.outputCSVString();
        return new ByteArrayInputStream(s.getBytes("UTF-8"));
    }

    private String outputCSVString() throws IOException {
        StringWriter writer = new StringWriter();
        this.writeCSV(writer);
        writer.flush();
        writer.close();
        return writer.toString();
    }

    private void openWorkbook(InputStream stream) throws IOException, InvalidFormatException {
        try {
            this.workbook = WorkbookFactory.create(stream);
            this.evaluator = this.workbook.getCreationHelper().createFormulaEvaluator();
            this.formatter = new DataFormatter(true);
        }
        finally {
            if(stream != null) {
                stream.close();
            }
        }
    }

    private void convertToCSV() {
        Sheet sheet = null;
        Row row = null;
        int lastRowNum = 0;
        this.csvData = new ArrayList<ArrayList>();

        int numSheets = this.workbook.getNumberOfSheets();

        for(int i = 0; i < numSheets; i++) {

            sheet = this.workbook.getSheetAt(i);
            if(sheet.getPhysicalNumberOfRows() > 0) {
                lastRowNum = sheet.getLastRowNum();
                for(int j = 0; j <= lastRowNum; j++) {
                    row = sheet.getRow(j);
                    this.rowToCSV(row);
                }
            }
        }
    }


    private void writeCSV(Writer writer) throws IOException {
        BufferedWriter bw = null;
        ArrayList<String> line = null;
        StringBuffer buffer = null;
        String csvLineElement = null;
        try {
            bw = new BufferedWriter(writer);
            for(int i = 0; i < this.csvData.size(); i++) {
                buffer = new StringBuffer();
                line = this.csvData.get(i);
                for(int j = 0; j < this.maxRowWidth; j++) {
                    if(line.size() > j) {
                        csvLineElement = line.get(j);
                        if(csvLineElement != null) {
                            buffer.append(this.escapeEmbeddedCharacters(
                                    csvLineElement));
                        }
                    }
                    if(j < (this.maxRowWidth - 1)) {
                        buffer.append(this.separator);
                    }
                }

                bw.write(buffer.toString().trim());

                if(i < (this.csvData.size() - 1)) {
                    bw.newLine();
                }
            }
        }
        finally {
            if(bw != null) {
                bw.flush();
                bw.close();
            }
        }
    }

    private void rowToCSV(Row row) {
        Cell cell = null;
        int lastCellNum = 0;
        ArrayList<String> csvLine = new ArrayList<String>();

        if(row != null) {
            lastCellNum = row.getLastCellNum();
            for(int i = 0; i <= lastCellNum; i++) {
                cell = row.getCell(i);
                if(cell == null) {
                    csvLine.add("");
                }
                else {
                    if(cell.getCellType() != Cell.CELL_TYPE_FORMULA) {
                        csvLine.add(this.formatter.formatCellValue(cell));
                    }
                    else {
                        csvLine.add(this.formatter.formatCellValue(cell, this.evaluator));
                    }
                }
            }
            if(lastCellNum > this.maxRowWidth) {
                this.maxRowWidth = lastCellNum;
            }
        }
        this.csvData.add(csvLine);
    }

   private String escapeEmbeddedCharacters(String field) {
        StringBuffer buffer = null;

        if(this.formattingConvention == ToCSV.EXCEL_STYLE_ESCAPING) {

            if(field.contains("\"")) {
                buffer = new StringBuffer(field.replaceAll("\"", "\\\"\\\""));
                buffer.insert(0, "\"");
                buffer.append("\"");
            }
            else {
                buffer = new StringBuffer(field);
                if((buffer.indexOf(this.separator)) > -1 ||
                        (buffer.indexOf("\n")) > -1) {
                    buffer.insert(0, "\"");
                    buffer.append("\"");
                }
            }
            return(buffer.toString().trim());
        } else {
            if(field.contains(this.separator)) {
                field = field.replaceAll(this.separator, ("\\\\" + this.separator));
            }
            if(field.contains("\n")) {
                field = field.replaceAll("\n", "\\\\\n");
            }
            return(field);
        }
    }
}