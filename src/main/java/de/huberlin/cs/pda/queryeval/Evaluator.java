package de.huberlin.cs.pda.queryeval;

import de.huberlin.cs.pda.queryeval.comparators.Comparator;
import de.huberlin.cs.pda.queryeval.esper.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluator {
    private final Logger logger = LoggerFactory.getLogger(Evaluator.class);

    private Map<String, EvalData> evalData;
    private Comparator comparator;
    private List<Map<String, Event>> baseMatches;
    private Map<String, List<Map<String, Event>>> evalQueriesMatches;

    public Evaluator(List<Map<String, Event>> baseMatches, Map<String, List<Map<String, Event>>> evalQueriesMatches, Comparator comparator){
        this.comparator = comparator;
        this.baseMatches = baseMatches;
        this.evalQueriesMatches = evalQueriesMatches;
        this.evalData = new HashMap<>();
    }

    public Map<String, EvalData> evaluate(){

        // Look at the matches of every evaluated query
        for(Map.Entry<String, List<Map<String, Event>>> evalQueryMatches : evalQueriesMatches.entrySet()) {
            String queryName = evalQueryMatches.getKey();
            logger.info("Now evaluating query '" + queryName + "'");

            EvalData queryData = new EvalData(queryName);
            List<Map<String, Event>> baseMatchesCopy = new ArrayList<Map<String, Event>>(baseMatches);

            for(Map<String, Event> match: evalQueryMatches.getValue()){

                int index = comparator.find(match, baseMatchesCopy);

                if(index != -1){
                    queryData.addTruePositive();
                    baseMatchesCopy.remove(index);
                }else{
                    queryData.addFalsePositive(match.toString());
                }
            }

            for(Map<String, Event> match: baseMatchesCopy){
                // BaseMatches has been reduced by all TruePositives at this point.
                // So every match that's left is a falseNegative
                queryData.addFalseNegative(match.toString());
            }

            evalData.put(queryName, queryData);
        }

        return evalData;
    }
}
