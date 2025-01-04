package com.example.grid.enumeration;

public enum CellSortEnum {
    NONE(0),
    ASC(1),
    DESC(2);

    public final Integer value;

    CellSortEnum(Integer value) {
        this.value = value;
    }
}
