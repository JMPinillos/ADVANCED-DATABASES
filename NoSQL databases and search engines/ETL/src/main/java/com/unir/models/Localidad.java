package com.unir.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Localidad {
    private String id;
    private String nombre;
    private Provincia provincia;
    private Municipio municipio;

    /**
     * Comprueba si existe la localidad en la lista de localidades
     * @param localidades
     * @return Localidad
     */
    public Localidad AgregarSiNoExiste(List<Localidad> localidades) {
    	// comprobar si existe la localidad
        for (Localidad localidad : localidades) {
            if (localidad.getNombre().equals(this.getNombre())) {
                return localidad;
            }
        }

        // si no existe la añadimos a la lista de localidades
        localidades.add(this);
        return this;
    }
}
