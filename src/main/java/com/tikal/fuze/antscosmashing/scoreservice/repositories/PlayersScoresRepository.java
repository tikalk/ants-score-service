package com.tikal.fuze.antscosmashing.scoreservice.repositories;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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


    public Integer getAfterCheckingGameAndTeamIds(int playerId, int teamId, int gameId, int userId){
        Item playerScoreItem = getTable().getItem("playerId", playerId);
        if(playerScoreItem==null)
            return null;
        if(playerScoreItem.getInt("teamId") != teamId || playerScoreItem.getInt("gameId")!=gameId || playerScoreItem.getInt("userId")!=userId){
            logger.warn("We got wrong input, as playerId had different teamId or gameId than before. We will override previous data and treat the previous score as 0, as we assume its a new player. Previous Item is {}, while current input is playerId {}, teamId {}, gameId {}. userId {}",playerScoreItem,playerId,teamId,gameId,userId);
            return 0;
        }
        return playerScoreItem.getInt("score");
    }

    private Table getTable() {
        return dynamoDb.getTable(tableName);
    }


    public void put(int playerId, int gameId,int userId,int teamId,int score){
        getTable()
                .putItem(new PutItemSpec().withItem(new Item()
                        .withInt("playerId", playerId)
                        .withInt("gameId", gameId)
                        .withInt("userId", userId)
                        .withInt("teamId", teamId)
                        .withInt("score", score)));
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
