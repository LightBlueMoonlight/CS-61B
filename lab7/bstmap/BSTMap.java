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
        //右系欸但
        public Node right;
        //有参构造
        public Node(K k, V v) {
            key = k;
            value = v;
        }
    }

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
        return true;
    }

    public V get(K key) {
        return get(root, key);
    }

    private V get(Node node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(node.left, key);
        } else if (cmp > 0) {
            return get(node.right, key);
        }
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
        set.add(node.key);
        addKeys(node.left, set);
        addKeys(node.right, set);
    }

    public V remove(K key) {
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
}
