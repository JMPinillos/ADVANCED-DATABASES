package com.unir.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MySqlPrices {
    private int f_est_id;
    private int fuel_id;
    private String price;
}
