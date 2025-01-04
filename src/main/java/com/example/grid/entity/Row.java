package com.example.grid.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("grid.row")
public class Row {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tableId;
    private BigDecimal sort;
}
