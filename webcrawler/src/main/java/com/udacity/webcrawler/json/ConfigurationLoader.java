package com.udacity.webcrawler.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A static utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

  private final Path path;

  /**
   * Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}.
   */
  public ConfigurationLoader(String filepath) {
    this.path = FileSystems.getDefault().getPath(filepath);
  }

  /**
   * Loads configuration from this {@link ConfigurationLoader}'s path
   *
   * @return the loaded {@link CrawlerConfiguration}.
   */
  public CrawlerConfiguration load() throws IOException{
    // TODO: Fill in this method.
    BufferedReader JSONReader = Files.newBufferedReader(this.path);
    CrawlerConfiguration config = read(JSONReader);
    JSONReader.close();
    return config;
  }

  /**
   * Loads crawler configuration from the given reader.
   *
   * @param reader a Reader pointing to a JSON string that contains crawler configuration.
   * @return a crawler configuration
   */

  public static CrawlerConfiguration read(Reader reader) {
    // TODO: Fill in this method
    ObjectMapper JSONReader = new ObjectMapper();
    JSONReader.disable(com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE);
    try {
    CrawlerConfiguration Config = JSONReader.readValue(reader,CrawlerConfiguration.class);
    return Config;
    }
    catch (Exception e) {
      System.out.print(e.getMessage());
      return new CrawlerConfiguration.Builder().build();
    }
  }


}
