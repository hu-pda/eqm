package de.huberlin.cs.pda.queryeval.partitioning;

import de.huberlin.cs.pda.queryeval.esper.event.StockEvent;
import de.huberlin.cs.pda.queryeval.esper.event.TaxiEvent;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Partitioner {

    public static void run(String dataset, File datasetFolder, String baseQuery, Integer windowSize, File saveDir) throws IOException {

        PartitionData dataPartitioning = new PartitionData();

        LineParser lineParser;
        List<PatternMatchFile> patterns = new ArrayList<>();

        TracePartitioning positiveTracePartitionType = TracePartitioning.POSITIVE_WINDOW;
        double positiveTraceSubsamplePercentage = 1.0;
        //TracePartitioning negativeTracePartitionType;
        //double negativeTraceSlidePercentage;
        //double negativeTraceSubsamplePercentage;

        patterns.add(new PatternMatchFile(new File(saveDir, baseQuery + "-esper-matches").getPath(), windowSize));

        switch (dataset) {
            case "dublin":
                lineParser = line -> {
                    String[] fields = line.split(",", 15);
                    long timestamp = Long.parseLong(fields[0]) / 1_000;

                    return new AbstractMap.SimpleImmutableEntry<>(line, timestamp);
                };
                break;
            case "google-cluster":
                lineParser = line -> {
                    String[] fields = line.split(",", 13);
                    long time = Long.parseLong(fields[0]);

                    return new AbstractMap.SimpleImmutableEntry<>(line, time);
                };
                break;
            case "debs2015":
                lineParser = line -> {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(TaxiEvent.DATE_TIME_FORMAT);
                    String[] fields = line.split(",", 17);
                    long dropoffDatetime = LocalDateTime.parse(fields[3], dateTimeFormatter).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                    return new AbstractMap.SimpleImmutableEntry<>(line, dropoffDatetime);
                };
                break;
            case "nasdaq":
                lineParser = line -> {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(StockEvent.DATE_TIME_FORMAT);
                    String[] fields = line.split(",", 8);
                    long date = LocalDateTime.parse(fields[2], dateTimeFormatter).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                    return new AbstractMap.SimpleImmutableEntry<>(line, date);
                };
                break;
            default:
                System.out.println("Choose a dataset [dublin|google-cluster|debs2015|nasdaq]");
                return;
        }

        List<List<String>> traces;
        //List<List<String>> negativeTraces;

        List<Map.Entry<String, Long>> sourceEvents = dataPartitioning.loadSourceEvents(datasetFolder, lineParser);

        for (PatternMatchFile pattern: patterns) {
            //pattern.createNewFile();
            // find the situations of interest of each match
            List<Map.Entry<String, Long>> situationsOfInterest = dataPartitioning.findSituationsOfInterest(pattern, lineParser);

            // partition the source data for the given matches to create positive traces
            traces = dataPartitioning.partitionData(sourceEvents, situationsOfInterest, pattern, positiveTracePartitionType);
            dataPartitioning.printStatistics(pattern, traces);
            dataPartitioning.writeTracesSubsample(traces, pattern.getPath() + "-positive", positiveTraceSubsamplePercentage);

            // partition the data to create negative traces
            // TODO: implement negatives traces properly
          //  negativeTraces = dataPartitioning.generateNegativeTraces(sourceEvents, pattern, negativeTracePartitionType, negativeTraceSlidePercentage);
          //  dataPartitioning.writeTracesSubsample(negativeTraces, pattern.getPath() + "-negative", negativeTraceSubsamplePercentage);
        }

    }
}
