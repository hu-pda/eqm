package de.huberlin.cs.pda.queryeval.esper.listener;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.event.bean.BeanEventBean;
import de.huberlin.cs.pda.queryeval.esper.event.ClusterTaskEvent;
import de.huberlin.cs.pda.queryeval.esper.event.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dimitar on 05.02.17.
 */
public class ClusterTaskEventListener extends EventListener {
    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        HashMap<String, Event> matchedEvents;
        for (EventBean event : newEvents) {
            matchedEvents = new HashMap<>();

            Object underlying = event.getUnderlying();
            @SuppressWarnings("unchecked")
            HashMap<String, BeanEventBean> data = (HashMap<String, BeanEventBean>) underlying;

            for (Map.Entry<String, BeanEventBean> entry : data.entrySet()) {
                matchedEvents.put(entry.getKey(), (ClusterTaskEvent) entry.getValue().getUnderlying());
            }
            matchedSequences.add(matchedEvents);
        }
    }
}
