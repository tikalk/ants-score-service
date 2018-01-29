package com.tikal.fuze.antscosmashing.scoreservice.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.tikal.fuze.antscosmashing.scoreservice.service.PlayerScoresService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessKinesisHitTrialEventsHandler implements RequestHandler<KinesisEvent, Void> {
    private static final Logger logger = LogManager.getLogger(ProcessKinesisHitTrialEventsHandler.class);

    private PlayerScoresService playerScoresService;

    @Override
    public Void handleRequest(KinesisEvent event, Context context) {
        try {
            if(playerScoresService ==null)
                playerScoresService = new PlayerScoresService();
//            event.getRecords().stream().map(rec -> new String(rec.getKinesis().getData().array())).forEach(playerScoresService::savePlayerScore);
            event.getRecords().stream().forEach(this::handleKinesisEventRecord);
            return null;
        } catch (Exception e) {
            logger.error("Failed to process event",e);
            throw new RuntimeException(e);
        }
    }

    private void handleKinesisEventRecord(KinesisEvent.KinesisEventRecord kinesisEventRecord) {
        try{
            logger.debug("Processing event: {}",kinesisEventRecord.getKinesis());
            playerScoresService.savePlayerScore(new String(kinesisEventRecord.getKinesis().getData().array()));
        } catch (Exception e) {
            logger.error("Failed to process event",e);
            throw new RuntimeException(e);
        }
    }


}
