package com.example.grid.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grid.entity.Row;
import com.example.grid.vo.DeleteVo;
import com.example.grid.vo.InsertVo;
import com.example.grid.vo.MoveVo;

import java.util.List;

public interface RowService extends IService<Row> {
    Row move(MoveVo moveVo);

    Row insert(InsertVo insertVo);

    void delete(DeleteVo deleteVo);

    List<Row> getByTableId(Long tableId);

    List<Row> getByTableIdAndReorderedRowIds(List<Long> rowIds, Long tableId);
}
