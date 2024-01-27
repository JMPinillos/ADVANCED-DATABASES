package com.unir.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Municipio {
    private String id;
    private String nombre;
    private Provincia provincia;

    /**
     * Comprueba si existe el municipio en la lista de municipios
     * @param municipios
     * @return Municipio
     */
    public Municipio AgregarSiNoExiste(List<Municipio> municipios) {
    	// comprobar si existe el municipio
        for (Municipio municipio : municipios) {
            if (municipio.getNombre().equals(this.getNombre())) {
                return municipio;
            }
        }

        municipios.add(this);
        return  this;
    }
}
