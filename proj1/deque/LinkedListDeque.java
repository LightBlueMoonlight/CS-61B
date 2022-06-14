package deque;
import java.util.Iterator;
//LinkedListDeque：双端链表/双向链表
public class LinkedListDeque<T> implements Deque<T> {
    //私有内部类
    private class Stuffnode {
        //泛型类，代表调用参数的类型
        private T item;
        //previous 前一位
        private Stuffnode pre;
        //next 后一位
        private Stuffnode next;

        //构造方法
        private Stuffnode(T x) {
            item = x;
        }
    }

    //默认长度是1
    private int size = 1;
    //哨兵
    private Stuffnode stuf;

    //构造方法，当LinkedListDeque被new成对象时对方法里的变量进行声明
    private LinkedListDeque(T x) {
        Stuffnode temp = new Stuffnode(x);
        stuf.next = temp;
        stuf.pre = temp;
        size = 1;
    }

    //无参构造
    public LinkedListDeque() {
        stuf = new Stuffnode(null);
        size = 0;
    }

    public void addFirst(T x) {
        if (size == 0) {
            Stuffnode newStuffnode = new Stuffnode(x);
            stuf.next = newStuffnode;
            stuf.pre = newStuffnode;
            newStuffnode.pre = stuf;
            newStuffnode.next = stuf;
            size++;
        } else {
            size++;
            Stuffnode newStuffnode = new Stuffnode(x);
            stuf.next.pre = newStuffnode;
            newStuffnode.next = stuf.next;
            newStuffnode.pre = stuf;
            stuf.next = newStuffnode;
        }
    }

    public void addLast(T x) {
        if (size == 0) {
            Stuffnode newStuffnode = new Stuffnode(x);
            stuf.next = newStuffnode;
            stuf.pre = newStuffnode;
            newStuffnode.pre = stuf;
            newStuffnode.next = stuf;
            size++;
        } else {
            Stuffnode newStuffnode = new Stuffnode(x);
            stuf.pre.next = newStuffnode;
            newStuffnode.pre = stuf.pre;
            newStuffnode.next = stuf;
            stuf.pre = newStuffnode;
            size++;
        }
    }

    public boolean isEmpty() {
        if (size == 0) {
            return true;
        }
        return false;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        while (stuf.next != stuf) {
            System.out.print(stuf.next.item);
            System.out.print(" ");
            stuf.next = stuf.next.next;
        }
        System.out.print("\n");
    }

    @Override
    public T get(int index) {
        if (size == 0) {
            return null;
        }
        int t = 0;
        Stuffnode a = stuf.next;
        while (a != stuf) {
            if (index == t) {
                return a.item;
            }
            a = a.next;
            t++;
        }
        return null;
    }

    public T removeFirst() {
        if (size >= 1) {
            Stuffnode temp = stuf.next;
            T t = stuf.next.item;
            stuf.next.next.pre = stuf;
            stuf.next = stuf.next.next;
            size--;
            temp.next = null;
            temp.pre = null;
            return t;
        }
        return null;
    }

    public T removeLast() {
        if (size >= 1) {
            Stuffnode temp = stuf.pre;
            T t = stuf.pre.item;
            stuf.pre = stuf.pre.pre;
            stuf.pre.next = stuf;
            size--;
            temp.next = null;
            temp.pre = null;
            return t;
        }
        return null;
    }


    //递归get
    public T getRecursive(int index) {
        if (size == 0) {
            return null;
        }
        for (int j = 0; j <= index; j++) {
            if (index == j) {
                return stuf.next.item;
            }
            stuf = stuf.next;
        }
        return getRecursive(index);
    }

    public Iterator<T> iterator() {
        return new ArrayIterator();
    }

    private class ArrayIterator implements Iterator<T> {
        private int wizPos;

         ArrayIterator() {
            wizPos = 0;
        }

        public boolean hasNext() {
            return wizPos < size;
        }

        public T next() {
            T returnItem = stuf.next.item;
            stuf.next = stuf.next.next;
            wizPos += 1;
            return returnItem;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (this == other) {
            return true;
        }

        if (other instanceof LinkedListDeque) {
            LinkedListDeque<T> o = (LinkedListDeque<T>) other;
            if (o.size != this.size) {
                return false;
            }
            for (int i = 0; i < o.size; i++) {
                if (!o.get(i).equals(this.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

}