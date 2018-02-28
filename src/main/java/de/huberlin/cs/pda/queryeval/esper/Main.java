package de.huberlin.cs.pda.queryeval.esper;

import com.espertech.esper.client.deploy.DeploymentException;
import com.espertech.esper.client.deploy.ParseException;
import de.huberlin.cs.pda.queryeval.esper.event.Event;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException, ParseException, DeploymentException, InterruptedException {
        OptionParser parser = new OptionParser();

        OptionSpec<String> datasetOption
                = parser.accepts("dataset", "Chose a dataset [dublin|google-cluster|debs2015|nasdaq]")
                        .withRequiredArg()
                        .required()
                        .ofType(String.class);

        OptionSpec<File> eplModuleOption
                = parser.accepts("epl-module", "EPL module with statements to run")
                        .withRequiredArg()
                        .required()
                        .ofType(File.class);

        OptionSpec<File> eventLogDataOption
                = parser.accepts("event-log-data", "File containing the event log") // data source
                        .withRequiredArg()
                        .required()
                        .ofType(File.class);

        OptionSpec<File> saveDirOption
                = parser.accepts("save-dir", "Folder containing the traces, models, and figures")
                        .withRequiredArg()
                        .required()
                        .ofType(File.class);

        OptionSpec<Void> help
                = parser.acceptsAll(Arrays.asList("h", "help", "?"), "Show help")
                        .forHelp();

        try {
            OptionSet options = parser.parse(args);

            if (options.has(help)) {
                parser.printHelpOn(System.out);
                return;
            }

            String dataset = datasetOption.value(options);
            File eplModule = eplModuleOption.value(options);
            File eventLogData = eventLogDataOption.value(options);
            File saveDir = saveDirOption.value(options);

            ProcessData dataProcessing;
            switch (dataset) {
                case "dublin":
                    dataProcessing = new ProcessDublinData();
                    break;
                case "google-cluster":
                    dataProcessing = new ProcessGoogleClusterData();
                    break;
                case "debs2015":
                    dataProcessing = new ProcessDEBS2015Data();
                    break;
                case "nasdaq":  // nasdaq
                    dataProcessing = new ProcessNasdaqData();
                    break;
                default:
                    System.out.println("Choose a dataset [dublin|google-cluster|debs2015|nasdaq]");
                    return;
            }

            long startingTime = 0L;
            Map<String, List<Map<String, Event>>> matches = dataProcessing.run(eventLogData, eplModule, startingTime);
            dataProcessing.write(matches, saveDir);

        } catch (NullPointerException | OptionException e) {
            parser.printHelpOn(System.out);
        }
    }
}
