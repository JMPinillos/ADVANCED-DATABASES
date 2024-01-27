package com.unir.app.read;

import com.unir.config.MySqlConnector;
import com.unir.types.TipoEstacion;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.*;

@Slf4j
public class ConsultarPrecios {
    private static final String DATABASE = "laboratorio01";
    /**
     * Método principal de la aplicación
     *  Nombre de la empresa con más estaciones de servicio terrestres.
     *  Nombre de la empresa con más estaciones de servicio marítimas.
     *  Localización, nombre de empresa, y margen de la estación con el precio más bajo para el combustible “Gasolina 95 E5” en la Comunidad de Madrid.
     *  Localización, nombre de empresa, y margen de la estación con el precio más bajo para el combustible “Gasóleo A” si resido en el centro de Albacete y no quiero desplazarme más de 10 KM.
     *  Provincia en la que se encuentre la estación de servicio marítima con el combustible “Gasolina 95 E5” más caro.
     * @param args Argumentos de la aplicación
     */
    public static void main(String[] args) {
        //Creamos conexión. No es necesario indicar puerto en host si usamos el default, 1521
        //Try-with-resources. Se cierra la conexión automáticamente al salir del bloque try
        try(Connection connection = new MySqlConnector("localhost", DATABASE).getConnection()) {

            // Empresa con más estaciones de servicio terrestres
            getEmpresaConMasEstaciones(connection, TipoEstacion.Terrestre);

            // Empresa con más estaciones de servicio marítimas
            getEmpresaConMasEstaciones(connection, TipoEstacion.Maritima);

            // Localización, nombre de empresa, y margen de la estación con el precio más bajo para el combustible “Gasolina 95 E5” en la Comunidad de Madrid.
            getEmpresaConCarburanteMasEconomico(connection, "Gasolina 95 E5", "Madrid");

            // Provincia en la que se encuentre la estación de servicio marítima con el combustible “Gasolina 95 E5” más caro.
            getProvinciaConEstacionMasCara(connection, TipoEstacion.Maritima, "Gasolina 95 E5");

            // Localización, nombre de empresa, y margen de la estación con el precio más bajo para el combustible “Gasóleo A” si resido en el centro de Albacete y no quiero desplazarme más de 10 KM.
            // El centro de Albacete se encuentra en la ubicación [38.994349, -1.858542]
            getEstacionMasEconomicaSegunPuntoOrigenConDistanciaMaxima(connection,  BigDecimal.valueOf(38.994349), BigDecimal.valueOf(-1.858542), "Gasóleo A", 10);

        } catch (Exception e) {
            log.error("Error al tratar con la base de datos", e);
        }
    }

    /**
     * Empresa con más estaciones de servicio
     * @param connection
     * @param tipoEstacion
     * @throws SQLException
     */
    private static void getEmpresaConMasEstaciones(Connection connection, TipoEstacion tipoEstacion) throws SQLException {
        PreparedStatement select = connection.prepareStatement("select count(*) as total, rotulos.nombre as empresa from estaciones\n" +
                "inner  join rotulos on estaciones.rotulo_id = rotulos.id\n" +
                "where tipoestacion = ? \n" +
                "group by empresa\n" +
                "order by total desc\n" +
                "limit 1;");

        select.setString(1, tipoEstacion.toString());

        ResultSet empresas = select.executeQuery();

        while (empresas.next()) {
            log.info("------------------------------------------------------------------------------");
            log.info("Empresa con más estaciones de servicio {}: {}",tipoEstacion, empresas.getString("empresa"));
        }
    }

