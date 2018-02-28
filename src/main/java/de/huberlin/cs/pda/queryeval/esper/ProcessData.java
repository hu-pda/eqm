package de.huberlin.cs.pda.queryeval.esper;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.deploy.*;
import de.huberlin.cs.pda.queryeval.esper.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by dimitar on 10.02.17.
 */
public abstract class ProcessData {
    private final Logger logger = LoggerFactory.getLogger(ProcessData.class);

    // process the event log
    public Map<String, List<Map<String, Event>>> run(File eventLog, File eplModule, long startingTime) throws IOException, ParseException, InterruptedException, DeploymentException {
        // a configuration is needed to disable the internal timer
        Configuration configuration = new Configuration();
        //configuration.addEventType(ClusterTaskEvent.class);
        // objects of CurrentTimeEvent must be supplied when running windowed statements
        configuration.getEngineDefaults().getThreading().setInternalTimerEnabled(false);

        EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(configuration);
        // initialize the service
        //epService.initialize();

        EPAdministrator administrator = epService.getEPAdministrator();
        // use the deployment administrative interface
        EPDeploymentAdmin deployAdmin = administrator.getDeploymentAdmin();
        // read in a new module with EPL statements
        Module module = deployAdmin.read(eplModule);

        // register the module with the deployment administrative interface
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setFailFast(true);
        DeploymentResult deploymentResult = deployAdmin.deploy(module, deploymentOptions);

        // process the event log with Esper
        Map<String, List<Map<String, Event>>> matches = esper(epService, deploymentResult, eventLog, startingTime);

        // undeploy the module, destroys all statements, removes the module
        deployAdmin.undeployRemove(deploymentResult.getDeploymentId());
        // destroy the service
        epService.destroy();

        return matches;
    }

    // returns a list of matches for every pattern
    protected abstract Map<String, List<Map<String, Event>>> esper(EPServiceProvider epService, DeploymentResult deploymentResult, File eventLog, long startingTime);

    // write the matches into files
    public void write(Map<String, List<Map<String, Event>>> matches, File path) {
        // for each pattern
        for (Map.Entry<String, List<Map<String, Event>>> matchedSequence : matches.entrySet()) {
            // get the name of the pattern
            File filename = new File(path, matchedSequence.getKey() + "-esper-matches");

            // get the keys of the sequence
            List<String> sequenceKeys = new ArrayList<>();
            for (Map<String, Event> firstMatchedSequence : matchedSequence.getValue()) {
                sequenceKeys.addAll(firstMatchedSequence.keySet());
                // get only one of the matched sequences
                break;
            }
            // each matched sequence is stored in a map: key -> event
            Collections.sort(sequenceKeys);

            logger.info("Writing matched events to file: {}", filename);
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename))) {
                for (Map<String, Event> sequence : matchedSequence.getValue()) {
                    for (String key : sequenceKeys) {
                        bufferedWriter.write(sequence.get(key).toString());
                        bufferedWriter.newLine();
                    }
                    bufferedWriter.newLine();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            logger.info("Writing matched events to file: {} done.", filename);
        }
    }
}
