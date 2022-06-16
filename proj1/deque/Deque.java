package deque;

public interface Deque<T> {
    void addFirst(T item);

    void addLast(T item);

    T removeLast();

    T removeFirst();

    int size();

    void printDeque();

    T get(int index);

    default boolean isEmpty() {
        return size() == 0;
    }
}
