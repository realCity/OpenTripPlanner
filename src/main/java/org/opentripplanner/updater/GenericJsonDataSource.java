package org.opentripplanner.updater;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.opentripplanner.util.xml.JsonDataListDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Slf4j
public abstract class GenericJsonDataSource<T> implements DataSource<T> {

  private static final Logger LOG = LoggerFactory.getLogger(GenericJsonDataSource.class);

  @Getter
  private String url;
  private final JsonDataListDownloader<T> jsonDataListDownloader;

  protected List<T> updates = List.of();

  public GenericJsonDataSource(String url, String jsonParsePath, String headerName, String headerValue) {
    this.url = url;
    jsonDataListDownloader = new JsonDataListDownloader<>(url, jsonParsePath, this::parseElement, headerName, headerValue);
  }

  public GenericJsonDataSource(String url, String jsonParsePath) {
    this(url, jsonParsePath, null, null);
  }

  protected abstract T parseElement(JsonNode jsonNode);

  @Override
  public boolean update() {
    List<T> updates = jsonDataListDownloader.download();
    if (updates != null) {
      synchronized(this) {
        this.updates = updates;
      }
      return true;
    }
    LOG.info("Can't update bike rental station list from: " + url + ", keeping current list.");
    return false;
  }

  @Override
  public List<T> getUpdates() {
    return updates;
  }

  public void setUrl(String url) {
    this.url = url;
    this.jsonDataListDownloader.setUrl(url);
  }
}
