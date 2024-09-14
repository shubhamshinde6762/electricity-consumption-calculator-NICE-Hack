package com.nice.coday;

import java.util.Objects;

public class VehicleInfo {
    private String vehicleType;
    private int numberOfUnitsForFullyCharge;
    private int mileage;

    public VehicleInfo(String vehicleType, int numberOfUnitsForFullyCharge, int mileage) {
        this.vehicleType = vehicleType;
        this.numberOfUnitsForFullyCharge = numberOfUnitsForFullyCharge;
        this.mileage = mileage;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getNumberOfUnitsForFullyCharge() {
        return numberOfUnitsForFullyCharge;
    }

    public void setNumberOfUnitsForFullyCharge(int numberOfUnitsForFullyCharge) {
        this.numberOfUnitsForFullyCharge = numberOfUnitsForFullyCharge;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }
}
