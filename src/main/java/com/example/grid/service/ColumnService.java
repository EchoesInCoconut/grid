package com.example.grid.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grid.entity.Column;
import com.example.grid.vo.DeleteVo;
import com.example.grid.vo.InsertVo;
import com.example.grid.vo.MoveVo;
import com.example.grid.vo.ReorderCellsVo;
import com.example.grid.vo.UpdateColumnVo;

import java.util.List;

public interface ColumnService extends IService<Column> {
    Column move(MoveVo moveVo);

    Column insert(InsertVo insertVo);

    void delete(DeleteVo deleteVo);

    List<Column> getByTableId(Long tableId);

    void updateColumn(UpdateColumnVo updateColumnVo);

    void reorderCells(ReorderCellsVo reorderCellsVo);
}
