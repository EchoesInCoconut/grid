package com.example.grid.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.grid.constant.GridConstant;
import com.example.grid.entity.Column;
import com.example.grid.enumeration.CellSortEnum;
import com.example.grid.mapper.ColumnMapper;
import com.example.grid.service.ColumnService;
import com.example.grid.vo.DeleteVo;
import com.example.grid.vo.InsertVo;
import com.example.grid.vo.MoveVo;
import com.example.grid.vo.ReorderCellsVo;
import com.example.grid.vo.UpdateColumnVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ColumnServiceImpl extends ServiceImpl<ColumnMapper, Column> implements ColumnService {
    @Autowired
    ColumnMapper columnMapper;

    @Override
    public Column move(MoveVo moveVo) {
        BigDecimal newSort;
        if (moveVo.getPrevId() != null && moveVo.getAfterId() != null) {
            BigDecimal prevSort = columnMapper.selectById(moveVo.getPrevId()).getSort();
            BigDecimal afterSort = columnMapper.selectById(moveVo.getAfterId()).getSort();
            newSort = prevSort.add(afterSort).divide(new BigDecimal("2"), GridConstant.SCALE, RoundingMode.HALF_UP);
        } else {
            newSort = moveVo.getPrevId() != null ?
                    columnMapper.selectById(moveVo.getPrevId()).getSort().add(BigDecimal.ONE) :
                    columnMapper.selectById(moveVo.getAfterId()).getSort().subtract(BigDecimal.ONE);
        }
        // todo: if prevId and afterId are both null
        Column column = columnMapper.selectById(moveVo.getId());
        column.setSort(newSort);
        // todo: no tableId
        columnMapper.updateById(column);
        return column;
    }

    @Override
    public Column insert(InsertVo insertVo) {
        BigDecimal sort;
        if (insertVo.getPrevId() != null && insertVo.getAfterId() != null) {
            Column prevColumn = columnMapper.selectById(insertVo.getPrevId());
            BigDecimal afterSort = columnMapper.selectById(insertVo.getAfterId()).getSort();
            sort = prevColumn.getSort().add(afterSort).divide(new BigDecimal("2"), GridConstant.SCALE, RoundingMode.HALF_UP);
        } else {
            Column neigbourColumn = insertVo.getPrevId() != null ?
                    columnMapper.selectById(insertVo.getPrevId()) : columnMapper.selectById(insertVo.getAfterId());
            sort = insertVo.getPrevId() != null ? neigbourColumn.getSort().add(BigDecimal.ONE) : neigbourColumn.getSort().subtract(BigDecimal.ONE);
        }
        Column column = new Column();
        column.setTableId(insertVo.getTableId());
        column.setSort(sort);
        columnMapper.insert(column);
        return column;
    }

    @Override
    public void delete(DeleteVo deleteVo) {
        List<Column> columns = columnMapper.selectList(new QueryWrapper<Column>().eq(GridConstant.TABLE_ID, deleteVo.getTableId()));
        if (CollectionUtils.isEmpty(columns) || columns.size() == 1) {
            // todo: when there is no column or only one column left
        }
        // cascade delete: cells will be deleted
        columnMapper.deleteById(deleteVo.getId());
    }

    @Override
    public List<Column> getByTableId(Long tableId) {
        return columnMapper.selectList(new QueryWrapper<Column>().eq(GridConstant.TABLE_ID, tableId).orderByAsc(GridConstant.SORT));
    }

    @Override
    public void updateColumn(UpdateColumnVo updateColumnVo) {
        Column column = columnMapper.selectById(updateColumnVo.getId());
        column.setHeader(updateColumnVo.getHeader());
        columnMapper.updateById(column);
    }

    @Override
    public void reorderCells(ReorderCellsVo reorderCellsVo) {
        List<Column> columns = new ArrayList<>();
        // make sure only one column can have 1 or 2 as cellSort
        if (!Objects.equals(reorderCellsVo.getCellSort(), CellSortEnum.NONE.value)) {
            columns = columnMapper.selectList(new QueryWrapper<Column>().ne(GridConstant.CELL_SORT, CellSortEnum.NONE.value));
            columns.forEach(column -> column.setCellSort(CellSortEnum.NONE.value));
        }

        Column column = columnMapper.selectById(reorderCellsVo.getColumnId());
        column.setCellSort(reorderCellsVo.getCellSort());
        columns.add(column);
        columnMapper.updateById(columns);
    }
}
