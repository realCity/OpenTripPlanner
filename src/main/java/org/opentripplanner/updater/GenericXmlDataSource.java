package org.opentripplanner.updater;

import org.opentripplanner.util.xml.XmlDataListDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public abstract class GenericXmlDataSource<T> implements DataSource<T> {

    private static final Logger LOG = LoggerFactory.getLogger(GenericXmlDataSource.class);

    private final String url;

    List<T> stations;

    private final XmlDataListDownloader<T> xmlDownloader;


    public GenericXmlDataSource(String url, String path) {
        this.url = url;
        xmlDownloader = new XmlDataListDownloader<>();
        xmlDownloader.setPath(path);
        /* TODO Do not make this class abstract, but instead make the client
         * provide itself the factory?
         */
        xmlDownloader.setDataFactory(this::parseElement);
    }

    @Override
    public boolean update() {
        List<T> newStations = xmlDownloader.download(url, false);
        if (newStations != null) {
            synchronized(this) {
                stations = newStations;
            }
            return true;
        }
        LOG.info("Can't update bike rental station list from: " + url + ", keeping current list.");
        return false;
    }

    @Override
    public synchronized List<T> getUpdates() {
        return stations;
    }

    public void setReadAttributes(boolean readAttributes) {
        // if readAttributes is true, read XML attributes of selected elements, instead of children
        xmlDownloader.setReadAttributes(readAttributes);
    }

    protected abstract T parseElement(Map<String, String> attributes);

    @Override
    public String toString() {
        return getClass().getName() + "(" + url + ")";
    }
}
