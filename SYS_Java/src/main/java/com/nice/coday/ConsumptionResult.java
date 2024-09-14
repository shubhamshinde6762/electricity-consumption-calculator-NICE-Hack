package com.nice.coday;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsumptionResult {

    private List<ConsumptionDetails> consumptionDetails = new ArrayList<>();
    public Map<String, Long> totalChargingStationTime = new HashMap<>();

    public void setConsumptionDetails(List<ConsumptionDetails> consumptionDetails) {
        this.consumptionDetails = consumptionDetails;
    }

    public void setTotalChargingStationTime(Map<String, Long> totalChargingStationTime) {
        this.totalChargingStationTime = totalChargingStationTime;
    }

    public List<ConsumptionDetails> getConsumptionDetails() {
        return consumptionDetails;
    }

    public Map<String, Long> getTotalChargingStationTime() {
        return totalChargingStationTime;
    }
}
