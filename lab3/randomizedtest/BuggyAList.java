package randomizedtest;

/** Array based list.
 *  @author Josh Hug
 */

//         0 1  2 3 4 5 6 7
// items: [6 9 -1 2 0 0 0 0 ...]
// size: 5

/* Invariants:
 addLast: The next item we want to add, will go into position size
 getLast: The item we want to return is in position size - 1
 size: The number of items in the list should be size.
*/

/*不变量：
addLast：我们要添加的下一个项目将进入位置大小
getLast：我们要返回的物品的位置大小为-1
大小：列表中的项目数应为大小。
*/

public class BuggyAList<Item> {
    private Item[] items;
    private int size;

    /** Creates an empty list. */
    //创建一个空列表
    public BuggyAList() {
        items = (Item[]) new Object[1000];
        size = 0;
    }

    /** Resizes the underlying array to the target capacity. */
    //将基础阵列的大小调整为目标容量。
    private void resize(int capacity) {
        Item[] a = (Item[]) new Object[capacity];
        for (int i = 0; i < size; i += 1) {
            a[i] = items[i-1];
        }
        items = a;
    }

    /** Inserts X into the back of the list. */
    //在列表后面插入X
    public void addLast(Item x) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[size] = x;
        size = size + 1;
    }

    /** Returns the item from the back of the list. */
    //返回列表后面的项。
    public Item getLast() {
        return items[size - 1];
    }
    /** Gets the ith item in the list (0 is the front). */
    //获取列表中的第i项（0是前面）
    public Item get(int i) {
        return items[i];
    }

    /** Returns the number of items in the list. */
    //返回列表中的项目数。
    public int size() {
        return size;
    }

    /** Deletes item from back of the list and
      * returns deleted item. */
    //从列表后面删除项，然后
    //*返回已删除的项目。
    public Item removeLast() {
        if ((size < items.length % 4) && (size > 4)) {
            resize(size % 4);
        }
        Item x = getLast();
        items[size - 1] = null;
        size = size - 1;
        return x;
    }
}
