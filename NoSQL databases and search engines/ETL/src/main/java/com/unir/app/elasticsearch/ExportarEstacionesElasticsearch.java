package com.unir.app.elasticsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.unir.config.MySqlConnector;
import com.unir.dtos.Precio;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

/**
 * Clase para crear un JSON para importar a Elasticsearch
 */
@Slf4j
public class ExportarEstacionesElasticsearch {
    private static final String DATABASE = "laboratorio01";
    private static final String JSON_FILE = "estaciones.json";
    private static final String JSON_FILE_MAPPING = "estacionesMapping.json";

    private static String HOST_BONSAI_ELASTICSEARCH = "";

    public static void main(String[] args) {
        // Conectar a la base de datos
        try(Connection connection = new MySqlConnector("localhost", DATABASE).getConnection()) {

            // Crear JSON para empresas
            crearJsonEstaciones(connection);

            // Como es necesario la variable de entorno de Bonsai, si no viene cargada lanzamos una excepción
            HOST_BONSAI_ELASTICSEARCH = System.getenv("HOST_BONSAI_ELASTICSEARCH");
            log.debug("HOST_BONSAI_ELASTICSEARCH:{}",HOST_BONSAI_ELASTICSEARCH);
            if(HOST_BONSAI_ELASTICSEARCH == null){
                throw new RuntimeException("No se ha encontrado la variable de entorno HOST_BONSAI_ELASTICSEARCH");
            }

            // Crear índice en Elasticsearch
            crearIndiceEnElasticsearch();

            // Importar JSON a Elasticsearch
            importarJson();
        } catch (Exception e) {
            log.error("Error al tratar con la base de datos", e);
        }
    }

    /**
     * Crea un JSON para importar a Elasticsearch
     * @param connection
     * @throws SQLException
     * @throws IOException
     */
    private static void crearJsonEstaciones(Connection connection) throws SQLException, IOException {
        PreparedStatement selectEstaciones = connection.prepareStatement("select e.id as estacion_id,\n" +
                "       r.nombre as empresa,\n" +
                "       e.codigopostal, e.latitud, e.longitud, e.localidad_id, e.margen, e.direccion,\n" +
                "       l.nombre as localidad,\n" +
                "       m.nombre as municipio,\n" +
                "       p.nombre as provincia,\n" +
                "       e.fechaprecios, e.horario, e.tipoestacion,\n" +
                "       tv.nombre as tipo_venta\n" +
                "from estaciones e\n" +
                "inner join localidades l on e.localidad_id = l.id\n" +
                "inner join municipios m on l.municipio_id = m.id\n" +
                "inner join provincias p on m.provincia_id = p.id\n" +
                "inner join rotulos r on e.rotulo_id = r.id\n" +
                "inner join tipo_ventas tv on e.tipoventa_id = tv.id;");

        ResultSet estaciones = selectEstaciones.executeQuery();
        
        // Creamos el archivo JSON
        try (FileWriter writer = new FileWriter(JSON_FILE)) {
            // Creamos el objeto Gson
            Gson gson = new GsonBuilder().create();

            log.info("----------------------------------- INICIO -------------------------------------------");
            log.info("Creando fichero JSON ...");
            while (estaciones.next()) {
                // Creamos el objeto JSON para la propiedad indice de estaciones
                JsonObject indexObject = new JsonObject();
                indexObject.addProperty("_index", "estaciones");

                // Creamos el objeto JSON para el índice de elasticsearch
                JsonObject indexPrincipal = new JsonObject();
                indexPrincipal.add("index", indexObject);

                // Escribimos el índice de elasticsearch en el fichero JSON
                writer.write(gson.toJson(indexPrincipal) + "\n");

                // Creamos el objeto JSON para la estación
                JsonObject empresaJsonObject = new JsonObject();

                // Añadimos las propiedades de la estación
                empresaJsonObject.addProperty("Empresa", estaciones.getString("empresa"));
                empresaJsonObject.addProperty("Direccion", estaciones.getString("direccion"));
                empresaJsonObject.addProperty("CodigoPostal", estaciones.getString("codigopostal"));
                empresaJsonObject.addProperty("Latitud", estaciones.getBigDecimal("latitud"));
                empresaJsonObject.addProperty("Longitud", estaciones.getBigDecimal("longitud"));
                empresaJsonObject.addProperty("Localidad", estaciones.getString("localidad"));
                empresaJsonObject.addProperty("Municipio", estaciones.getString("municipio"));
                empresaJsonObject.addProperty("Provincia", estaciones.getString("provincia"));
                empresaJsonObject.addProperty("FechaPrecios", estaciones.getDate("fechaprecios").toString());
                empresaJsonObject.addProperty("Horario", estaciones.getString("horario"));
                empresaJsonObject.addProperty("TipoEstacion", estaciones.getString("tipoestacion"));
                empresaJsonObject.addProperty("TipoVenta", estaciones.getString("tipo_venta"));
                empresaJsonObject.addProperty("Margen", estaciones.getString("margen"));

                // Obtener precios para esta estación.
                String estacionId = estaciones.getString("estacion_id");
                List<Precio> precios = obtenerPreciosPorEstacion(connection, estacionId);

                // Convertimos la lista de precios a JSON
                Type listType = new TypeToken<List<Precio>>() {
                }.getType();
                String preciosJson = gson.toJson(precios, listType);

                // Añadimos la propiedad precios al objeto JSON de la estación
                empresaJsonObject.add("Precios", gson.fromJson(preciosJson, JsonElement.class));

                // Escribimos el objeto JSON de la estación en el fichero JSON
                writer.write(gson.toJson(empresaJsonObject) + "\n");
            }


            // se escribe el fichero fisicamente en raiz del proyecto
            writer.flush();

            log.info("Se ha creado el fichero estaciones.json en la raíz del proyecto");
            log.info("----------------------------------- FIN -------------------------------------------");
        }
    }

