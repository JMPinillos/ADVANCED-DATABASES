package com.unir.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Precio {
    private Estacion estacion;
    private Carburante carburante;
    private Double precio;
}
