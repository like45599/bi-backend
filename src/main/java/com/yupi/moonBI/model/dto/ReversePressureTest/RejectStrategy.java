package com.yupi.moonBI.model.dto.ReversePressureTest;

import lombok.Data;
import org.springframework.stereotype.Component;


@Component("GEN_REJECT")
public class RejectStrategy implements GenChartStrategy {
    @Override
    public void execute() {
        System.out.println("Rejecting request due to high server load.");
    }
}