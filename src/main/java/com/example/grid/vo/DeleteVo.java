package com.example.grid.vo;

import lombok.Data;

@Data
public class DeleteVo {
    private Long tableId;
    // columnId or rowId
    private Long id;
}
