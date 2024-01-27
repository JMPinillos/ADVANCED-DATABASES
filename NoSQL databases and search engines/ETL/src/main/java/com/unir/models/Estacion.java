package com.unir.models;

import com.unir.types.TipoMargen;
import com.unir.types.TipoEstacion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Date;



@AllArgsConstructor
@Getter
public class Estacion {
    private String id;
    private Rotulo rotulo;
    private BigDecimal longitud;
    private BigDecimal latitud;
    private String codigoPostal;
    private String direccion;
    private Localidad localidad;
    private TipoMargen tipoMargen;
    private String horario;
    private Date fecha;
    private TipoEstacion tipoEstacion;
    private TipoVenta tipoVenta;

}


