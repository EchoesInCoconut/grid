package com.example.grid.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.grid.constant.GridConstant;
import com.example.grid.entity.Row;
import com.example.grid.mapper.RowMapper;
import com.example.grid.service.RowService;
import com.example.grid.vo.DeleteVo;
import com.example.grid.vo.InsertVo;
import com.example.grid.vo.MoveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class RowServiceImpl extends ServiceImpl<RowMapper, Row> implements RowService {
    @Autowired
    RowMapper rowMapper;

    @Override
    public Row move(MoveVo moveVo) {
        BigDecimal newSort;
        if (moveVo.getPrevId() != null && moveVo.getAfterId() != null) {
            BigDecimal prevSort = rowMapper.selectById(moveVo.getPrevId()).getSort();
            BigDecimal afterSort = rowMapper.selectById(moveVo.getAfterId()).getSort();
            newSort = prevSort.add(afterSort).divide(new BigDecimal("2"), GridConstant.SCALE, RoundingMode.HALF_UP);
        } else {
            newSort = moveVo.getPrevId() != null ?
                    rowMapper.selectById(moveVo.getPrevId()).getSort().add(BigDecimal.ONE) :
                    rowMapper.selectById(moveVo.getAfterId()).getSort().subtract(BigDecimal.ONE);
        }
        Row row = new Row();
        row.setId(moveVo.getId());
        row.setSort(newSort);
        rowMapper.updateById(row);
        return row;
    }

    @Override
    public Row insert(InsertVo insertVo) {
        BigDecimal sort;
        if (insertVo.getPrevId() != null && insertVo.getAfterId() != null) {
            Row prevRow = rowMapper.selectById(insertVo.getPrevId());
            BigDecimal afterSort = rowMapper.selectById(insertVo.getAfterId()).getSort();
            sort = prevRow.getSort().add(afterSort).divide(new BigDecimal("2"), GridConstant.SCALE, RoundingMode.HALF_UP);
        } else {
            Row neigbourRow = insertVo.getPrevId() != null ?
                    rowMapper.selectById(insertVo.getPrevId()) : rowMapper.selectById(insertVo.getAfterId());
            sort = insertVo.getPrevId() != null ? neigbourRow.getSort().add(BigDecimal.ONE) : neigbourRow.getSort().subtract(BigDecimal.ONE);
        }
        Row row = new Row();
        row.setTableId(insertVo.getTableId());
        row.setSort(sort);
        rowMapper.insert(row);
        return row;
    }

    @Override
    public void delete(DeleteVo deleteVo) {
        List<Row> rows = rowMapper.selectList(new QueryWrapper<Row>().eq(GridConstant.TABLE_ID, deleteVo.getTableId()));
        if (CollectionUtils.isEmpty(rows) || rows.size() == 1) {
            // todo: when there is no row or only one row left
        }
        // cascade delete: cells will be deleted
        rowMapper.deleteById(deleteVo.getId());
    }

    @Override
    public List<Row> getByTableId(Long tableId) {
        return rowMapper.selectList(new QueryWrapper<Row>().eq(GridConstant.TABLE_ID, tableId).orderByAsc(GridConstant.SORT));
    }

    @Override
    public List<Row> getByTableIdAndReorderedRowIds(List<Long> rowIds, Long tableId) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("field(id");
        // place rows where no cell is created at last
        Collections.reverse(rowIds);
        for (Long rowId : rowIds) {
            strBuilder.append(", ").append(rowId);
        }
        strBuilder.append(")");
        // where table_id = {tableId} order by field(id, 1, 2, ...)
        return rowMapper.selectList(
                new QueryWrapper<Row>()
                        .eq(GridConstant.TABLE_ID, tableId)
                        .orderByDesc(strBuilder.toString()));
    }
}
