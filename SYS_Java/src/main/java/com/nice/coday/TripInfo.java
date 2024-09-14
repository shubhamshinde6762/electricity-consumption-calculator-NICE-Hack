package com.nice.coday;

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