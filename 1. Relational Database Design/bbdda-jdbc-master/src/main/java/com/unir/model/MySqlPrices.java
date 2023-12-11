package com.unir.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MySqlPrices {
    private int st_id;
    private int fuel_id;
    private Float price;
}
