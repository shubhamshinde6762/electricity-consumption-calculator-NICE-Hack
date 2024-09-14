package com.nice.coday;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class InputOperations {
    public Map<String, VehicleInfo> readVehicleInfoFromCSV(Path csvPath) throws IOException {
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

                    VehicleInfo vehicleInfo = new VehicleInfo(vehicleType, (long) numberOfUnitsForFullyCharge, mileage);
                    vehicleInfoMap.put(vehicleType, vehicleInfo);
                }
            }
        }

        return vehicleInfoMap;
    }

    public List<Pair<Integer, String>> readPointsFromCSV(Path csvPath, String pointType) throws IOException {
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

    public Map<String, Map<String, Integer>> readChargingTimeFromCSV(Path csvPath) throws IOException {
        Map<String, Map<String, Integer>> chargingTimeMap = new HashMap<>();

        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line;
            br.readLine(); // Skip header

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

    public List<TripInfo> readTripInfoFromCSV(Path csvPath) throws IOException {
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
