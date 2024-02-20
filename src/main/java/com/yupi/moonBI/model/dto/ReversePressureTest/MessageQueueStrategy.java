package com.yupi.moonBI.model.dto.ReversePressureTest;

import org.springframework.stereotype.Component;

@Component("GEN_MQ")
public class MessageQueueStrategy implements GenChartStrategy {
    @Override
    public void execute() {
        System.out.println("Handling request asynchronously using message queue.");
    }
}