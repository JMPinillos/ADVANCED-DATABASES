package com.unir.app.write;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.unir.config.MySqlConnector;
import com.unir.model.*;
import lombok.extern.slf4j.Slf4j;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.text.Normalizer;

/**
 * La version para Oracle seria muy similar a esta, cambiando únicamente el Driver y los datos de sentencias.
 * La tabla de Oracle contiene muchas restricciones y triggers. Por simplicidad, usamos MySQL en este caso.
 */
@Slf4j
public class MySqlApplicationIntake {

    private static final String DATABASE = "laboratorio_EESS";
    private static final String CSV = "Precios_EESS.csv";
    private static List<MySqlProvinces> provinces = new LinkedList<>();
    private static List<MySqlMunicipalities> municipalities = new LinkedList<>();
    private static List<MySqlLocalities> localities = new LinkedList<>();
    private static List<MySqlOperators> operators = new LinkedList<>();
    private static List<MySqlFuels> fuels = new LinkedList<>();
    private static List<MySqlStations> stations = new LinkedList<>();
    private static List<MySqlPrices> prices = new LinkedList<>();

    public static void main(String[] args) {

        // Creamos conexion. No es necesario indicar puerto en host si usamos el default, 1521
        // Try-with-resources. Se cierra la conexión automáticamente al salir del bloque try
        try(Connection connection = new MySqlConnector("localhost", DATABASE).getConnection()) {

            log.info("Conexión establecida con la base de datos " + DATABASE);

            // Borramos los datos de la base de datos
            eraseDB(connection);
            log.info("DATOS DE LA BASE DE DATOS " + DATABASE + " BORRADOS");

            // Leemos los datos del fichero CSV
            provinces = readDataProvinces();
            municipalities = readDataMunicipalities();
            localities = readDataLocalities();
            operators = readDataOperators();
            fuels = readDataFuels();
            stations = readDataStations();
          //  prices = readDataPrices();

            // Introducimos los datos en la base de datos
            intakeProvinces(connection, provinces);
            log.info("PROVINCIAS INSERTADAS");
            intakeMunicipalities(connection, municipalities);
            log.info("MUNICIPIOS INSERTADOS");
            intakeLocalities(connection, localities);
            log.info("LOCALIDADES INSERTADAS");
            intakeOperators(connection, operators);
            log.info("OPERADORAS INSERTADAS");
            intakeFuels(connection, fuels);
            log.info("COMBUSTIBLES INSERTADOS");
            intakeStations(connection, stations);
            log.info("ESTACIONES DE SERVICIO INSERTADAS");
            //intakePrices(connection, prices);
            //log.info("PRECIOS INSERTADOS");

        } catch (Exception e) {
            log.error("Error al tratar con la base de datos", e);
        }
    }

    private static void eraseDB(Connection connection) {

        try {
            Statement deleteData = connection.createStatement();

            // Borramos la base de datos
            deleteData.executeUpdate("DELETE FROM prices");
            deleteData.executeUpdate("DELETE FROM stations");
            deleteData.executeUpdate("DELETE FROM fuels");
            deleteData.executeUpdate("DELETE FROM operators");
            deleteData.executeUpdate("DELETE FROM localities");
            deleteData.executeUpdate("DELETE FROM municipalities");
            deleteData.executeUpdate("DELETE FROM provinces");
        } catch (SQLException e) {
            log.error("ERROR AL VACIAR LA BASE DE DATOS", e);
        }
    }

