package bstmap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>,V> implements Map61B<K,V>{
    //根节点
    private Node root;
    private int size = 0;

    //节点类
    private class Node {
        public K key;
        public V value;
        //左节点
        public Node left;
        //右节点
        public Node right;
        //有参构造
        public Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    //清楚所有节点
    public void clear() {
        root = null;
        size = 0;
    }

    //是否包含当前key
    public boolean containsKey(K key) {
            return containsKey(root,key);
        }

    private boolean containsKey(Node node, K key) {
        if (node == null) {
            return false;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return containsKey(node.left, key);
        } else if (cmp > 0) {
            return containsKey(node.right, key);
        }
        // key=node.key
        return true;
    }

    //获取key对于的value
    public V get(K key) {
        return get(root, key);
    }

    private V get(Node node, K key) {
        //不存在根节点
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(node.left, key);
        } else if (cmp > 0) {
            return get(node.right, key);
        }
        // key=node.key
        return node.value;
    }

    public int size() {
        return size;
    }

    public void put(K key, V value) {
        root = put(root, key, value);
        size++;
    }

    private Node put(Node root, K key, V value) {
        //没有根节点，将当前key和value生成一个新节点
        if(root == null){
            return new Node(key,value);
        }
        if (key.compareTo(root.key) < 0){
            root.left = put(root.left,key,value);
        }else if(key.compareTo(root.key) > 0){
            root.right = put(root.right,key,value);
        }
        return root;
    }

    public Set<K> keySet() {
        HashSet<K> set = new HashSet<>();
        set.add(root.key);
        addKeys(root.left,set);
        addKeys(root.right,set);
        return set;
    }

    private void addKeys(Node node, Set<K> set) {

        if (node == null) {
            return;
        }
        System.out.println(node.key);
        set.add(node.key);
        addKeys(node.left, set);
        addKeys(node.right, set);
    }

    public V remove(K key) {
        //存在才删除
        if (containsKey(key)) {
            V targetValue = get(key);
            root = remove(root, key);
            size -= 1;
            return targetValue;
        }
        return null;
    }

    private Node remove(Node root, K key) {
        int cmp = key.compareTo(root.key);
        if (cmp < 0) {
            root.left = remove(root.left, key);
        } else if (cmp > 0) {
            root.right = remove(root.right, key);
        } else {
            if (root.left == null) {
                return root.right;
            }
            if (root.right == null) {
                return root.left;
            }
            Node originalNode = root;
            root = getMinChild(root.right);
            root.left = originalNode.left;
            root.right = remove(originalNode.right, root.key);
        }
        return root;
    }

    private Node getMinChild(Node node) {
        if (node.left == null) {
            return node;
        }
        return getMinChild(node.left);
    }

    public V remove(K key, V value) {
        return null;
    }

    public Iterator<K> iterator() {
        return  keySet().iterator();
    }

    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(Node node) {
        if (node == null) {
            return;
        }
        printInOrder(node.left);
        System.out.println(node.key.toString() + " -> " + node.value.toString());
        printInOrder(node.right);
    }

    public static void main(String[] args) {
        BSTMap<String, String> a = new BSTMap<String, String>();
        a.put("1","1");
        a.put("2","2");
        a.put("3","3");
        a.put("4","4");
        a.put("5","5");
        a.put("6","6");
        a.put("7","7");
        a.put("8","8");
        a.put("9","9");
        a.put("10","10");
        a.put("11","11");
        a.put("12","12");
        a.put("13","13");
        a.put("14","14");
        a.put("15","15");
        a.keySet();
    }
}
