package de.huberlin.cs.pda.queryeval.esper;

import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.deploy.DeploymentResult;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.core.service.EPStatementImpl;
import de.huberlin.cs.pda.queryeval.esper.event.Event;
import de.huberlin.cs.pda.queryeval.esper.event.TaxiEvent;
import de.huberlin.cs.pda.queryeval.esper.listener.EventListener;
import de.huberlin.cs.pda.queryeval.esper.listener.TaxiEventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by dimitar on 27.01.17.
 *
 * This class contains functions that read from the CSV file,
 * convert the lines to events, then send CurrentTimeEvent and TaxiEvent
 * to Esper. matchedSequences contains the matching events.
 */
public class ProcessDEBS2015Data extends ProcessData {
    private final Logger logger = LoggerFactory.getLogger(ProcessDEBS2015Data.class);

    @NotNull
    private static TaxiEvent convertLineToEvent(String line) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(TaxiEvent.DATE_TIME_FORMAT);

        String[] fields = line.split(",", 17);

        String medallion = fields[0];
        String hackLicense = fields[1];
        LocalDateTime pickupDatetime = LocalDateTime.parse(fields[2], dateTimeFormatter);
        LocalDateTime dropoffDatetime = LocalDateTime.parse(fields[3], dateTimeFormatter);
        int tripTimeInSeconds = Integer.parseInt(fields[4]);
        double tripDistance = Double.parseDouble(fields[5]);
        double pickupLongitude = Double.parseDouble(fields[6]);
        double pickupLatitude = Double.parseDouble(fields[7]);
        double dropoffLongitude = Double.parseDouble(fields[8]);
        double dropoffLatitude = Double.parseDouble(fields[9]);
        String paymentType = fields[10];
        double fareAmount = Double.parseDouble(fields[11]);
        double surcharge = Double.parseDouble(fields[12]);
        double mtaTax = Double.parseDouble(fields[13]);
        double tipAmount = Double.parseDouble(fields[14]);
        double tollsAmount = Double.parseDouble(fields[15]);
        double totalAmount = Double.parseDouble(fields[16]);

        // TODO should events with incorrect information be ignored
        /*
        // taxi trip with incorrect location information
        if (pickupLongitude == 0.0 || pickupLatitude == 0.0 || dropoffLongitude == 0.0 || dropoffLatitude == 0.0) {
            return null;
        }
        */

        return new TaxiEvent(medallion, hackLicense,
                pickupDatetime, dropoffDatetime,
                tripTimeInSeconds, tripDistance,
                pickupLongitude, pickupLatitude,
                dropoffLongitude, dropoffLatitude,
                paymentType,
                fareAmount, surcharge, mtaTax, tipAmount, tollsAmount,
                totalAmount);
    }

    @Override
    protected Map<String, List<Map<String, Event>>> esper(EPServiceProvider epService, DeploymentResult deploymentResult, File eventLog, long startingTime, String baseQuery, String[] evaluatedQueries, String[] evaluatedGroups) {
        EPRuntime runtime = epService.getEPRuntime();

        // send a starting CurrentTimeEvent
        //startingTime = LocalDateTime.parse("2013-01-01T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        runtime.sendEvent(new CurrentTimeEvent(startingTime));

        Map<EPStatement,String> filteredStatements = filterStatements(deploymentResult, baseQuery, evaluatedQueries, evaluatedGroups);
        Map<String, EventListener> listeners = new HashMap<>();
        for(Map.Entry<EPStatement,String> entry : filteredStatements.entrySet()) {
            EventListener listener = new TaxiEventListener();
            entry.getKey().addListener(listener);
            listeners.put(entry.getValue(), listener);
        }

        logger.info("Parsing events from file: {}", eventLog.getPath());

        // read all the lines from the file, convert them to TaxiEvent objects
        // send dropoffDatetime as a CurrentTimeEvent if the time has changed
        // send the TaxiEvent
        long processedEvents = 0L;
        long lastTimestamp = 0L;
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(eventLog), 2_097_152), "US-ASCII"))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                TaxiEvent taxiEvent = convertLineToEvent(line);

                long eventTimestamp = taxiEvent.getDropoffDatetime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                if (eventTimestamp > lastTimestamp) {
                    runtime.sendEvent(new CurrentTimeEvent(eventTimestamp));
                    lastTimestamp = eventTimestamp;
                }
                runtime.sendEvent(taxiEvent);

                if (processedEvents % 100_000 == 0) {
                    logger.info("{} events processed.", processedEvents);
                }
                processedEvents++;
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        logger.info("Parsing events from file: {} done.", eventLog.getPath());

        // return the found matches {<statement-id>: [{<variable-in-pattern>, <event>},]
        Map<String, List<Map<String, Event>>> matches = new HashMap<>();
        for (Map.Entry<String, EventListener> listener : listeners.entrySet()) {
            // defined in superclass
            matches.put(listener.getKey(), listener.getValue().getMatchedSequences());
        }

        return matches;
    }
}
