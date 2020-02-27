package io.willb.kafkaecho;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class PredictionDeserializer extends ObjectMapperDeserializer<Prediction> {
    public PredictionDeserializer(){
        // pass the class to the parent.
        super(Prediction.class);
    }
}
