package com.example.grid.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.grid.constant.GridConstant;
import com.example.grid.entity.Cell;
import com.example.grid.mapper.CellMapper;
import com.example.grid.service.CellService;
import com.example.grid.vo.UpdateCellVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CellServiceImpl extends ServiceImpl<CellMapper, Cell> implements CellService {

    @Autowired
    CellMapper cellMapper;

    @Override
    public Cell insertOrUpdate(UpdateCellVo updateCellVo) {
        Cell cell = cellMapper.selectOne(
                new QueryWrapper<Cell>()
                        .eq(GridConstant.ROW_ID, updateCellVo.getRowId())
                        .eq(GridConstant.COLUMN_ID, updateCellVo.getColumnId()));
        if (cell == null) {
            cell = new Cell();
            BeanUtils.copyProperties(updateCellVo, cell);
        } else {
            cell.setValue(updateCellVo.getValue());
        }
        cellMapper.insertOrUpdate(cell);
        return cell;
    }

    @Override
    public List<Cell> getByColumnId(Long columnId) {
        return cellMapper.selectList(new QueryWrapper<Cell>().eq(GridConstant.COLUMN_ID, columnId));
    }

    @Override
    public List<Cell> getByTableId(Long tableId) {
        return cellMapper.selectList(new QueryWrapper<Cell>().eq(GridConstant.TABLE_ID, tableId));
    }
}
