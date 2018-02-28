package de.huberlin.cs.pda.queryeval.comparators;

import de.huberlin.cs.pda.queryeval.esper.event.Event;

import java.util.List;
import java.util.Map;

public class LastEventComparator implements Comparator {

    @Override
    public int find(Map<String, Event> match, List<Map<String, Event>> listOfMatches) {
        for(Map<String, Event> entry: listOfMatches){
            // if(entry.)

            // TODO: Problem - kann letztes Event laut Query Definition nicht bestimmen aus dieser Map --> in der EPL Datei eindeutig benennen?
        }
        return -1;
    }
}
