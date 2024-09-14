package com.nice.coday;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ElectricityConsumptionCalculatorImpl implements ElectricityConsumptionCalculator {
    private final InputOperations inputOperations;
    private static final int DECIMAL_PLACES = 4;

    public ElectricityConsumptionCalculatorImpl() {
        this.inputOperations = new InputOperations();
    }

    public ElectricityConsumptionCalculatorImpl(InputOperations inputOperations) {
        this.inputOperations = inputOperations;
    }

    @Override
    public ConsumptionResult calculateElectricityAndTimeConsumption(ResourceInfo resourceInfo) throws IOException {
        ConsumptionResult consumptionResult = new ConsumptionResult();
        Map<String, Long> totalChargingStationTimeMap = new HashMap<>();
        List<ConsumptionDetails> consumptionDetailsList = new ArrayList<>();

        Map<String, VehicleInfo> vehicleInfoMap = inputOperations.readVehicleInfoFromCSV(resourceInfo.vehicleTypeInfoPath);
        List<Pair<Integer, String>> sortedPoints = new ArrayList<>();

        sortedPoints.addAll(inputOperations.readPointsFromCSV(resourceInfo.entryExitPointInfoPath, "EntryExitPoint"));
        sortedPoints.addAll(inputOperations.readPointsFromCSV(resourceInfo.chargingStationInfoPath, "ChargingStation"));

        Map<String, Map<String, Integer>> chargingTimeMap = inputOperations.readChargingTimeFromCSV(resourceInfo.timeToChargeVehicleInfoPath);
        List<TripInfo> trips = inputOperations.readTripInfoFromCSV(resourceInfo.tripDetailsPath);

        Collections.sort(sortedPoints);

        for (TripInfo trip : trips) {
            processTrip(trip, sortedPoints, vehicleInfoMap.get(trip.vehicleType), chargingTimeMap.get(trip.vehicleType), totalChargingStationTimeMap);
        }

        consumptionResult.setTotalChargingStationTime(totalChargingStationTimeMap);

        for (Map.Entry<String, VehicleInfo> entry : vehicleInfoMap.entrySet())
            consumptionDetailsList.add(entry.getValue().getConsumptionDetails());

        consumptionResult.setConsumptionDetails(consumptionDetailsList);

        return consumptionResult;
    }


    private void processTrip(TripInfo trip, List<Pair<Integer, String>> sortedPoints, VehicleInfo vehicleInfo, Map<String, Integer> chargingTimeMap, Map<String, Long> totalChargingStationTimeMap) {
        int entryPointer = findPointIndex(sortedPoints, trip.entryPoint);
        int exitPointer = findPointIndex(sortedPoints, trip.exitPoint);

        if (entryPointer == -1 || exitPointer == -1) {
            throw new IllegalArgumentException("Invalid entry or exit point for the trip.");
        }

        int direction = Integer.compare(exitPointer, entryPointer);
        long lastPointDistance = sortedPoints.get(entryPointer).key;
        int lastChargingStationIndex = -1;

        BigDecimal currentBattery = BigDecimal.valueOf(trip.remainingBatteryPercentage);
        BigDecimal distanceCanTravel = calculateDistanceCanTravel(currentBattery, BigDecimal.valueOf(vehicleInfo.getMileage()));
        BigDecimal lastBatteryPercentage = BigDecimal.ZERO;

        BigDecimal tempTotalTimeRequired = BigDecimal.ZERO;
        BigDecimal tempTotalUnitConsumed = BigDecimal.ZERO;

        Map<String, Long> tempChargingStationTimeMap = new HashMap<>(totalChargingStationTimeMap);

        for (int i = entryPointer; i != exitPointer + direction; i += direction) {
            Pair<Integer, String> currentPoint = sortedPoints.get(i);
            BigDecimal distanceTraveled = BigDecimal.valueOf(Math.abs(currentPoint.key - lastPointDistance));

            if (distanceTraveled.compareTo(distanceCanTravel) > 0 || (i == exitPointer && distanceTraveled.compareTo(distanceCanTravel) == 0)) {
                if (lastChargingStationIndex == -1) {
                    updateVehicleInfo(vehicleInfo, tempTotalTimeRequired, tempTotalUnitConsumed);
                    totalChargingStationTimeMap.putAll(tempChargingStationTimeMap);
                    return;
                }

                ChargingResult chargingResult = chargeVehicle(lastBatteryPercentage, vehicleInfo, sortedPoints.get(lastChargingStationIndex), chargingTimeMap);
                tempTotalTimeRequired = tempTotalTimeRequired.add(chargingResult.timeToCharge);
                tempTotalUnitConsumed = tempTotalUnitConsumed.add(chargingResult.totalUnitsRequired);
                tempChargingStationTimeMap.merge(chargingResult.chargingStationId, chargingResult.timeToCharge.longValue(), Long::sum);

                distanceCanTravel = calculateDistanceCanTravel(BigDecimal.valueOf(100.0), BigDecimal.valueOf(vehicleInfo.getMileage())).subtract(BigDecimal.valueOf(Math.abs(sortedPoints.get(lastChargingStationIndex).key - currentPoint.key)));

                currentBattery = distanceCanTravel.multiply(BigDecimal.valueOf(100.0)).divide(BigDecimal.valueOf(vehicleInfo.getMileage()), DECIMAL_PLACES, RoundingMode.HALF_UP);
                lastChargingStationIndex = -1;
            } else {
                currentBattery = currentBattery.subtract(distanceTraveled.multiply(BigDecimal.valueOf(100.0)).divide(BigDecimal.valueOf(vehicleInfo.getMileage()), DECIMAL_PLACES, RoundingMode.HALF_UP));
                distanceCanTravel = distanceCanTravel.subtract(distanceTraveled);
            }

//            System.out.println(distanceCanTravel);

            if (distanceCanTravel.compareTo(BigDecimal.ZERO) < 0) {
                updateVehicleInfo(vehicleInfo, tempTotalTimeRequired, tempTotalUnitConsumed);
                totalChargingStationTimeMap.putAll(tempChargingStationTimeMap);
                return;
            }

            if (currentPoint.value.startsWith("ChargingStation")) {
                lastChargingStationIndex = i;
                lastBatteryPercentage = currentBattery;
            }

            lastPointDistance = currentPoint.key;
        }

        updateVehicleInfo(vehicleInfo, tempTotalTimeRequired, tempTotalUnitConsumed);
        vehicleInfo.getConsumptionDetails().setNumberOfTripsFinished(vehicleInfo.getConsumptionDetails().getNumberOfTripsFinished() + 1);
        totalChargingStationTimeMap.putAll(tempChargingStationTimeMap);
    }


    private BigDecimal calculateDistanceCanTravel(BigDecimal batteryPercentage, BigDecimal mileage) {
        return batteryPercentage.multiply(mileage).divide(BigDecimal.valueOf(100.0), DECIMAL_PLACES, RoundingMode.HALF_UP);
    }

    private int findPointIndex(List<Pair<Integer, String>> sortedPoints, String point) {
        return sortedPoints.stream().filter(p -> p.value.endsWith(point)).findFirst().map(sortedPoints::indexOf).orElse(-1);
    }

    private void updateVehicleInfo(VehicleInfo vehicleInfo, BigDecimal timeRequired, BigDecimal unitConsumed) {
        ConsumptionDetails details = vehicleInfo.getConsumptionDetails();
        details.setTotalTimeRequired(details.getTotalTimeRequired() + timeRequired.longValue());
        details.setTotalUnitConsumed(details.getTotalUnitConsumed() + unitConsumed.doubleValue());
    }

    private ChargingResult chargeVehicle(BigDecimal lastBatteryPercentage, VehicleInfo vehicleInfo, Pair<Integer, String> chargingStation, Map<String, Integer> chargingTimeMap) {
        BigDecimal chargeNeeded = BigDecimal.valueOf(100.0).subtract(lastBatteryPercentage);
        BigDecimal totalUnitsRequired = BigDecimal.valueOf(vehicleInfo.getNumberOfUnitsForFullyCharge()).multiply(chargeNeeded).divide(BigDecimal.valueOf(100.0), DECIMAL_PLACES, RoundingMode.HALF_UP);
        String chargingStationId = chargingStation.value.split(":")[1];
        BigDecimal timeToCharge = totalUnitsRequired.multiply(BigDecimal.valueOf(chargingTimeMap.getOrDefault(chargingStationId, 0)));
        return new ChargingResult(chargingStationId, timeToCharge, totalUnitsRequired);
    }

    private static class ChargingResult {
        String chargingStationId;
        BigDecimal timeToCharge;
        BigDecimal totalUnitsRequired;

        ChargingResult(String chargingStationId, BigDecimal timeToCharge, BigDecimal totalUnitsRequired) {
            this.chargingStationId = chargingStationId;
            this.timeToCharge = timeToCharge;
            this.totalUnitsRequired = totalUnitsRequired;
        }
    }
}