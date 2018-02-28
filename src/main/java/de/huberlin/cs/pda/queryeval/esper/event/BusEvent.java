package de.huberlin.cs.pda.queryeval.esper.event;

import java.text.DecimalFormat;
import java.time.LocalDate;

/**
 * Created by dimitar on 25.01.17.
 *
 * Data from https://data.dublinked.ie/dataset/dublin-bus-gps-sample-data-from-dublin-city-council-insight-project
 */
public class BusEvent extends Event {
    private final long timestamp;
    private final int lineID;
    private final int direction;
    private final String journeyPatternID;
    private final LocalDate timeFrame;
    private final int vehicleJourneyID;
    private final String busOperator;
    private final int congestion;
    private final double longitude;
    private final double latitude;
    private final int delay;
    private final int blockID;
    private final int vehicleID;
    private final int stopID;
    private final int atStop;

    public BusEvent(long timestamp,
                    int lineID,
                    int direction,
                    String journeyPatternID,
                    LocalDate timeFrame,
                    int vehicleJourneyID,
                    String busOperator,
                    int congestion,
                    double longitude,
                    double latitude,
                    int delay,
                    int blockID,
                    int vehicleID,
                    int stopID,
                    int atStop) {
        this.timestamp = timestamp;
        this.lineID = lineID;
        this.direction = direction;
        this.journeyPatternID = journeyPatternID;
        this.timeFrame = timeFrame;
        this.vehicleJourneyID = vehicleJourneyID;
        this.busOperator = busOperator;
        this.congestion = congestion;
        this.longitude = longitude;
        this.latitude = latitude;
        this.delay = delay;
        this.blockID = blockID;
        this.vehicleID = vehicleID;
        this.stopID = stopID;
        this.atStop = atStop;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getLineID() {
        return lineID;
    }

    public int getDirection() {
        return direction;
    }

    public String getJourneyPatternID() {
        return journeyPatternID;
    }

    public LocalDate getTimeFrame() {
        return timeFrame;
    }

    public int getVehicleJourneyID() {
        return vehicleJourneyID;
    }

    public String getBusOperator() {
        return busOperator;
    }

    public int getCongestion() {
        return congestion;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getDelay() {
        return delay;
    }

    public int getBlockID() {
        return blockID;
    }

    public int getVehicleID() {
        return vehicleID;
    }

    public int getStopID() {
        return stopID;
    }

    public int getAtStop() {
        return atStop;
    }

    @Override
    public String toString() {
        DecimalFormat coordinateFormat = new DecimalFormat("#0.000000");

        return timestamp +
                "," + lineID +
                "," + direction +
                "," + journeyPatternID +
                "," + timeFrame +
                "," + vehicleJourneyID +
                "," + busOperator +
                "," + congestion +
                "," + coordinateFormat.format(longitude) +
                "," + coordinateFormat.format(latitude) +
                "," + delay +
                "," + blockID +
                "," + vehicleID +
                "," + stopID +
                "," + atStop;
    }
}
