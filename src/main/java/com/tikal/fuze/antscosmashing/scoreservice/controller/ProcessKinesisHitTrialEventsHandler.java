package com.tikal.fuze.antscosmashing.scoreservice.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.tikal.fuze.antscosmashing.scoreservice.service.PlayerScoresService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static com.tikal.fuze.antscosmashing.scoreservice.controller.StringUtils.trimDoubleQuotes;

public class ProcessKinesisHitTrialEventsHandler implements RequestHandler<KinesisEvent, Void> {
    private static final Logger logger = LogManager.getLogger(ProcessKinesisHitTrialEventsHandler.class);

    private PlayerScoresService playerScoresService;

    public ProcessKinesisHitTrialEventsHandler(){
        if(playerScoresService ==null)
            playerScoresService = new PlayerScoresService();
    }

    public ProcessKinesisHitTrialEventsHandler(PlayerScoresService playerScoresService) {
        this.playerScoresService=playerScoresService;
    }

    @Override
    public Void handleRequest(KinesisEvent event, Context context) {
        try {
//            event.getRecords().stream().map(rec -> new String(rec.getKinesis().getData().array())).forEach(playerScoresService::savePlayerScore);
            event.getRecords().stream().forEach(this::handleKinesisEventRecord);
            return null;
        } catch (Exception e) {
            logger.error("Failed to process event",e);
            return null;
        }
    }

    private void handleKinesisEventRecord(KinesisEvent.KinesisEventRecord kinesisEventRecord) {
        try{
            logger.debug("Processing event: {}",kinesisEventRecord.getKinesis());
            String kinesisData = new String(kinesisEventRecord.getKinesis().getData().array());
            handleKinesisData(kinesisData);
        } catch (Exception e) {
            logger.error("Failed to process event",e);
            throw new RuntimeException(e);
        }
    }

    public void handleKinesisData(String kinesisData) throws IOException {
        logger.debug("Got kinesis data {}",kinesisData);
        playerScoresService.savePlayerScore(trimDoubleQuotes(kinesisData.replaceAll("\\\\", "")));
    }


}
