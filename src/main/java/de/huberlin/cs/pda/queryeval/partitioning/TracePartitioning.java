package de.huberlin.cs.pda.queryeval.partitioning;

/**
 * Created by mitakas on 01/03/17.
 */
public enum TracePartitioning {
    // positive trace
    POSITIVE_WINDOW,
    POSITIVE_DOUBLE_WINDOW,
    POSITIVE_FULL,
    // negative trace
    NEGATIVE_WINDOW,
    NEGATIVE_SLIDING_WINDOW,
    NEGATIVE_OVERLAPPING_WINDOW,
    NEGATIVE_OVERLAPPING_FULL
}
