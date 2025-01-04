package com.example.grid.vo;

import com.example.grid.entity.Cell;
import com.example.grid.entity.Column;
import com.example.grid.entity.Row;
import com.example.grid.entity.Table;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GridVo {
    Table table;
    List<Column> columns;
    List<Row> rows;
    List<Map<String, String>> cellsByRows;
}
