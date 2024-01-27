package com.unir.app.write;

import com.unir.config.MongoConnector;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class InsertarDocMongo {
    public static void main(String[] args) {
        String databaseName = "Gasolina";
        String collectionName = "LaGasolinera";
        MongoConnector mongoConnector = new MongoConnector(databaseName, collectionName);

        try{
            MongoDatabase database = mongoConnector.getDatabase();
            MongoCollection<Document> collection = database.getCollection(collectionName);
            // Inserción de datos
            Document nuevoDocumento = new Document("Empresa", "OIL EXPRESS")
                    .append("Direccion", "AVENIDA PRINCIPAL, 123")
                    .append("CodigoPostal", "12345")
                    .append("Latitud", 40.123456)
                    .append("Longitud", -3.987654)
                    .append("Localidad", "CIUDAD DEL PETRÓLEO")
                    .append("Municipio", "PETROVILLE")
                    .append("Provincia", "PETROLANDIA")
                    .append("FechaPrecios", "2023-11-15")
                    .append("Horario", "L-D: 06:00 - 22:00")
                    .append("TipoEstacion", "Urbana")
                    .append("TipoVenta", "Gasolina y Diesel")
                    .append("Margen", "S")
                    .append("Precios", Document.parse("{ \"carburante\": \"gasolina 95\", \"precio\": 1.349 }, { \"carburante\": \"gasóleo A\", \"precio\": 1.199 }"));

            collection.insertOne(nuevoDocumento);

        } finally{
            mongoConnector.close();
        }
    }
}
