package com.nice.coday;

import java.io.IOException;

public interface ElectricityConsumptionCalculator {

    ConsumptionResult calculateElectricityAndTimeConsumption(ResourceInfo resourcesInfo) throws IOException;
}
