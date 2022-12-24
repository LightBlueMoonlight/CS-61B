package gitlet;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.join;


public class NotherUtils {
    /**
     * 输出报错信息并退出
     */
    public static void message(String mesage) {
        Utils.message(mesage);
        System.exit(0);
    }

    public static void clearFile(File file)  {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void rm(File file) {
        if (!file.delete()) {
            throw new IllegalArgumentException
            (String.format("rm: %s: Failed to delete.", file.getPath()));
        }
    }

    public static Commit getHeadBranchCommitId() {
        //读取HEAD下的分支 例如：master
        String headFileString = Utils.readContentsAsString(Repository.HEAD);
        //因为分支都在heads下，所以用HEAD读取到的分支名做一个拼接，用来读取当前分支下的内容
        File headBranch = join(Repository.HEADS, headFileString);
        //读取headBranch下的内容
        String headBranchText = Utils.readContentsAsString(headBranch);
        //根据commitId生成commit文件
        Commit parentCommit = Commit.fromFile(headBranchText);
        return parentCommit;
    }

    public static Commit getBranch(String branch) {
        File headBranch = join(Repository.HEADS, branch);
        //读取headBranch下的内容
        String headBranchText = Utils.readContentsAsString(headBranch);
        //根据commitId生成commit文件
        Commit parentCommit = Commit.fromFile(headBranchText);
        return parentCommit;
    }

    public static String getBytes(byte[] bytes) {
        String aa = "aaaaaaa";
        try {
            aa = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) { //有可能会出现不能支持的编码格式，捕捉异常。
            e.printStackTrace();
        }
        return aa;
    }

    static final int UID_LENGTH = 40;

    public static boolean isHeadBranch() {
        //查看添加暂存区下目录
        List<String> addStageList = Utils.plainFilenamesIn(Repository.ADD_STAGE);
        //查看删除暂存区下目录
        List<String> removeStageList = Utils.plainFilenamesIn(Repository.REMOVE_STAGE);
        boolean flag = addStageList == null || addStageList.isEmpty();
        boolean flag2 = removeStageList == null || removeStageList.isEmpty();

        //判断暂存区是否存在，或为空
        if (flag && flag2) {
            //报错
            return true;
        }
        return false;
    }

    public static String getKey(Map<String, String> map, String value) {
        List<String> list = new ArrayList<>();
        for (String key : map.keySet()) {
            if (map.get(key).equals(value)) {
                list.add(key);
            }
        }
        return list.get(0);
    }

}



