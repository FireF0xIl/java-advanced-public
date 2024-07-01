package info.kgeorgiy.ja.firef0xil.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Implementation for interface {@link ParallelMapper}.
 * @author FireF0xIl
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threads;
    private final JobQueue jobs = new JobQueue();
    private volatile boolean finish = false;

    /**
     * Constructs new {@link ParallelMapperImpl} with specified number of {@code threads}
     *
     * @param threads number of concurrent threads.
     */
    public ParallelMapperImpl(final int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("Number of threads should be positive number");
        }
        final Runnable worker = () -> {
            try {
                while (!Thread.interrupted()) {
                    jobs.poll().run();
                }
            } catch (final InterruptedException ignored) {}
            finally {
                Thread.currentThread().interrupt();
            }

        };

        this.threads = Stream
                .generate(() -> new Thread(worker))
                .limit(threads)
                .peek(Thread::start)
                .collect(Collectors.toList());
    }

    private class JobQueue {
        private final Queue<Runnable> pendingJobs = new ArrayDeque<>();

        /**
         * Get new job to execute
         *
         * @return {@link Runnable} to execute
         * @throws InterruptedException if calling thread was interrupted
         */
        public synchronized Runnable poll() throws InterruptedException {
            while (pendingJobs.isEmpty()) {
                wait();
            }
            return pendingJobs.poll();
        }

        /**
         * Add new job to queue
         *
         * @param job {@code job} to execute.
         */
        public synchronized void add(final Runnable job) {
            if (!finish) {
                pendingJobs.add(job);
                notify();
            }
        }

        /**
         * Finish all tasks.
         */
        public synchronized void close() {
            notifyAll();
        }
    }

    private class Job<T, R> {
        private final List<R> data;
        private int left;
        private RuntimeException exception = null;

        /**
         * Constructs new {@link Job} with specified tasks to execute
         *
         * @param f mapper function.
         * @param args values to map.
         */
        public Job(final Function<? super T, ? extends R> f, final List<? extends T> args) {
            data = new ArrayList<>(Collections.nCopies(args.size(), null));
            left = args.size();
            IntStream.range(0, left).<Runnable>mapToObj(i -> () -> {
                try {
                    write(i, f.apply(args.get(i)));
                } catch (final RuntimeException e) {
                    suppress(e);
                } finally {
                    decrement();
                }
            }).forEach(jobs::add);
        }

        private synchronized void write(final int index, final R arg) {
            data.set(index, arg);
        }

        private synchronized void decrement() {
            if (--left == 0) {
                notify();
            }
        }

        private synchronized void suppress(final RuntimeException e) {
            if (exception == null) {
                exception = e;
            } else {
                exception.addSuppressed(e);
            }
        }

        /**
         * Return evaluated data or throw exception if failed
         *
         * @return {@code List<R>} with evaluated results
         * @throws InterruptedException if calling thread was interrupted
         */
        public synchronized List<R> getData() throws InterruptedException {
            while (left > 0 && !finish) {
                wait();
            }
            if (left > 0) {
                throw new InterruptedException("Threads are interrupted");
            }
            if (exception != null) {
                throw exception;
            } else {
                return data;
            }
        }
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performs in parallel.
     *
     * @param f mapper function.
     * @param args values to map.
     * @param <T> value type.
     * @param <R> output type.
     * @return {@code List<R>} with evaluated results
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> f, final List<? extends T> args) throws InterruptedException {
        if (finish) {
            throw new RuntimeException("Parallel mapper was finished");
        }
        if (args.isEmpty()) {
            return List.of();
        }
        return new Job<T, R>(f, args).getData();
    }

    /**
     * Stops all threads. All unfinished mappings leave in undefined state.
     */
    @Override
    public void close() {
        if (!finish) {
            finish = true;
            synchronized (this) {
                threads.forEach(Thread::interrupt);
                jobs.close();
                joinThreads(threads);
            }
        }
    }

    private void joinThreads(final List<Thread> threads)  {
        for (int i = 0; i < threads.size(); i++) {
            try {
                threads.get(i).join();
            } catch (final InterruptedException e) {
                i--;
            }
        }
    }
}
