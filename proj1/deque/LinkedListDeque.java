package deque;
    //LinkedListDeque：双端链表/双向链表
    public class LinkedListDeque <T>{
        //私有内部类
        private  class stuffnode{
            //泛型类，代表调用参数的类型
            private T item;
            //previous 前一位
            private stuffnode pre;
            //next 后一位
            private stuffnode next;
            //构造方法
            private stuffnode(T x) {
                item=x;
            }
        }

        //默认长度是1
        private int size=1;
        //first：头指针
        private stuffnode first;
        //last：尾指针
        private stuffnode last;
        //哨兵
        private stuffnode stuf;

        //构造方法，当LinkedListDeque被new成对象时对方法里的变量进行声明
        private LinkedListDeque(T x) {
            first=new stuffnode(x);
            stuf=new stuffnode(x);
            //第一个链表的前一位是哨兵
            first.next=stuf;
            //第一个链表的后一位也是哨兵
            first.pre=stuf;
            stuf.next=first;
            stuf.pre=first;
            last=first;
            last.next=stuf;
            size=1;
        }

        //无参构造
        public LinkedListDeque() {
            stuf=new stuffnode(null);
            first=stuf;
            last=first;
            size=0;
        }

        public void addFirst(T x) {
            if(size==0) {
                first=new stuffnode(x);
                stuf.next=first;
                stuf.pre=first;
                first.pre=stuf;
                first.next=stuf;
                last.pre=stuf;
                last=first;
                size++;
            }
            else {
                size++;
                stuffnode newFirst=new stuffnode(x);
                stuf.next=newFirst;
                newFirst.next=first;
                newFirst.pre=stuf;
                first.pre=newFirst;
                first=newFirst;
                first.pre=stuf;
                first.next=newFirst.next;
            }
        }

        public void addLast(T x) {
            if(isEmpty()) {
                last=new stuffnode(x);
                stuf.pre=last;
                stuf.next=last;
                last.next=stuf;
                last.pre=stuf;
                first=last;
                first.next=stuf;
                first.pre=stuf;
                size++;
            } else {
                stuffnode oldLast=last;
                last=new stuffnode(x);
                last.pre=oldLast;
                oldLast.next=last;
                last.next=stuf;
                stuf.pre=last;
                size++;
            }
        }

        public boolean isEmpty() {
            if(size==0) {
                return true;
            }
            return false;
        }

        public int size() {
            return size;
        }

        public void printDeque() {
            stuffnode p = first;
            while(p != stuf) {
                System.out.print(p.item);
                System.out.print(" ");
                p=p.next;
            }
            System.out.print("\n");
        }

        public T removeFirst() {
            if(size >=1 ) {
                T t=first.item;
                first=first.next;
                stuf.next=first;
                size--;
                return t;
            }
                return null;
        }

        public T removeLast() {
            if(size >= 1) {
                T t=last.item;
                last=last.pre;
                last.next=stuf;
                size--;
                return t;
            }
            return null;
        }

        public T get(int index) {
            if(index>size || index < size){
                return null;
            }
            int t=0;
            stuffnode a = first;
            while(a!=stuf) {
                if(index == t) {
                    return a.item;
                }
                a=a.next;
                t++;
            }
            return null;
        }

        //递归get
        public T getRecursive(int index){
            if(index>size || index < size){
                return null;
            }
            int a =0;
            int b =a++;
            if(index == 0){
                return stuf.next.item;
            }else if(index == b){
                return stuf.item;
            }else{
                for(int j =0;j <=index;j++){
                    stuf=stuf.next;
                }
                return getRecursive(index);
            }
        }
    }