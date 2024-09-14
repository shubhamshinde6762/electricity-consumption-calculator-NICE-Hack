package com.nice.coday;

import java.util.Objects;

public class VehicleInfo {
    private String vehicleType;
    private Long numberOfUnitsForFullyCharge;
    private int mileage;
    private ConsumptionDetails consumptionDetails;

    public VehicleInfo(String vehicleType, Long numberOfUnitsForFullyCharge, int mileage) {
        this.vehicleType = vehicleType;
        this.numberOfUnitsForFullyCharge = numberOfUnitsForFullyCharge;
        this.mileage = mileage;
        this.consumptionDetails = new ConsumptionDetails();
        consumptionDetails.setVehicleType(vehicleType);
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Long getNumberOfUnitsForFullyCharge() {
        return numberOfUnitsForFullyCharge;
    }

    public ConsumptionDetails getConsumptionDetails() {
        return consumptionDetails;
    }

    public void setNumberOfUnitsForFullyCharge(Long numberOfUnitsForFullyCharge) {
        this.numberOfUnitsForFullyCharge = numberOfUnitsForFullyCharge;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }
}
