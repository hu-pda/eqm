package de.huberlin.cs.pda.queryeval.esper;

import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.deploy.DeploymentResult;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.core.service.EPStatementImpl;
import de.huberlin.cs.pda.queryeval.esper.event.BusEvent;
import de.huberlin.cs.pda.queryeval.esper.event.Event;
import de.huberlin.cs.pda.queryeval.esper.listener.BusEventListener;
import de.huberlin.cs.pda.queryeval.esper.listener.EventListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by dimitar on 30.01.17.
 */
public class ProcessDublinData extends ProcessData {
    private final Logger logger = LoggerFactory.getLogger(ProcessDublinData.class);

    @Nullable
    private BusEvent convertLineToEvent(String line) {
        String[] fields = line.split(",", 15);

        try {
            long timestamp = Long.parseLong(fields[0]);
            int lineID = Integer.parseInt(fields[1]);
            int direction = Integer.parseInt(fields[2]);
            String journeyPatternID = fields[3];
            LocalDate timeFrame = LocalDate.parse(fields[4], DateTimeFormatter.ISO_DATE);
            int vehicleJourneyID = Integer.parseInt(fields[5]);
            String busOperator = fields[6];
            int congestion = Integer.parseInt(fields[7]);
            double longitude = Double.parseDouble(fields[8]);
            double latitude = Double.parseDouble(fields[9]);
            int delay = Integer.parseInt(fields[10]);
            int blockID = Integer.parseInt(fields[11]);
            int vehicleID = Integer.parseInt(fields[12]);
            int stopID = Integer.parseInt(fields[13]);
            int atStop = Integer.parseInt(fields[14]);

            return new BusEvent(timestamp,
                    lineID,
                    direction,
                    journeyPatternID,
                    timeFrame,
                    vehicleJourneyID,
                    busOperator,
                    congestion,
                    longitude,
                    latitude,
                    delay,
                    blockID,
                    vehicleID,
                    stopID,
                    atStop);
        } catch (NumberFormatException nfe) {
            logger.debug("Could not parse line: {}", nfe.getMessage());
            return null;
        }
    }

    @Override
    protected Map<String, List<Map<String, Event>>> esper(EPServiceProvider epService, DeploymentResult deploymentResult, File eventLog, long startingTime) {
        EPRuntime runtime = epService.getEPRuntime();

        // send a starting CurrentTimeEvent
        //startingTime = LocalDateTime.parse("2013-01-01T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        runtime.sendEvent(new CurrentTimeEvent(startingTime));

        Map<EPStatement, EventListener> listeners = new HashMap<>();

        // add the corresponding listener to each statement
        for (EPStatement statement: deploymentResult.getStatements()) {
            if (((EPStatementImpl) statement).isNameProvided()) {
                EventListener listener = new BusEventListener();
                statement.addListener(listener);
                listeners.put(statement, listener);
            }
        }

        logger.info("Parsing events from file: {}", eventLog.getPath());

        // read all the lines from the file, convert them to TaxiEvent objects
        // send timestamp as a CurrentTimeEvent if the time has changed
        // send the TaxiEvent
        long processedEvents = 0L;
        long lastTimestamp = 0L;
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(eventLog), 2_097_152), "US-ASCII"))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                BusEvent busEvent = convertLineToEvent(line);

                if (busEvent == null) {
                    continue;
                }

                // the timestamp is in microseconds
                long eventTimestamp = busEvent.getTimestamp() / 1_000;
                if (eventTimestamp > lastTimestamp) {
                    runtime.sendEvent(new CurrentTimeEvent(eventTimestamp));
                    lastTimestamp = eventTimestamp;
                }
                runtime.sendEvent(busEvent);

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
        for (Map.Entry<EPStatement, EventListener> listener : listeners.entrySet()) {
            matches.put(listener.getKey().getName(), listener.getValue().getMatchedSequences());
        }

        return matches;
    }
}
