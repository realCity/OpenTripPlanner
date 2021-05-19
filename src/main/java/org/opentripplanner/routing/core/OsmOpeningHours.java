package org.opentripplanner.routing.core;

import ch.poole.openinghoursparser.OpeningHoursParseException;
import ch.poole.openinghoursparser.OpeningHoursParser;
import ch.poole.openinghoursparser.Rule;
import ch.poole.openinghoursparser.Util;
import io.leonard.OpeningHoursEvaluator;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.opentripplanner.util.I18NString;

/**
 *
 */
@EqualsAndHashCode
@RequiredArgsConstructor
public class OsmOpeningHours implements I18NString, TimeRestriction, Serializable {

    private final List<Rule> rules;

    @Override
    public boolean isTraverseableAt(LocalDateTime now) {
        return OpeningHoursEvaluator.isOpenAt(now, rules);
    }

    @Override
    public Optional<LocalDateTime> earliestDepartureTime(LocalDateTime now) {
        return OpeningHoursEvaluator.isOpenNext(now, rules);
    }

    @Override
    public Optional<LocalDateTime> latestArrivalTime(LocalDateTime now) {
        return OpeningHoursEvaluator.wasLastOpen(now, rules);
    }

    public static OsmOpeningHours parseFromOsm(String openingHours)
    throws OpeningHoursParseException {
        var parser = new OpeningHoursParser(new ByteArrayInputStream(openingHours.getBytes()));
        var rules = parser.rules(true);
        return new OsmOpeningHours(rules);
    }

    @Override
    public String toString(Locale locale) {
        return toString();
    }

    @Override
    public String toString() {
        return Util.rulesToOpeningHoursString(rules);
    }
}
