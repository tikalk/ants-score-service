package com.tikal.fuze.antscosmashing.scoreservice.repositories;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DbManager {
    private static final Logger logger = LogManager.getLogger(DbManager.class);
    private static DbManager dbManager;
    private DynamoDB dynamoDb;
    private  String smashedAntsTableName;
    private  String playersScoreTableName;
    private  String gameIdScoreIndexName;
    private String db_region;
    private String teamsScoreTableName;


    private DbManager() {
        loadTableNames();
        initDynamoDbClient();
    }

    public static synchronized DbManager getInstance(){
        if(dbManager==null)
            dbManager = new DbManager();
        return dbManager;
    }

    public String getSmashedAntsTableName() {
        return smashedAntsTableName;
    }

    public String getPlayersScoreTableName() {
        return playersScoreTableName;
    }

    public String getGameIdScoreIndexName() {
        return gameIdScoreIndexName;
    }

    public DynamoDB getDynamoDb() {
        return dynamoDb;
    }

    private void loadTableNames() {
        try {
            Properties prop = new Properties();
            InputStream inputStream =
                    DbManager.class.getClassLoader().getResourceAsStream("config.properties");

            prop.load(inputStream);
            smashedAntsTableName = prop.getProperty("smashedAnts_tableName");
            playersScoreTableName = prop.getProperty("playersScore_tableName");
            teamsScoreTableName = prop.getProperty("teamsScore_tableName");
            gameIdScoreIndexName = prop.getProperty("gameIdScoreIndexName");
            db_region = prop.getProperty("db_region");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initDynamoDbClient() {
        logger.debug("Connecting to the DB...");
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(Regions.fromName(db_region)));
        dynamoDb = new DynamoDB(client);
    }

    public String getTeamsScoreTableName() {
        return teamsScoreTableName;
    }
}
