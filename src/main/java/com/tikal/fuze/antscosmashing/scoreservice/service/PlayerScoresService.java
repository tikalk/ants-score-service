package com.tikal.fuze.antscosmashing.scoreservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikal.fuze.antscosmashing.scoreservice.repositories.PlayersScoresRepository;
import com.tikal.fuze.antscosmashing.scoreservice.repositories.SmashedAntsRepository;
import com.tikal.fuze.antscosmashing.scoreservice.repositories.TeamsScoresRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

import static java.lang.System.getenv;

public class PlayerScoresService {
    private static final Logger logger = LogManager.getLogger(PlayerScoresService.class);

    private ObjectMapper mapper = new ObjectMapper();


    private PlayersScoresRepository playersScoresRepository;
    private TeamsScoresRepository teamsScoresRepository;
    private SmashedAntsRepository smashedAntsRepository;

    private int hitScore;
    private int firstHitScore;
    private int selfHitScore;
    private int firstSelfHitScore;

    public PlayerScoresService(int hitScore, int firstHitScore, int selfHitScore, int firstSelfHitScore) {
        this();
        this.hitScore = hitScore;
        this.firstHitScore = firstHitScore;
        this.selfHitScore = selfHitScore;
        this.firstSelfHitScore = firstSelfHitScore;
    }

    public PlayerScoresService() {
        playersScoresRepository = new PlayersScoresRepository();
        teamsScoresRepository = new TeamsScoresRepository();
        smashedAntsRepository = new SmashedAntsRepository();
    }

    public String getPlayersScores(String gameId)  {
        List<String> playersScores = playersScoresRepository.getPlayersScoresByGameId(Integer.valueOf(gameId));
        return playersScores.toString();
    }

    public String getTeamsScores(String gameId) throws JsonProcessingException {
        List<String> teamsScores = teamsScoresRepository.getTeamsScoresByGameId(Integer.valueOf(gameId));
        return teamsScores.toString();
    }

    public void savePlayerScore(String hitTrialStr) throws IOException {
        logger.debug("Handling hitTrialStr: {}", hitTrialStr);
        setEnvVariables();
        JsonNode hitTrial = mapper.readTree(hitTrialStr);

        String type = hitTrial.get("type").textValue();
        String antId = null;
        if (!type.equals("miss"))
            antId = hitTrial.get("antId").textValue();
        int playerId = hitTrial.get("playerId").intValue();
        int teamId = hitTrial.get("teamId").intValue();
        int gameId = hitTrial.get("gameId").intValue();


        if (type.equals("miss"))
            handleMiss(hitTrialStr);
        else if (type.equals("hit"))
            handleHitOrFirstHit(playerId, antId, gameId, teamId, false);
        if (type.equals("selfHit"))
            handleHitOrFirstHit(playerId, antId, gameId, teamId, true);
    }

    private void setEnvVariables() {
        if(getenv("HIT")==null){
            logger.warn("HIT score is missing. we assume a testing mode so we fallback to default");
            logger.debug("hitScore:{}, firstHitScore:{}, selfHitScore:{}, firstSelfHitScore:{}",hitScore,firstHitScore,selfHitScore,selfHitScore);
            return;
        }

        hitScore = Integer.valueOf(getenv("HIT"));
        firstHitScore = Integer.valueOf(getenv("FIRST_HIT"));
        selfHitScore =  Integer.valueOf(getenv("SELF_HIT"));
        firstSelfHitScore = Integer.valueOf(getenv("FIRST_SELF_HIT"));
        logger.debug("hitScore:{}, firstHitScore:{}, selfHitScore:{}, firstSelfHitScore:{}",hitScore,firstHitScore,selfHitScore,selfHitScore);
    }

    private void handleMiss(String hitTrialStr) {
        logger.debug("Ignoring the miss HitTrial:",hitTrialStr);
    }

    private void handleHitOrFirstHit(int playerId , String antId, int gameId,int teamId,boolean self) {
        int score;
        if (isSmashedNow(antId)) {
            smashedAntsRepository.put(antId, playerId);
            score = (self) ? firstSelfHitScore : firstHitScore;
            logger.debug("firstHit event:{}" , playerId);
        } else {
            score = (self) ? selfHitScore : hitScore;
            logger.debug("hit event:{}" , playerId);
        }
        playersScoresRepository.put(playerId ,gameId,score);
        teamsScoresRepository.put(teamId ,gameId,score);
        logger.debug("Updated team id {} to a new score " , teamId);
    }



    private boolean isSmashedNow(String antId) {
        return !(smashedAntsRepository.isExist(antId));
    }


}
