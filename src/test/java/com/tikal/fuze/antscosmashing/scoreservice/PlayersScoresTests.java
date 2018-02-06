package com.tikal.fuze.antscosmashing.scoreservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikal.fuze.antscosmashing.scoreservice.controller.GetScoresWebApiHandler;
import com.tikal.fuze.antscosmashing.scoreservice.controller.PostHitTrialWebApiHandler;
import com.tikal.fuze.antscosmashing.scoreservice.controller.ProcessKinesisHitTrialEventsHandler;
import com.tikal.fuze.antscosmashing.scoreservice.service.PlayerScoresService;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;

import static java.lang.System.getenv;

@Ignore
public class PlayersScoresTests {
    private static final Logger logger = LogManager.getLogger(PlayersScoresTests.class);


    private ObjectMapper om = new ObjectMapper();

    private PlayerScoresService playerScoresService = new PlayerScoresService(2,4,-4,-2);



    @Test
    public void testPostHitTrialWebApi() throws IOException {
        String data = getStringFromInputFile("post-score.json");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());
        new PostHitTrialWebApiHandler(playerScoresService).handleRequest(inputStream,new ByteArrayOutputStream(),null);
    }

    @Test
    public void testPlayerScoreServicePostToKinesis() throws IOException {
        String kinesisData = "\"{\\\"type\\\": \\\"hit\\\",\\\"antId\\\": \\\"11122\\\",\\\"playerId\\\": 9,\\\"gameId\\\":5,\\\"userId\\\":55,\\\"teamId\\\":7}\"";
        new ProcessKinesisHitTrialEventsHandler(playerScoresService).handleKinesisData(kinesisData);
    }

    @Test
    public void testGetScoresWebApi() throws IOException {
        String data = getStringFromInputFile("get-scores-by-gameId.json");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        OutputStream os = new  ByteArrayOutputStream();
        new GetScoresWebApiHandler().handleRequest(inputStream,os,null);
        logger.debug(os.toString());
    }

    @Test
    public void testHandleStr() throws IOException {
        String str="{\"type\": \"hit\",\"antId\": \"11122\",\"playerId\": 114,\"gameId\":6,\"userId\":5,\"teamId\":7}";
        playerScoresService.savePlayerScore(str);
    }







    private String getStringFromInputFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return FileUtils.readFileToString(file, Charset.defaultCharset());
    }



}

