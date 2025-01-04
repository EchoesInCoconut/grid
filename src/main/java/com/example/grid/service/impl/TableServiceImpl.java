package com.example.grid.service.impl;

import com.example.grid.entity.Table;
import com.example.grid.mapper.TableMapper;
import com.example.grid.service.TableService;
import com.example.grid.vo.UpdateTableNameVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TableServiceImpl implements TableService {

    @Autowired
    TableMapper tableMapper;

    @Override
    public Table create(String name) {
        Table table = new Table();
        table.setName(name);
        tableMapper.insert(table);
        return table;
    }

    @Override
    public Table getById(Long id) {
        return tableMapper.selectById(id);
    }

    @Override
    public List<Table> getAll() {
        return tableMapper.selectList(null);
    }

    @Override
    public void delete(Long id) {
        // cascade delete: rows, columns, and cells will be deleted
        tableMapper.deleteById(id);
    }

    @Override
    public void updateTableName(UpdateTableNameVo updateTableNameVo) {
        Table table = new Table();
        table.setId(updateTableNameVo.getTableId());
        table.setName(updateTableNameVo.getTableName());
        tableMapper.updateById(table);
    }
}
