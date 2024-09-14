package com.nice.coday;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ElectricityConsumptionCalculatorTest {
    @InjectMocks
    private static ConsumptionResult resultData;

    private ResourceInfo resourceInfo;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test1() throws IOException {

        Path chargingStationInfoPath = Paths.get("src/main/resources/TestCase1/ChargingStationInfo.csv");
        Path entryExitPointInfoPath = Paths.get("src/main/resources/TestCase1/EntryExitPointInfo.csv");
        Path timeToChargeVehicleInfoPath = Paths.get("src/main/resources/TestCase1/TimeToChargeVehicleInfo.csv");
        Path tripDetailsPath = Paths.get("src/main/resources/TestCase1/TripDetails.csv");
        Path vehicleTypeInfoPath = Paths.get("src/main/resources/TestCase1/VehicleTypeInfo.csv");

        resourceInfo = new ResourceInfo(chargingStationInfoPath, entryExitPointInfoPath, timeToChargeVehicleInfoPath, tripDetailsPath, vehicleTypeInfoPath);
        ElectricityConsumptionCalculator analyzer = new ElectricityConsumptionCalculatorImpl();
        resultData = analyzer.calculateElectricityAndTimeConsumption(resourceInfo);

        //Total Unit Consume by all vehicles
        double expectedTotalUnitsConsumed = 690.60; // The expected sum of all TotalUnitConsumed
        double actualTotalUnitsConsumed  = resultData.getConsumptionDetails().stream().mapToDouble(ConsumptionDetails::getTotalUnitConsumed).sum();
        Assert.assertEquals(expectedTotalUnitsConsumed, actualTotalUnitsConsumed, 1.0);

        //Total Unit Consume by Vehicle Type V1
        double expectedTotalUnitsConsumedByV1 = 534.06; // The expected sum of TotalUnitConsumed for VehicleType "V1"
        double actualTotalUnitsConsumedByV1 = resultData.getConsumptionDetails().stream()
                .filter(cd -> cd.getVehicleType().equals("V1"))
                .mapToDouble(ConsumptionDetails::getTotalUnitConsumed)
                .sum();
        Assert.assertEquals(expectedTotalUnitsConsumedByV1, actualTotalUnitsConsumedByV1, 1.0);

        //Total Unit Consume by Vehicle Type V2
        double expectedTotalUnitsConsumedByV2 = 156.53; // The expected sum of TotalUnitConsumed for VehicleType "V1"
        double actualTotalUnitsConsumedByV2 = resultData.getConsumptionDetails().stream()
                .filter(cd -> cd.getVehicleType().equals("V2"))
                .mapToDouble(ConsumptionDetails::getTotalUnitConsumed)
                .sum();
        Assert.assertEquals(expectedTotalUnitsConsumedByV2, actualTotalUnitsConsumedByV2, 1.0);

        // Total Time required for charging Vehicle Type V2
        long expectedTotalTimeRequiredByV2 = 55022;
        long actualTotalTimeRequiredByV1 = resultData.getConsumptionDetails().stream().filter(cd -> cd.getVehicleType().equals("V2")).mapToLong(ConsumptionDetails::getTotalTimeRequired).sum();
        Assert.assertEquals(expectedTotalTimeRequiredByV2, actualTotalTimeRequiredByV1, 50);

        //Total time required for charging any vehicle at Charging Station Ch2
        int expectedTotalTimeRequiredAtC2 = 10570;
        double actualTotalTimeRequiredAtC2 = resultData.getTotalChargingStationTime().get("C2");
        Assert.assertEquals(expectedTotalTimeRequiredAtC2, actualTotalTimeRequiredAtC2, 50);

        //Total time required for charging any vehicle at Charging Station Ch10
        int expectedTotalTimeRequiredAtC10 = 46500;
        double actualTotalTimeRequiredAtC10 = resultData.getTotalChargingStationTime().get("C10");
        Assert.assertEquals(expectedTotalTimeRequiredAtC10, actualTotalTimeRequiredAtC10, 50);

        //Number of trips finished by vehicle type V2
        long expectedNumberOfTripsFinished = 16;
        long  actualNumberOfTripsFinished = resultData.getConsumptionDetails().stream()
                .map(ConsumptionDetails::getNumberOfTripsFinished)
                .reduce(0L, Long::sum);
        Assert.assertEquals(expectedNumberOfTripsFinished, actualNumberOfTripsFinished, 0.0);
    }

    @Test
    public void test2() throws IOException {

        Path chargingStationInfoPath = Paths.get("src/main/resources/TestCase2/ChargingStationInfo.csv");
        Path entryExitPointInfoPath = Paths.get("src/main/resources/TestCase2/EntryExitPointInfo.csv");
        Path timeToChargeVehicleInfoPath = Paths.get("src/main/resources/TestCase2/TimeToChargeVehicleInfo.csv");
        Path tripDetailsPath = Paths.get("src/main/resources/TestCase2/TripDetails.csv");
        Path vehicleTypeInfoPath = Paths.get("src/main/resources/TestCase2/VehicleTypeInfo.csv");

        resourceInfo = new ResourceInfo(chargingStationInfoPath, entryExitPointInfoPath, timeToChargeVehicleInfoPath, tripDetailsPath, vehicleTypeInfoPath);
        ElectricityConsumptionCalculator analyzer = new ElectricityConsumptionCalculatorImpl();
        resultData = analyzer.calculateElectricityAndTimeConsumption(resourceInfo);

        //Total Unit Consume by all vehicles
        double expectedTotalUnitsConsumed = 3972;
        double actualTotalUnitsConsumed  = resultData.getConsumptionDetails().stream().mapToDouble(ConsumptionDetails::getTotalUnitConsumed).sum();
        Assert.assertEquals(expectedTotalUnitsConsumed, actualTotalUnitsConsumed, 2.0);

        //Total Unit Consume by Vehicle Type V4
        double expectedTotalUnitsConsumedByV4 = 670.27;
        double actualTotalUnitsConsumedByV4 = resultData.getConsumptionDetails().stream()
                .filter(cd -> cd.getVehicleType().equals("V4"))
                .mapToDouble(ConsumptionDetails::getTotalUnitConsumed)
                .sum();
        System.out.println("actualTotalUnitsConsumedByV4 = " + actualTotalUnitsConsumedByV4);
        Assert.assertEquals(expectedTotalUnitsConsumedByV4, actualTotalUnitsConsumedByV4, 1);

        //Total Time required for charging Vehicle Type V1
        int expectedTotalTimeRequiredByV1 = 238437; // The expected sum of TotalUnitConsumed for VehicleType "V2"
        long actualTotalTimeRequiredByV1 = resultData.getConsumptionDetails().stream().filter(cd -> cd.getVehicleType().equals("V1")).mapToLong(ConsumptionDetails::getTotalTimeRequired).sum();
        Assert.assertEquals(expectedTotalTimeRequiredByV1, actualTotalTimeRequiredByV1, 120);
    }

    @Test
    public void test3() throws IOException {

        Path chargingStationInfoPath = Paths.get("src/main/resources/TestCase3/ChargingStationInfo.csv");
        Path entryExitPointInfoPath = Paths.get("src/main/resources/TestCase3/EntryExitPointInfo.csv");
        Path timeToChargeVehicleInfoPath = Paths.get("src/main/resources/TestCase3/TimeToChargeVehicleInfo.csv");
        Path tripDetailsPath = Paths.get("src/main/resources/TestCase3/TripDetails.csv");
        Path vehicleTypeInfoPath = Paths.get("src/main/resources/TestCase3/VehicleTypeInfo.csv");

        resourceInfo = new ResourceInfo(chargingStationInfoPath, entryExitPointInfoPath, timeToChargeVehicleInfoPath, tripDetailsPath, vehicleTypeInfoPath);
        ElectricityConsumptionCalculator analyzer = new ElectricityConsumptionCalculatorImpl();
        resultData = analyzer.calculateElectricityAndTimeConsumption(resourceInfo);

        //Total Unit Consume by all vehicles
        double expectedTotalUnitsConsumed = 0;
        double actualTotalUnitsConsumed  = resultData.getConsumptionDetails().stream().mapToDouble(ConsumptionDetails::getTotalUnitConsumed).sum();
        Assert.assertEquals(expectedTotalUnitsConsumed, actualTotalUnitsConsumed, 0.0);

        //Number of trips finished
        long expectedNumberOfTripsFinished = 0;
        long actualNumberOfTripsFinished = resultData.getConsumptionDetails().stream()
                .map(ConsumptionDetails::getNumberOfTripsFinished)
                .reduce(0L, Long::sum);
        Assert.assertEquals(expectedNumberOfTripsFinished, actualNumberOfTripsFinished, 0.0);
    }

    @Test
    public void test4() throws IOException {

        Path chargingStationInfoPath = Paths.get("src/main/resources/TestCase4/ChargingStationInfo.csv");
        Path entryExitPointInfoPath = Paths.get("src/main/resources/TestCase4/EntryExitPointInfo.csv");
        Path timeToChargeVehicleInfoPath = Paths.get("src/main/resources/TestCase4/TimeToChargeVehicleInfo.csv");
        Path tripDetailsPath = Paths.get("src/main/resources/TestCase4/TripDetails.csv");
        Path vehicleTypeInfoPath = Paths.get("src/main/resources/TestCase4/VehicleTypeInfo.csv");

        resourceInfo = new ResourceInfo(chargingStationInfoPath, entryExitPointInfoPath, timeToChargeVehicleInfoPath, tripDetailsPath, vehicleTypeInfoPath);
        ElectricityConsumptionCalculator analyzer = new ElectricityConsumptionCalculatorImpl();
        resultData = analyzer.calculateElectricityAndTimeConsumption(resourceInfo);

        //Total Unit Consume by all vehicles
        double expectedTotalUnitsConsumed = 736.0;
        double actualTotalUnitsConsumed  = resultData.getConsumptionDetails().stream().mapToDouble(ConsumptionDetails::getTotalUnitConsumed).sum();
        Assert.assertEquals(expectedTotalUnitsConsumed, actualTotalUnitsConsumed, 2.0);

        //Total Unit Consume by Vehicle Type V4
        double expectedTotalUnitsConsumedByV4 = 174.0;
        double actualTotalUnitsConsumedByV4 = resultData.getConsumptionDetails().stream()
                .filter(cd -> cd.getVehicleType().equals("V4"))
                .mapToDouble(ConsumptionDetails::getTotalUnitConsumed)
                .sum();
        Assert.assertEquals(expectedTotalUnitsConsumedByV4, actualTotalUnitsConsumedByV4, 1.0);

        //Total Time required for charging Vehicle Type V2
        long expectedTotalTimeRequiredByV1 = 11649;
        long actualTotalTimeRequiredByV1 = resultData.getConsumptionDetails().stream()
                .filter(cd -> cd.getVehicleType().equals("V2"))
                .mapToLong(ConsumptionDetails::getTotalTimeRequired)
                .sum();
        Assert.assertEquals(expectedTotalTimeRequiredByV1, actualTotalTimeRequiredByV1, 60L);

        //Number of trips finished
        long expectedNumberOfTripsFinished = 40L;
        long  actualNumberOfTripsFinished = resultData.getConsumptionDetails().stream()
                .map(ConsumptionDetails::getNumberOfTripsFinished)
                .reduce(0L, Long::sum);
        Assert.assertEquals(expectedNumberOfTripsFinished, actualNumberOfTripsFinished, 0.0);

    }

    @Test
    public void test5() throws IOException {

        Path chargingStationInfoPath = Paths.get("src/main/resources/TestCase5/ChargingStationInfo.csv");
        Path entryExitPointInfoPath = Paths.get("src/main/resources/TestCase5/EntryExitPointInfo.csv");
        Path timeToChargeVehicleInfoPath = Paths.get("src/main/resources/TestCase5/TimeToChargeVehicleInfo.csv");
        Path tripDetailsPath = Paths.get("src/main/resources/TestCase5/TripDetails.csv");
        Path vehicleTypeInfoPath = Paths.get("src/main/resources/TestCase5/VehicleTypeInfo.csv");

        resourceInfo = new ResourceInfo(chargingStationInfoPath, entryExitPointInfoPath, timeToChargeVehicleInfoPath, tripDetailsPath, vehicleTypeInfoPath);
        ElectricityConsumptionCalculator analyzer = new ElectricityConsumptionCalculatorImpl();
        resultData = analyzer.calculateElectricityAndTimeConsumption(resourceInfo);

        // Total Unit Consume by all vehicles
        double expectedTotalUnitsConsumed = 351797;
        double actualTotalUnitsConsumed  = resultData.getConsumptionDetails().stream()
                .mapToDouble(ConsumptionDetails::getTotalUnitConsumed)
                .sum();
        Assert.assertEquals(expectedTotalUnitsConsumed, actualTotalUnitsConsumed, 10.0);

        // Total Unit Consume by Vehicle Type V4
        double expectedTotalUnitsConsumedByV4 = 39110;
        double actualTotalUnitsConsumedByV4 = resultData.getConsumptionDetails().stream()
                .filter(cd -> cd.getVehicleType().equals("V4"))
                .mapToDouble(ConsumptionDetails::getTotalUnitConsumed)
                .sum();
        Assert.assertEquals(expectedTotalUnitsConsumedByV4, actualTotalUnitsConsumedByV4, 10.0);

        // Total Time required for charging Vehicle Type V2
        long expectedTotalTimeRequiredByV2 = 10716795;
        long actualTotalTotalTimeRequiredByV2 = resultData.getConsumptionDetails().stream().filter(cd -> cd.getVehicleType().equals("V2")).mapToLong(ConsumptionDetails::getTotalTimeRequired).sum();
        Assert.assertEquals(expectedTotalTimeRequiredByV2, actualTotalTotalTimeRequiredByV2, 600L);

        // Number of trips finished
        long expectedNumberOfTripsFinished = 10000;
        long actualNumberOfTripsFinished = resultData.getConsumptionDetails().stream()
                .map(ConsumptionDetails::getNumberOfTripsFinished)
                .reduce(0L, Long::sum);
        Assert.assertEquals(expectedNumberOfTripsFinished, actualNumberOfTripsFinished);
    }

    @Test
    public void test6() throws IOException {

        Path chargingStationInfoPath = Paths.get("src/main/resources/TestCase6/ChargingStationInfo.csv");
        Path entryExitPointInfoPath = Paths.get("src/main/resources/TestCase6/EntryExitPointInfo.csv");
        Path timeToChargeVehicleInfoPath = Paths.get("src/main/resources/TestCase6/TimeToChargeVehicleInfo.csv");
        Path tripDetailsPath = Paths.get("src/main/resources/TestCase6/TripDetails.csv");
        Path vehicleTypeInfoPath = Paths.get("src/main/resources/TestCase6/VehicleTypeInfo.csv");

        resourceInfo = new ResourceInfo(chargingStationInfoPath, entryExitPointInfoPath, timeToChargeVehicleInfoPath, tripDetailsPath, vehicleTypeInfoPath);
        ElectricityConsumptionCalculator analyzer = new ElectricityConsumptionCalculatorImpl();
        resultData = analyzer.calculateElectricityAndTimeConsumption(resourceInfo);

        //Total Unit Consume by all vehicles
        double expectedTotalUnitsConsumed = 2741580; // The expected sum of all TotalUnitConsumed
        double actualTotalUnitsConsumed  = resultData.getConsumptionDetails().stream().mapToDouble(ConsumptionDetails::getTotalUnitConsumed).sum();
        System.out.println("actualTotalUnitsConsumed : " + actualTotalUnitsConsumed);
        Assert.assertEquals(expectedTotalUnitsConsumed, actualTotalUnitsConsumed, 50.0);

        //Total Unit Consume by Vehicle Type V16
        double expectedTotalUnitsConsumedByV16 = 224771; // The expected sum of TotalUnitConsumed for VehicleType "V1"
        double actualTotalUnitsConsumedByV16 = resultData.getConsumptionDetails().stream()
                .filter(cd -> cd.getVehicleType().equals("V16"))
                .mapToDouble(ConsumptionDetails::getTotalUnitConsumed)
                .sum();
        System.out.println("actualTotalUnitsConsumedByV16 = " + actualTotalUnitsConsumedByV16);
        Assert.assertEquals(expectedTotalUnitsConsumedByV16, actualTotalUnitsConsumedByV16, 10.0);


        //Total Time required for charging Vehicle Type V12
        long expectedTotalTimeRequiredByV12 = 41961924L;
        long actualTotalTimeRequiredByV12 = resultData.getConsumptionDetails().stream().filter(cd -> cd.getVehicleType().equals("V12"))
                .mapToLong(ConsumptionDetails::getTotalTimeRequired)
                .sum();
        Assert.assertEquals(expectedTotalTimeRequiredByV12, actualTotalTimeRequiredByV12, 600);

        //Number of trips finished
        long expectedNumberOfTripsFinished = 29795;
        long actualNumberOfTripsFinished = resultData.getConsumptionDetails().stream()
                .map(ConsumptionDetails::getNumberOfTripsFinished)
                .reduce(0L, Long::sum);
        Assert.assertEquals(expectedNumberOfTripsFinished, actualNumberOfTripsFinished);

    }

    @Test
    public void test7() throws IOException {

        Path chargingStationInfoPath = Paths.get("src/main/resources/TestCase7/ChargingStationInfo.csv");
        Path entryExitPointInfoPath = Paths.get("src/main/resources/TestCase7/EntryExitPointInfo.csv");
        Path timeToChargeVehicleInfoPath = Paths.get("src/main/resources/TestCase7/TimeToChargeVehicleInfo.csv");
        Path tripDetailsPath = Paths.get("src/main/resources/TestCase7/TripDetails.csv");
        Path vehicleTypeInfoPath = Paths.get("src/main/resources/TestCase7/VehicleTypeInfo.csv");

        resourceInfo = new ResourceInfo(chargingStationInfoPath, entryExitPointInfoPath, timeToChargeVehicleInfoPath, tripDetailsPath, vehicleTypeInfoPath);
        ElectricityConsumptionCalculator analyzer = new ElectricityConsumptionCalculatorImpl();
        resultData = analyzer.calculateElectricityAndTimeConsumption(resourceInfo);

        //Total Unit Consume by all vehicles
        double expectedTotalUnitsConsumed = 12823458; // The expected sum of all TotalUnitConsumed
        double actualTotalUnitsConsumed  = resultData.getConsumptionDetails().stream().mapToDouble(ConsumptionDetails::getTotalUnitConsumed).sum();
        Assert.assertEquals(expectedTotalUnitsConsumed, actualTotalUnitsConsumed, 200.0);

        //Total Unit Consume by Vehicle Type V29
        double expectedTotalUnitsConsumedByV29 = 143514.85; // The expected sum of TotalUnitConsumed for VehicleType "V1"
        double actualTotalUnitsConsumedByV29 = resultData.getConsumptionDetails().stream()
                .filter(cd -> cd.getVehicleType().equals("V29"))
                .mapToDouble(ConsumptionDetails::getTotalUnitConsumed)
                .sum();
        Assert.assertEquals(expectedTotalUnitsConsumedByV29, actualTotalUnitsConsumedByV29, 20.0);


        //Total time required for charging any vehicle at Charging Station Ch183
        long expectedTotalTimeRequiredAtC183 = 22999411L;
        long actualTotalTimeRequiredAtC183 = resultData.getTotalChargingStationTime().get("C183");
        Assert.assertEquals(expectedTotalTimeRequiredAtC183, actualTotalTimeRequiredAtC183, 720L);

        //Number of trips finished
        long expectedNumberOfTripsFinished = 99227;
        long  actualNumberOfTripsFinished = resultData.getConsumptionDetails().stream()
                .map(ConsumptionDetails::getNumberOfTripsFinished)
                .reduce(0L, Long::sum);
        Assert.assertEquals(expectedNumberOfTripsFinished, actualNumberOfTripsFinished);

    }
}
