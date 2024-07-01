package info.kgeorgiy.ja.firef0xil.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final Comparator<? super T> comparator;
    private final SetArray<T> data;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends T> collection) {
       this(collection, null);
    }

    public ArraySet(Comparator<? super T> comparator) {
        this(Collections.emptyList(), comparator);
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comparator) {
        this.comparator = comparator;
        ArrayList<T> tmp = new ArrayList<>(collection.size());
        tmp.addAll(collection);
        tmp.sort(comparator);
        int j = 1;
        for (int i = 1; i < tmp.size(); i++) {
            T prev = tmp.get(i - 1);
            T cur = tmp.get(i);
            if (compareElements(prev, cur) != 0) {
                tmp.set(j++, cur);
            }
        }
        if (tmp.size() > j) {
            tmp.subList(j, tmp.size()).clear();
        }
        tmp.trimToSize();
        data = new SetArray<>(tmp, false);
    }

    private ArraySet(List<T> list, Comparator<? super T> comparator, boolean reverse) {
        data = new SetArray<>(list, reverse);
        this.comparator = comparator;
    }

    private ArraySet(SetArray<T> setArray, Comparator<? super T> comparator) {
        data = new SetArray<>(setArray);
        this.comparator = Collections.reverseOrder(comparator);
    }

    @Override
    public T lower(T t) {
        return getElement(lowerGet(t));
    }

    private int lowerGet(T t) {
        return normalizeIndex(get(t),- 1, - 1);
    }

    @Override
    public T floor(T t) {
        return getElement(floorGet(t));
    }

    private int floorGet(T t) {
        return normalizeIndex(get(t), - 1, 0);
    }

    @Override
    public T ceiling(T t) {
        return getElement(ceilingGet(t));
    }

    private int ceilingGet(T t) {
        return normalizeIndex(get(t), 0, 0);
    }

    @Override
    public T higher(T t) {
        return getElement(higherGet(t));
    }

    private int higherGet(T t) {
        return normalizeIndex(get(t), 0, 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return get((T) o) >= 0;
    }

    public int normalizeIndex(int pos, int first, int second) {
        if (pos < 0) {
            return Math.abs(pos) - 1 + first;
        } else {
            return pos + second;
        }
    }

    private int get(T t) {
        return Collections.binarySearch(data, t, comparator);
    }


    private T getElement(int index) {
        if (index < 0 || data.size() <= index) {
            return null;
        } else {
            return data.get(index);
        }
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return new SetIterator<>(data);
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(data, comparator);
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    private int compareElements(T fromElement, T toElement) {
        List<T> first = Arrays.asList(fromElement, toElement);
        List<T> second = Arrays.asList(toElement, fromElement);
        first.sort(comparator);
        second.sort(comparator);
        if (first.equals(second)) {
            return first.get(0) == fromElement ? -1 : 1;
        } else {
            return 0;
        }
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        if (compareElements(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        int fromIndex = fromInclusive ? ceilingGet(fromElement) : higherGet(fromElement);
        int toIndex = toInclusive ? floorGet(toElement) : lowerGet(toElement);
        if (fromIndex > toIndex || fromIndex == -1 || toIndex == -1){
            return new ArraySet<>(comparator);
        }
        return new ArraySet<>(data.subList(fromIndex, toIndex + 1), comparator, data.reverse);
    }

    private NavigableSet<T> getHeadTail(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        return compareElements(fromElement, toElement) > 0 ?
                new ArraySet<>(comparator) :
                subSet(fromElement, fromInclusive, toElement, toInclusive);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        if (!isEmpty()) {
            T fromElement = first();
            return getHeadTail(fromElement, true, toElement, inclusive);
        } else {
            return new ArraySet<>(comparator);
        }
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        if (!isEmpty()) {
            T toElement = last();
            return getHeadTail(fromElement, inclusive, toElement, true);
        } else {
            return new ArraySet<>(comparator);
        }
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        checkEmpty();
        return data.get(0);
    }

    @Override
    public T last() {
        checkEmpty();
        return data.get(data.size() - 1);
    }

    @Override
    public int size() {
        return data.size();
    }

    private void checkEmpty() {
        if (isEmpty()) throw new NoSuchElementException();
    }

    private static class SetArray<T> extends AbstractList<T> implements RandomAccess {
        private final List<T> data;
        private final boolean reverse;

        public SetArray(SetArray<T> setArray) {
            data = setArray.data;
            this.reverse = !setArray.reverse;
        }

        public SetArray(List<T> data, boolean reverse) {
            this.data = data;
            this.reverse = reverse;
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public T get(int index) {
            return reverse ? data.get(data.size() - 1 - index) : data.get(index);
        }

    }

    private static class SetIterator<T> implements Iterator<T> {
        SetArray<T> data;
        private int cur = 0;

        public SetIterator(SetArray<T> data) {
            this.data = data;
        }

        @Override
        public boolean hasNext() {
            return cur < data.size();
        }

        @Override
        public T next() {
            if (cur < data.size()) {
                return data.get(cur++);
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
