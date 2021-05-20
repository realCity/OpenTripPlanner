package org.opentripplanner.updater.bike_rental.datasources;

import org.opentripplanner.routing.bike_rental.BikeRentalStation;
import org.opentripplanner.updater.GenericXmlDataSource;
import org.opentripplanner.updater.bike_rental.datasources.params.BikeRentalDataSourceParameters;
import org.opentripplanner.util.NonLocalizedString;

import java.util.Map;

class BixiBikeRentalDataSource extends GenericXmlDataSource<BikeRentalStation> {
    public BixiBikeRentalDataSource(BikeRentalDataSourceParameters config) {
        super(config.getUrl(),"//stations/station");
    }

    @Override
    protected BikeRentalStation parseElement(Map<String, String> attributes) {
        if (!"true".equals(attributes.get("installed"))) {
            return null;
        }
        BikeRentalStation brstation = new BikeRentalStation();
        brstation.id = attributes.get("id");
        brstation.x = Double.parseDouble(attributes.get("long"));
        brstation.y = Double.parseDouble(attributes.get("lat"));
        brstation.name = new NonLocalizedString(attributes.get("name"));
        brstation.bikesAvailable = Integer.parseInt(attributes.get("nbBikes"));
        brstation.spacesAvailable = Integer.parseInt(attributes.get("nbEmptyDocks"));
        return brstation;
    }
}
