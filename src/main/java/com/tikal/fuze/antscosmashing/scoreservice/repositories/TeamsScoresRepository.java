package com.tikal.fuze.antscosmashing.scoreservice.repositories;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder.N;
import static com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder.if_not_exists;
import static java.util.stream.Collectors.toList;

public class TeamsScoresRepository {
    private static final Logger logger = LogManager.getLogger(TeamsScoresRepository.class);
    private final String gameIdScoreIndexName;

    private ObjectMapper mapper = new ObjectMapper();

    private DynamoDB dynamoDb;
    private String tableName;

    public TeamsScoresRepository(){
        dynamoDb=DbManager.getInstance().getDynamoDb();
        tableName =DbManager.getInstance().getTeamsScoreTableName();
        gameIdScoreIndexName = DbManager.getInstance().getGameIdScoreIndexName();
    }


    private Table getTable() {
        return dynamoDb.getTable(tableName);
    }


    public void put(int teamId,int gameId, int score){
        //put a new record with score 0 in case it doesn't exist
        getTable().updateItem(new UpdateItemSpec()
                .withPrimaryKey("teamId",teamId)
                .withExpressionSpec(
                        new ExpressionSpecBuilder()
                                .addUpdate(N("score").set(if_not_exists("score", score)))
                                .addUpdate(N("gameId").set(gameId))
                                .buildForUpdate())
        );

        //Add the new score
        getTable().updateItem(new UpdateItemSpec()
                .withPrimaryKey("teamId",teamId)
                .withExpressionSpec(
                        new ExpressionSpecBuilder()
                                .addUpdate(N("score").set(N("score").plus(score))).buildForUpdate())
        );


    }


    public List<String> getTeamsScoresByGameId(int gameId)  {
        List<String> teamsScores = getTeamScoresItemsStream(gameId)
                .map(i ->
                        mapper.createObjectNode().put("id", i.getInt("teamId")).put("score", i.getInt("score"))
                ).map(on -> on.toString()).collect(toList());
        logger.debug("Teams for gameId {} are {}",gameId,teamsScores);
        return teamsScores;
    }



    private Stream<Item> getTeamScoresItemsStream(int gameId) {
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