    private static List<MySqlProvinces> readDataProvinces() {

        // Try-with-resources. Se cierra el reader automáticamente al salir del bloque try
        // CSVReader nos permite leer el fichero CSV linea a linea
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(CSV))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(';').build()).build()) {

            // Saltamos la primera linea, que contiene los nombres de las columnas del CSV
            reader.skip(1);
            String[] nextLine;

            // Leemos el fichero linea a linea
            while((nextLine = reader.readNext()) != null) {

                MySqlProvinces province = null;

                for (MySqlProvinces compare : provinces) {
                    if (compare.getName().equals(nextLine[0])) {
                        province = compare;
                        break;
                    }
                }

                if (province == null) {
                    province = new MySqlProvinces (
                            (provinces.size()+1),   // ID segun el contenido de la tabla.
                            nextLine[0]             // Cogemos el dato de la columna provincia. 
                    );
                    provinces.add(province);
                }
            }
            return provinces;

        } catch (IOException e) {
            log.error("Error al leer el fichero" + CSV, e);
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void intakeProvinces(Connection connection, List<MySqlProvinces> provinces) throws SQLException {

        // Consultas de la tabla provincias
        String selectSqlProvinces = "SELECT COUNT(*) FROM provinces WHERE name = ?";
        String insertSqlProvinces = "INSERT INTO provinces (pro_id, name)"
                + "VALUES (?, ?)";

        int lote = 5;
        int contador = 0;

        // Preparamos las consultas, una unica vez para poder reutilizarlas en el batch
        PreparedStatement insertStatementProvinces = connection.prepareStatement(insertSqlProvinces);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (MySqlProvinces province : provinces) {

            // Comprobamos si la provincia existe
            PreparedStatement selectStatementProvinces = connection.prepareStatement(selectSqlProvinces);
            selectStatementProvinces.setString(1, province.getName()); // Nombre de la provincia

            ResultSet resultSet = selectStatementProvinces.executeQuery();
            resultSet.next(); // Nos movemos a la primera fila
            int rowCount = resultSet.getInt(1);

            // Si no existe, insertamos. Si existe, no hacemos nada.
            if(rowCount == 0) {
                fillInsertStatementProvinces(insertStatementProvinces, province);
                insertStatementProvinces.addBatch();
            }

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                insertStatementProvinces.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatementProvinces.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }

    private static List<MySqlMunicipalities> readDataMunicipalities() {

        // Try-with-resources. Se cierra el reader automáticamente al salir del bloque try
        // CSVReader nos permite leer el fichero CSV linea a linea
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(CSV))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(';').build()).build()) {

            // Saltamos la primera linea, que contiene los nombres de las columnas del CSV
            reader.skip(1);
            String[] nextLine;

            // Leemos el fichero linea a linea
            while((nextLine = reader.readNext()) != null) {

                MySqlMunicipalities municipalitie = null;

                for (MySqlMunicipalities compare : municipalities) {
                    if (compare.getName().equals(nextLine[1])) {
                        municipalitie = compare;
                        break;
                    }
                }

                if (municipalitie == null) {
                    // Buscamos en la lista de provincias para obtener el código.
                    int id_pro = 0;
                    for (MySqlProvinces province : provinces) {
                        if (province.getName().equals(nextLine[0])) {
                            id_pro = province.getPro_id();
                        }
                    }
                    municipalitie = new MySqlMunicipalities(
                            (municipalities.size()+1),  // ID segun el contenido de la tabla.
                            id_pro,
                            nextLine[1]                 // Cogemos el dato de la columna municipio.
                    );
                    municipalities.add(municipalitie);
                }
            }
            return municipalities;

        } catch (IOException e) {
            log.error("Error al leer el fichero" + CSV, e);
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void intakeMunicipalities(Connection connection, List<MySqlMunicipalities> municipalities) throws SQLException {

        // Consultas de la tabla Municipios
        String selectSqlMunicipalities = "SELECT COUNT(*) FROM municipalities WHERE name = ?";
        String insertSqlMunicipalities = "INSERT INTO municipalities (mun_id, pro_id, name)"
                + "VALUES (?, ?, ?)";

        int lote = 5;
        int contador = 0;

        // Preparamos las consultas, una unica vez para poder reutilizarlas en el batch
        PreparedStatement insertStatementMunicipalities = connection.prepareStatement(insertSqlMunicipalities);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (MySqlMunicipalities municipalitie : municipalities) {

            // Comprobamos si el municipio existe
            PreparedStatement selectStatementMunicipalities = connection.prepareStatement(selectSqlMunicipalities);
            selectStatementMunicipalities.setString(1, municipalitie.getName()); // Nombre del municipio

            ResultSet resultSet = selectStatementMunicipalities.executeQuery();
            resultSet.next(); // Nos movemos a la primera fila
            int rowCount = resultSet.getInt(1);

            // Si no existe, insertamos. Si existe, no hacemos nada.
            if(rowCount == 0) {
                fillInsertStatementMunicipalities(insertStatementMunicipalities, municipalitie);
                insertStatementMunicipalities.addBatch();
            }

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                insertStatementMunicipalities.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatementMunicipalities.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }

    private static List<MySqlLocalities> readDataLocalities() {

        // Try-with-resources. Se cierra el reader automáticamente al salir del bloque try
        // CSVReader nos permite leer el fichero CSV linea a linea
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(CSV))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(';').build()).build()) {

            // Saltamos la primera linea, que contiene los nombres de las columnas del CSV
            reader.skip(1);
            String[] nextLine;

            // Leemos el fichero linea a linea
            while((nextLine = reader.readNext()) != null) {

                MySqlLocalities localitie = null;

                for (MySqlLocalities compare : localities) {
                    if (compare.getName().equals(nextLine[2])) {
                        localitie = compare;
                        break;
                    }
                }

                if (localitie == null) {
                    // Buscamos en la lista de municipios para obtener el código.
                    int id_mun = 0;
                    for (MySqlMunicipalities municipalitie : municipalities) {
                        if (municipalitie.getName().equals(nextLine[1])) {
                            id_mun = municipalitie.getMun_id();
                        }
                    }
                    localitie = new MySqlLocalities(
                            (localities.size()+1),  // ID segun el contenido de la tabla.
                            id_mun,
                            nextLine[2]                 // Cogemos el dato de la columna localidad.
                    );
                    localities.add(localitie);
                }
            }
            return localities;

        } catch (IOException e) {
            log.error("Error al leer el fichero" + CSV, e);
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void intakeLocalities(Connection connection, List<MySqlLocalities> localities) throws SQLException {

        // Consultas de la tabla Municipios
        String selectSqlLocalities = "SELECT COUNT(*) FROM localities WHERE name = ?";
        String insertSqlLocalities = "INSERT INTO localities (loc_id, mun_id, name)"
                + "VALUES (?, ?, ?)";

        int lote = 5;
        int contador = 0;

        // Preparamos las consultas, una unica vez para poder reutilizarlas en el batch
        PreparedStatement insertStatementLocalities = connection.prepareStatement(insertSqlLocalities);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (MySqlLocalities localitie : localities) {

            // Comprobamos si la localidad existe
            PreparedStatement selectStatementLocalities = connection.prepareStatement(selectSqlLocalities);
            selectStatementLocalities.setString(1, localitie.getName()); // Nombre de la localidad

            ResultSet resultSet = selectStatementLocalities.executeQuery();
            resultSet.next(); // Nos movemos a la primera fila
            int rowCount = resultSet.getInt(1);

            // Si no existe, insertamos. Si existe, no hacemos nada.
            if(rowCount == 0) {
                fillInsertStatementLocalities(insertStatementLocalities, localitie);
                insertStatementLocalities.addBatch();
            }

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                insertStatementLocalities.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatementLocalities.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }

    private static List<MySqlOperators> readDataOperators() {

        // Try-with-resources. Se cierra el reader automáticamente al salir del bloque try
        // CSVReader nos permite leer el fichero CSV linea a linea
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(CSV))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(';').build()).build()) {

            // Saltamos la primera linea, que contiene los nombres de las columnas del CSV
            reader.skip(1);
            String[] nextLine;

            while((nextLine = reader.readNext()) != null) {

                MySqlOperators operator = null;

                for (MySqlOperators compare : operators) {
                    if (compare.getName().contentEquals(nextLine[25])) {
                        operator = compare;
                        break;
                    }
                }

                if (operator == null) {
                    operator = new MySqlOperators (
                            (operators.size()+1),   // ID segun el contenido de la tabla.
                            nextLine[25]            // Cogemos el dato de la columna rotulo.
                    );
                    operators.add(operator);
                }
            }
            return operators;

        } catch (IOException e) {
            log.error("Error al leer el fichero" + CSV, e);
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void intakeOperators(Connection connection, List<MySqlOperators> operators) throws SQLException {

        // Consultas de la tabla Municipios
        String selectSqlOperators = "SELECT COUNT(*) FROM operators WHERE name = ?";
        String insertSqlOperators = "INSERT INTO operators (op_id, name)"
                + "VALUES (?, ?)";

        int lote = 5;
        int contador = 0;

        // Preparamos las consultas, una unica vez para poder reutilizarlas en el batch
        PreparedStatement insertStatementOperators = connection.prepareStatement(insertSqlOperators);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (MySqlOperators operator : operators) {

            // Comprobamos si el operador existe
            PreparedStatement selectStatementOperators = connection.prepareStatement(selectSqlOperators);
            selectStatementOperators.setString(1, operator.getName()); // Nombre del operador

            ResultSet resultSet = selectStatementOperators.executeQuery();
            resultSet.next(); // Nos movemos a la primera fila
            int rowCount = resultSet.getInt(1);

            // Si no existe, insertamos. Si existe, no hacemos nada.
            if(rowCount == 0) {
                fillInsertStatementOperators(insertStatementOperators, operator);
                insertStatementOperators.addBatch();
            }

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                insertStatementOperators.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatementOperators.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }

    private static List<MySqlFuels> readDataFuels() {

        // Try-with-resources. Se cierra el reader automáticamente al salir del bloque try
        // CSVReader nos permite leer el fichero CSV linea a linea
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(CSV))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(';').build()).build()) {

            String[] nextLine = reader.readNext();
            int i = 9;

            while(i <= 24) {

                MySqlFuels fuel = null;

                for (MySqlFuels compare : fuels) {
                    if (compare.getName().equals(nextLine[i])) {
                        fuel = compare;
                        break;
                    }
                }

                if (fuel == null) {
                    fuel = new MySqlFuels (
                            (fuels.size()+1),   // ID segun el contenido de la tabla.
                            nextLine[i]             // Cogemos el dato de la columna que contiene el nombre del carburante.
                    );
                    fuels.add(fuel);
                }
                i++;
            }
            return fuels;

        } catch (IOException e) {
            log.error("Error al leer el fichero" + CSV, e);
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void intakeFuels(Connection connection, List<MySqlFuels> fuels) throws SQLException {

        // Consultas de la tabla Municipios
        String selectSqlFuels = "SELECT COUNT(*) FROM fuels WHERE name = ?";
        String insertSqlFuels = "INSERT INTO fuels (fuel_id, name)"
                + "VALUES (?, ?)";

        int lote = 5;
        int contador = 0;

        // Preparamos las consultas, una unica vez para poder reutilizarlas en el batch
        PreparedStatement insertStatementFuels = connection.prepareStatement(insertSqlFuels);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (MySqlFuels fuel : fuels) {

            // Comprobamos si el combustible existe
            PreparedStatement selectStatementFuels = connection.prepareStatement(selectSqlFuels);
            selectStatementFuels.setString(1, fuel.getName()); // Nombre del carburante

            ResultSet resultSet = selectStatementFuels.executeQuery();
            resultSet.next(); // Nos movemos a la primera fila
            int rowCount = resultSet.getInt(1);

            // Si no existe, insertamos. Si existe, no hacemos nada.
            if(rowCount == 0) {
                fillInsertStatementFuels(insertStatementFuels, fuel);
                insertStatementFuels.addBatch();
            }

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                insertStatementFuels.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatementFuels.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }

    private static List<MySqlStations> readDataStations() {

        // Try-with-resources. Se cierra el reader automáticamente al salir del bloque try
        // CSVReader nos permite leer el fichero CSV linea a linea
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(CSV))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(';').build()).build()) {

            // Saltamos la primera linea, que contiene los nombres de las columnas del CSV
            reader.skip(1);
            String[] nextLine;

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm");

            // Leemos el fichero linea a linea
            while((nextLine = reader.readNext()) != null) {

                MySqlStations station = null;

                for (MySqlStations compare : stations) {
                    if (compare.getAddress().equals(nextLine[4]) && compare.getMargen().equals(nextLine[5]) && compare.getHorario().equals(nextLine[27])) {
                        station = compare;
                        break;
                    }
                }

                if (station == null) {
                    // Buscamos en la lista de localidades para obtener el código.
                    int id_loc = 0;
                    for (MySqlLocalities localitie : localities) {
                        if (localitie.getName().equals(nextLine[2])) {
                            id_loc = localitie.getLoc_id();
                        }
                    }

                    // Buscamos en la lista de operadores para obtener el código.
                    int id_op = 0;
                    for (MySqlOperators operator : operators) {
                        if (operator.getName().equals(nextLine[25])) {
                            id_op = operator.getOp_id();
                        }
                    }

                    station = new MySqlStations(
                            (stations.size()+1),  // ID segun el contenido de la tabla.
                            id_loc,
                            id_op,
                            nextLine[3],    // Cogemos el dato de la columna CP.
                            nextLine[4],    // Cogemos el dato de la columna Dirección.
                            nextLine[5],    // Cogemos el dato de la columna Margen.
                            nextLine[6].isEmpty()?0:Float.parseFloat(nextLine[6].replace(",", ".")),  // Cogemos el dato de la columna Longitud.
                            nextLine[7].isEmpty()?0:Float.parseFloat(nextLine[7].replace(",", ".")),  // Cogemos el dato de la columna Latitud.
                            nextLine[8].isEmpty()?null:new Date(format.parse(nextLine[8]).getTime()),
                            nextLine[26].isEmpty()?"M":"T", // Cogemos el dato de la columna Tipo.
                            nextLine[27]    // Cogemos el dato de la columna Horario.
                    );
                    stations.add(station);
                }
            }
            return stations;

        } catch (IOException e) {
            log.error("Error al leer el fichero" + CSV, e);
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static void intakeStations(Connection connection, List<MySqlStations> stations) throws SQLException {

        // Consultas de la tabla provincias
        String insertSqlStations = "INSERT INTO stations (st_id, loc_id, op_id, cp, address, margin, longitude, latitude, price_date, type, schedule)"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int lote = 5;
        int contador = 0;

        // Preparamos las consultas, una unica vez para poder reutilizarlas en el batch
        PreparedStatement insertStatementStations = connection.prepareStatement(insertSqlStations);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (MySqlStations station : stations) {

            fillInsertStatementStations(insertStatementStations, station);
            insertStatementStations.addBatch();

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                insertStatementStations.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatementStations.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }

    private static List<MySqlPrices> readDataPrices() {

        // Try-with-resources. Se cierra el reader automáticamente al salir del bloque try
        // CSVReader nos permite leer el fichero CSV linea a linea
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(CSV))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(';').build()).build()) {

            // Saltamos la primera linea, que contiene los nombres de las columnas del CSV
            reader.skip(1);
            String[] nextLine;

            MySqlPrices price = null;

            // Leemos el fichero linea a linea
            while((nextLine = reader.readNext()) != null) {

                int i = 9;
                int id_fuel = 1;

                while (i <= 24) {

                        if (nextLine[i].isEmpty()) {

                        } else{
                            // Buscamos en la lista de estaciones de servicio para obtener el código.
                            int id_st = 0;
                            for (MySqlStations station : stations) {
                                if (station.getAddress().equals(nextLine[4]) && station.getMargen().equals(nextLine[5]) && station.getHorario().equals(nextLine[27])) {
                                    id_st = station.getStation_id();
                                }
                            }

                            price = new MySqlPrices(
                                    id_st,
                                    id_fuel,
                                    Float.parseFloat(nextLine[i].replace(",", ".")) // Cogemos el dato del precio.                        );
                            );
                            prices.add(price);
                        }
                    i++;
                    id_fuel++;
                }
            }
            return prices;

        } catch (IOException e) {
            log.error("Error al leer el fichero" + CSV, e);
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void intakePrices(Connection connection, List<MySqlPrices> prices) throws SQLException {

        // Consultas de la tabla provincias
        String insertSqlPrice = "INSERT INTO prices (st_id, fuel_id, price)"
                + "VALUES (?, ?, ?)";

        int lote = 5;
        int contador = 0;

        // Preparamos las consultas, una unica vez para poder reutilizarlas en el batch
        PreparedStatement insertStatementPrices = connection.prepareStatement(insertSqlPrice);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (MySqlPrices price : prices) {

            fillInsertStatementPrices(insertStatementPrices, price);
            insertStatementPrices.addBatch();

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                insertStatementPrices.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatementPrices.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }

    private static void fillInsertStatementProvinces(PreparedStatement statement, MySqlProvinces provinces) throws SQLException {
        statement.setInt(1, provinces.getPro_id());
        statement.setString(2, provinces.getName());
    }

    private static void fillInsertStatementMunicipalities(PreparedStatement statement, MySqlMunicipalities municipalities) throws SQLException {
        statement.setInt(1, municipalities.getMun_id());
        statement.setInt(2, municipalities.getPro_id());
        statement.setString(3, municipalities.getName());
    }

    private static void fillInsertStatementLocalities(PreparedStatement statement, MySqlLocalities localities) throws SQLException {
        statement.setInt(1, localities.getLoc_id());
        statement.setInt(2, localities.getMun_id());
        statement.setString(3, localities.getName());
    }

    private static void fillInsertStatementOperators(PreparedStatement statement, MySqlOperators operators) throws SQLException {
        statement.setInt(1, operators.getOp_id());
        statement.setString(2, operators.getName());
    }

    private static void fillInsertStatementFuels(PreparedStatement statement, MySqlFuels fuels) throws SQLException {
        statement.setInt(1, fuels.getFuel_id());
        statement.setString(2, fuels.getName());
    }

    private static void fillInsertStatementStations(PreparedStatement statement, MySqlStations stations) throws SQLException {
        statement.setInt(1, stations.getStation_id());
        statement.setInt(2, stations.getLoc_id());
        statement.setInt(3, stations.getOp_id());
        statement.setString(4, stations.getCp());
        statement.setString(5, stations.getAddress());
        statement.setString(6, stations.getMargen());
        statement.setFloat(7, stations.getLongitud());
        statement.setFloat(8, stations.getLatitud());
        statement.setDate(9, stations.getPrice_date());
        statement.setString(10, stations.getTipo());
        statement.setString(11, stations.getHorario());
    }

    private static void fillInsertStatementPrices(PreparedStatement statement, MySqlPrices prices) throws SQLException {
        statement.setInt(1, prices.getSt_id());
        statement.setInt(2, prices.getFuel_id());
        statement.setFloat(3, prices.getPrice());
    }
}
