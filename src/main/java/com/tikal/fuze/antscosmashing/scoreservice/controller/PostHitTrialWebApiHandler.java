package com.tikal.fuze.antscosmashing.scoreservice.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tikal.fuze.antscosmashing.scoreservice.service.PlayerScoresService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class PostHitTrialWebApiHandler implements RequestStreamHandler {
    private static final Logger logger = LogManager.getLogger(PostHitTrialWebApiHandler.class);
    private ObjectMapper om = new ObjectMapper();
    private PlayerScoresService playerScoresService;

    public PostHitTrialWebApiHandler(){
        if(playerScoresService ==null)
            playerScoresService = new PlayerScoresService();
    }

    public PostHitTrialWebApiHandler(PlayerScoresService playerScoresService) {
        this.playerScoresService=playerScoresService;
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)  {
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            JsonNode event = om.readTree(reader);
            String inputBody = event.get("body").textValue();
            logger.debug("The input event contains body {}",inputBody);
            playerScoresService.savePlayerScore(inputBody);


            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            ObjectNode result = om.createObjectNode()
                    .put("statusCode", 200)
                    .put("body", "OK");
            writer.write(result.toString());
            writer.close();
        } catch (Exception e) {
            logger.error("Failed to process event",e);
            throw new RuntimeException(e);
        }
    }
}
