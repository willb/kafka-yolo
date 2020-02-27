package io.willb.kafkaecho;

public class Prediction {
    public String prediction;
    public String image;

    public Prediction() {}

    public Prediction(String prediction, String image) {
        this.prediction = prediction;
        this.image = image;
    }
}
