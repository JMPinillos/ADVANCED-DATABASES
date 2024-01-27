package com.unir.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class TipoVenta {
    private String id;
    private String nombre;

    /**
     * Comprueba si existe el tipo de venta en la lista de tipos de venta
     * @param tiposVenta
     * @return TipoVenta
     */
    public TipoVenta AgregarSiNoExiste(List<TipoVenta> tiposVenta) {
        // comprueba si existe el tipo de venta en la lista de tipos de venta y si no existe se añade
        for (TipoVenta tipoVenta : tiposVenta) {
            if (tipoVenta.getNombre().equals(this.getNombre())) {
                return tipoVenta;
            }
        }

        tiposVenta.add(this);

        return this;
    }
}
