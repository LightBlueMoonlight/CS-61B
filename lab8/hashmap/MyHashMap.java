package hashmap;

import java.util.*;
import java.util.function.Consumer;

//        哈希表支持的Map实现。提供摊销固定时间
//        在最佳情况下，通过get（）、remove（）和put（）访问元素。
//        假定永远不会插入空键，并且在remove（）时不会向下调整大小。
public class MyHashMap<K, V> implements Map61B<K, V> {

    /* 哈希表的底层数据结构 */
    private Collection<Node>[] buckets;
    private int size = 0;
    //初始容量大小是创建时给数组分配的容量大小，默认值为16
    private static final int DEFAULT_INITIAL_SIZE = 16;
    //负载因子
    private static final double DEFAULT_MAX_LOAD_FACTOR = 0.75;
    //最大负载系数，bucket所容纳的最大平均元素的数量，假设容器中有100个元素，容器中bucket的最大平均元素值是12.5，bucket数量起码是＞8（即最少9个才能装下100个元素）
    private double maxLoadFactor;


    public Iterator<K> iterator() {
        return new MyHashMapIterator();
    }

    private class MyHashMapIterator implements Iterator<K> {
        private final Iterator<Node> nodeIterator = new MyHashMapNodeIterator();

        public boolean hasNext() {
            return nodeIterator.hasNext();
        }

        public K next() {
            return nodeIterator.next().key;
        }
    }

    public void forEach(Consumer<? super K> action) {

    }

    public Spliterator<K> spliterator() {
        return null;
    }

    @Override
    public void clear() {
        size =0;
        //清空相当于重新创建了一个table
        buckets = createTable(DEFAULT_INITIAL_SIZE);
    }

    @Override
    public boolean containsKey(K key) {
        return get(key)!=null;
    }

    //元素的key的hash值对数组长度取模，获取到存储数组下标的位置
    private int getBucketIndex(K key){
        return key.hashCode() % buckets.length;
    }

    @Override
    public V get(K key) {
        Node node = getNode(key);
        if (node == null){
            return null;
        }
        return node.value;
    }

    private Node getNode(K key) {
        //获取数组下标，找对应链表的值
        int bucketIndex = getBucketIndex(key);
        return getNode(key,bucketIndex);
    }

    private Node getNode(K key,int bucketIndex) {
        for (Node node : buckets[bucketIndex]){
            if (node.key.equals(key)){
                return node;
            }
        }
            return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        int bucketIndex = getBucketIndex(key);
        Node node = getNode(key,bucketIndex);
        if (node!= null){
            node.value = value;
            return;
        }
        node = createNode(key,value);
        buckets[bucketIndex].add(node);
        size +=1;
        //如果超过最大负载系数就进行扩容
        if((double) (size / buckets.length) > maxLoadFactor){
            resize(buckets.length * 2);
        }
    }

    private void resize(int i) {
        Collection<Node>[] newBuckets = createTable(i);
        Iterator<Node> nodeIterator = new MyHashMapNodeIterator();
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            int bucketIndex = node.key.hashCode() % newBuckets.length;
            newBuckets[bucketIndex].add(node);
        }
        buckets = newBuckets;
    }

    private class MyHashMapNodeIterator implements Iterator<Node> {
        private final Iterator<Collection<Node>> bucketsIterator = Arrays.stream(buckets).iterator();
        private Iterator<Node> currentBucketIterator;
        private int nodesLeft = size;

        @Override
        public boolean hasNext() {
            return nodesLeft > 0;
        }

        @Override
        public Node next() {
            if (currentBucketIterator == null || !currentBucketIterator.hasNext()) {
                Collection<Node> currentBucket = bucketsIterator.next();
                while (currentBucket.size() == 0) {
                    currentBucket = bucketsIterator.next();
                }
                currentBucketIterator = currentBucket.iterator();
            }
            nodesLeft -= 1;
            return currentBucketIterator.next();
        }
    }


    @Override
    public Set<K> keySet() {
        HashSet<K> set = new HashSet<>();
        for (K key : this) {
            set.add(key);
        }
        return set;
    }

    @Override
    public V remove(K key) {
        int bucketIndex = getBucketIndex(key);
        Node node = getNode(key, bucketIndex);
        if (node == null) {
            return null;
        }
        size -= 1;
        buckets[bucketIndex].remove(node);
        return node.value;
    }

    @Override
    public V remove(K key, V value) {
        int bucketIndex = getBucketIndex(key);
        Node node = getNode(key, bucketIndex);
        if (node == null || !node.value.equals(value)) {
            return null;
        }
        size -= 1;
        buckets[bucketIndex].remove(node);
        return node.value;
    }

    /**
     私有帮助器类，用于存储单个键值映射。此类的起始代码应该易于理解，并且不需要任何修改
     */
    protected class Node {
        K key;
        V value;
        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    //无参构造，没有指定参数时，默认生成一个大小为16，负载因子为0.75的hashMap
    public MyHashMap() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_MAX_LOAD_FACTOR);
    }

    //按照指定数组大小生成负载因子为0.75的hashMap
    public MyHashMap(int initialSize) {
        this(initialSize, DEFAULT_MAX_LOAD_FACTOR);
    }

//    MyHashMap构造函数，用于创建initialSize的后备数组。
//    负载系数（#items/#buckets）应始终小于等于loadFactor
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        maxLoadFactor = maxLoad;
    }

    /**
     * 返回要放置在哈希表桶中的新节点
     */
    private Node createNode(K key, V value) {
        return new Node(key,value);
    }

//返回作为哈希表桶的数据结构
//        哈希表桶的唯一要求是：
//            1.插入项目（“添加”方法）
//            2.删除项目（“Remove”方法）
//            3.遍历项（“迭代器”方法）
//            java.util.Collection支持这些方法中的每一种，
//            Java中的大多数数据结构都继承自Collection，因此我们几乎可以使用任何数据结构作为我们的桶。
//            重写此方法以将不同的数据结构用作 基础bucket类型
//        请确保调用此工厂方法，而不是创建
//使用新操作符创建自己的BUCKET数据结构！
    protected Collection<Node> createBucket() {
        //创建一个空的列表
        return new LinkedList<>();
    }

//    返回一个表以支持哈希表。根据评论 上面，该表可以是集合对象的数组
//    请确保在创建表时调用此工厂方法，以便
//    所有BUCKET类型都是JAVA.UTIL.COLLECTION
    private Collection<Node>[] createTable(int tableSize) {
        //创建一个大小为tableSize，类型为Node的数组
        Collection<Node>[] table = new Collection[tableSize];
        for (int i=0;i<tableSize;i++){
            //为每个数组下标创建列表用来存放对应下标的值
            table[i] = createBucket();
        }
        return table;
    }



}
