package org.opentripplanner.updater.bike_rental.datasources;

import org.opentripplanner.routing.bike_rental.BikeRentalStation;
import org.opentripplanner.updater.GenericXmlDataSource;
import org.opentripplanner.updater.bike_rental.datasources.params.BikeRentalDataSourceParameters;
import org.opentripplanner.util.NonLocalizedString;

import java.util.HashSet;
import java.util.Map;

/**
 * NextBike bike rental data source.
 * url: {@code https://nextbike.net/maps/nextbike-live.xml?city=<city uid>}
 * Check https://nextbike.net/maps/nextbike-live.xml full feed to find the city uid
 * to use for your data location.
 */
class NextBikeRentalDataSource extends GenericXmlDataSource<BikeRentalStation> {

    private final String networkName;

    public NextBikeRentalDataSource(BikeRentalDataSourceParameters config) {
        super(config.getUrl(),"//city/place");
        // this feed sets values on place node attributes, rather than in child elements
        this.setReadAttributes(true);
        this.networkName = config.getNetwork("NextBike");
    }

    @Override
    protected BikeRentalStation parseElement(Map<String, String> attributes) {

        // some place entries appear to actually be checked-out bikes, not stations
        if (attributes.get("bike") != null) {
            return null;
        }

        BikeRentalStation brstation = new BikeRentalStation();

        brstation.networks = new HashSet<>();
        brstation.networks.add(this.networkName);

        brstation.id = attributes.get("number");
        brstation.x = Double.parseDouble(attributes.get("lng"));
        brstation.y = Double.parseDouble(attributes.get("lat"));
        brstation.name = new NonLocalizedString(attributes.get("name"));
        brstation.spacesAvailable = Integer.parseInt(attributes.get("bike_racks"));

        // number of bikes available is reported as "5+" if >= 5
        String numBikes = attributes.get("bikes");
        if (numBikes.equals("5+")) {
            brstation.bikesAvailable = 5;
        } else {
            brstation.bikesAvailable = Integer.parseInt(numBikes);
        }
        
        return brstation;
    }
}
