package de.huberlin.cs.pda.queryeval.partitioning;

import java.util.Map;

/**
 * Created by dimitar on 16/03/17.
 */
@FunctionalInterface
public interface LineParser {
    Map.Entry<String, Long> parseLine(String line);
}
