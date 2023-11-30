package com.unir.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Date;

@AllArgsConstructor
@Getter
public class MySqlStations {
    private int station_id;
    private int loc_id;
    private int op_id;
    private String cp;
    private String address;
    private String margen;
    private Float longitud;
    private Float latitud;
    private Date price_date;
    private String tipo;
    private String horario;


}
