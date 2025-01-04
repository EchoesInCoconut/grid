package com.example.grid.facade.impl;

import com.example.grid.entity.Cell;
import com.example.grid.entity.Column;
import com.example.grid.entity.Row;
import com.example.grid.entity.Table;
import com.example.grid.enumeration.CellSortEnum;
import com.example.grid.facade.GridFacade;
import com.example.grid.service.CellService;
import com.example.grid.service.ColumnService;
import com.example.grid.service.RowService;
import com.example.grid.service.TableService;
import com.example.grid.vo.Coordinate;
import com.example.grid.vo.CreateTableVo;
import com.example.grid.vo.GridVo;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GridFacadeImpl implements GridFacade {

    @Autowired
    TableService tableService;

    @Autowired
    ColumnService columnService;

    @Autowired
    RowService rowService;

    @Autowired
    CellService cellService;

    @Override
    public Long create(CreateTableVo createTableVo) {
        Table table = tableService.create(createTableVo.getName());
        // batch insert columns
        List<Column> columns = new ArrayList<>();
        for (int i = 0; i < createTableVo.getColumnSize(); i++) {
            Column column = new Column();
            column.setTableId(table.getId());
            column.setSort(new BigDecimal(String.valueOf(i)));
            columns.add(column);
        }
        // rewriteBatchedStatements=true helps concatenate batch insert sql
        columnService.saveBatch(columns);
        // batch insert rows
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < createTableVo.getRowSize(); i++) {
            Row row = new Row();
            row.setTableId(table.getId());
            row.setSort(new BigDecimal(String.valueOf(i)));
            rows.add(row);
        }
        rowService.saveBatch(rows);
        return table.getId();
    }

    @Override
    public GridVo getByTableId(Long tableId) {
        GridVo gridVo = new GridVo();
        // table
        Table table = tableService.getById(tableId);
        if (table == null) {
            return null;
        }
        gridVo.setTable(table);
        // columns ordered by sort
        List<Column> columns = columnService.getByTableId(tableId);
        gridVo.setColumns(columns);
        // rows
        List<Row> rows = this.getReorderedRows(columns, tableId);
        gridVo.setRows(rows);
        // cells
        List<Cell> cells = cellService.getByTableId(tableId);
        // [{columnId: cellValue, ...}, {columnId: cellValue, ...}, ...]
        // each map is a row
        List<Map<String, String>> cellsByRows = this.arrangeCellsIntoCellsByRows(cells, rows, columns);
        gridVo.setCellsByRows(cellsByRows);
        return gridVo;
    }

    /**
     * @return id of newly generated table
     */
    @Override
    public Long saveExcel(MultipartFile file, String tableName, Integer hasColumnHeaders) {
        try (InputStream inputStream = file.getInputStream()) {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            // generate table from Excel
            return this.convertExcelSheetToGrid(sheet, tableName, hasColumnHeaders == 1);
        } catch (IOException exception) {
            return null;
        }
    }

    /**
     * @return fileName
     */
    @Override
    public byte[] writeToExcel(Long tableId) {
        List<Column> columns = columnService.getByTableId(tableId);
        List<Row> rows = rowService.getByTableId(tableId);
        List<Cell> cells = cellService.getByTableId(tableId);
        Map<Coordinate, Cell> coordinateCellMap = new HashMap<>();
        for (Cell cell : cells) {
            coordinateCellMap.put(new Coordinate(cell.getRowId(), cell.getColumnId()), cell);
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            // column headers
            XSSFRow headers = sheet.createRow(0);
            int headerIndex = 0;
            for (Column column : columns) {
                XSSFCell header = headers.createCell(headerIndex++);
                header.setCellValue(column.getHeader());
            }
            // cells
            int rowIndex = 1;
            for (Row row : rows) {
                XSSFRow xlsRow = sheet.createRow(rowIndex++);
                int cellIndex = 0;
                for (Column column : columns) {
                    XSSFCell xlsCell = xlsRow.createCell(cellIndex++);
                    Cell cell = coordinateCellMap.get(new Coordinate(row.getId(), column.getId()));
                    xlsCell.setCellValue(cell == null ? null : cell.getValue());
                }
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            workbook.write(os);
            return os.toByteArray();
        } catch (IOException exception) {
            return null;
        }
    }

    private Long convertExcelSheetToGrid(XSSFSheet sheet, String tableName, Boolean hasColumnHeaders) {
        List<Row> rows = new ArrayList<>();
        List<Column> columns = new ArrayList<>();
        List<Cell> cells = new ArrayList<>();
        Map<Long, Row> sortToRow = new HashMap<>();
        Map<Long, Column> sortToColumn = new HashMap<>();
        // coordinate has sort of row as rowId, and sort of column as columnId
        Map<Coordinate, Cell> sortToCell = new HashMap<>();
        List<String> headerNames = new ArrayList<>();

        // getLastRowNum() returns the index (starts from 0) of the last row
        int lastRowNum = sheet.getLastRowNum();
        // if file is empty, getLastRowNum() returns -1
        // if file is empty, create a table where there is only one column and one row
        if (lastRowNum == -1) {
            CreateTableVo createTableVo = new CreateTableVo();
            createTableVo.setName(tableName);
            createTableVo.setRowSize(1);
            createTableVo.setColumnSize(1);
            return this.create(createTableVo);
        }
        Table table = tableService.create(tableName);
        for (int i = 0; i <= lastRowNum; i++) {
            // first condition:
            // if there are column headers, and file only contains one row (which will be identified as headers)
            if ((hasColumnHeaders && lastRowNum == 0) || !(hasColumnHeaders && i == 0)) {
                Row row = new Row();
                row.setTableId(table.getId());
                row.setSort(new BigDecimal(Integer.valueOf(i).toString()));
                rows.add(row);
                sortToRow.put((long) i, row);
            }
            // a blank row has no cell
            if (sheet.getRow(i) != null) {
                // getLastCellNum() returns the index (starts from 0) of the last cell PLUS ONE
                for (int j = 0; j < sheet.getRow(i).getLastCellNum(); j++) {
                    if (hasColumnHeaders && i == 0) {
                        headerNames.add(this.convertExcelCellValueToString(sheet.getRow(0).getCell(j)));
                    } else {
                        // create cell
                        Cell cell = new Cell();
                        cell.setValue(this.convertExcelCellValueToString(sheet.getRow(i).getCell(j)));
                        cells.add(cell);
                        sortToCell.put(new Coordinate((long) i, (long) j), cell);
                    }
                    // create column
                    if (!sortToColumn.containsKey((long) j)) {
                        Column column = new Column();
                        column.setTableId(table.getId());
                        column.setSort(new BigDecimal(Integer.valueOf(j).toString()));
                        columns.add(column);
                        sortToColumn.put((long) j, column);
                    }
                }
            }
        }
        this.assignNamesToHeaders(hasColumnHeaders, columns, headerNames);
        columnService.saveBatch(columns);
        rowService.saveBatch(rows);
        // replace fake rowId and columnId (sort) with real ones
        for (Coordinate coordinate : sortToCell.keySet()) {
            Cell cell = sortToCell.get(coordinate);
            cell.setColumnId(sortToColumn.get(coordinate.getColumnId()).getId());
            cell.setRowId(sortToRow.get(coordinate.getRowId()).getId());
            cell.setTableId(table.getId());
        }
        cellService.saveBatch(cells);
        return table.getId();
    }

    private void assignNamesToHeaders(Boolean hasColumnHeaders, List<Column> columns, List<String> headerNames) {
        if (hasColumnHeaders) {
            for (int i = 0; i < columns.size(); i++) {
                if (i < headerNames.size()) {
                    columns.get(i).setHeader(headerNames.get(i));
                }
            }
        }
    }

    private String convertExcelCellValueToString(XSSFCell xlsCell) {
        if (xlsCell == null) {
            return null;
        }
        switch (xlsCell.getCellType()) {
            case STRING:
                return xlsCell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(xlsCell)) {
                    DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
                    return df.format(xlsCell.getDateCellValue());
                }
                return String.valueOf(xlsCell.getNumericCellValue());
            default:
                return null;
        }
    }

    private List<Map<String, String>> arrangeCellsIntoCellsByRows(List<Cell> cells, List<Row> rows, List<Column> columns) {
        Map<Coordinate, Cell> coordinateCellMap = new HashMap<>();
        for (Cell cell : cells) {
            coordinateCellMap.put(new Coordinate(cell.getRowId(), cell.getColumnId()), cell);
        }
        List<Map<String, String>> cellsByRows = new ArrayList<>();
        for (Row row : rows) {
            // columnId: cell value
            Map<String, String> cellsByRow = new HashMap<>();
            for (Column column : columns) {
                Coordinate coordinate = new Coordinate(row.getId(), column.getId());
                if (coordinateCellMap.containsKey(coordinate)) {
                    // cell exists in database
                    cellsByRow.put(column.getId().toString(), coordinateCellMap.get(coordinate).getValue());
                } else {
                    // cell does not exist in database
                    cellsByRow.put(column.getId().toString(), null);
                }
            }
            cellsByRows.add(cellsByRow);
        }
        return cellsByRows;
    }

    private List<Row> getReorderedRows(List<Column> columns, Long tableId) {
        // check if any column has 1 or 2 as cellSort
        Optional<Column> optionalColumn = columns.stream()
                .filter(column -> !Objects.equals(column.getCellSort(), CellSortEnum.NONE.value))
                .findAny();
        if (optionalColumn.isPresent()) {
            Column reorderedColumn = optionalColumn.get();
            List<Cell> cellsToBeReordered = cellService.getByColumnId(reorderedColumn.getId());
            if (CollectionUtils.isEmpty(cellsToBeReordered)) {
                return rowService.getByTableId(tableId);
            }
            List<Cell> reorderedCells;
            if (Objects.equals(reorderedColumn.getCellSort(), CellSortEnum.ASC.value)) {
                // sort asc
                reorderedCells = cellsToBeReordered.stream()
                        .sorted(Comparator.comparing(Cell::getValue, Comparator.nullsLast(Comparator.naturalOrder())))
                        .collect(Collectors.toList());
            } else {
                // sort desc
                reorderedCells = cellsToBeReordered.stream()
                        .sorted(Comparator.comparing(Cell::getValue, Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
                        .collect(Collectors.toList());
            }
            List<Long> reorderedRowIds = reorderedCells.stream().map(Cell::getRowId).collect(Collectors.toList());
            // reorder rows according to reorderedRowIds
            return rowService.getByTableIdAndReorderedRowIds(reorderedRowIds, tableId);
        } else {
            // ordered by sort by default
            return rowService.getByTableId(tableId);
        }
    }
}
