package de.huberlin.cs.pda.queryeval.esper.event;

/**
 * Created by dimitar on 25.01.17.
 *
 * Data from https://github.com/google/cluster-data
 * Download using "gsutil cp gs://clusterdata-2011-2/ ."
 */
public class ClusterTaskEvent extends Event {
    private final long time;
    // to better deal with missing values all are String
    // see "gsutil cat gs://clusterdata-2011-2/schema.csv"
    private final String missingInfo; // int
    private final String jobID; // long
    private final String taskIndex; // int
    private final String machineID; // long
    private final String eventType; // int
    private final String user;
    private final String schedulingClass; // int
    private final String priority; // int
    private final String cpuRequest; // double
    private final String memoryRequest; // double
    private final String diskSpaceRequest; // double
    private final String differentMachinesRestriction; // int

    public ClusterTaskEvent(long time,
                            String missingInfo,
                            String jobID,
                            String taskIndex,
                            String machineID,
                            String eventType,
                            String user,
                            String schedulingClass,
                            String priority,
                            String cpuRequest,
                            String memoryRequest,
                            String diskSpaceRequest,
                            String differentMachinesRestriction) {
        this.time = time;
        this.missingInfo = missingInfo;
        this.jobID = jobID;
        this.taskIndex = taskIndex;
        this.machineID = machineID;
        this.eventType = eventType;
        this.user = user;
        this.schedulingClass = schedulingClass;
        this.priority = priority;
        this.cpuRequest = cpuRequest;
        this.memoryRequest = memoryRequest;
        this.diskSpaceRequest = diskSpaceRequest;
        this.differentMachinesRestriction = differentMachinesRestriction;
    }

    public long getTime() {
        return time;
    }

    public String getMissingInfo() {
        return missingInfo;
    }

    public String getJobID() {
        return jobID;
    }

    public String getTaskIndex() {
        return taskIndex;
    }

    public String getMachineID() {
        return machineID;
    }

    public String getEventType() {
        return eventType;
    }

    public String getUser() {
        return user;
    }

    public String getSchedulingClass() {
        return schedulingClass;
    }

    public String getPriority() {
        return priority;
    }

    public String getCpuRequest() {
        return cpuRequest;
    }

    public String getMemoryRequest() {
        return memoryRequest;
    }

    public String getDiskSpaceRequest() {
        return diskSpaceRequest;
    }

    public String getDifferentMachinesRestriction() {
        return differentMachinesRestriction;
    }

    @Override
    public String toString() {
        return time +
                "," + missingInfo +
                "," + jobID +
                "," + taskIndex +
                "," + machineID +
                "," + eventType +
                "," + user +
                "," + schedulingClass +
                "," + priority +
                "," + cpuRequest +
                "," + memoryRequest +
                "," + diskSpaceRequest +
                "," + differentMachinesRestriction;
    }
}
