package de.huberlin.cs.pda.queryeval.esper;

import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.deploy.DeploymentResult;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.core.service.EPStatementImpl;
import de.huberlin.cs.pda.queryeval.esper.event.Event;
import de.huberlin.cs.pda.queryeval.esper.event.StockEvent;
import de.huberlin.cs.pda.queryeval.esper.listener.EventListener;
import de.huberlin.cs.pda.queryeval.esper.listener.StockEventListener;
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
 * Created by dimitar on 10.02.17.
 */
public class ProcessNasdaqData extends ProcessData {
    private final Logger logger = LoggerFactory.getLogger(ProcessNasdaqData.class);

    @NotNull
    private static StockEvent convertLineToEvent(String line) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(StockEvent.DATE_TIME_FORMAT);

        String[] fields = line.split(",", 8);

        String ticker = fields[0];
        String per = fields[1];
        LocalDateTime date = LocalDateTime.parse(fields[2], dateTimeFormatter);
        double open = Double.parseDouble(fields[3]);
        double high = Double.parseDouble(fields[4]);
        double low = Double.parseDouble(fields[5]);
        double close = Double.parseDouble(fields[6]);
        long vol = Long.parseLong(fields[7]);

        return new StockEvent(ticker, per, date, open, high, low, close, vol);
    }

    @Override
    protected Map<String, List<Map<String, Event>>> esper(EPServiceProvider epService, DeploymentResult deploymentResult, File eventLog, long startingTime, String baseQuery, String[] evaluatedQueries, String[] evaluatedGroups) {
        EPRuntime runtime = epService.getEPRuntime();

        // send a starting CurrentTimeEvent
        //startingTime = LocalDateTime.parse("2010-11-01T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        runtime.sendEvent(new CurrentTimeEvent(startingTime));

        Map<EPStatement,String> filteredStatements = filterStatements(deploymentResult, baseQuery, evaluatedQueries, evaluatedGroups);

        Map<String, EventListener> listeners = new HashMap<>();
        for(Map.Entry<EPStatement,String> entry : filteredStatements.entrySet()){
            EventListener listener = new StockEventListener();
            entry.getKey().addListener(listener);
            listeners.put(entry.getValue(), listener);
        }

        logger.info("Parsing events from file: {}", eventLog.getPath());

        // read all the lines from the file, convert them to StockEvent objects
        // send date as a CurrentTimeEvent if the time has changed
        // send the StockEvent
        long processedEvents = 0L;
        long lastTimestamp = 0L;
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(eventLog), 2_097_152), "US-ASCII"))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                StockEvent stockEvent = convertLineToEvent(line);

                long eventTimestamp = stockEvent.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                if (eventTimestamp > lastTimestamp) {
                    runtime.sendEvent(new CurrentTimeEvent(eventTimestamp));
                    lastTimestamp = eventTimestamp;
                }
                runtime.sendEvent(stockEvent);

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
