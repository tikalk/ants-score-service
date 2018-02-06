package com.tikal.fuze.antscosmashing.scoreservice.repositories;

import com.amazonaws.services.dynamodbv2.document.*;

import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder;
import com.amazonaws.services.dynamodbv2.xspec.IfNotExistsFunction;
import com.amazonaws.services.dynamodbv2.xspec.N;
import com.amazonaws.services.dynamodbv2.xspec.UpdateItemExpressionSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toList;

public class PlayersScoresRepository {
    private static final Logger logger = LogManager.getLogger(PlayersScoresRepository.class);
    private final String gameIdScoreIndexName;

    private ObjectMapper mapper = new ObjectMapper();

    private DynamoDB dynamoDb;
    private String tableName;

    public PlayersScoresRepository(){
        dynamoDb=DbManager.getInstance().getDynamoDb();
        tableName =DbManager.getInstance().getPlayersScoreTableName();
        gameIdScoreIndexName = DbManager.getInstance().getGameIdScoreIndexName();
    }


    private Table getTable() {
        return dynamoDb.getTable(tableName);
    }


    public void put(int playerId, int score){
        //put a new record with score 0 in case it doesn't exist
        getTable().updateItem(new UpdateItemSpec()
                .withPrimaryKey("playerId",playerId)
                .withExpressionSpec(
                        new ExpressionSpecBuilder()
                        .addUpdate(N("score").set(if_not_exists("score", 0))).buildForUpdate())
        );

        //Add the new score
        getTable().updateItem(new UpdateItemSpec()
                .withPrimaryKey("playerId",playerId)
                .withExpressionSpec(
                        new ExpressionSpecBuilder()
                                .addUpdate(N("score").set(N("score").plus(score))).buildForUpdate())
        );


    }


    public List<String> getPlayersScoresByGameId(int gameId)  {
        List<String> playersScores = getPlayerScoresItemsStream(gameId)
                .map(i ->
                        mapper.createObjectNode().put("id", i.getInt("playerId")).put("score", i.getInt("score"))
                ).map(on -> on.toString()).collect(toList());
        logger.debug("Players for gameId {} are {}",gameId,playersScores);
        return playersScores;
    }

    public List<String> getTeamsScoresByGameId(int gameId) throws JsonProcessingException {
        Map<Integer, Integer> teamsScoresMap = getPlayerScoresItemsStream(gameId)
                .collect(groupingBy(i -> i.getInt("teamId"), summingInt(i -> i.getInt("score"))));

        Map<Integer, Integer> finalMap = new LinkedHashMap<>();
        teamsScoresMap.entrySet().stream()
            .sorted(Map.Entry.<Integer, Integer>comparingByValue()
                .reversed())
                .forEachOrdered(e -> finalMap.put(e.getKey(), e.getValue()));

        List<String> teamsScores = finalMap.entrySet().stream().map(e -> mapper.createObjectNode().put("id", e.getKey()).put("score", e.getValue()))
                .map(on -> on.toString()).collect(toList());

        logger.debug("Teams scores for gameId {} are {}",gameId,teamsScores);
        return teamsScores;
    }

    private Stream<Item> getPlayerScoresItemsStream(int gameId) {
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("gameId = :gameId")
                .withValueMap(new ValueMap()
                        .withInt(":gameId", gameId))
                .withScanIndexForward(false);

        ItemCollection<QueryOutcome> items = getTable().getIndex(gameIdScoreIndexName).query(spec);
        Iterable<Item> iterable = () -> items.iterator();
        return StreamSupport.stream(iterable.spliterator(), false);
    }

}
