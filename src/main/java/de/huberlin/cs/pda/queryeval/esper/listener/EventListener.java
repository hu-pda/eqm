package de.huberlin.cs.pda.queryeval.esper.listener;

import com.espertech.esper.client.UpdateListener;
import de.huberlin.cs.pda.queryeval.esper.event.Event;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class EventListener implements UpdateListener {
    List<Map<String, Event>> matchedSequences = new LinkedList<>();

    public List<Map<String, Event>> getMatchedSequences() {
        return matchedSequences;
    }
}
