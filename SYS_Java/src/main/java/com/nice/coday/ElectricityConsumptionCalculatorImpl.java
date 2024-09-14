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
        Collections.sort(sortedPoints);
        Map<String, Map<String, Integer>> chargingTimeMap = readChargingTimeFromCSV(resourceInfo.timeToChargeVehicleInfoPath);
        List<TripInfo> trips = readTripInfoFromCSV(resourceInfo.tripDetailsPath);

        for (TripInfo trip : trips) {
            processTrip(trip, sortedPoints, vehicleInfoMap.get(trip.vehicleType), chargingTimeMap.get(trip.vehicleType), totalCharginStationTimeMap);
        }

        consumptionResult.setTotalChargingStationTime(totalCharginStationTimeMap);

        for (Map.Entry<String, VehicleInfo> entry : vehicleInfoMap.entrySet())
            consumptionDetailsList.add(entry.getValue().getConsumptionDetails());

        consumptionResult.setConsumptionDetails(consumptionDetailsList);

        return consumptionResult;
    }

    private void processTrip(TripInfo trip, List<Pair<Integer, String>> sortedPoints, VehicleInfo vehicleInfo, Map<String, Integer> chargingTimeMap, Map<String, Long> totalCharginStationTimeMap) {
        int currentBattery = trip.remainingBatteryPercentage;
        int distanceCanTravel = currentBattery * vehicleInfo.getMileage() / 100;
        int lastChargingStationIndex = -1;
        int lastBatteryPercentage = currentBattery;
        int entryIndex = -1;
        int exitIndex = -1;
        int lastPointDistance = 0;

        for (int i = 0; i < sortedPoints.size(); i++) {
            if (sortedPoints.get(i).value.endsWith(trip.entryPoint)) entryIndex = i;
            if (sortedPoints.get(i).value.endsWith(trip.exitPoint)) exitIndex = i;
        }

        int direction = entryIndex < exitIndex ? 1 : -1;
        vehicleInfo.getConsumptionDetails().setNumberOfTripsFinished(vehicleInfo.getConsumptionDetails().getNumberOfTripsFinished() + 1);

        for (int i = entryIndex; i != exitIndex + direction; i += direction) {
            Pair<Integer, String> currentPoint = sortedPoints.get(i);
            int distanceTraveled = Math.abs(currentPoint.key - lastPointDistance);

            if (distanceTraveled > distanceCanTravel) {
                if (lastChargingStationIndex == -1 || Math.abs(sortedPoints.get(lastChargingStationIndex).key - currentPoint.key) > vehicleInfo.getMileage())
                    return;

                double chargePercentage = 100 - lastBatteryPercentage;
                double totalUnitsRequired = vehicleInfo.getNumberOfUnitsForFullyCharge() * chargePercentage / 100;
                double timeToCharge = totalUnitsRequired * chargingTimeMap.get(vehicleInfo.getVehicleType());
//                ConsumptionDetails temp =  vehicleInfo.getConsumptionDetails();
                vehicleInfo.getConsumptionDetails().setTotalTimeRequired(vehicleInfo.getConsumptionDetails().getTotalTimeRequired() + (int) timeToCharge);
                vehicleInfo.getConsumptionDetails().setTotalUnitConsumed(vehicleInfo.getConsumptionDetails().getTotalUnitConsumed() + totalUnitsRequired);

                totalCharginStationTimeMap.put(sortedPoints.get(lastChargingStationIndex).value.split(":")[1], totalCharginStationTimeMap.get(sortedPoints.get(lastChargingStationIndex).value.split(":")[1]) + 1);
                distanceCanTravel = vehicleInfo.getMileage() - Math.abs(sortedPoints.get(lastChargingStationIndex).key - currentPoint.key);
                currentBattery = distanceCanTravel * 100 / vehicleInfo.getMileage();
                lastChargingStationIndex = -1;
            } else currentBattery = Math.max(0, currentBattery - (distanceTraveled * 100 / vehicleInfo.getMileage()));


            if (currentPoint.value.startsWith("ChargingStation")) {
                lastChargingStationIndex = i;
                lastBatteryPercentage = currentBattery;
            }

            lastPointDistance = currentPoint.key;
        }

    }

    // Other methods (readVehicleInfoFromCSV, readPointsFromCSV, readChargingTimeFromCSV, readTripInfoFromCSV) remain unchanged
    private Map<String, VehicleInfo> readVehicleInfoFromCSV(Path csvPath) throws IOException {
        Map<String, VehicleInfo> vehicleInfoMap = new HashMap<>();

        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line;
            br.readLine(); // Skip header

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
            br.readLine(); // Skip header

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
            br.readLine(); // Skip header

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