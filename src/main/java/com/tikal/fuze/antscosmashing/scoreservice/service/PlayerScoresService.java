package com.tikal.fuze.antscosmashing.scoreservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikal.fuze.antscosmashing.scoreservice.repositories.PlayersScoresRepository;
import com.tikal.fuze.antscosmashing.scoreservice.repositories.SmashedAntsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

import static java.lang.System.getenv;

public class PlayerScoresService {
    private static final Logger logger = LogManager.getLogger(PlayerScoresService.class);

    private ObjectMapper mapper = new ObjectMapper();


    private PlayersScoresRepository playersScoresRepository;
    private SmashedAntsRepository smashedAntsRepository;

    private int hitScore=2;
    private int firstHitScore=4;
    private int selfHitScore=-2;
    private int firstSelfHitScore=-4;



    public PlayerScoresService() {
        playersScoresRepository = new PlayersScoresRepository();
        smashedAntsRepository = new SmashedAntsRepository();
    }

    public String getPlayersScores(String gameId)  {
        List<String> playersScores = playersScoresRepository.getPlayersScoresByGameId(Integer.valueOf(gameId));
        return playersScores.toString();
    }

    public String getTeamsScores(String gameId) throws JsonProcessingException {
        List<String> teamsScores = playersScoresRepository.getTeamsScoresByGameId(Integer.valueOf(gameId));
        return teamsScores.toString();
    }

    public void savePlayerScore(String hitTrialStr) throws IOException {
            logger.debug("Handling hitTrialStr: {}",hitTrialStr);
            setEnvVariables();
            JsonNode hitTrial = mapper.readTree(hitTrialStr);

            String type = hitTrial.get("type").textValue();
            String antId = hitTrial.get("antId").textValue();
            int playerId = hitTrial.get("playerId").intValue();
            int gameId = hitTrial.get("gameId").intValue();
            int userId = hitTrial.get("userId").intValue();
            int teamId = hitTrial.get("teamId").intValue();

            if (type.equals("miss"))
                handleMiss(playerId, antId);
            else if (type.equals("hit"))
                handleHitOrFirstHit(playerId, gameId, userId, teamId,antId, false);
            if (type.equals("selfHit"))
                handleHitOrFirstHit(playerId, gameId, userId, teamId,antId, true);
    }

    private void setEnvVariables() {
        hitScore = Integer.valueOf(getenv("HIT"));
        firstHitScore = Integer.valueOf(getenv("FIRST_HIT"));
        selfHitScore =  Integer.valueOf(getenv("SELF_HIT"));
        firstSelfHitScore = Integer.valueOf(getenv("FIRST_SELF_HIT"));
        logger.debug("hitScore:{}, firstHitScore:{}, selfHitScore:{}, firstSelfHitScore:{}",hitScore,firstHitScore,selfHitScore,selfHitScore);
    }

    private void handleMiss(int playerId, String antId) {
    }

    private void handleHitOrFirstHit(int playerId , int gameId, int userId, int teamId, String antId, boolean self) {
        int playerScore;
        int score;
        if (isSmashedNow(antId)) {
            smashedAntsRepository.put(antId, playerId);
            score = (self) ? firstSelfHitScore : firstHitScore;
            logger.debug("firstHit event:{}" , playerId);
        } else {
            score = (self) ? selfHitScore : hitScore;
            logger.debug("hit event:{}" , playerId);
        }
        playerScore = increasePlayerScore(playerId, gameId, userId, teamId, score);
        logger.debug("Updated player id {} to a new score {}" , playerId, playerScore);
    }

    private int increasePlayerScore(Integer playerId,int gameId,int userId,int teamId, int score) {
        Integer previousScore = playersScoresRepository.get(playerId);
//        Integer previousScore=null;
        if (previousScore == null)
            previousScore = 0;
        playersScoresRepository.put(playerId, gameId, userId, teamId,previousScore + score);
        return previousScore + score;
    }

    private boolean isSmashedNow(String antId) {
        return !(smashedAntsRepository.isExist(antId));
    }


}
