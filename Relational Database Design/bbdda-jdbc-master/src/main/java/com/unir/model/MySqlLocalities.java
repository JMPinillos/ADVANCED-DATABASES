package com.unir.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MySqlLocalities {
    private int loc_id;
    private int mun_id;
    private String name;
}
