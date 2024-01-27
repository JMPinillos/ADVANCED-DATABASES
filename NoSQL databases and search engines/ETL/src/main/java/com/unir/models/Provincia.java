package com.unir.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Provincia {
    private String id;
    private String nombre;

    /**
     * Comprueba si existe la provincia en la lista de provincias
     * @param provincias
     * @return Provincia
     */
    public Provincia AgregarSiNoExiste(List<Provincia> provincias) {
        // comprueba si existe la provincia en la lista de provincias y si no existe se añade
        for (Provincia provincia : provincias) {
            if (provincia.getNombre().equals(this.getNombre())) {
                return provincia;
            }
        }

        provincias.add(this);

        return  this;

    }
}
