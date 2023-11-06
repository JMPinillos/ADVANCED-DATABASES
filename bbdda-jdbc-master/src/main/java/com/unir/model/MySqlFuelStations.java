package com.unir.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MySqlFuelStations {
    private int station_id;
    private String name;
    private String cp;
    private float longitud;
    private float latitud;
    private int loc_id;
    private String margen;
    private String address;
    private DateTime price_date;
    private String horario;
    private int op_id;
}
