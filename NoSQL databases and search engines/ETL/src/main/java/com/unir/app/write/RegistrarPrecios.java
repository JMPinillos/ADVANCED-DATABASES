package com.unir.app.write;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.unir.config.MySqlConnector;
import com.unir.models.*;
import com.unir.types.TipoEstacion;
import com.unir.types.TipoMargen;
import lombok.extern.slf4j.Slf4j;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Programa que se encarga de registrar los precios de los carburantes en las diferentes estaciones de repostaje
 */
@Slf4j
public class RegistrarPrecios {

    private static final String DATABASE = "laboratorio01";

    // Ya que hay muchos registros en el CSV y no me interesa buscar contínuamente si ya existe algo en base de datos
    // para no hacer tantas consultas, voy a guardar en memoria los registros que ya he procesado
    // bien sean aquellos que tengo que insertar porque no existen o aquellos que tengo que actualizar
    // porque lanzo una segunda vez un proceso de actualización.
    private static List<Carburante> carburantes = new LinkedList<>();
    private static List<TipoVenta> tipoVentas = new LinkedList<>();
    private static List<Provincia> provincias = new LinkedList<>();
    private static List<Municipio> municipios = new LinkedList<>();
    private static List<Localidad> localidades = new LinkedList<>();
    private static List<Rotulo> rotulos = new LinkedList<>();
    private static List<Estacion> estaciones = new LinkedList<>();
    private static List<Precio> precios = new LinkedList<>();


    public static void main(String[] args) {

        //Creamos conexión. No es necesario indicar puerto en host si usamos el default, 1521
        //Try-with-resources. Se cierra la conexión automáticamente al salir del bloque try
        try(Connection connection = new MySqlConnector("localhost", DATABASE).getConnection()) {

            log.info("Conexión establecida con la base de datos MySQL");

            // Se vacía la base de datos para que no haya datos duplicados
            vaciarBaseDatos(connection);
            log.info("Base de datos vaciada correctamente");

            // Leemos las estaciones terrestres y las cargamos en memoria
            leerCsvEstaciones(TipoEstacion.Terrestre);
            log.info("Se han cargado en memoria las estaciones terrestres, número total = {}", estaciones.size());

            // Leer las estaciones marítimas y las cargamos en memoria
            leerCsvEstaciones(TipoEstacion.Maritima);
            log.info("Se han cargado en memoria las estaciones marítimas, ahora el número número total de estaciones es = {}", estaciones.size());

            // Cargamos los datos en la base de datos
            cargarDatosEnBaseDatos(connection);

            log.info("Carga de información finalizada correctamente. Se han cargado las estaciones, número total = {}", estaciones.size());

        } catch (Exception e) {
            log.error("Error al tratar con la base de datos", e);
        }
    }

