package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    //构造一个泛型数组
    private T[] items;
    //数组前面的标志位 类似哨兵的pre
    private int begin;
    //数组后面的标志位 类似哨兵的next
    private int end;
    //数组整体的长度
    private int size;
    //数组使用率/负载系数
    private double ratio;
    //头节点计数器
    private int s1;
    //尾节点计数器
    private int s2;


    //无参构造,创建一个空列表
    public ArrayDeque() {
        items = (T[]) new Object[8];
        begin = items.length - 1;
        size = 0;
        ratio = 0;
        end = 0;
        //首部添加超出原始的值，如原来是100,现在要添加到101,s1=101
        s1 = 0;
        //末尾添加超出原始的值，如原来是100,现在要添加到101,s2=101
        s2 = 0;
    }

    @Override
    public void addLast(T item) {
        if (end <= begin && end < items.length) {
            items[end] = item;
            size++;
            end++;
            ratio = size / items.length;
            s2++;
        } else {
            //添加的数组超过原始数组后新建一个新的数组a，空间是原始数组的2倍
            T[] a = (T[]) new Object[items.length * 2];
            if (s2 > 0) {
                //对原来数组中的值进行copy到新的数组a中
                System.arraycopy(items, 0, a, 0, s2);
            }
            if (s1 > 0) {
                //对原来数组中的值进行copy到新的数组a中
                System.arraycopy(items, s2, a, items.length + s2, s1);
            }
            items = a;
            items[end] = item;
            end++;
            begin = items.length - s1 - 1;
            size = size + 1;
            ratio = size / items.length;
            s2++;
        }
    }

    @Override
    public void addFirst(T item) {
        if (begin >= 0 && begin >= end) {
            items[begin] = item;
            begin--;
            size++;
            s1++;
            ratio = size / items.length;
        } else {
            T[] a = (T[]) new Object[items.length * 2];
            if (s2 > 0) {
                System.arraycopy(items, 0, a, 0, s2);
            }
            if (s1 > 0) {
                System.arraycopy(items, s2, a, items.length + s2, s1);
            }
            items = a;
            begin = items.length - s1 - 1;
            items[begin] = item;
            begin--;
            size = size + 1;
            ratio = size / items.length;
            s1++;
        }
    }

    private T d;

    @Override
    public T removeLast() {
        if (s2 > 0) {
            s2--;
            size--;
            end--;
            d = items[end];
            items[end] = null;
            ratio = (double) size / items.length;
            //数组使用率小于25% 就将数组的长度减少，大于16是因为只考虑扩容之后的情况
            if (ratio < 0.25 && items.length > 16) {
                recycle();
            }
            return d;
        } else if (s1 > 0 && s2 == 0) {
            size--;
            s1--;
            begin++;
            d = items[items.length - 1];
            System.arraycopy(items, begin, items, begin + 1, s1);
            ratio = (double) size / items.length;
            items[begin] = null;
            if (ratio < 0.25 && items.length > 16) {
                recycle();
            }
            return d;
        } else {
            return null;
        }
    }

    private T c;

    @Override
    public T removeFirst() {
        if (s1 > 0 && s1 <= size) {
            s1--;
            size--;
            begin++;
            if (items[begin] != null) {
                c = items[begin];
            }
            items[begin] = null;
            ratio = (double) size / items.length;
            if (ratio < 0.25 && items.length > 16) {
                recycle();
            }
            return c;
        } else if (s2 > 0 && s1 == 0) {
            size--;
            s2--;
            end--;
            c = items[0];
            System.arraycopy(items, 1, items, 0, s2);
            ratio = (double) size / items.length;
            items[end] = null;
            if (ratio < 0.25 && items.length > 16) {
                recycle();
            }
            return c;
        } else {
            return null;
        }
    }

    private void recycle() {
        if (size > 0) {
            T[] a = (T[]) new Object[items.length / 4];
            if (s1 > 0) {
                System.arraycopy(items, begin + 1, a, a.length - s1, s1);

            }
            if (s2 > 0) {
                System.arraycopy(items, 0, a, 0, s2);

            }
            items = a;
            begin = items.length - s1 - 1;
            end = s2;

        } else {
            T[] a = (T[]) new Object[8];
            if (s1 > 0) {
                System.arraycopy(items, begin + 1, a, s2, s1 - 1);
            }
            if (s2 > 0) {
                System.arraycopy(items, 0, a, 0, s2);
            }
            items = a;
            begin = items.length - 1 - s2;
            end = items.length - s1;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index > size - 1) {
            return null;
        }
        if (index < s1) {
            return items[begin + index + 1];
        }
        if (index >= s1 && index < size) {
            return items[index - s1];
        } else {
            return null;
        }
    }

    public void printDeque() {
        int i = begin;
        int j = -1;
        for (int n = 0; n < s1; n++) {
            i++;
            System.out.print(items[i]);
            System.out.print(' ');
        }
        for (int m = 0; m < s2; m++) {
            j++;
            System.out.print(items[j]);
            System.out.print(' ');
        }
        System.out.print("\n");
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
            T item = get(wizPos);
            wizPos += 1;
            return item;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (other == this) {
            return true;
        }

        if (other instanceof Deque) {
            Deque o = (Deque) other;
            if (o.size() != size) {
                return false;
            }
            for (int i = 0; i < o.size(); i++) {
                if (o.get(i) == null) {
                    continue;
                }

                if (!o.get(i).equals(this.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }
}
