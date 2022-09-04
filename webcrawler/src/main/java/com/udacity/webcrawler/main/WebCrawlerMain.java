package com.udacity.webcrawler.main;

import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Objects;

public final class WebCrawlerMain {

  private final CrawlerConfiguration config;

  private WebCrawlerMain(CrawlerConfiguration config) {
    this.config = Objects.requireNonNull(config);
  }

  @Inject
  private WebCrawler crawler;

  @Inject
  private Profiler profiler;

  private void run() throws Exception {
    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

    crawler = profiler.wrap(WebCrawler.class,crawler);
    CrawlResult result = crawler.crawl(config.getStartPages());
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);


    
    if (this.config.getResultPath() == null || this.config.getResultPath().isEmpty()) {
        OutputStreamWriter STOUTWriter = new OutputStreamWriter(System.out);
        resultWriter.write(STOUTWriter);
    }
    else {
      Path outPath = FileSystems.getDefault().getPath(this.config.getResultPath());
      resultWriter.write(outPath);
    }

    if (this.config.getProfileOutputPath() == null || this.config.getProfileOutputPath().isEmpty()) {
      OutputStreamWriter STOUTWriter = new OutputStreamWriter(System.out);
      profiler.writeData(STOUTWriter);
  }
    else {
    Path outPath = FileSystems.getDefault().getPath(this.config.getProfileOutputPath());
      profiler.writeData(outPath);
    }


    // TODO: Write the profile data to a text file (or System.out if the file name is empty)
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain [starting-url]");
      return;
    }

    CrawlerConfiguration config = new ConfigurationLoader(args[0]).load();
    new WebCrawlerMain(config).run();
  }
}
