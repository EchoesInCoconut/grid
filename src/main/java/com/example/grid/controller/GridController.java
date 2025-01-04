package com.example.grid.controller;

import com.example.grid.entity.Cell;
import com.example.grid.entity.Column;
import com.example.grid.entity.Row;
import com.example.grid.entity.Table;
import com.example.grid.enumeration.GridExceptionEnum;
import com.example.grid.facade.GridFacade;
import com.example.grid.service.CellService;
import com.example.grid.service.ColumnService;
import com.example.grid.service.RowService;
import com.example.grid.service.TableService;
import com.example.grid.vo.CreateTableVo;
import com.example.grid.vo.DeleteVo;
import com.example.grid.vo.GridVo;
import com.example.grid.vo.InsertVo;
import com.example.grid.vo.MoveVo;
import com.example.grid.vo.ReorderCellsVo;
import com.example.grid.vo.UpdateCellVo;
import com.example.grid.vo.UpdateColumnVo;
import com.example.grid.vo.UpdateTableNameVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/grid")
@CrossOrigin
public class GridController {
    @Autowired
    TableService tableService;

    @Autowired
    ColumnService columnService;

    @Autowired
    RowService rowService;

    @Autowired
    CellService cellService;

    @Autowired
    GridFacade gridFacade;

    @GetMapping("/all")
    ResponseEntity<List<Table>> getAllTables() {
        return ResponseEntity.ok().body(tableService.getAll());
    }

    @PostMapping("/create")
    ResponseEntity<Long> create(@RequestBody CreateTableVo createTableVo) {
        Long tableId = gridFacade.create(createTableVo);
        return ResponseEntity.ok().body(tableId);
    }

    @GetMapping("/delete/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        tableService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get/{id}")
    ResponseEntity<?> getByTableId(@PathVariable Long id) {
        GridVo gridVo = gridFacade.getByTableId(id);
        if (gridVo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GridExceptionEnum.NO_TABLE);
        }
        return ResponseEntity.ok().body(gridVo);
    }

    @PostMapping("/move/column")
    ResponseEntity<Column> moveColumn(@RequestBody MoveVo moveVo) {
        Column updatedColumn = columnService.move(moveVo);
        return ResponseEntity.ok().body(updatedColumn);
    }

    @PostMapping("/move/row")
    ResponseEntity<Row> moveRow(@RequestBody MoveVo moveVo) {
        Row updatedRow = rowService.move(moveVo);
        return ResponseEntity.ok().body(updatedRow);
    }

    @PostMapping("/insert/column")
    ResponseEntity<Column> insertColumn(@RequestBody InsertVo insertVo) {
        Column insertedColumn = columnService.insert(insertVo);
        return ResponseEntity.ok().body(insertedColumn);
    }

    @PostMapping("/insert/row")
    ResponseEntity<Row> insertRow(@RequestBody InsertVo insertVo) {
        Row insertedRow = rowService.insert(insertVo);
        return ResponseEntity.ok().body(insertedRow);
    }

    @PostMapping("/delete/column")
    ResponseEntity<Void> deleteColumn(@RequestBody DeleteVo deleteVo) {
        columnService.delete(deleteVo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete/row")
    ResponseEntity<Void> deleteRow(@RequestBody DeleteVo deleteVo) {
        rowService.delete(deleteVo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update/cell")
    ResponseEntity<Cell> updateCell(@RequestBody UpdateCellVo updateCellVo) {
        Cell updatedCell = cellService.insertOrUpdate(updateCellVo);
        return ResponseEntity.ok().body(updatedCell);
    }

    @PostMapping("/update/column")
    ResponseEntity<Void> updateColumn(@RequestBody UpdateColumnVo updateColumnVo) {
        columnService.updateColumn(updateColumnVo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reorder/cells-in-column")
    ResponseEntity<Void> reorderCells(@RequestBody ReorderCellsVo reorderCellsVo) {
        columnService.reorderCells(reorderCellsVo);
        return ResponseEntity.ok().build();
    }

    /**
     * starts from Excel cell A1
     */
    @PostMapping("/import/excel")
    ResponseEntity<Long> importExcel(@RequestPart MultipartFile file, @RequestParam Integer hasColumnHeaders) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        int i = file.getOriginalFilename().lastIndexOf(".");
        Long tableId = gridFacade.saveExcel(file, uuid + "_" + file.getOriginalFilename().substring(0, i), hasColumnHeaders);
        if (tableId == null) {
            // todo: handle exception
        }
        return ResponseEntity.ok().body(tableId);
    }

    @PostMapping("/update/table-name")
    ResponseEntity<?> updateTableName(@RequestBody UpdateTableNameVo updateTableNameVo) {
        tableService.updateTableName(updateTableNameVo);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/get/table-name/{id}")
    ResponseEntity<?> getTableNameById(@PathVariable Long id) {
        Table table = tableService.getById(id);
        if (table == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GridExceptionEnum.NO_TABLE);
        }
        return ResponseEntity.ok().body(table);
    }

    @GetMapping(value = "/export/excel/{id}")
    ResponseEntity<?> importToExcel(@PathVariable Long id) {
        byte[] bytes = gridFacade.writeToExcel(id);
        if (bytes == null) {
            // todo: handle exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GridExceptionEnum.DOWNLOAD_FAILURE);
        }
        HttpHeaders headers = new HttpHeaders();
        // application/ms-excel
        headers.setContentType(MediaType.parseMediaType("application/octet-stream"));
        headers.setContentDispositionFormData("attachment", tableService.getById(id).getName() + ".xlsx");
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
