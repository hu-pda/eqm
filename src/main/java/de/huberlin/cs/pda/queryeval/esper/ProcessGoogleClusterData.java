package de.huberlin.cs.pda.queryeval.esper;

import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.deploy.DeploymentResult;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.core.service.EPStatementImpl;
import de.huberlin.cs.pda.queryeval.esper.event.ClusterTaskEvent;
import de.huberlin.cs.pda.queryeval.esper.event.Event;
import de.huberlin.cs.pda.queryeval.esper.listener.ClusterTaskEventListener;
import de.huberlin.cs.pda.queryeval.esper.listener.EventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by dimitar on 30.01.17.
 *
 * Only supports TaskEvent Traces for now.
 */
public class ProcessGoogleClusterData extends ProcessData {
    private final Logger logger = LoggerFactory.getLogger(ProcessGoogleClusterData.class);

    @NotNull
    private static ClusterTaskEvent convertLineToEvent(String line) {
        String[] fields = line.split(",", 13);

        long time = Long.parseLong(fields[0]);
        String missingInfo = fields[1];
        String jobID = fields[2];
        String taskIndex = fields[3];
        String machineID = fields[4];
        String eventType = fields[5];
        String user = fields[6];
        String schedulingClass = fields[7];
        String priority = fields[8];
        String cpuRequest = fields[9];
        String memoryRequest = fields[10];
        String diskSpaceRequest = fields[11];
        String differentMachinesRestriction = fields[12];

        return new ClusterTaskEvent(time,
                missingInfo,
                jobID,
                taskIndex,
                machineID,
                eventType,
                user,
                schedulingClass,
                priority,
                cpuRequest,
                memoryRequest,
                diskSpaceRequest,
                differentMachinesRestriction);
    }

    protected Map<String, List<Map<String, Event>>> esper(EPServiceProvider epService, DeploymentResult deploymentResult, File eventLog, long startingTime) {
        EPRuntime runtime = epService.getEPRuntime();

        // send a starting CurrentTimeEvent, time starts at 19:00 of May 1, 2011
        //startingTime = LocalDateTime.parse("2011-05-01T19:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        runtime.sendEvent(new CurrentTimeEvent(startingTime));

        Map<EPStatement, EventListener> listeners = new HashMap<>();

        // add the corresponding listener to each statement
        for (EPStatement statement: deploymentResult.getStatements()) {
            if (((EPStatementImpl) statement).isNameProvided()) {
                EventListener listener = new ClusterTaskEventListener();
                statement.addListener(listener);
                listeners.put(statement, listener);
            }
        }

        logger.info("Parsing events from file: {}", eventLog.getPath());

        // read all the lines from the file, convert them to event objects
        // send timestamp as a CurrentTimeEvent if the time has changed
        // send the event
        long processedEvents = 0L;
        long lastTimestamp = 0L;
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(eventLog), 2_097_152), "US-ASCII"))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                ClusterTaskEvent clusterTaskEvent = convertLineToEvent(line);

                // the event time is given as microseconds after the start of the trace window
                long eventTime = clusterTaskEvent.getTime();

                // time of 0 means that the event has been scheduled before the trace window
                if (eventTime == 0) {
                    continue;
                }

                // convert the event time from microseconds to milliseconds
                eventTime = eventTime / 1_000;

                // calculate the actual starting time of the event
                eventTime = startingTime + eventTime;
                if (eventTime > lastTimestamp) {
                    runtime.sendEvent(new CurrentTimeEvent(eventTime));
                    lastTimestamp = eventTime;
                }
                runtime.sendEvent(clusterTaskEvent);

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
