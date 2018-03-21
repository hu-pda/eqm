package de.huberlin.cs.pda.queryeval.esper;

import com.espertech.esper.client.*;
import com.espertech.esper.client.deploy.*;
import com.espertech.esper.core.service.EPStatementImpl;
import com.espertech.esper.client.annotation.Tag;
import de.huberlin.cs.pda.queryeval.esper.event.Event;
import de.huberlin.cs.pda.queryeval.esper.listener.ClusterTaskEventListener;
import de.huberlin.cs.pda.queryeval.esper.listener.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.lang.annotation.Annotation;

/**
 * Created by dimitar on 10.02.17.
 */
public abstract class ProcessData {
    private final Logger logger = LoggerFactory.getLogger(ProcessData.class);

    // process the event log
    public Map<String, List<Map<String, Event>>> run(File eventLog, File eplModule, long startingTime, String baseQuery, String[] evaluatedQueries, String[] evaluatedGroups) throws IOException, ParseException, InterruptedException, DeploymentException {
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
        Map<String, List<Map<String, Event>>> matches = esper(epService, deploymentResult, eventLog, startingTime, baseQuery, evaluatedQueries, evaluatedGroups);

        // undeploy the module, destroys all statements, removes the module
        deployAdmin.undeployRemove(deploymentResult.getDeploymentId());
        // destroy the service
        epService.destroy();

        return matches;
    }

    // returns a list of matches for every pattern
    protected abstract Map<String, List<Map<String, Event>>> esper(EPServiceProvider epService, DeploymentResult deploymentResult, File eventLog, long startingTime, String baseQuery, String[] evaluatedQueries, String[] evaluatedGroups);

    // write the matches into files
    public void write(Map<String, List<Map<String, Event>>> matches, String path) {
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



    /**
     * There are two commandline parameters "evaluated-queries" and "evaluated-groups" of which at least one has to be provided.
     * A listener will be created for a query iff
     *          a) the name of the query is in "evaluated-queries"
     *          b) the query has a group-tag that is in "evaluated-groups", and:
     *              - if "evaluated-queries" has been provided the query name additionally has to be in "evaluated-queries"
     *
     * @param deploymentResult
     * @param evaluatedQueries
     * @param evaluatedGroups
     * @return
     */
    protected Map<EPStatement, String> filterStatements(DeploymentResult deploymentResult, String baseQuery, String[] evaluatedQueries, String[] evaluatedGroups){
        Map<EPStatement, String> filteredStatements = new HashMap<>();
        List<String> queryList = new ArrayList<>(Arrays.asList(evaluatedQueries));
        List<String> groupList = new ArrayList<>(Arrays.asList(evaluatedGroups));

        for (EPStatement statement: deploymentResult.getStatements()) {
            if (((EPStatementImpl) statement).isNameProvided()) {
                boolean addListener = false;
                String groupStr = "";
                if(baseQuery.equals(statement.getName())){
                    addListener = true;
                }

                // 1. Check for Group association - group has precedence:
                if(queryList.size() == 0 || queryList.contains(statement.getName())) {
                    // If there are no queries in the list or the query is in there we have to check whether a group was provided
                    if (groupList.size() > 0) {
                        Annotation[] annotations = statement.getAnnotations();
                        for (Annotation annotation : annotations) {
                            if (annotation.annotationType().equals(Tag.class)) {
                                Tag tagAnnotation = (Tag) annotation;
                                if (tagAnnotation.name().equals("Group")) {
                                    for (String group : evaluatedGroups) {
                                        if (tagAnnotation.value().equals(group)) {
                                            // the query belongs to one of the evaluated groups --> add listener
                                            addListener = true;
                                            groupStr = group;
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        // No groups provided
                        // 2. Check again for queryList containment
                        if(queryList.contains(statement.getName())){
                            addListener = true;
                        }
                    }
                }

                if(addListener){
                    if(groupStr.equals("")){
                        filteredStatements.put(statement, statement.getName());
                    }else {
                        filteredStatements.put(statement, groupStr + "." + statement.getName());
                    }
                }
            }
        }
        return filteredStatements;
    }
}
