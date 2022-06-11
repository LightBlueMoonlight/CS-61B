package deque;

public interface Deque<T>{
    public void addFirst(T item);

    public void addLast(T item);

    public boolean isEmpty();

    public T removeLast();

    public T removeFirst();

    public int size();

    public void printDeque();

    public T get(int index);
}
