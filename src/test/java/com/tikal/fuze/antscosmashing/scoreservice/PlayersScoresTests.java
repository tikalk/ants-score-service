package com.tikal.fuze.antscosmashing.scoreservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikal.fuze.antscosmashing.scoreservice.controller.GetScoresWebApiHandler;
import com.tikal.fuze.antscosmashing.scoreservice.controller.PostHitTrialWebApiHandler;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;

public class PlayersScoresTests {
    private static final Logger logger = LogManager.getLogger(PlayersScoresTests.class);


    private ObjectMapper om = new ObjectMapper();


    @Test
    @Ignore
    public void testPostHitTrialWebApi() throws IOException {
        String data = getStringFromInputFile("post-score.json");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());
        new PostHitTrialWebApiHandler().handleRequest(inputStream,new ByteArrayOutputStream(),null);
    }

    @Test
    public void testGetScoresWebApi() throws IOException {
        String data = getStringFromInputFile("get-scores-by-gameId.json");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        OutputStream os = new  ByteArrayOutputStream();
        new GetScoresWebApiHandler().handleRequest(inputStream,os,null);
        logger.debug(os.toString());
    }





    private String getStringFromInputFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return FileUtils.readFileToString(file, Charset.defaultCharset());
    }


}