    /**
     * Localización, nombre de empresa, y margen de la estación con el precio más bajo para un combustible dado y una provincia dada.
     * @param connection
     * @param carburante
     * @param provincia
     * @throws SQLException
     */
    private static void getEmpresaConCarburanteMasEconomico(Connection connection, String carburante, String provincia) throws SQLException {
        PreparedStatement select = connection.prepareStatement(
            "select longitud, latitud, direccion, codigopostal, rotulos.nombre as empresa, margen, precio, " +
                "carburantes.nombre as carburante, provincias.nombre as provincia, municipios.nombre as municipio, localidades.nombre as localidad " +
                "from estaciones\n" +
                "inner join rotulos on estaciones.rotulo_id = rotulos.id\n" +
                "inner join precios on estaciones.id = precios.estacion_id\n" +
                "inner join localidades on estaciones.localidad_id = localidades.id\n" +
                "inner join municipios on localidades.municipio_id = municipios.id\n" +
                "inner join provincias on municipios.provincia_id = provincias.id\n" +
                "inner join carburantes on precios.carburante_id = carburantes.id\n" +
                "where provincias.nombre = ? and carburantes.nombre like ?\n" +
                    "order by precio\n" +
                    "limit 1");

        select.setString(1, provincia);
        select.setString(2, carburante);


        ResultSet empresas = select.executeQuery();

        while (empresas.next()) {

            log.info("------------------------------------------------------------------------------");
            log.info("Estos son los datos de la empresa con el carburante más económico del carburante {} y de la provincia {}:", carburante, provincia);
            log.info("  - Latitud: {}", empresas.getString("latitud"));
            log.info("  - Longitud: {}", empresas.getString("longitud"));
            log.info("  - Provincia: {}", empresas.getString("provincia"));
            log.info("  - Municipio: {}", empresas.getString("municipio"));
            log.info("  - Localidad: {}", empresas.getString("localidad"));
            log.info("  - Dirección: {}", empresas.getString("direccion"));
            log.info("  - Código postal: {}", empresas.getString("codigopostal"));
            log.info("  - Margen: {}", empresas.getString("margen"));
            log.info("  - Empresa: {}", empresas.getString("empresa"));
            log.info("  - Precio: {}", empresas.getString("precio"));

        }
    }

