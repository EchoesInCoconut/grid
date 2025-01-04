package com.example.grid.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grid.entity.Cell;
import com.example.grid.vo.UpdateCellVo;

import java.util.List;

public interface CellService extends IService<Cell> {
    Cell insertOrUpdate(UpdateCellVo updateCellVo);

    List<Cell> getByColumnId(Long columnId);

    List<Cell> getByTableId(Long tableId);
}
