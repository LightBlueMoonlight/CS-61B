package gitlet;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.readObject;

//目录结构将名称映射到对 blob 和其他树（子目录）的引用
public class Tree implements Serializable {
    private Map<String, String> tree = new HashMap<>();

    public Tree() {
        this.tree = tree;
    }

    public static void main(String[] args) throws IOException {
        File file = new File("D:\\WordRqmErrors.log");
        System.out.println(file.getPath());
        //readObject(file, Commit.class);
    }

}
