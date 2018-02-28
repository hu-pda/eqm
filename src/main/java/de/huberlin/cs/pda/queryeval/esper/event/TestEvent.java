package de.huberlin.cs.pda.queryeval.esper.event;

import java.util.Objects;

/**
 * Created by mitakas on 28/05/17.
 */
public class TestEvent extends Event {
    private final long timestamp;
    private final int x;
    private final int y;
    private final int z;

    public TestEvent(long timestamp,
                     int x,
                     int y,
                     int z) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return timestamp +
                "," + x +
                "," + y +
                "," + z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestEvent testEvent = (TestEvent) o;
        return timestamp == testEvent.timestamp &&
                x == testEvent.x &&
                y == testEvent.y &&
                z == testEvent.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, x, y, z);
    }
}