    /**
     * Lee los datos del csv de estaciones terrestres de postaje repostaje y los guarda en memoria
     */
    private static void leerCsvEstaciones(TipoEstacion tipoEstacion) {
        String nombreFichero = "";
        TipoVenta tipoVenta = null;
        if (tipoEstacion == TipoEstacion.Terrestre) {
        	nombreFichero = "preciosEstacionesTerrestres.csv";
        } else {
        	nombreFichero = "preciosEstacionesMaritimas.csv";
        }

        // Try-with-resources. Se cierra el reader automáticamente al salir del bloque try
        // CSVReader nos permite leer el fichero CSV linea a linea
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(nombreFichero))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(';').build()).build()) {


            // Saltamos la primera línea, que contiene los nombres de las columnas del CSV
            // No la saltamos ya que la necesitamos para sacar los nombres de los carburantes
            String[] nextLine;
            nextLine = reader.readNext();
            // Definimos los rangos del csv dependiendo si es terrestre o marítimo
            Integer carburanteIndiceCsvInicio = tipoEstacion.equals(TipoEstacion.Terrestre)? 9: 8;
            Integer carburanteIndiceCsvFin = tipoEstacion.equals(TipoEstacion.Terrestre)? 24: 12;

            // Sacamos los carburantes
            for (int i = carburanteIndiceCsvInicio; i < carburanteIndiceCsvFin; i++) {

                // quiero quitar la palabra Precio de la columna
                String nombreCarburante = nextLine[i].replace("Precio ", "");

                Carburante carburante = new Carburante(
                        UUID.randomUUID().toString(),
                        nombreCarburante
                );

                // Comprobamos si el carburante ya existe en la lista de carburantes
                carburante = carburante.AgregarSiNoExiste(carburantes);
            }

            // Como estamos en un CSV de estaciones terrestres, añadimos el tipo de venta de vehículos terrestres
            if (tipoEstacion == TipoEstacion.Terrestre) {
                tipoVenta = new TipoVenta(UUID.randomUUID().toString(), "Vehículos terrestres");
                tipoVentas.add(tipoVenta);
            }

            // Leemos el fichero linea a linea
            while((nextLine = reader.readNext()) != null) {
                // Comprobamos si la provincia ya existe en la lista de provincias
                Provincia provincia = new Provincia(
                        UUID.randomUUID().toString(),
                        nextLine[0]
                );

                // Si no existe la añadimos a la lista de provincias
                provincia = provincia.AgregarSiNoExiste(provincias);


                // Comprobamos si el municipio ya existe en la lista de municipios
                Municipio municipio = new Municipio(
                        UUID.randomUUID().toString(),
                        nextLine[1],
                        provincia
                );

                // Si no existe lo añadimos a la lista de municipios
                municipio = municipio.AgregarSiNoExiste(municipios);

                // Comprobamos si la localidad ya existe en la lista de localidades
                Localidad localidad = new Localidad(
                        UUID.randomUUID().toString(),
                        nextLine[2],
                        provincia,
                        municipio
                );

                // Si no existe lo añadimos a la lista de localidades
                localidad = localidad.AgregarSiNoExiste(localidades);


                // Comprobamos si el tipo de venta ya existe en la lista de tipos de ventas
                // En este cáso solo para marítimas, para las estaciones terrestres hemos creado uno por defecto: Vehículos terrestres
                if (tipoEstacion == TipoEstacion.Maritima) {
                    tipoVenta = new TipoVenta(
                            UUID.randomUUID().toString(),
                            nextLine[14]
                    );

                    // Si no existe lo añadimos a la lista de tipos de ventas
                    tipoVenta = tipoVenta.AgregarSiNoExiste(tipoVentas);
                }

                // Comprobamos si el rótulo ya existe en la lista de rótulos
                Rotulo rotulo = new Rotulo(
                        UUID.randomUUID().toString(),
                        nextLine[tipoEstacion.equals(TipoEstacion.Terrestre)? 24: 13]
                );

                // Si no existe lo añadimos a la lista de rótulos
                rotulo = rotulo.AgregarSiNoExiste(rotulos);

                // Estación de repostaje
                Estacion estacion = new Estacion(
                        UUID.randomUUID().toString(),
                        rotulo,
                        nextLine[6].isEmpty()?null: convertirStringABigDecimal(nextLine[6]),
                        nextLine[7].isEmpty()?null: convertirStringABigDecimal(nextLine[7]),
                        nextLine[3],
                        nextLine[4],
                        localidad,
                        TipoMargen.valueOf(tipoEstacion.equals(TipoEstacion.Terrestre)?  nextLine[5] : TipoMargen.N.toString()),
                        nextLine[tipoEstacion.equals(TipoEstacion.Terrestre)? 25 : 15],
                        tipoEstacion.equals(TipoEstacion.Terrestre)? convertirStringAFecha(nextLine[8]) : new Date(System.currentTimeMillis()),
                        tipoEstacion,
                        tipoVenta
                );

                // Fecha actual
                Date fechaActual = new Date(System.currentTimeMillis());

                // Si no existe la añadimos a la lista de estaciones, en este caso no hace falta ver si ya existe por que todas las estaciones
                // son diferentes
                estaciones.add(estacion);


                // Se recorren los carburantes y metemos en memoria los precios por cada estación
                for (int i = carburanteIndiceCsvInicio; i < carburanteIndiceCsvFin; i++) {

                    // Si no tiene precie no lo agregamos a la lista de precios
                    if (!nextLine[i].isEmpty()){
                        // Obtenemos el carburante
                        Carburante carburante = carburantes.get(i - carburanteIndiceCsvInicio);

                        // Creamos el precio
                        Precio precio = new Precio(
                                estacion,
                                carburante,
                                convertirStringADouble(nextLine[i])
                        );

                        // Añadimos el precio a la lista de precios
                        precios.add(precio);
                    }


                }
            }
        } catch (IOException e) {
            log.error("Error al leer el fichero CSV y sacar las provincias", e);
            throw new RuntimeException(e);
        } catch (CsvValidationException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Vacía la base de datos para evitar tener que andar comprobando si existen registros en la base de datos
     *
     * @param connection - Conexión a la base de datos
     * @return - True si se ha vaciado la base de datos
     */
    private static void vaciarBaseDatos(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM precios");
            statement.executeUpdate("DELETE FROM carburantes");
            statement.executeUpdate("DELETE FROM estaciones");
            statement.executeUpdate("DELETE FROM localidades");
            statement.executeUpdate("DELETE FROM municipios");
            statement.executeUpdate("DELETE FROM provincias");
            statement.executeUpdate("DELETE FROM rotulos");

        } catch (SQLException e) {
            log.error("Error al vaciar la base de datos", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Carga los datos en la base de datos
     *
     * @param connection - Conexión a la base de datos
     * @throws SQLException - Error al cargar los datos en la base de datos
     */
    private static void cargarDatosEnBaseDatos (Connection connection) throws SQLException {
        // Insertamos las provincias en la base de datos
        agregarProvincias(connection);
        log.info("Se han insertado las provincias correctamente");
        // Insertamos los municipios en la base de datos
        agregarMunicipios(connection);
        log.info("Se han insertado los municipios correctamente");

        // Insertamos las localidades en la base de datos
        agregarLocalidades(connection);
        log.info("Se han insertado las localidades correctamente");

        // Insertamos los carburantes en la base de datos
        agregarCarburantes(connection);
        log.info("Se han insertado los carburantes correctamente");

        // Insertamos los rótulos en la base de datos
        agregarRotulos(connection);
        log.info("Se han insertado los rótulos correctamente");

        // Insertamos los tipos de venta en la base de datos
        agregarTipoVenta(connection);
        log.info("Se han insertado los tipos de venta correctamente");

        // Insertamos las estaciones en la base de datos
        agregarEstaciones(connection);
        log.info("Se han insertado las estaciones correctamente");

        // Insertamos los precios en la base de datos
        agregarPrecios(connection);
        log.info("Se han insertado los precios correctamente");
    }


    // Método privado para convertir un string con comas a BigDecimal
    private static BigDecimal convertirStringABigDecimal(String valor) {
        valor = valor.replace(",", ".");
        return new BigDecimal(valor);
    }

    // Método privado para convertir un string a fecha
    private static Date convertirStringAFecha(String valor) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        return new Date(format.parse(valor).getTime());

    }

    // Método privado para convertir un string a double
    private static Double convertirStringADouble(String valor) {
        valor = valor.replace(",", ".");
        return Double.parseDouble(valor);
    }

    /**
     * Inserta las provincias en la base de datos
     *
     * @param connection - Conexión a la base de datos
     * @throws SQLException - Error al insertar las provincias
     */
    private static void agregarProvincias(Connection connection) throws SQLException {

        String insertSql = "INSERT INTO provincias (id, nombre) "
                + "VALUES (?, ?)";
        int lote = 10;
        int contador = 0;

        // Preparamos las consultas, una única vez para poder reutilizarlas en el batch
        PreparedStatement insertStatement = connection.prepareStatement(insertSql);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (Provincia provincia : provincias) {
            // Añadimos los parámetros a la consulta
            insertStatement.setString(1, provincia.getId());
            insertStatement.setString(2, provincia.getNombre());

            insertStatement.addBatch();

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                insertStatement.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatement.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }

    /**
     * Inserta los municipios en la base de datos
     *
     * @param connection - Conexión a la base de datos
     * @throws SQLException - Error al insertar los municipios
     */
    private static void agregarMunicipios(Connection connection) throws SQLException {

            String insertSql = "INSERT INTO municipios (id, nombre, provincia_id) "
                    + "VALUES (?, ?, ?)";
            int lote = 500;
            int contador = 0;

            // Preparamos las consultas, una única vez para poder reutilizarlas en el batch
            PreparedStatement insertStatement = connection.prepareStatement(insertSql);

            // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
            connection.setAutoCommit(false);

            for (Municipio municipio : municipios) {
                // Añadimos los parámetros a la consulta
                insertStatement.setString(1, municipio.getId());
                insertStatement.setString(2, municipio.getNombre());
                insertStatement.setString(3, municipio.getProvincia().getId());

                insertStatement.addBatch();

                // Ejecutamos el batch cada lote de registros
                if (++contador % lote == 0) {
                    insertStatement.executeBatch();
                }
            }

            // Ejecutamos el batch final
            insertStatement.executeBatch();

            // Hacemos commit y volvemos a activar el autocommit
            connection.commit();
            connection.setAutoCommit(true);
    }

    /**
     * Inserta las localidades en la base de datos
     *
     * @param connection - Conexión a la base de datos
     * @throws SQLException - Error al insertar las localidades
     */
    private static void agregarLocalidades(Connection connection) throws SQLException {

        String insertSql = "INSERT INTO localidades (id, nombre,  municipio_id) "
                + "VALUES (?, ?, ?)";
        int lote = 500;
        int contador = 0;

        // Preparamos las consultas, una única vez para poder reutilizarlas en el batch
        PreparedStatement insertStatement = connection.prepareStatement(insertSql);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (Localidad localidad : localidades) {
            // Añadimos los parámetros a la consulta
            insertStatement.setString(1, localidad.getId());
            insertStatement.setString(2, localidad.getNombre());
            insertStatement.setString(3, localidad.getMunicipio().getId());

            insertStatement.addBatch();

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                insertStatement.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatement.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }

    /**
     * Inserta carburantes en la base de datos
     * @param connection - Conexión a la base de datos
     * @throws SQLException - Error al insertar los carburantes
     */
    private static void agregarCarburantes(Connection connection) throws SQLException {

        String insertSql = "INSERT INTO carburantes (id, nombre) "
                + "VALUES (?, ?)";
        int lote = 500;
        int contador = 0;

        // Preparamos las consultas, una única vez para poder reutilizarlas en el batch
        PreparedStatement insertStatement = connection.prepareStatement(insertSql);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (Carburante carburante : carburantes) {
            // Añadimos los parámetros a la consulta
            insertStatement.setString(1, carburante.getId());
            insertStatement.setString(2, carburante.getNombre());

            insertStatement.addBatch();

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                insertStatement.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatement.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }

    /**
     * Inserta los rótulos en la base de datos
     * @param connection - Conexión a la base de datos
     * @throws SQLException - Error al insertar los rótulos
     */
    private static void agregarRotulos(Connection connection) throws SQLException {

            String insertSql = "INSERT INTO rotulos (id, nombre) "
                    + "VALUES (?, ?)";
            int lote = 500;
            int contador = 0;

            // Preparamos las consultas, una única vez para poder reutilizarlas en el batch
            PreparedStatement insertStatement = connection.prepareStatement(insertSql);

            // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
            connection.setAutoCommit(false);

            for (Rotulo rotulo : rotulos) {
                // Añadimos los parámetros a la consulta
                insertStatement.setString(1, rotulo.getId());
                insertStatement.setString(2, rotulo.getNombre());

                insertStatement.addBatch();

                // Ejecutamos el batch cada lote de registros
                if (++contador % lote == 0) {
                    insertStatement.executeBatch();
                }
            }

            // Ejecutamos el batch final
            insertStatement.executeBatch();

            // Hacemos commit y volvemos a activar el autocommit
            connection.commit();
            connection.setAutoCommit(true);
    }

    /**
     * Inserta los tipos de venta en la base de datos
     * @param connection - Conexión a la base de datos
     * @throws SQLException - Error al insertar los rótulos
     */
    private static void agregarTipoVenta(Connection connection) throws SQLException {

        String insertSql = "INSERT INTO tipo_ventas (id, nombre) "
                + "VALUES (?, ?)";
        int lote = 500;
        int contador = 0;

        // Preparamos las consultas, una única vez para poder reutilizarlas en el batch
        PreparedStatement insertStatement = connection.prepareStatement(insertSql);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (TipoVenta tipoVenta : tipoVentas) {
            // Añadimos los parámetros a la consulta
            insertStatement.setString(1, tipoVenta.getId());
            insertStatement.setString(2, tipoVenta.getNombre());

            insertStatement.addBatch();

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                insertStatement.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatement.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }


    /**
     * Inserta las estaciones en la base de datos
     * @param connection - Conexión a la base de datos
     * @throws SQLException - Error al insertar las estaciones
     */
    private static void agregarEstaciones(Connection connection) throws SQLException {

        String insertSql = "INSERT INTO estaciones (id,codigopostal,latitud, longitud, localidad_id,margen,direccion,fechaprecios,horario,tipoestacion,tipoventa_id, rotulo_id)"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int lote = 500;
        int contador = 0;

        // Preparamos las consultas, una única vez para poder reutilizarlas en el batch
        PreparedStatement insertStatement = connection.prepareStatement(insertSql);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (Estacion estacion : estaciones) {
            // Añadimos los parámetros a la consulta
            insertStatement.setString(1, estacion.getId());
            insertStatement.setString(2, estacion.getCodigoPostal());

            // Si no tiene latitud o longitud, se inserta un null
            if (estacion.getLatitud() == null) {
            	insertStatement.setNull(3, java.sql.Types.FLOAT);
            } else {
            	insertStatement.setBigDecimal(3, estacion.getLatitud());
            }

            if (estacion.getLongitud() == null) {
            	insertStatement.setNull(4, java.sql.Types.FLOAT);
            } else {
            	insertStatement.setBigDecimal(4, estacion.getLongitud());
            }

            insertStatement.setString(5, estacion.getLocalidad().getId());
            insertStatement.setString(6, estacion.getTipoMargen().toString());
            insertStatement.setString(7, estacion.getDireccion());
            insertStatement.setDate(8, estacion.getFecha());
            insertStatement.setString(9, estacion.getHorario());
            insertStatement.setString(10, estacion.getTipoEstacion().toString());
            insertStatement.setString(11, estacion.getTipoVenta().getId());
            insertStatement.setString(12, estacion.getRotulo().getId());

            insertStatement.addBatch();

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                insertStatement.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatement.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }

    /**
     * Inserta los precios en la base de datos
     * @param connection - Conexión a la base de datos
     * @throws SQLException - Error al insertar los precios
     */
    private static void agregarPrecios(Connection connection) throws SQLException {

        String insertSql = "INSERT INTO precios (estacion_id, carburante_id, precio) "
                + "VALUES (?, ?, ?)";
        int lote = 500;
        int contador = 0;

        // Preparamos las consultas, una única vez para poder reutilizarlas en el batch
        PreparedStatement insertStatement = connection.prepareStatement(insertSql);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (Precio precio : precios) {
            // Añadimos los parámetros a la consulta
            insertStatement.setString(1, precio.getEstacion().getId());
            insertStatement.setString(2, precio.getCarburante().getId());
            insertStatement.setDouble(3, precio.getPrecio());

            insertStatement.addBatch();

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                insertStatement.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatement.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }

}
