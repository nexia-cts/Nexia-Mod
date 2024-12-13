package com.nexia.core.utilities.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.fabricmc.loader.api.FabricLoader;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import static com.nexia.core.NexiaCore.config;

@SuppressWarnings("unused")
public class MongoManager {
    private final static Gson GSON = new GsonBuilder().create();

    private final ExecutorService service;

    private MongoClient client;
    private MongoDatabase database;

    public MongoManager() {
        this.service = Executors.newCachedThreadPool();
        this.openConnection();
    }

    public void openConnection() {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) return;
        final MongoCredential mongoCredential = MongoCredential.createCredential(
                config.username,
                config.database,
                config.password.toCharArray()
        );

        String connectionString = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s",
                config.username,
                config.password,
                config.host,
                config.port,
                config.database,
                "admin" // Authentication database, change this if your user is created in another database
        );

        // Configure the MongoClientSettings
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .credential(mongoCredential)
                .applyConnectionString(new ConnectionString(connectionString))
                .build();

        try {
            this.client = MongoClients.create(mongoClientSettings);
            this.database = this.client.getDatabase(config.database);
            System.out.println("Connection to MongoDB established successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to connect to MongoDB: " + e.getMessage());
        }
    }

    public void closeConnection() {
        if (this.client != null) {
            this.client.close();
        }
    }

    public <T> void insertObject(String collection, T type) {
        getCollection(collection)
                .insertOne(toDocument(type));
    }

    public <T> void replaceObject(String collection, Bson filter, T type) {
        getCollection(collection)
                .replaceOne(filter, toDocument(type));
    }

    public <T> T getObject(String collection, Bson filter, Class<T> aClass) {
        final Document document = getCollection(collection)
                .find(filter)
                .first();

        if (document == null) {
            return null;
        }

        return toObject(document, aClass);
    }

    public void runAsync(Runnable runnable) {
        this.service.submit(runnable);
    }

    public <T> Future<T> callAsync(Callable<T> callable) {
        return this.service.submit(callable);
    }

    public <T> T toObject(Document document, Class<T> aClass) {
        return GSON.fromJson(GSON.toJson(document), aClass);
    }

    public <T> Document toDocument(T type) {
        return GSON.fromJson(GSON.toJson(type), Document.class);
    }

    public MongoClient getClient() {
        return this.client;
    }

    public MongoDatabase getDatabase() {
        return this.database;
    }

    public MongoCollection<Document> getCollection(String collection) {
        return this.database.getCollection(collection);
    }

    public void shutdown() {
        this.client.close();
    }
}
