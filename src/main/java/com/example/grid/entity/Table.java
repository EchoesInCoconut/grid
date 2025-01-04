package com.example.grid.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("grid.table")
public class Table {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
}
