package com.nice.coday;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

class Pair<K extends Comparable<K>, V> implements Comparable<Pair<K, V>> {
    K key;
    V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(Pair<K, V> other) {
        return this.key.compareTo(other.key);
    }
}

class TripInfo {
    int id;
    String vehicleType;
    int remainingBatteryPercentage;
    String entryPoint;
    String exitPoint;

    public TripInfo(int id, String vehicleType, int remainingBatteryPercentage, String entryPoint, String exitPoint) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.remainingBatteryPercentage = remainingBatteryPercentage;
        this.entryPoint = entryPoint;
        this.exitPoint = exitPoint;
    }
}

public class ElectricityConsumptionCalculatorImpl implements ElectricityConsumptionCalculator {
    @Override
    public ConsumptionResult calculateElectricityAndTimeConsumption(ResourceInfo resourceInfo) throws IOException {
        ConsumptionResult consumptionResult = new ConsumptionResult();
        Map<String, Long> totalCharginStationTimeMap = new HashMap<>();
        List<ConsumptionDetails> consumptionDetailsList = new ArrayList<>();

        Map<String, VehicleInfo> vehicleInfoMap = readVehicleInfoFromCSV(resourceInfo.vehicleTypeInfoPath);
        List<Pair<Integer, String>> sortedPoints = new ArrayList<>();

        sortedPoints.addAll(readPointsFromCSV(resourceInfo.entryExitPointInfoPath, "EntryExitPoint"));
        sortedPoints.addAll(readPointsFromCSV(resourceInfo.chargingStationInfoPath, "ChargingStation"));
        Map<String, Map<String, Integer>> chargingTimeMap = readChargingTimeFromCSV(resourceInfo.timeToChargeVehicleInfoPath);
        List<TripInfo> trips = readTripInfoFromCSV(resourceInfo.tripDetailsPath);

        Collections.sort(sortedPoints);

        for (Pair<Integer, String> point : sortedPoints) {
            //System.out.println("Integer: " + point.key + ", String: " + point.value);
        }

        for (TripInfo trip : trips) {
            processTrip(trip, sortedPoints, vehicleInfoMap.get(trip.vehicleType), chargingTimeMap.get(trip.vehicleType), totalCharginStationTimeMap);
        }

        consumptionResult.setTotalChargingStationTime(totalCharginStationTimeMap);

        for (Map.Entry<String, VehicleInfo> entry : vehicleInfoMap.entrySet())
            consumptionDetailsList.add(entry.getValue().getConsumptionDetails());

        consumptionResult.setConsumptionDetails(consumptionDetailsList);

        return consumptionResult;
    }

