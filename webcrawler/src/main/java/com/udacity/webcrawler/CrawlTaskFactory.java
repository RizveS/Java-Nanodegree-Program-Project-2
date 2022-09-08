package com.udacity.webcrawler;

import javax.inject.Inject;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;
import java.util.ArrayList;



public class CrawlTaskFactory {
    Map<String,Integer> counts;
    Set<String> visitedUrls;
    Instant deadline;
    private final Clock clock;
    List<Pattern> ignoredUrls;
    PageParserFactory parserFactory;

    CrawlTaskFactory(
        Clock clock,
        Map<String,Integer> counts,
        Set<String> visitedUrls,
        List<Pattern> ignoredUrls,
        Instant deadline, 
        PageParserFactory parserFactory
    ) {
        this.clock = clock;
        this.counts = Collections.synchronizedMap(counts);
        this.visitedUrls = Collections.synchronizedSet(visitedUrls);
        this.deadline = deadline;
        this.ignoredUrls = ignoredUrls;
        this.parserFactory = parserFactory;
    }

    public RecursiveAction get(String url, int maxDepth) {
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return new CrawlTaskEnd();
        }

        for (Pattern pattern: ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return new CrawlTaskEnd();
            }
        }

        if (visitedUrls.contains(url))  {
            return new CrawlTaskEnd();
        }
        else {
            synchronized(visitedUrls) {
            visitedUrls.add(url); //Will add url to visitedUrls and then go onto call the recursive version of CrawlTask, CrawlTaskRecurse.
            }
        }
        
        PageParser.Result result = parserFactory.get(url).parse();
        return new CrawlTaskRecurse(url, visitedUrls, counts, result, maxDepth,this);
    }


    protected class CrawlTaskRecurse extends RecursiveAction {
        String url;
        int MaxDepth;
        Map<String,Integer> counts;
        Set<String> visitedUrls;
        PageParser.Result result;
        CrawlTaskFactory crawlTaskFactory;


        public CrawlTaskRecurse(String url, 
        Set<String> visitedUrls, 
        Map<String,Integer> counts, 
        PageParser.Result result,
        int maxDepth, CrawlTaskFactory crawltaskFactory) {
                this.url = url;
                this.visitedUrls = visitedUrls;
                this.counts = counts;
                this.result = result;
                this.crawlTaskFactory = crawltaskFactory;
                this.MaxDepth = maxDepth;
        }

        protected void compute() {
            synchronized(visitedUrls) {
                visitedUrls.add(url);
            }
           

           synchronized(counts) {
           Map<String,Integer> TempStore = counts;
           for (Map.Entry<String,Integer> e: result.getWordCounts().entrySet()) {

                    counts.compute(e.getKey(), 
                    (k,v) -> TempStore.containsKey(k)? v + e.getValue(): e.getValue());
                }
           }

           List<RecursiveAction> TaskList = new ArrayList<>();
           for (String link: result.getLinks()) {
            TaskList.add(crawlTaskFactory.get(link,MaxDepth-1));
           }
           invokeAll(TaskList);
        }
    }
    protected class CrawlTaskEnd extends RecursiveAction {
        protected void compute() {
            return;
        }
    }   
}
