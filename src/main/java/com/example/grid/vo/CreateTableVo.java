package com.example.grid.vo;

import lombok.Data;

@Data
public class CreateTableVo {
    private String name;
    private Integer rowSize;
    private Integer columnSize;
}
