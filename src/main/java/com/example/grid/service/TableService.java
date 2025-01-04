package com.example.grid.service;


import com.example.grid.entity.Table;
import com.example.grid.vo.UpdateTableNameVo;

import java.util.List;

public interface TableService {
    Table create(String name);

    Table getById(Long id);

    List<Table> getAll();

    void delete(Long id);

    void updateTableName(UpdateTableNameVo updateTableNameVo);
}
