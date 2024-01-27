package com.unir.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Rotulo {
    private String id;
    private String nombre;

    /**
     * Comprueba si existe la provincia en la lista de rótulos
     * @param rotulos
     * @return Rotulo
     */
    public Rotulo AgregarSiNoExiste(List<Rotulo> rotulos) {
        // comprueba si existe la provincia en la lista de rotulos y si no existe se añade
        for (Rotulo rotulo : rotulos) {
            if (rotulo.getNombre().equals(this.getNombre())) {
                return rotulo;
            }
        }

        rotulos.add(this);

        return  this;

    }
}
