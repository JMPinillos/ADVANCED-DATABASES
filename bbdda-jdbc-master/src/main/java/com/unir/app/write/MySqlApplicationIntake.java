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
            //List<MySqlMunicipalities> provinces = readDataProvinces();
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

            // Creamos la lista de provincias
            List<MySqlProvinces> provinces = new LinkedList<>();

            // Saltamos la primera linea, que contiene los nombres de las columnas del CSV
            reader.skip(1);
            String[] nextLine;

            // Leemos el fichero linea a linea
            while((nextLine = reader.readNext()) != null) {

                // Creamos el departamento y lo añadimos a la lista
                MySqlProvinces province = new MySqlProvinces(
                        nextLine[0]
                );
                provinces.add(province);
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

        String selectSql = "SELECT COUNT(*) FROM provinces WHERE name = ?";
        String insertSql = "INSERT INTO provinces (name)"
                + "VALUES (?)";

        int lote = 5;
        int contador = 0;

        // Preparamos las consultas, una unica vez para poder reutilizarlas en el batch
        PreparedStatement insertStatement = connection.prepareStatement(insertSql);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (MySqlProvinces province : provinces) {

            // Comprobamos si el empleado existe
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            selectStatement.setString(1, province.getName()); // Nombre de la provincia

            ResultSet resultSet = selectStatement.executeQuery();
            resultSet.next(); // Nos movemos a la primera fila
            int rowCount = resultSet.getInt(1);

            // Si no existe, insertamos. Si existe, no hacemos nada.
            if(rowCount > 0) {
                //fillUpdateStatement(updateStatement, employee);
                //updateStatement.addBatch();
            } else {
                fillInsertStatementProvinces(insertStatement, province);
                insertStatement.addBatch();
            }

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
     * Rellena los parámetros de un PreparedStatement para una consulta INSERT.
     *
     * @param statement - PreparedStatement
     * @param provinces - Departamento
     * @throws SQLException - Error al rellenar los parámetros
     */
    private static void fillInsertStatementProvinces(PreparedStatement statement, MySqlProvinces provinces) throws SQLException {
        statement.setString(1, provinces.getName());
    }
/*
    private static List<MySqlFuel> readData() {

        // Try-with-resources. Se cierra el reader automáticamente al salir del bloque try
        // CSVReader nos permite leer el fichero CSV linea a linea
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader("Empleados.csv"))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(',').build()).build()) {

            // Creamos la lista de empleados y el formato de fecha
            List<MySqlFuel> employees = new LinkedList<>();
            SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd");

            // Saltamos la primera linea, que contiene los nombres de las columnas del CSV
            reader.skip(1);
            String[] nextLine;

            // Leemos el fichero linea a linea
            while((nextLine = reader.readNext()) != null) {

                // Creamos el empleado y lo añadimos a la lista
                MySqlFuel employee = new MySqlFuel(
                        Integer.parseInt(nextLine[0]),
                        nextLine[1],
                        nextLine[2],
                        nextLine[3],
                        new Date(format.parse(nextLine[4]).getTime()),
                        new Date(format.parse(nextLine[5]).getTime()),
                        nextLine[6],
                        new Date(format.parse(nextLine[7]).getTime()),
                        new Date(format.parse(nextLine[8]).getTime())
                );
                employees.add(employee);
            }
            return employees;
        } catch (IOException e) {
            log.error("Error al leer el fichero CSV", e);
            throw new RuntimeException(e);
        } catch (CsvValidationException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Introduce los datos en la base de datos.
     * Si el empleado ya existe, se actualiza.
     * Si no existe, se inserta.
     *
     * Toma como referencia el campo emp_no para determinar si el empleado existe o no.
     * @param connection - Conexión a la base de datos
     * @param employees - Lista de empleados
     * @throws SQLException - Error al ejecutar la consulta
     */
/*    private static void intake(Connection connection, List<MySqlFuel> employees) throws SQLException {

        // Consultas para los empleados
        String selectSql = "SELECT COUNT(*) FROM employees WHERE emp_no = ?";
        String insertSql = "INSERT INTO employees (emp_no, first_name, last_name, gender, hire_date, birth_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE employees SET first_name = ?, last_name = ?, gender = ?, hire_date = ?, birth_date = ? WHERE emp_no = ?";

        // Consultas para la tabla "dept_emp", que relaciona a los empleados con los departamentos.
        String selectSqlDeptEmp = "SELECT COUNT(*) FROM dept_emp WHERE emp_no = ? and dept_no = ?";
        String insertSqlDeptEmp = "INSERT INTO dept_emp (emp_no, dept_no, from_date, to_date) "
                + "VALUES (?, ?, ?, ?)";
        String updateSqlDeptEmp = "UPDATE dept_emp SET from_date = ?, to_date = ? WHERE emp_no = ? and dept_no = ?";

        int lote = 5;
        int contador = 0;

        // Preparamos las consultas, una unica vez para poder reutilizarlas en el batch
        PreparedStatement insertStatement = connection.prepareStatement(insertSql);
        PreparedStatement updateStatement = connection.prepareStatement(updateSql);
        PreparedStatement insertStatementDeptEmp = connection.prepareStatement(insertSqlDeptEmp);
        PreparedStatement updateStatementDeptEmp = connection.prepareStatement(updateSqlDeptEmp);

        // Desactivamos el autocommit para poder ejecutar el batch y hacer commit al final
        connection.setAutoCommit(false);

        for (MySqlFuel employee : employees) {

            // Comprobamos si el empleado existe
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            selectStatement.setInt(1, employee.getEmployeeId()); // Código del empleado
            ResultSet resultSet = selectStatement.executeQuery();
            resultSet.next(); // Nos movemos a la primera fila
            int rowCount = resultSet.getInt(1);

            // Si existe, actualizamos. Si no, insertamos
            if(rowCount > 0) {
                fillUpdateStatement(updateStatement, employee);
                updateStatement.addBatch();
            } else {
                fillInsertStatement(insertStatement, employee);
                insertStatement.addBatch();
            }

            // Comprobamos si la relación entre departamento y empleado existe
            PreparedStatement selectStatementDeptEmp = connection.prepareStatement(selectSqlDeptEmp);
            selectStatementDeptEmp.setInt(1, employee.getEmployeeId()); // ID del empleado
            selectStatementDeptEmp.setString(2, employee.getDept_no()); // ID del departamento

            ResultSet resultSetDeptEmp = selectStatementDeptEmp.executeQuery();
            resultSetDeptEmp.next(); // Nos movemos a la primera fila
            int rowCountDeptEmp = resultSetDeptEmp.getInt(1);

            // Si existe, actualizamos. Si no, insertamos
            if(rowCountDeptEmp > 0) {
                fillUpdateStatementDeptEmp(updateStatementDeptEmp, employee);
                updateStatementDeptEmp.addBatch();
            } else {
                fillInsertStatementDeptEmp(insertStatementDeptEmp, employee);
                insertStatementDeptEmp.addBatch();
            }

            // Ejecutamos el batch cada lote de registros
            if (++contador % lote == 0) {
                updateStatement.executeBatch();
                insertStatement.executeBatch();
                updateStatementDeptEmp.executeBatch();
                insertStatementDeptEmp.executeBatch();
            }
        }

        // Ejecutamos el batch final
        insertStatement.executeBatch();
        updateStatement.executeBatch();
        updateStatementDeptEmp.executeBatch();
        insertStatementDeptEmp.executeBatch();

        // Hacemos commit y volvemos a activar el autocommit
        connection.commit();
        connection.setAutoCommit(true);
    }

    /**
     * Rellena los parámetros de un PreparedStatement para una consulta INSERT.
     *
     * @param statement - PreparedStatement
     * @param employee - Empleado
     * @throws SQLException - Error al rellenar los parámetros
     */
/*    private static void fillInsertStatement(PreparedStatement statement, MySqlFuel employee) throws SQLException {
        statement.setInt(1, employee.getEmployeeId());
        statement.setString(2, employee.getFirstName());
        statement.setString(3, employee.getLastName());
        statement.setString(4, employee.getGender());
        statement.setDate(5, employee.getHireDate());
        statement.setDate(6, employee.getBirthDate());
    }

    /**
     * Rellena los parámetros de un PreparedStatement para una consulta UPDATE.
     *
     * @param statement - PreparedStatement
     * @param employee - Empleado
     * @throws SQLException - Error al rellenar los parámetros
     */
/*    private static void fillUpdateStatement(PreparedStatement statement, MySqlFuel employee) throws SQLException {
        statement.setString(1, employee.getFirstName());
        statement.setString(2, employee.getLastName());
        statement.setString(3, employee.getGender());
        statement.setDate(4, employee.getHireDate());
        statement.setDate(5, employee.getBirthDate());
        statement.setInt(6, employee.getEmployeeId());
    }

    /**
     * Tabla
     * Rellena los parámetros de un PreparedStatement para una consulta INSERT.
     *
     * @param statement - PreparedStatement
     * @param employee - Empleado
     * @throws SQLException - Error al rellenar los parámetros
     */
/*    private static void fillInsertStatementDeptEmp(PreparedStatement statement, MySqlFuel employee) throws SQLException {
        statement.setInt(1, employee.getEmployeeId());
        statement.setString(2, employee.getDept_no());
        statement.setDate(3, employee.getFromDate());
        statement.setDate(4, employee.getToDate());
    }

    /**
     * Rellena los parámetros de un PreparedStatement para una consulta UPDATE.
     *
     * @param statement - PreparedStatement
     * @param employee - Empleado
     * @throws SQLException - Error al rellenar los parámetros
     */
 /*   private static void fillUpdateStatementDeptEmp(PreparedStatement statement, MySqlFuel employee) throws SQLException {
        statement.setDate(1, employee.getFromDate());
        statement.setDate(2, employee.getToDate());
        statement.setInt(3, employee.getEmployeeId());
        statement.setString(4, employee.getDept_no());
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
