package com.unir.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@AllArgsConstructor
@Getter
public class MySqlStations {
    private int station_id;
    private int loc_id;
    private int op_id;
    private int fuel_id;
    private int cp;
    private float longitud;
    private float latitud;
    private enum margen {D,I,N};
    private String address;
    private Date price_date;
    private String horario;
    private enum tipo {P,M};

}
