package de.huberlin.cs.pda.queryeval.comparators;

import de.huberlin.cs.pda.queryeval.esper.event.Event;

import java.util.List;
import java.util.Map;

public interface Comparator {

    /**
     Checks a List of Esper Matches (Map<String,Event>) for this particular match.

     Returns -1 if the match has not been found.
     Returns the index in the list if it has been found.
     */
    int find(Map<String,Event> match, List<Map<String, Event>> listOfMatches);
}
