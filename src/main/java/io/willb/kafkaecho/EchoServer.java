package io.willb.kafkaecho;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EchoServer {
    @Incoming("echo-input-topic")
    @Outgoing("echo-output-topic")
    @Broadcast
    public String process(String message) {
        return message;
    }
}