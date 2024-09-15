
---

# Electricity Consumption Calculator

This project is designed to calculate daily aggregate electricity consumption and total charging time required for electric vehicles traveling on a designated green corridor (highway) from Kashmir to Kanyakumari. The solution uses several input files to determine consumption details based on vehicle specifications, charging station locations, and trip details.

## Problem Statement

NICE Power Ltd, in partnership with the Government of India, aims to make the new highway a zero-emission zone by allowing only electric vehicles. The goal of this project is to assess the daily energy consumption of vehicles and calculate the total charging time at each charging station.

## Features
- Calculates total energy consumption for each vehicle type.
- Determines the total time spent charging at each station.
- Computes the number of finished trips for each vehicle type.
- Handles complex scenarios with varying charging rates at different stations.

## Inputs

1. **ChargingStationInfo.csv**: Contains the list of charging stations and their distances.
2. **EntryExitPointInfo.csv**: Lists entry and exit points along the highway.
3. **VehicleTypeInfo.csv**: Describes vehicle specifications such as battery capacity and mileage.
4. **TimeToChargeVehicleInfo.csv**: Provides the time to charge specific vehicles at each charging station.
5. **TripDetails.csv**: Contains trip-specific information including entry/exit points and battery status.

## Outputs

- **ConsumptionResult JSON**: 
  - Total energy consumed by each vehicle type.
  - Total time taken to charge each vehicle type.
  - Count of completed trips for each vehicle type.
  - Charging time spent at each station.

### Example Output
```json
{
  "vehicleType": "V1",
  "totalEnergyConsumed": 350,
  "totalTimeSpentCharging": 12000,
  "completedTrips": 5,
  "chargingStationTime": {
    "C1": 6000,
    "C2": 6000
  }
}
```

## Approach

### 1. **Data Parsing and Preprocessing**
   - **Vehicle Info**: Read and store vehicle specifications from `VehicleTypeInfo.csv`.
   - **Charging Stations**: Parse charging station data, storing station names and distances.
   - **Trip Data**: Parse trip details, including entry/exit points and remaining battery.
   - **Charging Times**: For each station and vehicle type, calculate the time needed for full charging.

### 2. **Sorting Points**
   - Combine entry/exit points and charging stations into a single sorted list for easier traversal.

### 3. **Trip Simulation**
   - For each trip, determine the entry and exit points.
   - Calculate how far the vehicle can travel before it requires charging.
   - Choose the farthest reachable charging station if a vehicle can’t complete its trip in one charge.
   - Charge the vehicle fully at the station, then continue the trip.
   - Update consumption and time statistics after each trip.

### 4. **Edge Cases**
   - **Charging Infeasibility**: If a vehicle can't reach any charging station, it is excluded from the calculations.
   - **Battery Edge Cases**: If a vehicle starts with low battery but no nearby charging station, it may fail the trip.
   - **Multiple Charging Stops**: Vehicles may need to stop at multiple charging stations during a trip, and the last reachable station is selected each time.

## Time Complexity

- **O(T * P)** for processing trips, where:
  - `T` is the number of trips.
  - `P` is the number of points (charging stations and entry/exit points).

Sorting the points takes **O(P log P)**, while simulating each trip requires iterating over the points, leading to an overall complexity of **O(T * P)**.

## Space Complexity

- **O(P + T + V)**, where:
  - `P` is the number of points (charging stations and entry/exit points).
  - `T` is the number of trips.
  - `V` is the number of vehicle types.

Most of the space is consumed by storing the sorted points, vehicle data, and trip results.

## Edge Cases Handled

- Vehicles with insufficient charge to reach any station are excluded.
- Handling trips where charging stations are equidistant from entry/exit points.
- Charging stops only happen when necessary, minimizing total time and consumption.

## Tech Specifications

- **Java**: The code is implemented in Java, using Java's built-in data structures and utilities.
- **Maven**: Use Maven for project setup and dependency management.
- **JUnit**: The project is unit tested using JUnit, with test cases provided in the `/test` directory.

## Installation and Usage

### Prerequisites
- Java 8+
- Maven

### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/shubhamshinde6762/SYS_Nice_hack.git
   ```
2. Navigate to the project directory:
   ```bash
   cd electricity-consumption-calculator
   ```
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   java -jar target/electricity-consumption-calculator-1.0.jar
   ```
