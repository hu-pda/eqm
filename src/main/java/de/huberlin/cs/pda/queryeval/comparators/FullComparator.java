package de.huberlin.cs.pda.queryeval.comparators;

import de.huberlin.cs.pda.queryeval.esper.event.Event;

import java.util.List;
import java.util.Map;

public class FullComparator implements Comparator {

    @Override
    public int find(Map<String, Event> match, List<Map<String, Event>>listOfMatches) {
        for(int i=0; i < listOfMatches.size(); i++){
            if(listOfMatches.get(i).equals(match)){
                return i;
            }
        }
        return -1;
    }
}
