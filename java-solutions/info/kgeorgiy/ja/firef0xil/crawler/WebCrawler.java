package info.kgeorgiy.ja.firef0xil.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Implementation for interfaces {@link AdvancedCrawler} and {@link Crawler}.
 * @author FireF0xIl
 */

public class WebCrawler implements AdvancedCrawler {
    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int perHost;
    private boolean active = true;
    private final static int AWAIT_TIME = 10;
    private final static TimeUnit AWAIT_UNIT = TimeUnit.SECONDS;
    private final ConcurrentHashMap<String, LimitQueue> hosts = new ConcurrentHashMap<>();

    /**
     * Constructs new {@link WebCrawler} with specified instance of {@link Downloader}
     * and number of {@code downloaders, extractors and perHost}.
     *
     * @param downloader specified instance of {@link Downloader}
     * @param downloaders maximal amount of additional threads available to create for download
     * @param extractors maximal amount of additional threads available to create for extract
     * @param perHost maximal connections per website
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    private class LimitQueue {
        private final ArrayDeque<Runnable> jobs = new ArrayDeque<>();
        private int currentRunning = 0;

        /**
         * Add new job to queue
         *
         * @param job instance of {@link Runnable} to execute
         */
        public synchronized void add(Runnable job) {
            jobs.add(job);
            next();
        }

        private synchronized void next() {
            if (currentRunning < perHost) {
                if (!jobs.isEmpty()) {
                    Runnable job = jobs.poll();
                    increment();
                    downloaders
                            .submit(() -> {
                                try {
                                    job.run();
                                } finally {
                                    decrement();
                                    next();
                                }
                            }
                        );
                }
            }
        }

        private synchronized void increment() {
            currentRunning++;
        }
        private synchronized void decrement() {
            currentRunning--;
        }
    }

    /**
     * Downloads web site up to specified depth.
     *
     * @param url start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth download depth.
     * @return download result.
     * @throws IllegalStateException if called after close
     */
    @Override
    public Result download(String url, int depth) {
        return download(url, depth, null);
    }

    /**
     * Downloads web site up to specified depth.
     *
     * @param url   start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth download depth.
     * @param hosts domains to follow, pages on another domains should be ignored.
     * @return download result.
     * @throws IllegalStateException if called after close
     */
    @Override
    public Result download(String url, int depth, List<String> hosts) {
        if (!active) {
            throw new IllegalStateException("Attempt to download after close");
        }
        Queue<String> now = new ArrayDeque<>();
        Queue<String> next = new ArrayDeque<>();
        now.add(url);
        Set<String> visited = ConcurrentHashMap.newKeySet();
        Map<String, IOException> errors = new ConcurrentHashMap<>();
        Set<String> result = ConcurrentHashMap.newKeySet();
        Set<String> allowedHosts = (hosts == null) ? null : hosts.stream().collect(Collectors.toUnmodifiableSet());
        visited.add(url);
        for (int current = 1; current <= depth; current++) {
            if (now.isEmpty()) {
                break;
            } else {
                Phaser phaser = new Phaser(now.size());
                while (!now.isEmpty()) {
                    String link = now.poll();
                    process(link, depth - current, phaser, visited, result, errors, allowedHosts, next);
                }
                phaser.awaitAdvance(0);
                now.addAll(next);
                next.clear();
            }
        }
        return new Result(new ArrayList<>(result), errors);
    }

    private synchronized void addAll(Queue<String> queue, List<String> urls) {
        queue.addAll(urls);
    }


    private static boolean allowed(String host, Collection<String> allowedHosts) {
        return allowedHosts == null || allowedHosts.contains(host);
    }

    private void process(final String url,
                         final int depth,
                         final Phaser phaser,
                         final Set<String> visited,
                         final Set<String> result,
                         final Map<String, IOException> errors,
                         final Set<String> allowedHosts,
                         final Queue<String> queue) {
        try {
            String host = URLUtils.getHost(url);
            if (allowed(host, allowedHosts)) {
                hosts.computeIfAbsent(host, __ -> new LimitQueue())
                        .add(() -> {
                            try {
                                Document document = downloader.download(url);
                                result.add(url);
                                if (depth > 0) {
                                    phaser.register();
                                    extractors.submit(() -> {
                                        try {
                                            addAll(queue,
                                                    document
                                                            .extractLinks()
                                                            .stream()
                                                            .filter(visited::add)
                                                            .collect(Collectors.toList()));
                                        } catch (IOException e) {
                                            errors.put(url, e);
                                        } finally {
                                            phaser.arrive();
                                        }
                                    });
                                }
                            } catch (IOException e) {
                                errors.put(url, e);
                            } finally {
                                phaser.arrive();
                            }
                        });
            } else {
                phaser.arrive();
            }
        } catch (MalformedURLException e) {
            errors.put(url, e);
        }
    }

    /**
     * Closes this web-crawler, relinquishing any allocated resources.
     */
    @Override
    public void close() {
        if (active) {
            active = false;
            downloaders.shutdown();
            extractors.shutdown();
            while (true) {
                try {
                    if (downloaders.awaitTermination(AWAIT_TIME, AWAIT_UNIT)
                            && extractors.awaitTermination(AWAIT_TIME, AWAIT_UNIT)) {
                        break;
                    }
                } catch (InterruptedException ignored) {}
                downloaders.shutdownNow();
                extractors.shutdownNow();
            }
        }
    }

    private static int getInt(String[] args, int index, int defaultValue) {
        return index < args.length ? Integer.parseInt(args[index]) : defaultValue;
    }

    /**
     * Start point of {@link WebCrawler}.
     *
     * Crawl a website
     * @param args url [depth [downloads [extractors [perHost]]]]
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length >= 5) {
            System.err.println("Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
        } else {
            try {
                try (Crawler crawler = new WebCrawler(
                        new CachingDownloader(),
                        getInt(args, 2, 2),
                        getInt(args, 3, 2),
                        getInt(args, 4, 2))
                ) {
                    Result result = crawler.download(args[0], getInt(args, 1, 1));
                    System.out.println(result.getDownloaded());
                    System.out.println(result.getErrors());
                }
            } catch (NumberFormatException e) {
                System.err.println("Not all arguments can be converted to int");
            } catch (IOException e) {
                System.err.println("I/O exception while creating CachingDownloader");
            }
        }
    }
}
