package org.opentripplanner.updater.vehicle_parking;

import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.updater.DataSource;
import org.opentripplanner.util.NonLocalizedString;
import org.opentripplanner.util.xml.XmlDataListDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * Load bike park from a KML placemarks. Use name as bike park name and point coordinates. Rely on:
 * 1) bike park to be KML Placemarks, 2) geometry to be Point.
 * 
 * Bike park-and-ride and "OV-fiets mode" development has been funded by GoAbout
 * (https://goabout.com/).
 * 
 * @author laurent
 * @author GoAbout
 */
class KmlBikeParkDataSource implements DataSource<VehicleParking> {

    private static final Logger LOG = LoggerFactory.getLogger(KmlBikeParkDataSource.class);

    private final String url;

    private final String feedId;

    private final String namePrefix;

    private final boolean zip;

    private final XmlDataListDownloader<VehicleParking> xmlDownloader;

    private List<VehicleParking> bikeParks;

    public KmlBikeParkDataSource(String url, String feedId, String namePrefix, boolean zip) {
        this.url = url;
        this.feedId = feedId;
        this.namePrefix = namePrefix;
        this.zip = zip;

        xmlDownloader = new XmlDataListDownloader<>();
        xmlDownloader
                .setPath("//*[local-name()='kml']/*[local-name()='Document']/*[local-name()='Placemark']|//*[local-name()='kml']/*[local-name()='Document']/*[local-name()='Folder']/*[local-name()='Placemark']");
        xmlDownloader.setDataFactory(attributes -> {
            if (!attributes.containsKey("name")) {
                LOG.warn("Missing name in KML Placemark, cannot create bike park.");
                return null;
            }
            if (!attributes.containsKey("Point")) {
                LOG.warn("Missing Point geometry in KML Placemark, cannot create bike park.");
                return null;
            }

            var name = (this.namePrefix != null ? this.namePrefix : "") + attributes.get("name").trim();
            String[] coords = attributes.get("Point").trim().split(",");
            var x = Double.parseDouble(coords[0]);
            var y = Double.parseDouble(coords[1]);
            var id = String.format(Locale.US, "%s[%.3f-%.3f]",
                name.replace(" ", "_"), x, y);

            var localizedName = new NonLocalizedString(name);

            return VehicleParking.builder()
                    .name(localizedName)
                    .x(x)
                    .y(y)
                    .id(new FeedScopedId(this.feedId, id))
                    .entrance((builder) -> builder
                            .entranceId(new FeedScopedId(this.feedId, id))
                            .name(localizedName)
                            .x(x)
                            .y(y))
                    .build();
        });
    }

    /**
     * Update the data from the source;
     * 
     * @return true if there might have been changes
     */
    @Override
    public boolean update() {
        List<VehicleParking> newBikeParks = xmlDownloader.download(url, zip);
        if (newBikeParks != null) {
            synchronized (this) {
                // Update atomically
                bikeParks = newBikeParks;
            }
            return true;
        }
        return false;
    }

    @Override
    public synchronized List<VehicleParking> getUpdates() {
        return bikeParks;
    }

    @Override
    public String toString() {
        return getClass().getName() + "(" + url + ")";
    }
}