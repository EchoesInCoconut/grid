package com.example.grid.enumeration;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum GridExceptionEnum {
    NO_TABLE(0, "The table is not found"),
    NO_COLUMN(1, "The column is not found"),
    NO_ROW(2, "The row is not found"),
    DOWNLOAD_FAILURE(3, "Download fails");

    private final Integer value;
    private final String message;

    GridExceptionEnum(Integer value, String message) {
        this.value = value;
        this.message = message;
    }

}
