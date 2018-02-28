package de.huberlin.cs.pda.queryeval.esper.event;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by dimitar on 25.01.17.
 *
 * Data from the following paper:
 *  Zbigniew Jerzak, Holger Ziekow:
 *  The DEBS 2015 grand challenge. DEBS 2015: 266-268
 */
public class TaxiEvent extends Event {
    private final String medallion;
    private final String hackLicense;
    private final LocalDateTime pickupDatetime;
    private final LocalDateTime dropoffDatetime; // the events are sorted by the dropoffDatetime
    private final int tripTimeInSeconds;
    private final double tripDistance;
    private final double pickupLongitude;
    private final double pickupLatitude;
    private final double dropoffLongitude;
    private final double dropoffLatitude;
    private final String paymentType;
    private final double fareAmount;
    private final double surcharge;
    private final double mtaTax;
    private final double tipAmount;
    private final double tollsAmount;
    private final double totalAmount;

    public static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public TaxiEvent(String medallion, String hackLicense,
                     LocalDateTime pickupDatetime, LocalDateTime dropoffDatetime,
                     int tripTimeInSeconds, double tripDistance,
                     double pickupLongitude, double pickupLatitude,
                     double dropoffLongitude, double dropoffLatitude,
                     String paymentType,
                     double fareAmount, double surcharge, double mtaTax, double tipAmount, double tollsAmount,
                     double totalAmount) {

        this.medallion = medallion;
        this.hackLicense = hackLicense;
        this.pickupDatetime = pickupDatetime;
        this.dropoffDatetime = dropoffDatetime;
        this.tripTimeInSeconds = tripTimeInSeconds;
        this.tripDistance = tripDistance;
        this.pickupLongitude = pickupLongitude;
        this.pickupLatitude = pickupLatitude;
        this.dropoffLongitude = dropoffLongitude;
        this.dropoffLatitude = dropoffLatitude;
        this.paymentType = paymentType;
        this.fareAmount = fareAmount;
        this.surcharge = surcharge;
        this.mtaTax = mtaTax;
        this.tipAmount = tipAmount;
        this.tollsAmount = tollsAmount;
        this.totalAmount = totalAmount;
    }

    public String getMedallion() {
        return medallion;
    }

    public String getHackLicense() {
        return hackLicense;
    }

    public LocalDateTime getPickupDatetime() {
        return pickupDatetime;
    }

    public LocalDateTime getDropoffDatetime() {
        return dropoffDatetime;
    }

    public int getTripTimeInSeconds() {
        return tripTimeInSeconds;
    }

    public double getTripDistance() {
        return tripDistance;
    }

    public double getPickupLongitude() {
        return pickupLongitude;
    }

    public double getPickupLatitude() {
        return pickupLatitude;
    }

    public double getDropoffLongitude() {
        return dropoffLongitude;
    }

    public double getDropoffLatitude() {
        return dropoffLatitude;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public double getFareAmount() {
        return fareAmount;
    }

    public double getSurcharge() {
        return surcharge;
    }

    public double getMtaTax() {
        return mtaTax;
    }

    public double getTipAmount() {
        return tipAmount;
    }

    public double getTollsAmount() {
        return tollsAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    @Override
    public String toString() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        DecimalFormat coordinateFormat = new DecimalFormat("#0.000000");

        return medallion + ',' + hackLicense +
                ',' + dateTimeFormatter.format(pickupDatetime) +
                ',' + dateTimeFormatter.format(dropoffDatetime) +
                ',' + tripTimeInSeconds +
                ',' + decimalFormat.format(tripDistance) +
                ',' + coordinateFormat.format(pickupLongitude) +
                ',' + coordinateFormat.format(pickupLatitude) +
                ',' + coordinateFormat.format(dropoffLongitude) +
                ',' + coordinateFormat.format(dropoffLatitude) +
                ',' + paymentType +
                ',' + decimalFormat.format(fareAmount) +
                ',' + decimalFormat.format(surcharge) +
                ',' + decimalFormat.format(mtaTax) +
                ',' + decimalFormat.format(tipAmount) +
                ',' + decimalFormat.format(tollsAmount) +
                ',' + decimalFormat.format(totalAmount);
    }
}
