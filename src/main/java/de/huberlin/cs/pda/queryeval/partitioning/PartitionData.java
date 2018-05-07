package de.huberlin.cs.pda.queryeval.partitioning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by dimitar on 01/03/17.
 */
public class PartitionData {
    private final Logger logger = LoggerFactory.getLogger(PartitionData.class);

    /*
        Load all the events in a list, mapping event string to date
     */
    List<Map.Entry<String, Long>> loadSourceEvents(File sourceFolder, LineParser lineParser) {
        logger.info("Reading events from folder: {}", sourceFolder);

        List<Map.Entry<String, Long>> sourceEvents = new ArrayList<>();

        File[] files = sourceFolder.listFiles();
        for(File file : files){
            logger.info("Reading events from file: {}", file);
            try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file), 2_097_152), "US-ASCII"))) {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    Map.Entry<String, Long> event = lineParser.parseLine(line);
                    sourceEvents.add(event);
                }

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        logger.info("Reading events from folder: {} done.", sourceFolder);

        return sourceEvents;
    }

    /*
        Read the pattern matches and find the situations of interest (last event in each pattern match).
    */
    List<Map.Entry<String, Long>> findSituationsOfInterest(File patternMatchData, LineParser lineParser) {
        logger.info("Reading situations of interest for pattern: {}", patternMatchData);

        // list of (event, date)
        List<Map.Entry<String, Long>> situationOfInterestForPattern = new ArrayList<>();

        try (BufferedReader fileReader = new BufferedReader(new FileReader(patternMatchData))) {
            String currentLine;
            String lastLine = null;

            // there are no empty lines at the start of the patternMatchData file
            while ((currentLine = fileReader.readLine()) != null) {
                if (currentLine.isEmpty()) {
                    Map.Entry<String, Long> lastSituationOfInterest = lineParser.parseLine(lastLine);
                    situationOfInterestForPattern.add(lastSituationOfInterest);
                }
                lastLine = currentLine;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        logger.info("Reading situations of interest for pattern: {} done.", patternMatchData);

        return situationOfInterestForPattern;
    }

    /*
        Partition the data based on the partitioning type.

        TODO better partitioning
    */
    List<List<String>> partitionData(List<Map.Entry<String, Long>> sourceEvents,
                                            List<Map.Entry<String, Long>> situations,
                                            PatternMatchFile pattern,
                                            TracePartitioning partitionType) {

        logger.info("Partitioning data for pattern: {}, type: {}, window size: {} ms.", pattern, partitionType, pattern.getWindowSize());


        List<List<String>> traces = new ArrayList<>();

        // switch for the type of trace partitioning
        if (partitionType == TracePartitioning.POSITIVE_FULL) {
            traces = partitionDataUsingSituations(sourceEvents, situations);
        } else if (partitionType == TracePartitioning.POSITIVE_WINDOW) {
            traces = partitionDataUsingWindow(sourceEvents, situations, pattern, 1.0);
        } else if (partitionType == TracePartitioning.POSITIVE_DOUBLE_WINDOW) {
            traces = partitionDataUsingWindow(sourceEvents, situations, pattern, 2.0);
        }

        logger.info("Partitioning data for pattern: {} done.", pattern);

        return traces;
    }

    /*
        Partition the source data into positive traces, where each trace contains
        events contained between situations of interest.
     */
    private List<List<String>> partitionDataUsingSituations(List<Map.Entry<String, Long>> sourceEvents,
                                                            List<Map.Entry<String, Long>> situations) {
        List<List<String>> traces = new ArrayList<>();

        int lastIndex = 0;

        for (Map.Entry<String, Long> situation : situations) {
            List<String> currentTrace = new ArrayList<>();
            ListIterator<Map.Entry<String, Long>> events = sourceEvents.listIterator(lastIndex);

            while (events.hasNext()) {
                Map.Entry<String, Long> currentEvent = events.next();

                // check if we found the situation of interest
                if (situation.getKey().equals(currentEvent.getKey())) {
                    // add the situation of interest at the end of the trace
                    currentTrace.add(situation.getKey());
                    lastIndex++;
                    // process the next situation of interest
                    break;
                } else {
                    currentTrace.add(currentEvent.getKey());
                    lastIndex++;
                }

            }

            traces.add(currentTrace);
        }

        return traces;
    }

    /*
        Partition the source data into positive traces, where each trace contains events
        from a window of time. A modifier allows the window to be changed.

        TODO better partitioning
     */
    private List<List<String>> partitionDataUsingWindow(List<Map.Entry<String, Long>> sourceEvents,
                                                        List<Map.Entry<String, Long>> situations,
                                                        PatternMatchFile pattern,
                                                        double windowModifier) {

        List<List<String>> traces = new ArrayList<>();

        int lastEventBeforeLastWindowStart = 0;

        for (Map.Entry<String, Long> situation: situations) {

            long situationHappens = situation.getValue();
            long windowStart = situationHappens - (long) (pattern.getWindowSize() * windowModifier);

            List<String> currentTrace = new ArrayList<>();
            ListIterator<Map.Entry<String, Long>> events = sourceEvents.listIterator(lastEventBeforeLastWindowStart);

            int lastIndex = lastEventBeforeLastWindowStart;

            while (events.hasNext()) {
                lastIndex++;

                Map.Entry<String, Long> currentEvent = events.next();
                long currentEventHappens = currentEvent.getValue();

                // current event inside window -> add to current trace
                // current event after situation of interest -> break
                if (currentEventHappens > windowStart && currentEventHappens < situationHappens) {
                    currentTrace.add(currentEvent.getKey());
                } else if (currentEventHappens == situationHappens) {
                    // current event is situation of interest
                    if (situation.getKey().equals(currentEvent.getKey())) {
                        currentTrace.add(situation.getKey());
                        lastEventBeforeLastWindowStart = lastIndex - currentTrace.size();
                        break;
                    } else { // current event has the same time as situation of interest
                        currentTrace.add(currentEvent.getKey());
                    }
                } else if (currentEventHappens > situationHappens) {
                    break;
                }

            }

            traces.add(currentTrace);
        }

        return traces;
    }

    /*
        Generate negative traces using a sliding window with some percentage of overlap.
        The traces should be filtered to remove any that contain complete positive traces.

            NEGATIVE_WINDOW -> window size same as positive, move using %
                            -> only use those that contain no positive traces

        TODO better partitioning
     */
    List<List<String>> generateNegativeTraces(List<Map.Entry<String, Long>> sourceEvents,
                                                     PatternMatchFile pattern,
                                                     TracePartitioning partitionType,
                                                     double slidePercentage) {

        logger.info("Partitioning data for pattern: {}, type: {}, window size: {} ms.", pattern, partitionType, pattern.getWindowSize());

        List<List<String>> traces = new ArrayList<>();
        int index = 0;

        while (true) {
            Map.Entry<String, Long> startEvent = sourceEvents.get(index);

            long startEventHappens = startEvent.getValue();
            long nextStart = startEventHappens + (long) (pattern.getWindowSize() * slidePercentage);

            ListIterator<Map.Entry<String, Long>> events = sourceEvents.listIterator(index);

            // collect events for trace
            List<String> currentTrace = new ArrayList<>();
            while (events.hasNext()) {

                Map.Entry<String, Long> currentEvent = events.next();
                long currentEventHappens = currentEvent.getValue();

                long nextCutoff = startEventHappens + pattern.getWindowSize();

                if (currentEventHappens < nextStart) {
                    index++;
                }

                if (currentEventHappens < nextCutoff) {
                    currentTrace.add(currentEvent.getKey());
                } else {
                    currentTrace.add(currentEvent.getKey());
                    break;
                }
            }
            traces.add(currentTrace);

            // TODO filter short traces at the end
            if (!events.hasNext() || (sourceEvents.size() - index <= pattern.getWindowSize())) {
                break;
            }
        }

        logger.info("Partitioning data for pattern: {} done.", pattern);

        return traces;
    }

    /*
        Find out if there is overlap between the positive traces.
    */
    public void findOverlap(List<List<String>> traces, LineParser lineParser) {
        List<Map.Entry<Map.Entry<String, Long>, Map.Entry<String, Long>>> startAndEndOfTraces = new ArrayList<>();

        for (List<String> trace :  traces) {
            Map.Entry<String, Long> start = lineParser.parseLine(trace.get(0));
            Map.Entry<String, Long> end = lineParser.parseLine(trace.get(trace.size() - 1));

            Map.Entry<Map.Entry<String, Long>, Map.Entry<String, Long>> borders = new AbstractMap.SimpleImmutableEntry<>(start, end);
            startAndEndOfTraces.add(borders);
        }

        for (Map.Entry<Map.Entry<String, Long>, Map.Entry<String, Long>> borders : startAndEndOfTraces) {
            System.out.println("Start = [" + borders.getKey().getValue() + "] End = [" + borders.getValue().getValue() + "]");
        }
    }

    /*
        Print statistics about the traces: min, max, average, median.
    */
    void printStatistics(PatternMatchFile pattern, List<List<String>> traces) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        long sum = 0;

        int[] sizes = new int[traces.size()];
        for (int i = 0; i < traces.size(); i++) {
            sizes[i] = traces.get(i).size();

            if (sizes[i] < min) {
                min = sizes[i];
            } else if (sizes[i] > max) {
                max = sizes[i];
            }

            sum += sizes[i];
        }

        double avg = sum / traces.size();

        Arrays.sort(sizes);
        int median;
        if (sizes.length % 2 == 0) {
            median = (sizes[(sizes.length / 2) - 1] + sizes[(sizes.length / 2) + 1]) / 2;
        } else {
            median = sizes[sizes.length / 2];
        }

        logger.info("Number of traces for pattern {}: {}", pattern.getName(), traces.size());
        logger.info("Size of traces: average: {}, median: {}, min: {}, max: {}", avg, median, min, max);
    }

    /*
        Write the traces into a file. Takes a lot of time.
    */
    public void writeTraces(List<List<String>> traces, String path, double subsamplePercentage) {

        String filename = path + "-traces";
        logger.info("Writing traces to file: {}", filename);

        int index = (int) (subsamplePercentage * traces.size());
        int count = 0;

        // TODO better way to subsample
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename))) {
            for (List<String> trace : traces) {
                if (subsamplePercentage != 1.0) {
                    if (count == index) {
                        // write trace
                        for (String event : trace) {
                            bufferedWriter.write(event);
                            bufferedWriter.newLine();
                        }
                        bufferedWriter.newLine();
                        count = 0;
                    } else {
                        count++;
                    }
                } else {
                    for (String event : trace) {
                        bufferedWriter.write(event);
                        bufferedWriter.newLine();
                    }
                    bufferedWriter.newLine();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        logger.info("Writing traces to file: {} done.", filename);
    }

    public void writeTracesSubsample(List<List<String>> traces, String path, double subsamplePercentage) {
        String filename = path + "-traces";
        logger.info("Writing traces to file: {}", filename);

        Random rand = new Random(42);
        int size = (int) (traces.size() * subsamplePercentage);

        while(traces.size() > size) {
            traces.remove(rand.nextInt(traces.size()));
        }

        logger.info("Writing {} traces", traces.size());

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename))) {
            for (List<String> trace : traces) {
                for (String event : trace) {
                    bufferedWriter.write(event);
                    bufferedWriter.newLine();
                }
                bufferedWriter.newLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        logger.info("Writing traces to file: {} done", filename);
    }
}
