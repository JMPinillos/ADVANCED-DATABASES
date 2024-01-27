package com.unir.config;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;
@Getter
@Slf4j
public class MongoConnector {
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public MongoConnector(String databaseName, String collectionName){
        log.debug("Connectando a MongoDB");
        try{
            this.mongoClient = MongoClients.create(System.getenv("MONGO_URL"));
            this.database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            log.debug("Conexion existosa");

        }catch(MongoException e){
            log.error("Error al conectar con la base de datos", e);
            throw new RuntimeException(e);
        }
    }
    public void close(){
        log.debug("Cerrando conexion con MongoDB");
        this.mongoClient.close();
    }
}