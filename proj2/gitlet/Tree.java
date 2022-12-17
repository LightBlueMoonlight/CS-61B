package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

//目录结构将名称映射到对 blob 和其他树（子目录）的引用
public class Tree implements Serializable {
    private Map<String, String> tree = new HashMap<>();

    public Tree() {
        this.tree = tree;
    }

}
