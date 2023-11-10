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
import java.util.LinkedList;
import java.util.List;

/**
 * La version para Oracle seria muy similar a esta, cambiando únicamente el Driver y los datos de sentencias.
 * La tabla de Oracle contiene muchas restricciones y triggers. Por simplicidad, usamos MySQL en este caso.
 */
@Slf4j
public class MySqlApplicationIntake {

    private static final String DATABASE = "laboratorio_EESS";
    private static List<MySqlProvinces> provinces = new LinkedList<>();
    private static List<MySqlMunicipalities> municipalities = new LinkedList<>();
    private static List<MySqlLocalities> localities = new LinkedList<>();
    private static List<MySqlOperators> operators = new LinkedList<>();
    private static List<MySqlFuels> fuels = new LinkedList<>();
    private static List<MySqlPrices> prices = new LinkedList<>();
    private static List<MySqlFuelStations> fuelStations = new LinkedList<>();

    public static void main(String[] args) {

        // Creamos conexion. No es necesario indicar puerto en host si usamos el default, 1521
        // Try-with-resources. Se cierra la conexión automáticamente al salir del bloque try
        try(Connection connection = new MySqlConnector("localhost", DATABASE).getConnection()) {

            log.info("Conexión establecida con la base de datos " + DATABASE);

            // Leemos los datos del fichero CSV
            //provinces = readDataProvinces();
            //municipalities = readDataMunicipalities();
            //localities = readDataLocalities();
            //operators = readDataOperators();
            fuels = readDataFuels();
            //stations = readDataStations();
            //prices = readDataPrices();

            // Introducimos los datos en la base de datos
            //intakeProvinces(connection, provinces);
            //intakeMunicipalities(connection, municipalities);
            //intakeLocalities(connection, localities);
            //intakeOperators(connection, operators);
            intakeFuels(connection, fuels);
            //intakeFuelStations(connection, fuelStations);
            //intakePrices(connection, prices);

        } catch (Exception e) {
            log.error("Error al tratar con la base de datos", e);
        }
    }

    private static List<MySqlProvinces> readDataProvinces() {

        // Try-with-resources. Se cierra el reader automáticamente al salir del bloque try
        // CSVReader nos permite leer el fichero CSV linea a linea
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader("Precios_EESS_terrestres.csv"))
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
            log.error("Error al leer el fichero Precios_EESS_terrestres", e);
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
                new FileReader("Precios_EESS_terrestres.csv"))
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
            log.error("Error al leer el fichero Precios_EESS_terrestres", e);
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
                new FileReader("Precios_EESS_terrestres.csv"))
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
                    // Buscamos en la lista de provincias para obtener el código.
                    int id_pro = 0;
                    for (MySqlMunicipalities municipalitie : municipalities) {
                        if (municipalitie.getName().equals(nextLine[1])) {
                            id_pro = municipalitie.getMun_id();
                        }
                    }
                    localitie = new MySqlLocalities(
                            (localities.size()+1),  // ID segun el contenido de la tabla.
                            id_pro,
                            nextLine[2]                 // Cogemos el dato de la columna localidad.
                    );
                    localities.add(localitie);
                }
            }
            return localities;

        } catch (IOException e) {
            log.error("Error al leer el fichero Precios_EESS_terrestres", e);
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

            // Comprobamos si el municipio existe
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
                new FileReader("Precios_EESS_terrestres.csv"))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(';').build()).build()) {

            // Saltamos la primera linea, que contiene los nombres de las columnas del CSV
            reader.skip(1);
            String[] nextLine;

            while((nextLine = reader.readNext()) != null) {

                MySqlOperators operator = null;

                for (MySqlOperators compare : operators) {
                    if (compare.getName().equals(nextLine[24])) {
                        operator = compare;
                        break;
                    }
                }

                if (operator == null) {
                    operator = new MySqlOperators (
                            (operators.size()+1),   // ID segun el contenido de la tabla.
                            nextLine[24]             // Cogemos el dato de la columna provincia.
                    );
                    operators.add(operator);
                }
            }
            return operators;

        } catch (IOException e) {
            log.error("Error al leer el fichero Precios_EESS_terrestres", e);
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

            // Comprobamos si el municipio existe
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
                new FileReader("Precios_EESS_terrestres.csv"))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(';').build()).build()) {

            String[] nextLine = reader.readNext();
            int i = 9;

            while(i <= 23) {

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
                            nextLine[i]             // Cogemos el dato de la columna provincia.
                    );
                    fuels.add(fuel);
                }
                i++;
            }
            return fuels;

        } catch (IOException e) {
            log.error("Error al leer el fichero Precios_EESS_terrestres", e);
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

            // Comprobamos si el municipio existe
            PreparedStatement selectStatementFuels = connection.prepareStatement(selectSqlFuels);
            selectStatementFuels.setString(1, fuel.getName()); // Nombre del operador

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

    /**
     * Rellena los parámetros de un PreparedStatement para una consulta INSERT.
     *
     * @param statement - PreparedStatement
     * @param provinces - Departamento
     * @throws SQLException - Error al rellenar los parámetros
     */
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

    /**
     * Devuelve el último id de una columna de una tabla.
     * Util para obtener el siguiente id a insertar.
     *
     * @param connection - Conexión a la base de datos
     * @param table - Nombre de la tabla
     * @param fieldName - Nombre de la columna
     * @return - Último id de la columna
     * @throws SQLException - Error al ejecutar la consulta
     */
    private static int lastId(Connection connection, String table, String fieldName) throws SQLException {
        String selectSql = "SELECT MAX(?) FROM ?";
        PreparedStatement selectStatement = connection.prepareStatement(selectSql);
        selectStatement.setString(1, fieldName);
        selectStatement.setString(2, table);
        ResultSet resultSet = selectStatement.executeQuery();
        resultSet.next(); // Nos movemos a la primera fila
        return resultSet.getInt(1);
    }
}
