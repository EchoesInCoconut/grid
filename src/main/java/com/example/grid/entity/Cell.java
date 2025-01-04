package com.example.grid.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@TableName("grid.cell")
public class Cell {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long rowId;
    private Long columnId;
    private Long tableId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String value;
}
