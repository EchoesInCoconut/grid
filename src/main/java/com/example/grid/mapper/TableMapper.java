package com.example.grid.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grid.entity.Table;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TableMapper extends BaseMapper<Table> {
}
