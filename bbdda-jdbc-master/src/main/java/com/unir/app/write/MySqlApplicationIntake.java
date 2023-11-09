package com.unir.app.write;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.unir.config.MySqlConnector;
import com.unir.model.MySqlFuelStations;
import com.unir.model.MySqlFuel;
import com.unir.model.MySqlMunicipalities;
import com.unir.model.MySqlProvinces;
import lombok.extern.slf4j.Slf4j;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * La version para Oracle seria muy similar a esta, cambiando únicamente el Driver y los datos de sentencias.
 * La tabla de Oracle contiene muchas restricciones y triggers. Por simplicidad, usamos MySQL en este caso.
 */
@Slf4j
public class MySqlApplicationIntake {

    private static final String DATABASE = "laboratorio_EESS";

    public static void main(String[] args) {

        // Creamos conexion. No es necesario indicar puerto en host si usamos el default, 1521
        // Try-with-resources. Se cierra la conexión automáticamente al salir del bloque try
        try(Connection connection = new MySqlConnector("localhost", DATABASE).getConnection()) {

            log.info("Conexión establecida con la base de datos " + DATABASE);

            // Leemos los datos del fichero CSV de los departamentos
            List<MySqlProvinces> provinces = readDataProvinces();
            //List<MySqlMunicipalities> municipalities = readDataProvinces();
            // Introducimos los datos en la base de datos
            intakeProvinces(connection, provinces);

            //List<MySqlFuel> employees = readData();

            // Introducimos los datos en la base de datos
            //intake(connection, employees);

        } catch (Exception e) {
            log.error("Error al tratar con la base de datos", e);
        }
    }

    /**
     * Lee los datos del fichero CSV y los devuelve en una lista de empleados.
     * El fichero CSV debe estar en la raíz del proyecto.
     *
     * @return - Lista de empleados
     */

    private static List<MySqlProvinces> readDataProvinces() {

        // Try-with-resources. Se cierra el reader automáticamente al salir del bloque try
        // CSVReader nos permite leer el fichero CSV linea a linea
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader("Precios_EESS_terrestres.csv"))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(';').build()).build()) {

            // Creamos las listas
            List<MySqlProvinces> provinces = new LinkedList<>();
            List<MySqlMunicipalities> municipalities = new LinkedList<>();

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

                MySqlMunicipalities municipalitie = null;

                for (MySqlMunicipalities compare : municipalities) {
                    if (compare.getName().equals(nextLine[1])) {
                        municipalitie = compare;
                        break;
                    }
                }

                if (province == null) {
                    MySqlMunicipalities municipalitie = new MySqlMunicipalities(
                            (municipalities.size()+1),  // ID segun el contenido de la tabla. 
                            nextLine[1]                 // Cogemos el dato de la columna municipio. 
                    );
                    municipalities.add(municipalitie);
                }
            }
            return provinces;
            //return municipalities;

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

    private static void intakeMunicipalities(Connection connection, List<MySqlMunicipalities> municipalities) throws SQLException {

        // Consultas de la tabla provincias para coger el id
        String selectSqlProvinces = "SELECT id_pro FROM provinces WHERE name = ?";

        // Consultas de la tabla Municipios
        String insertSqlMunicipalities = "INSERT INTO municipalities (mun_id, pro_id, name)"
                + "VALUES (?, ?, ?)";

        int lote = 5;
        int contador = 0;

        // Preparamos las consultas, una unica vez para poder reutilizarlas en el batch
        PreparedStatement selectStatementIdProvincesMun = connection.prepareStatement(selectSqlProvinces);
        PreparedStatement insertStatementMunicipalities = connection.prepareStatement(insertSqlMunicipalities);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (MySqlMunicipalities municipalitie : municipalities) {

            // Comprobamos si el municipio existe
            PreparedStatement selectStatementMunicipalities = connection.prepareStatement(selectSqlMunicipalities);
            selectStatementMunicipalities.setString(1, municipalitie.getName()); // Nombre del municipio

            ResultSet resultSet = selectStatementMunicipalities.executeQuery();
            resultSet.next(); // Nos movemos a la primera fila
            int rowCount = resultSet.getPro_id;

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

    private static void fillSelectStatementIdProvToIdMun(PreparedStatement statement, MySqlProvinces provinces) throws SQLException {
        statement.setString(2, provinces.getName());
    }

    private static void fillInsertStatementMunicipalities(PreparedStatement statement, MySqlMunicipalities municipalities) throws SQLException {
        statement.setInt(1, municipalities.getMun_id());
        statement.setName(2, fillSelectStatementIdProvToIdMun);
        //statement.setName(2, municipalities.getPro_id());
        statement.setString(3, municipalities.getName());
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
