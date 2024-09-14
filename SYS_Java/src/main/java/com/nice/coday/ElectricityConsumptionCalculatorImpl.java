package com.nice.coday;

import java.io.IOException;
import java.util.*;

public class ElectricityConsumptionCalculatorImpl implements ElectricityConsumptionCalculator {
    private final InputOperations inputOperations;

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

        double currentBattery = trip.remainingBatteryPercentage;
        double distanceCanTravel = calculateDistanceCanTravel(currentBattery, vehicleInfo.getMileage());
        double lastBatteryPercentage = currentBattery;

        double tempTotalTimeRequired = 0.00;
        double tempTotalUnitConsumed = 0.00;

        Map<String, Long> tempChargingStationTimeMap = new HashMap<>(totalChargingStationTimeMap);

        for (int i = entryPointer; i != exitPointer + direction; i += direction) {
            Pair<Integer, String> currentPoint = sortedPoints.get(i);
            double distanceTraveled = Math.abs(currentPoint.key - lastPointDistance);

            if (distanceTraveled > distanceCanTravel) {
                if (lastChargingStationIndex == -1) {
                    updateVehicleInfo(vehicleInfo, tempTotalTimeRequired, tempTotalUnitConsumed);
                    totalChargingStationTimeMap.putAll(tempChargingStationTimeMap);
                    return;
                }

                ChargingResult chargingResult = chargeVehicle(lastBatteryPercentage, vehicleInfo, sortedPoints.get(lastChargingStationIndex), chargingTimeMap);
                tempTotalTimeRequired += chargingResult.timeToCharge;
                tempTotalUnitConsumed += chargingResult.totalUnitsRequired;
                tempChargingStationTimeMap.merge(chargingResult.chargingStationId, (long) chargingResult.timeToCharge, Long::sum);

                distanceCanTravel = calculateDistanceCanTravel(100.0, vehicleInfo.getMileage()) - Math.abs(sortedPoints.get(lastChargingStationIndex).key - currentPoint.key);

                if (distanceCanTravel < 0) {
                    updateVehicleInfo(vehicleInfo, tempTotalTimeRequired, tempTotalUnitConsumed);
                    totalChargingStationTimeMap.putAll(tempChargingStationTimeMap);
                    return;
                }

                currentBattery = (distanceCanTravel / vehicleInfo.getMileage()) * 100.0;
                lastChargingStationIndex = -1;
            } else {
                currentBattery = Math.max(0.0, currentBattery - (distanceTraveled * 100.0 / vehicleInfo.getMileage()));
                distanceCanTravel -= distanceTraveled;
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

    private double calculateDistanceCanTravel(double batteryPercentage, double mileage) {
        return roundToTwoDecimalPlaces(batteryPercentage * mileage / 100.0);
    }

    private int findPointIndex(List<Pair<Integer, String>> sortedPoints, String point) {
        return sortedPoints.stream().filter(p -> p.value.endsWith(point)).findFirst().map(sortedPoints::indexOf).orElse(-1);
    }

    private void updateVehicleInfo(VehicleInfo vehicleInfo, double timeRequired, double unitConsumed) {
        ConsumptionDetails details = vehicleInfo.getConsumptionDetails();
        details.setTotalTimeRequired(details.getTotalTimeRequired() + (long) roundToTwoDecimalPlaces(timeRequired));
        details.setTotalUnitConsumed(details.getTotalUnitConsumed() + roundToTwoDecimalPlaces(unitConsumed));
    }

    private ChargingResult chargeVehicle(double lastBatteryPercentage, VehicleInfo vehicleInfo, Pair<Integer, String> chargingStation, Map<String, Integer> chargingTimeMap) {
        double chargeNeeded = roundToTwoDecimalPlaces(100.0 - lastBatteryPercentage);
        double totalUnitsRequired = roundToTwoDecimalPlaces(vehicleInfo.getNumberOfUnitsForFullyCharge() * chargeNeeded / 100.0);
        String chargingStationId = chargingStation.value.split(":")[1];
        double timeToCharge = roundToTwoDecimalPlaces(totalUnitsRequired * chargingTimeMap.getOrDefault(chargingStationId, 1));
        return new ChargingResult(chargingStationId, timeToCharge, totalUnitsRequired);
    }

    private double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static class ChargingResult {
        String chargingStationId;
        double timeToCharge;
        double totalUnitsRequired;

        ChargingResult(String chargingStationId, double timeToCharge, double totalUnitsRequired) {
            this.chargingStationId = chargingStationId;
            this.timeToCharge = timeToCharge;
            this.totalUnitsRequired = totalUnitsRequired;
        }
    }
}