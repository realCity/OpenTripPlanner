package org.opentripplanner.routing.algorithm.intermediateplaces;

import org.opentripplanner.model.GenericLocation;
import org.opentripplanner.model.IntermediateGenericLocation;

import java.time.Duration;

public abstract class GenericLocationWrapper {

    abstract public GenericLocation location();

    public abstract Duration getMinWaitTime();

    public abstract Duration getMaxWaitTime();

    public static GenericLocationWrapper of(GenericLocation location) {
        return location instanceof IntermediateGenericLocation ?
                new Intermediate((IntermediateGenericLocation) location) :
                new Generic(location);
    }

    private static class Intermediate extends GenericLocationWrapper {

        private final IntermediateGenericLocation location;

        public Intermediate(IntermediateGenericLocation location) {
            this.location = location;
        }

        @Override public GenericLocation location() {
            return location;
        }

        @Override public Duration getMinWaitTime() {
            return location.minWaitTime;
        }

        @Override public Duration getMaxWaitTime() {
            return location.maxWaitTime;
        }
    }

    private static class Generic extends GenericLocationWrapper {

        private final GenericLocation location;

        public Generic(GenericLocation location) {
            this.location = location;
        }

        @Override public GenericLocation location() {
            return location;
        }

        @Override public Duration getMinWaitTime() {
            return Duration.ofSeconds(0);
        }

        @Override public Duration getMaxWaitTime() {
            return Duration.ofSeconds(0);
        }
    }
}
