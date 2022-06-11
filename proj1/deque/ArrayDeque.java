package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>,Iterable<T>{
    //构造一个泛型数组
    private T[] items;
    //数组前面的标志位 类似哨兵的pre
    private int begin;
    //数组后面的标志位 类似哨兵的next
    private int end;
    //数组整体的长度
    private int size;
    private int start;
    private int last;
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
        start = 0;
        last = 0;
        end = items.length;
        //首部添加超出原始的值，如原来是100,现在要添加到101,s1=101
        s1 = 0;
        //末尾添加超出原始的值，如原来是100,现在要添加到101,s2=101
        s2 = 0;
    }


    public void addLast(T item) {
        if (last <= begin && last < items.length) {
            items[last] = item;
            size++;
            last++;
            ratio = size / items.length;
            s2++;
        } else {
            //添加的数组超过原始数组后新建一个新的数组a，空间是原始数组的2倍
            T[] a = (T[]) new Object[items.length * 2];
            if (s2 > 0) {
                //对原来数组中的值进行copy到新的数组a中
                System.arraycopy(items, start, a, 0, s2);
            }
            if (begin < items.length - 1) {
                System.arraycopy(items, begin + 1, a, a.length - s1, s1);
            }
            items = a;
            end = items.length;
            start = 0;
            last = s2;
            begin = items.length - s1 - 1;
            //将添加的值放到数组的末尾
            items[last] = item;
            last++;
            size = size + 1;
            s2++;
        }
    }

    public void addFirst(T item) {
        if (begin >= 0 && begin >= last) {
            items[begin] = item;
            begin--;
            size++;
            s1++;
            ratio = (double) size / items.length;
        } else {
            T[] a = (T[]) new Object[items.length * 2];
            if (s2 > 0) {
                System.arraycopy(items, start, a, 0, s2);
            }

            if (s1 > 0) {
                System.arraycopy(items, begin + 1, a, a.length - s1, s1);
            }
            items = a;
            end = items.length;
            begin = items.length - s1 - 1;

            if (begin >= 0 && begin < items.length) {
                items[begin] = item;
                begin--;
            }
            start = 0;
            last = s2;
            size = size + 1;
            ratio = (double) size / items.length;
            s1++;
        }
    }

    private T d;

    public T removeLast() {
        if (s2 > 0) {
            s2--;
            size--;
            last--;
            d = items[last];
            ratio = (double) size / items.length;
            //数组使用率小于25% 就将数组的长度减少，大于16是因为只考虑扩容之后的情况
            if (ratio < 0.25 && items.length > 16) {
                recycle();
            }
            return d;
        } else if (s1 > 0) {
            s1--;
            end--;
            size--;
            d = items[end];
            ratio = (double) size / items.length;
            if (ratio < 0.25 && items.length > 16) {
                recycle();
            }
            return d;
        } else {
            return null;
        }
    }

    private int c;

    public T removeFirst() {
        if (s1 > 0) {
            s1--;
            size--;
            begin++;
            d = items[begin];
            ratio = (double) size / items.length;
            if (ratio < 0.25 && items.length > 16) {
                recycle();
            }
            return d;
        } else if (s2 > 0) {
            s2--;
            d = items[start];
            start = start + 1;
            size--;
            ratio = (double) size / items.length;
            if (ratio < 0.25 && items.length > 16) {
                recycle();
            }
            return d;
        } else return null;
    }

    private void recycle() {
        if (size > 0) {
            T[] a = (T[]) new Object[size];
            if (s1 > 0){
                System.arraycopy(items, begin + 1, a, 0, s1);}
            if (s2 > 0){
                System.arraycopy(items, start, a, s1, s2);}
            items = a;
            s2 = size;
            s1 = 0;
            begin = items.length - 1;
            end = items.length;
            start = 0;
            last = s2;
        } else {
            T[] a = (T[]) new Object[8];

            //if(begin+1<=items.length-1)
            if (s1 > 0){
                System.arraycopy(items, begin + 1, a, 0, s1);}
            if (s2 > 0){
                System.arraycopy(items, start, a, s1, s2);}
            items = a;
            s2 = size;
            s1 = 0;
            begin = items.length - 1;
            end = items.length;
            start = 0;
            last = s2;
        }
    }

    public int size() {
        return size;
    }

    public T get(int index) {
        if (index < s1){
            return items[begin + index + 1];}
        if (index >= s1 && index < size){
            return items[start + (index - s1)];}
        else return null;
    }

    public void printDeque() {

        for (int i = begin + 1; i < end; i++) {
            System.out.print(items[i]);
            System.out.print(' ');
        }
        for (int j = start; j < last; j++) {
            System.out.print(items[j]);
            System.out.print(' ');
        }
        System.out.print("\n");
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Iterator<T> iterator(){
        return new ArrayIterator();
    }

    private class ArrayIterator implements Iterator<T>{
        private int wizPos;
        public ArrayIterator() {
            wizPos=0;
        }

        public boolean hasNext() {
            return wizPos < size;
        }

        public T next(){
            if(wizPos >= size){
                return null;
            }
            int returnItrm =0;
            if (wizPos < s1){
                items[returnItrm] = items[begin + wizPos + 1];}
            if (wizPos >= s1 && wizPos < size){
                items[returnItrm] = items[start + (wizPos - s1)];}
            wizPos += 1;
            return items[returnItrm];
        }
    }

    @Override
    public boolean equals(Object other){
        if(other==null){
            return false;
        }

        if(this == other){
            return true;
        }

        if(other instanceof ArrayDeque){
            ArrayDeque<T> o =(ArrayDeque<T>) other;
            if (o.size !=this.size){
                return false;
            }
            for(int i=0;i<o.size;i++){
                if(!o.get(i).equals(this.get(i)) ){
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        ArrayDeque<String> lld1 = new ArrayDeque<String>();
        ArrayDeque<String> lld2 = new ArrayDeque<String>();
//        lld1.addFirst("1");
//        lld1.addFirst("2");
//        lld2.addFirst("1");
//        lld2.addFirst("2");



        for (int i = 0; i <= 10000; i++) {
            lld1.addFirst(String.valueOf(i));
            lld2.addFirst(String.valueOf(i));
        }

        if(lld1.equals(lld2)){
            System.out.println("相等");
        }
//        lld1.removeLast();

//        Iterator<Integer> seer = lld1.iterator();
//        while (seer.hasNext()){
//            int n = seer.next();
//            System.out.println(n);
//        }
    }
}
