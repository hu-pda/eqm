package de.huberlin.cs.pda.queryeval;

import de.huberlin.cs.pda.queryeval.comparators.Comparator;
import de.huberlin.cs.pda.queryeval.esper.event.Event;

import java.util.List;
import java.util.Map;

public class Evaluator {

    private EvalData evalData;
    private Comparator comparator;
    private List<Map<String, Event>> baseMatches;
    private List<Map<String, Event>> evalMatches;

    public Evaluator(List<Map<String, Event>> baseMatches, List<Map<String, Event>> evalMatches, Comparator comparator){
        evalData = new EvalData();
        this.comparator = comparator;
        this.baseMatches = baseMatches;
        this.evalMatches = evalMatches;
    }

    public EvalData evaluate(){

        for(Map<String, Event> match: evalMatches){
            int index = comparator.find(match, baseMatches);

            if(index != -1){
                evalData.addTruePositive();
                baseMatches.remove(index);
            }else{
                evalData.addFalsePositive(match.toString());
            }
        }

        for(Map<String, Event> match: baseMatches){
            // BaseMatches has been reduced by all TruePositives at this point.
            // So every match that's left is a falseNegative
            evalData.addFalseNegative(match.toString());
        }

        return evalData;
    }
}
