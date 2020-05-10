package com.bookstore.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum  StockStatementType {
    STORAGE(0,"入库"),
    PUTS_IN_STORAGE(1,"出库")

    ;
    private Integer type;
    private String msg;
}
