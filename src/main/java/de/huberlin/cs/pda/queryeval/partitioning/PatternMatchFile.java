package de.huberlin.cs.pda.queryeval.partitioning;

import java.io.File;

/**
 * Created by dimitar on 09/03/17.
 */
class PatternMatchFile extends File {

    private final long windowSize; // window size of query in milliseconds

    PatternMatchFile(String filename, long windowSize) {
        super(filename);
        this.windowSize = windowSize;
    }

    public long getWindowSize() {
        return windowSize;
    }
}