    /**
     * Provincia en la que se encuentre la estación de servicio más caro.
     * @param connection
     * @param tipoEstacion
     * @param carburante
     * @throws SQLException
     */
    private static void getProvinciaConEstacionMasCara(Connection connection, TipoEstacion tipoEstacion, String carburante) throws SQLException {
        PreparedStatement select = connection.prepareStatement(
            "select provincias.nombre, precio from provincias\n" +
                "inner join municipios on provincias.id = municipios.provincia_id\n" +
                "inner join localidades on municipios.id = localidades.municipio_id\n" +
                "inner join estaciones on localidades.id = estaciones.localidad_id\n" +
                "inner join precios on estaciones.id = precios.estacion_id\n" +
                "inner join carburantes on precios.carburante_id = carburantes.id\n" +
                "where estaciones.tipoestacion = ?\n" +
                "and carburantes.nombre = ?\n" +
                "order by precio desc\n" +
                "limit 1");

        select.setString(1, tipoEstacion.toString());
        select.setString(2, carburante);


        ResultSet empresas = select.executeQuery();

        while (empresas.next()) {

            log.info("------------------------------------------------------------------------------");
            log.info("La provincia con el precio más caro del carburante {} y del tipo de estación {}",
                    carburante, tipoEstacion);
            log.info("Estos son los datos:");
            log.info("  - Provincia: {}", empresas.getString("nombre"));
            log.info("  - Precio: {}", empresas.getString("precio"));

        }
    }
    // Localización, nombre de empresa, y margen de la estación con el precio más bajo para el combustible “Gasóleo A” si resido en el centro de Albacete y no quiero desplazarme más de 10 KM.
    private static void getEstacionMasEconomicaSegunPuntoOrigenConDistanciaMaxima(Connection connection, BigDecimal latitud, BigDecimal longitud, String carburante, int distanciaMaxima) throws SQLException {
       /*
       Esta es la formula Haversine que he utilizado para calcular la distancia.

       6371 * 2 * ASIN(
        SQRT(
            POW(SIN(RADIANS((latitud - tu_latitud) / 2)), 2) +
            COS(RADIANS(tu_latitud)) * COS(RADIANS(latitud)) *
            POW(SIN(RADIANS((longitud - tu_longitud) / 2)), 2)
            )
        ) <= tu_distancia
        */

        PreparedStatement select = connection.prepareStatement(
            "SELECT  latitud, longitud, provincias.nombre as provincia, " +
                    "municipios.nombre as municipio, localidades.nombre as localidad,\n" +
                    "codigopostal,\n" +
                    "direccion, rotulos.nombre as empresa, margen as margen,\n" +
                    "carburantes.nombre as carburante,\n" +
                    "precios.precio,\n" +
                    "6371 * 2 * ASIN(\n" +
                    "    SQRT(\n" +
                    "    POW(SIN(RADIANS((latitud - ?) / 2)), 2) +\n" +
                    "    COS(RADIANS(?)) * COS(RADIANS(latitud)) *\n" +
                    "    POW(SIN(RADIANS((longitud - ?) / 2)), 2)\n" +
                    "    )\n" +
                    ") as distancia\n" +
                    "FROM estaciones\n" +
                    "inner join rotulos on estaciones.rotulo_id = rotulos.id\n" +
                    "inner join localidades on estaciones.localidad_id = localidades.id\n" +
                    "inner join municipios on localidades.municipio_id = municipios.id\n" +
                    "inner join provincias on municipios.provincia_id = provincias.id\n" +
                    "inner join precios on estaciones.id = precios.estacion_id\n" +
                    "inner join carburantes on precios.carburante_id = carburantes.id\n" +
                    "WHERE carburantes.nombre = ?\n" +
                    "AND (\n" +
                    "    6371 * 2 * ASIN(\n" +
                    "        SQRT(\n" +
                    "        POW(SIN(RADIANS((latitud - ?) / 2)), 2) +\n" +
                    "        COS(RADIANS(?)) * COS(RADIANS(latitud)) *\n" +
                    "        POW(SIN(RADIANS((longitud - ?) / 2)), 2)\n" +
                    "        )\n" +
                    "    )\n" +
                    ") <= ? \n" +
                    "order by precio\n" +
                    "limit 1");

        select.setBigDecimal(1, latitud);
        select.setBigDecimal(2, latitud);
        select.setBigDecimal(3, longitud);
        select.setString(4, carburante);
        select.setBigDecimal(5, latitud);
        select.setBigDecimal(6, latitud);
        select.setBigDecimal(7, longitud);
        select.setInt(8, distanciaMaxima);

        ResultSet empresas = select.executeQuery();

        while (empresas.next()) {
            log.info("------------------------------------------------------------------------------");
            log.info("La empresa con el carburante {} más barato y que se encuentra a menos de {} km de distancia de tu ubicación, [latitud {}, longitud {}], con el precio {} .",
                    carburante, distanciaMaxima, latitud, longitud, empresas.getDouble("precio"));
            log.info("Estos son los datos:");
            log.info("  - Distancia: {} km", empresas.getString("distancia"));
            log.info("  - Latitud: {}", empresas.getString("latitud"));
            log.info("  - Longitud: {}", empresas.getString("longitud"));
            log.info("  - Provincia: {}", empresas.getString("provincia"));
            log.info("  - Municipio: {}", empresas.getString("municipio"));
            log.info("  - Localidad: {}", empresas.getString("localidad"));
            log.info("  - Dirección: {}", empresas.getString("direccion"));
            log.info("  - Código postal: {}", empresas.getString("codigopostal"));
            log.info("  - Margen: {}", empresas.getString("margen"));
            log.info("  - Empresa: {}", empresas.getString("empresa"));
            log.info("  - Precio: {}", empresas.getString("precio"));
        }

    }
}
