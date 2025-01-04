package com.example.grid.vo;

import lombok.Data;

@Data
public class UpdateCellVo {
    private Long tableId;
    private Long rowId;
    private Long columnId;
    private String value;
}