    private void processTrip(TripInfo trip, List<Pair<Integer, String>> sortedPoints, VehicleInfo vehicleInfo, Map<String, Integer> chargingTimeMap, Map<String, Long> totalChargingStationTimeMap) {
        int currentBattery = trip.remainingBatteryPercentage;
        int distanceCanTravel = currentBattery * vehicleInfo.getMileage() / 100;
        int lastChargingStationIndex = -1;
        int lastBatteryPercentage = currentBattery;
        int entryIndex = -1;
        int exitIndex = -1;
        int lastPointDistance = 0;

        // Step 1: Finding entry and exit points
        //System.out.println("=== Starting Trip Processing ===");
        //System.out.println("Trip Entry Point: " + trip.entryPoint + ", Exit Point: " + trip.exitPoint);

        for (int i = 0; i < sortedPoints.size(); i++) {
            if (sortedPoints.get(i).value.endsWith(trip.entryPoint)) entryIndex = i;
            if (sortedPoints.get(i).value.endsWith(trip.exitPoint)) exitIndex = i;
        }

        if (entryIndex == -1 || exitIndex == -1) {
            throw new IllegalArgumentException("Invalid entry or exit point for the trip.");
        }

        //System.out.println("Entry Point Found at Index: " + entryIndex + " (" + sortedPoints.get(entryIndex).value + ")");
        //System.out.println("Exit Point Found at Index: " + exitIndex + " (" + sortedPoints.get(exitIndex).value + ")");

        int direction = entryIndex < exitIndex ? 1 : -1;
        lastPointDistance = sortedPoints.get(entryIndex).key;

        // Step 2: Updating trip count
        vehicleInfo.getConsumptionDetails().setNumberOfTripsFinished(vehicleInfo.getConsumptionDetails().getNumberOfTripsFinished() + 1);
        //System.out.println("Starting Trip from index: " + entryIndex + " to index: " + exitIndex + ", Direction: " + (direction == 1 ? "Forward" : "Backward"));

        // Step 3: Processing each point in the trip
        for (int i = entryIndex + direction; i != exitIndex + direction; i += direction) {
            Pair<Integer, String> currentPoint = sortedPoints.get(i);
            int distanceTraveled = Math.abs(currentPoint.key - lastPointDistance);

            //System.out.println("\n--- Step: Traveling to Point ---");
            //System.out.println("At Point: " + currentPoint.value + ", Distance Traveled: " + distanceTraveled + " km, Distance Can Travel: " + distanceCanTravel + " km, Current Battery: " + currentBattery + "%");

            if (distanceTraveled > distanceCanTravel) {
                //System.out.println(">>> Need to charge, Last Charging Station Index: " + lastChargingStationIndex);
                if (lastChargingStationIndex == -1 || Math.abs(sortedPoints.get(lastChargingStationIndex).key - currentPoint.key) > vehicleInfo.getMileage()) {
                    //System.out.println(">>> No suitable charging station available. Stopping the trip.");
                    return;
                }

                // Step 4: Calculate the charge needed
                double chargeNeeded = 100 - lastBatteryPercentage;
                double totalUnitsRequired = vehicleInfo.getNumberOfUnitsForFullyCharge() * chargeNeeded / 100;
                //System.out.println(">>> Charging at Station: " + sortedPoints.get(lastChargingStationIndex).value + ", Units Required: " + totalUnitsRequired);

                String chargingStationId = sortedPoints.get(lastChargingStationIndex).value.split(":")[1];
                double timeToCharge = totalUnitsRequired * chargingTimeMap.getOrDefault(chargingStationId, 0);

                // Update vehicle info with charging details
                vehicleInfo.getConsumptionDetails().setTotalTimeRequired(vehicleInfo.getConsumptionDetails().getTotalTimeRequired() + (int) timeToCharge);
                vehicleInfo.getConsumptionDetails().setTotalUnitConsumed(vehicleInfo.getConsumptionDetails().getTotalUnitConsumed() + totalUnitsRequired);
                totalChargingStationTimeMap.put(chargingStationId, totalChargingStationTimeMap.getOrDefault(chargingStationId, 0L) + (long) timeToCharge);

                //System.out.println(">>> Charging Completed. Time to Charge: " + timeToCharge + " minutes");

                // Update battery and distance after charging
                distanceCanTravel = vehicleInfo.getMileage() - Math.abs(sortedPoints.get(lastChargingStationIndex).key - currentPoint.key);
                currentBattery = distanceCanTravel * 100 / vehicleInfo.getMileage();
                //System.out.println(">>> New Distance Can Travel: " + distanceCanTravel + " km, New Battery: " + currentBattery + "%");
                lastChargingStationIndex = -1;
            } else {
                currentBattery = Math.max(0, currentBattery - (distanceTraveled * 100 / vehicleInfo.getMileage()));
                distanceCanTravel -= distanceTraveled;
                //System.out.println(">>> New Distance Can Travel: " + distanceCanTravel + " km, New Battery: " + currentBattery + "%");
            }

            //System.out.println(">>> After Moving to Point: " + currentPoint.value + ", Remaining Battery: " + currentBattery + "%");

            // Step 5: Check for charging station
            if (currentPoint.value.startsWith("ChargingStation")) {
                lastChargingStationIndex = i;
                lastBatteryPercentage = currentBattery;
                //System.out.println(">>> Charging Station Found at Index: " + lastChargingStationIndex + " (" + currentPoint.value + ")");
            }

            lastPointDistance = currentPoint.key;
        }

        //System.out.println("=== Trip Successfully Processed ===");
    }


    private Map<String, VehicleInfo> readVehicleInfoFromCSV(Path csvPath) throws IOException {
        Map<String, VehicleInfo> vehicleInfoMap = new HashMap<>();

        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 3) {
                    String vehicleType = values[0].trim();
                    int numberOfUnitsForFullyCharge = Integer.parseInt(values[1].trim());
                    int mileage = Integer.parseInt(values[2].trim());

                    VehicleInfo vehicleInfo = new VehicleInfo(vehicleType, numberOfUnitsForFullyCharge, mileage);
                    vehicleInfoMap.put(vehicleType, vehicleInfo);
                }
            }
        }

        return vehicleInfoMap;
    }

    private List<Pair<Integer, String>> readPointsFromCSV(Path csvPath, String pointType) throws IOException {
        List<Pair<Integer, String>> points = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 2) {
                    String pointName = values[0].trim();
                    int distance = Integer.parseInt(values[1].trim());
                    points.add(new Pair<>(distance, pointType + ":" + pointName));
                }
            }
        }

        return points;
    }

    private Map<String, Map<String, Integer>> readChargingTimeFromCSV(Path csvPath) throws IOException {
        Map<String, Map<String, Integer>> chargingTimeMap = new HashMap<>();

        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 3) {
                    String vehicleType = values[0].trim();
                    String chargingStation = values[1].trim();
                    int timeToChargePerUnit = Integer.parseInt(values[2].trim());

                    chargingTimeMap.computeIfAbsent(vehicleType, k -> new HashMap<>()).put(chargingStation, timeToChargePerUnit);
                }
            }
        }

        return chargingTimeMap;
    }

    private List<TripInfo> readTripInfoFromCSV(Path csvPath) throws IOException {
        List<TripInfo> trips = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 5) {
                    int id = Integer.parseInt(values[0].trim());
                    String vehicleType = values[1].trim();
                    int remainingBatteryPercentage = Integer.parseInt(values[2].trim());
                    String entryPoint = values[3].trim();
                    String exitPoint = values[4].trim();

                    trips.add(new TripInfo(id, vehicleType, remainingBatteryPercentage, entryPoint, exitPoint));
                }
            }
        }

        return trips;
    }
}