package com.example.grid.facade;

import com.example.grid.vo.CreateTableVo;
import com.example.grid.vo.GridVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface GridFacade {
    Long create(CreateTableVo createTableVo);
    GridVo getByTableId(Long tableId);
    Long saveExcel(MultipartFile file, String tableName, Integer hasColumnHeaders);
    byte[] writeToExcel(Long tableId);
}
