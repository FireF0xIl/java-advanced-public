package info.kgeorgiy.ja.firef0xil.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Implementation for interfaces {@link ScalarIP}, {@link ListIP} and {@link AdvancedIP}.
 * @author FireF0xIl
 */
public class IterativeParallelism implements AdvancedIP {
    private final ParallelMapper mapper;

    /**
     * Constructs new {@link IterativeParallelism} with {@code null} mapper.
     */
    public IterativeParallelism() {
        mapper = null;
    }

    /**
     * Constructs new {@link IterativeParallelism} with specified {@code mapper}
     *
     * @param mapper mapper to use
     */
    public IterativeParallelism(final ParallelMapper mapper) {
        this.mapper = mapper;
    }


    /**
     * Join values to string.
     *
     * @param threads number of concurrent threads.
     * @param values values to join.
     *
     * @return list of joined result of {@link #toString()} call on each value.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return controller(
                threads,
                values,
                stream -> stream.map(o -> o == null ? "null" : o.toString()).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    /**
     * Filters values by predicate.
     *
     * @param threads number of concurrent threads.
     * @param values values to filter.
     * @param predicate filter predicate.
     * @param <T> value type.
     *
     * @return list of values satisfying given predicated. Order of values is preserved.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return controller(threads, values, stream -> stream.filter(predicate).collect(Collectors.toList()), this::collect);
    }

    /**
     * Maps values.
     *
     * @param threads number of concurrent threads.
     * @param values values to filter.
     * @param f mapper function.
     * @param <T> input value type.
     * @param <U> output value type.
     *
     * @return list of values mapped by given function.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f) throws InterruptedException {
        return controller(threads, values, stream -> stream.map(f).collect(Collectors.toList()), this::collect);
    }

    private <T> List<T> collect(Stream<List<T>> stream) {
        return stream.collect(Collectors.flatMapping(List::stream, Collectors.toList()));
    }

    /**
     * Returns maximum value.
     *
     * @param threads number or concurrent threads.
     * @param values values to get maximum of.
     * @param comparator value comparator.
     * @param <T> value type.
     *
     * @return maximum of given values
     *
     * @throws InterruptedException if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return controller(threads, values, stream -> stream.max(comparator).orElse(null),
                stream -> stream.max(comparator).orElseThrow(NoSuchElementException::new));
    }

    /**
     * Returns minimum value.
     *
     * @param threads number or concurrent threads.
     * @param values values to get minimum of.
     * @param comparator value comparator.
     * @param <T> value type.
     *
     * @return minimum of given values
     *
     * @throws InterruptedException if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    /**
     * Returns whether all values satisfies predicate.
     *
     * @param threads number or concurrent threads.
     * @param values values to test.
     * @param predicate test predicate.
     * @param <T> value type.
     *
     * @return whether all values satisfies predicate or {@code true}, if no values are given
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return controller(threads, values, stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(Boolean::booleanValue));
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads number or concurrent threads.
     * @param values values to test.
     * @param predicate test predicate.
     * @param <T> value type.
     *
     * @return whether any value satisfies predicate or {@code false}, if no values are given
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, Predicate.not(predicate));
    }

    /**
     * Reduces values using monoid.
     *
     * @param threads number of concurrent threads.
     * @param values  values to reduce.
     * @param monoid  monoid to use.
     * @param <T> value type.
     * @return values reduced by provided monoid or {@link Monoid#getIdentity() identity} if not values specified.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> T reduce(final int threads, final List<T> values, final Monoid<T> monoid) throws InterruptedException {
        return controller(threads, values, stream -> reducer(stream, monoid), stream -> reducer(stream, monoid));
    }

    /**
     * Maps and reduces values using monoid.
     *
     * @param threads number of concurrent threads.
     * @param values  values to reduce.
     * @param lift    mapping function.
     * @param monoid  monoid to use.
     * @param <T> value type.
     * @param <R> output value type.
     * @return values reduced by provided monoid or {@link Monoid#getIdentity() identity} if not values specified.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T, R> R mapReduce(final int threads, final List<T> values, final Function<T, R> lift, final Monoid<R> monoid) throws InterruptedException {
        return controller(threads, values, stream -> reducer(stream.map(lift), monoid), stream -> reducer(stream, monoid));
    }

    private <T> T reducer(final Stream<T> stream, final Monoid<T> monoid) {
        return stream.reduce(monoid.getIdentity(), monoid.getOperator());
    }

    private <T, U> U controller(
            final int threads,
            final List<T> values,
            final Function<Stream<T>, U> function,
            final Function<Stream<U>, U> collector) throws InterruptedException {
        final List<Stream<T>> split = split(threads, values);
        final List<U> results = mapper != null ? mapper.map(function, split) : map(split, function);
        return collector.apply(results.stream());
    }

    private <T, U> List<U> map(
            final List<Stream<T>> values,
            final Function<Stream<T>, U> function
    ) throws InterruptedException {
        final int count = values.size();
        final List<U> res = new ArrayList<>(Collections.nCopies(count, null));
        final List<Thread> threadList = IntStream
                .range(0, count)
                .mapToObj(i -> new Thread(() -> res.set(i, function.apply(values.get(i)))))
                .peek(Thread::start)
                .collect(Collectors.toList());
        joinThreads(threadList);
        return res;
    }

    private <T> List<Stream<T>> split(final int threads, final List<T> values) {
        if (threads < 1) {
            throw new IllegalArgumentException("Number of threads should be positive number");
        }
        if (values.size() == 0) {
            return List.of();
        }
        final int count = Math.min(threads, values.size());
        final int s = values.size() / count;
        int additionalPool = values.size() % count;
        final List<Stream<T>> res = new ArrayList<>(Collections.nCopies(count, null));
        int left = 0;
        for (int i = 0; i < count; i++) {
            int right = left + s + (additionalPool-- > 0 ? 1 : 0);
            res.set(i, values.subList(left, right).stream());
            left = right;
        }
        return res;
    }

    private void joinThreads(final List<Thread> threads) throws InterruptedException {
        InterruptedException exception = null;
        for (int i = 0; i < threads.size(); i++) {
            try {
                threads.get(i).join();
            } catch (final InterruptedException e) {
                if (exception == null) {
                    for (int j = i; j < threads.size(); j++) {
                        threads.get(j).interrupt();
                    }
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
                i--;
            }
        }
        if (exception != null) {
            throw exception;
        }
    }
}
