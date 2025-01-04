package com.example.grid.vo;

import lombok.Data;

@Data
public class MoveVo {
    // id of moved column/row
    private Long id;
    // id of left column/above row
    private Long prevId;
    // id of right column/below row
    private Long afterId;
}
