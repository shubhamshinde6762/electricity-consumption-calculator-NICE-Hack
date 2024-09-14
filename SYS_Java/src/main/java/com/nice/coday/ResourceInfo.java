package com.nice.coday;

import java.nio.file.Path;

public class ResourceInfo {

    Path chargingStationInfoPath;
    Path entryExitPointInfoPath;
    Path timeToChargeVehicleInfoPath;
    Path tripDetailsPath;
    Path vehicleTypeInfoPath;

    public ResourceInfo(Path chargingStationInfoPath,
                        Path entryExitPointInfoPath,
                        Path timeToChargeVehicleInfoPath,
                        Path tripDetailsPath,
                        Path vehicleTypeInfoPath
                        ) {
        this.entryExitPointInfoPath = entryExitPointInfoPath;
        this.timeToChargeVehicleInfoPath = timeToChargeVehicleInfoPath;
        this.chargingStationInfoPath = chargingStationInfoPath;
        this.tripDetailsPath = tripDetailsPath;
        this.vehicleTypeInfoPath = vehicleTypeInfoPath;
    }

    public Path getChargingStationInfoPath() {
        return chargingStationInfoPath;
    }

    public void setChargingStationInfoPath(Path chargingStationInfoPath) {
        this.chargingStationInfoPath = chargingStationInfoPath;
    }

    public Path getEntryExitPointInfoPath() {
        return entryExitPointInfoPath;
    }

    public void setEntryExitPointInfoPath(Path entryExitPointInfoPath) {
        this.entryExitPointInfoPath = entryExitPointInfoPath;
    }

    public Path getTimeToChargeVehicleInfoPath() {
        return timeToChargeVehicleInfoPath;
    }

    public void setTimeToChargeVehicleInfoPath(Path timeToChargeVehicleInfoPath) {
        this.timeToChargeVehicleInfoPath = timeToChargeVehicleInfoPath;
    }

    public Path getTripDetailsPath() {
        return tripDetailsPath;
    }

    public void setTripDetailsPath(Path tripDetailsPath) {
        this.tripDetailsPath = tripDetailsPath;
    }

    public Path getVehicleTypeInfoPath() {
        return vehicleTypeInfoPath;
    }

    public void setVehicleTypeInfoPath(Path vehicleTypeInfoPath) {
        this.vehicleTypeInfoPath = vehicleTypeInfoPath;
    }
}
