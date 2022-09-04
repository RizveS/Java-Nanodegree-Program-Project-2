package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParserFactory;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;
  private final int maxDepth;
  private final PageParserFactory pageParserFactory;
  private final List<Pattern> ignoredUrls;


  @Inject
  ParallelWebCrawler(
      Clock clock,
      PageParserFactory pageParserFactory,
      @Timeout Duration timeout,
      @MaxDepth int maxDepth,
      @PopularWordCount int popularWordCount,
      @TargetParallelism int threadCount,
      @IgnoredUrls List<Pattern> ignoredUrls) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.pageParserFactory = pageParserFactory;
    this.ignoredUrls = ignoredUrls;
    this.maxDepth = maxDepth;
    
  }


  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    Instant deadline = clock.instant().plus(timeout);
    Map<String, Integer> counts = new HashMap<>();
    Set<String> visitedUrls = new HashSet<>();

    CrawlTaskFactory crawlTaskFactory = new CrawlTaskFactory(clock,
     counts, visitedUrls, ignoredUrls, deadline, 
     pageParserFactory);

    for (String url: startingUrls) {
        ForkJoinPool.commonPool().invoke(crawlTaskFactory.get(url, maxDepth));
    }
    
    
    if (counts.isEmpty()) {
      return new CrawlResult.Builder()
          .setWordCounts(counts)
          .setUrlsVisited(visitedUrls.size())
          .build();
    }

    return new CrawlResult.Builder()
        .setWordCounts(WordCounts.sort(counts, popularWordCount))
        .setUrlsVisited(visitedUrls.size())
        .build();
    
    
  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }

}
