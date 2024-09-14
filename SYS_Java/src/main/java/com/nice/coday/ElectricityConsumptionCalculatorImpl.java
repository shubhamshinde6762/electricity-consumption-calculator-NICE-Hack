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
        Map<String, VehicleInfo> vehicleInfoMap = readVehicleInfoFromCSV(resourceInfo.vehicleTypeInfoPath);
        List<Pair<Integer, String>> sortedPoints = new ArrayList<>();
        sortedPoints.addAll(readPointsFromCSV(resourceInfo.entryExitPointInfoPath, "EntryExitPoint"));
        sortedPoints.addAll(readPointsFromCSV(resourceInfo.chargingStationInfoPath, "ChargingStation"));
        Collections.sort(sortedPoints);
        Map<String, Map<String, Integer>> chargingTimeMap = readChargingTimeFromCSV(resourceInfo.timeToChargeVehicleInfoPath);
        List<TripInfo> trips = readTripInfoFromCSV(resourceInfo.tripDetailsPath);

        for (TripInfo trip : trips) {
            processTrip(trip, sortedPoints, vehicleInfoMap.get(trip.vehicleType), chargingTimeMap.get(trip.vehicleType));
        }

        return null; // Replace with actual calculation result if needed
    }

    private void processTrip(TripInfo trip, List<Pair<Integer, String>> sortedPoints, VehicleInfo vehicleInfo, Map<String, Integer> chargingTimeMap) {
        System.out.println("\nProcessing Trip ID: " + trip.id);
        int currentBattery = trip.remainingBatteryPercentage;
        int lastChargingStationIndex = -1;
        int entryIndex = -1;
        int exitIndex = -1;
        int lastPointDistance = 0;

        for (int i = 0; i < sortedPoints.size(); i++) {
            if (sortedPoints.get(i).value.endsWith(trip.entryPoint)) entryIndex = i;
            if (sortedPoints.get(i).value.endsWith(trip.exitPoint)) exitIndex = i;
        }

        int direction = entryIndex < exitIndex ? 1 : -1;
        for (int i = entryIndex; i != exitIndex + direction; i += direction) {
            Pair<Integer, String> currentPoint = sortedPoints.get(i);
            int distanceTraveled = Math.abs(currentPoint.key - lastPointDistance);
            int batteryUsed = (distanceTraveled * 100) / (vehicleInfo.getMileage() * vehicleInfo.getNumberOfUnitsForFullyCharge());
            currentBattery -= batteryUsed;

            System.out.printf("At point %s (distance %d): Battery %d%%, Used %d%%\n",
                    currentPoint.value, currentPoint.key, currentBattery, batteryUsed);

            if (currentBattery < 20 && lastChargingStationIndex != -1) {
                String chargingStation = sortedPoints.get(lastChargingStationIndex).value.split(":")[1];
                int chargingTime = chargingTimeMap.get(chargingStation) * (100 - currentBattery) / 100;
                currentBattery = 100;
                System.out.printf("Charging at %s: Time taken %d minutes, Battery now 100%%\n",
                        chargingStation, chargingTime);
            }

            if (currentPoint.value.startsWith("ChargingStation")) {
                lastChargingStationIndex = i;
            }

            lastPointDistance = currentPoint.key;
        }

        System.out.println("Trip completed. Final battery percentage: " + currentBattery + "%");
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

                    chargingTimeMap
                            .computeIfAbsent(vehicleType, k -> new HashMap<>())
                            .put(chargingStation, timeToChargePerUnit);
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