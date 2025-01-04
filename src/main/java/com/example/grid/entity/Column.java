package com.example.grid.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("grid.column")
public class Column {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tableId;
    private BigDecimal sort;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String header;
    // 0: no sort
    // 1: sort asc
    // 2: sort desc
    // only one of all the columns can have 1 or 2 as cellSort, others 0
    private Integer cellSort;
}
