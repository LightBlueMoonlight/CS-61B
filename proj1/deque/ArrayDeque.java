package deque;

public class ArrayDeque<T> {
    //构造一个泛型数组
    private T[] items;
    //sentinel头节点
    private int begin;
    //sentinel尾节点
    private int end;
    //数组整体的长度
    private int size;
    //头指针
    private int start;
    //尾指针
    private int last;
    //数组使用率/负载系数
    private double ratio;

    private int s1;
    private int s2;


    //无参构造,创建一个空列表
    public ArrayDeque() {
        items = (T[]) new Object[8];
        //当前的添加的项目是size-1
        begin=items.length-1;
        //长度为0
        size=0;
        ratio=0;
        start=0;
        last=0;
        //添加的下一个项目是size
        end=items.length;
        s1=0;
        //超出原始items后的值，如原来是100,现在要添加到101,s2=101
        s2=0;
    }


    public void addLast(T item) {
        if(last <= begin && last < items.length) {
            items[last]=item;
            size++;
            last++;
            ratio=size/items.length;
            s2++;
        } else {
            //添加的数组超过原始数组后新建一个新的数组a，空间是原始数组的2倍
            T[] a = (T[]) new Object[items.length*2];
            if(s2 > 0){
                //对原来数组中的值进行copy到新的数组a中
                System.arraycopy(items, start, a, 0,s2);
            }
            if(begin < items.length-1){
                System.arraycopy(items, begin+1, a, a.length-s1, s1);
            }
            items = a;
            end=items.length;
            start=0;
            last=s2;
            begin=items.length-s1-1;
            //将添加的值放到数组的末尾
            items[last]=item;
            last++;
            size = size + 1;
            s2++;
        }
    }

    public  void addFirst(T item) {
        if(begin >= 0 && begin >= last) {
            items[begin]=item;
            //头哨兵前移
            begin--;
            size++;
            s1++;
            ratio=(double) size/items.length;
        } else {
            T[] a = (T[]) new Object[items.length*2];
            if(s2>0)
                System.arraycopy(items, start, a, 0, s2);
            if(s1>0)
                System.arraycopy(items, begin+1, a, a.length-s1, s1);
            items = a;
            end=items.length;
            begin=items.length-s1-1;
            if(begin>=0&&begin<items.length)
                items[begin]=item;
            begin--;
            start=0;
            last=s2;
            size = size + 1;
            ratio=(double) size/items.length;
            s1++;
        }
    }

    private T d;
    public T removeLast() {
        if(s2 > 0) {
            s2--;
            size--;
            last--;
            d=items[last];
            ratio=(double) size/items.length;
            //数组使用率小于25% 就将数组的长度减少，大于16是因为只考虑扩容之后的情况
            if(ratio < 0.25 && items.length > 16) {
                recycle();
            }
            return d;
        } else if(s1>0){
            s1--;
            end--;
            size--;
            d=items[end];
            ratio=(double) size/items.length;
            if(ratio<0.25&&items.length>16) {
                recycle();
            }
            return d;
        }
        else{
            return  null;
        }
    }

    private int c;
    public T removeFirst() {
        if(s1>0) {
            s1--;
            size--;
            begin++;
            d=items[begin];
            ratio=(double) size/items.length;
            if(ratio<0.25&&items.length>16) {
                recycle();
            }
            return d;
        }
        else if(s2>0) {
            s2--;
            d=items[start];
            start=start+1;
            size--;
            ratio=(double) size/items.length;
            if(ratio<0.25&&items.length>16) {
                recycle();
            }
            return d;
        }
        else return  null;
    }

    private void recycle() {
        if (size > 0) {
            T[] a = (T[]) new Object[size];
            if (s1 > 0)
                System.arraycopy(items, begin + 1, a, 0, s1);
            if (s2 > 0)
                System.arraycopy(items, start, a, s1, s2);
            items = a;
            s2 = size;
            s1 = 0;
            begin = items.length - 1;
            end = items.length;
            start = 0;
            last = s2;
        }
        else {
            T[] a = (T[]) new Object[1];

            //if(begin+1<=items.length-1)
            if (s1 > 0)
                System.arraycopy(items, begin + 1, a, 0, s1);
            if (s2 > 0)
                System.arraycopy(items, start, a, s1, s2);
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

    public 	T get(int index) {
        if(index<s1)
            return items[begin+index+1];
        if(index>=s1&&index<size)
            return  items[start+(index-s1)];
        else return  null;
    }

    public void  printDeque() {

        for(int i=begin+1;i<end;i++) {
            System.out.print(items[i]);
            System.out.print(' ');
        }
        for(int j=start;j<last;j++) {
            System.out.print(items[j]);
            System.out.print(' ');
        }
        System.out.print("\n");
    }

    public boolean isEmpty() {
        return size==0;
    }


    public static void main(String[] args) {
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        for (int i = 0;i <= 5 ;i++){
            lld1.addFirst(i);
            lld1.addLast(i);
            lld1.printDeque();
        }
    }


}
