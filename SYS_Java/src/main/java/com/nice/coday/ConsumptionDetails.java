package com.nice.coday;

public class ConsumptionDetails {

    private  String vehicleType;
    private Double totalUnitConsumed = 0.0;
    private Long totalTimeRequired = 0l;

    private Long numberOfTripsFinished = 0l;

    public ConsumptionDetails() {
    }

    public ConsumptionDetails(String vehicleType, Double totalUnitConsumed, Long totalTimeRequired, Long numberOfTripsFinished) {
        this.vehicleType = vehicleType;
        this.totalUnitConsumed = totalUnitConsumed;
        this.totalTimeRequired = totalTimeRequired;
        this.numberOfTripsFinished = numberOfTripsFinished;
    }


    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Double getTotalUnitConsumed() {
        return totalUnitConsumed;
    }

    public void setTotalUnitConsumed(Double totalUnitConsumed) {
        this.totalUnitConsumed = totalUnitConsumed;
    }

    public Long getTotalTimeRequired() {
        return totalTimeRequired;
    }

    public void setTotalTimeRequired(Long totalTimeRequired) {
        this.totalTimeRequired = totalTimeRequired;
    }

    public Long getNumberOfTripsFinished() {
        return numberOfTripsFinished;
    }

    public void setNumberOfTripsFinished(Long numberOfTripsFinished) {
        this.numberOfTripsFinished = numberOfTripsFinished;
    }


}
