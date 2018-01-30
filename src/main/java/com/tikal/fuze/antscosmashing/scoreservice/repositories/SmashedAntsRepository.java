package com.tikal.fuze.antscosmashing.scoreservice.repositories;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;

public class SmashedAntsRepository {

    private DynamoDB dynamoDb;
    private String tableName;

    public SmashedAntsRepository(){
        dynamoDb=DbManager.getInstance().getDynamoDb();
        tableName =DbManager.getInstance().getSmashedAntsTableName();
    }

    public boolean isExist(String antId){
        return dynamoDb.getTable(tableName).getItem("antId", antId) !=null;
    }

    public Integer get(String antId){
        return dynamoDb.getTable(tableName).getItem("antId", antId).getInt("playerId");
    }


    public void put(String antId, int playerId){
        dynamoDb.getTable(tableName)
                .putItem(new PutItemSpec().withItem(new Item()
                        .withString("antId", antId)
                        .withInt("playerId", playerId)));
    }

}
