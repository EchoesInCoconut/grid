package com.example.grid.vo;

import lombok.Data;

@Data
public class InsertVo {
    // id of left column/above row
    private Long prevId;
    // id of right column/below row
    private Long afterId;
    private Long tableId;
}
