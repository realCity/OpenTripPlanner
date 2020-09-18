package org.opentripplanner.routing.algorithm.intermediateplaces;

import org.opentripplanner.model.GenericLocation;

import java.time.Duration;
import java.time.Instant;

public class LocationAndSearchWindow {

    private final GenericLocation location;

    private Instant minTime;

    private Instant maxTime;

    public LocationAndSearchWindow(GenericLocation location) {
        this.location = location;
    }

    public GenericLocation requestLocation() {
        return location;
    }

    public long requestDepartAtTime() {
        return minTime.getEpochSecond();
    }

    public long requestArriveByTime() {
        return maxTime.getEpochSecond();
    }

    public Duration requestSearchWindow() {
        return Duration.between(minTime, maxTime);
    }

    public void extendToInclude(Instant instant) {
        if (isUnbounded()) {
            minTime = instant;
            maxTime = instant;
        } else {
            if (minTime.isAfter(instant)) {
                minTime = instant;
            }
            if (maxTime.isBefore(instant)) {
                maxTime = instant;
            }
        }
    }

    public boolean isUnbounded() {
        return minTime == null && maxTime == null;
    }
}