    /**
     * Obtiene los precios para una estación
     * @param conexion
     * @param estacionId
     * @return Lista de precios
     */
    private static List<Precio> obtenerPreciosPorEstacion(Connection conexion, String estacionId) {
        // Obtener precios para esta estación
        List<Precio> precios = new ArrayList<>();
        String selectPrecios = "SELECT c.nombre as carburante, pr.precio " +
                "FROM precios pr " +
                "INNER JOIN carburantes c ON pr.carburante_id = c.id " +
                "WHERE pr.estacion_id = ?";

        try (PreparedStatement preparedStatement = conexion.prepareStatement(selectPrecios)) {
            preparedStatement.setString(1, estacionId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String carburante = resultSet.getString("carburante");
                    double precio = resultSet.getDouble("precio");
                    precios.add(new Precio(carburante, precio));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return precios;
    }

    /**
     * Crea el índice en Elasticsearch
     */
    private  static void crearIndiceEnElasticsearch(){
        // Creamos la URL para crear el índice
        String url = String.format("%s/estaciones", HOST_BONSAI_ELASTICSEARCH);

        // Leer el contenido del archivo de mapeo
        StringBuilder mappingJson = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(JSON_FILE_MAPPING))) {
            String line;
            while ((line = reader.readLine()) != null) {
                mappingJson.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Creamos el cliente HTTP
        HttpClient client = HttpClients.createDefault();
        HttpPut putRequest = new HttpPut(url);
        putRequest.setEntity(new StringEntity(mappingJson.toString(), ContentType.APPLICATION_JSON));


        try {
            HttpResponse response = client.execute(putRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("Status Code: " + statusCode);
            // Aquí puedes manejar la respuesta
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /**
     * Importa el JSON a Elasticsearch
     * @throws IOException
     */
    private static void importarJson() throws IOException {
        // Creamos la URL para importar el JSON
        String url = String.format("%s/_bulk", HOST_BONSAI_ELASTICSEARCH);

        // Creamos el archivo JSON
        File file = new File(JSON_FILE);

        // Creamos el cliente HTTP para enviar el JSON a Elasticsearch/Bonsai
        HttpClient client = HttpClients.createDefault();
        HttpPut putRequest = new HttpPut(url);
        putRequest.setEntity(new FileEntity(file, ContentType.APPLICATION_JSON));

        try {
            HttpResponse response = client.execute(putRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("Status Code: " + statusCode);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
