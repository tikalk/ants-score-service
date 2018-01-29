package com.tikal.fuze.antscosmashing.scoreservice.repositories;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DbManager {
    private static Regions REGION = Regions.US_WEST_2;
    private static final Logger logger = LogManager.getLogger(DbManager.class);
    private static DynamoDB dynamoDb;


    public static synchronized DynamoDB getDB(){
        if(dynamoDb==null)
            initDynamoDbClient();
        return dynamoDb;
    }

    private static void initDynamoDbClient() {
        logger.debug("Connecting to the DB...");
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(REGION));
        dynamoDb = new DynamoDB(client);
    }
}
