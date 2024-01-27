package com.unir.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Carburante {
    private String id;
    private String nombre;

    /**
     * Comprueba si existe la provincia en la lista de provincias
     * @param carburantes Lista de carburantes
     * @return Carburante
     */
    public Carburante AgregarSiNoExiste(List<Carburante> carburantes) {
        // comprueba si existe la provincia en la lista de provincias
        for (Carburante carburante : carburantes) {
            if (carburante.getId().equals(this.getId())) {
                return carburante;
            }
        }

        carburantes.add(this);
        return this;

    }
}
