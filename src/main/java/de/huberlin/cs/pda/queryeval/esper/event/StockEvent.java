package de.huberlin.cs.pda.queryeval.esper.event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by dimitar on 25.01.17.
 *
 * Data from http://www.eoddata.com/products/historicaldata.aspx
 */
public class StockEvent extends Event {
    private final String ticker;
    private final String per;
    private final LocalDateTime date;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final long vol;

    public static String DATE_TIME_FORMAT = "yyyyMMddHHmm";

    public StockEvent(String ticker,
                      String per,
                      LocalDateTime date,
                      double open,
                      double high,
                      double low,
                      double close,
                      long vol) {
        this.ticker = ticker;
        this.per = per;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.vol = vol;
    }

    public String getTicker() {
        return ticker;
    }

    public String getPer() {
        return per;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public long getVol() {
        return vol;
    }

    @Override
    public String toString() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

        return ticker +
                "," + per +
                "," + dateTimeFormatter.format(date) +
                "," + open +
                "," + high +
                "," + low +
                "," + close +
                "," + vol;
    }
}
