package gitlet;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
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
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static void add(Blob blob) {
        boolean flg2 = false;

        List<String> addStageList = Utils.plainFilenamesIn(Repository.ADD_STAGE);
        if (!addStageList.contains(blob.blobId())) {
            flg2 = true;
        }
        List<String> removeStageList = Utils.plainFilenamesIn(Repository.REMOVE_STAGE);
        if (removeStageList != null && !removeStageList.isEmpty()) {
            for (String str : removeStageList) {
                Blob blob1 = Blob.fromFile(str);
                if (blob1.getFileName().equals(blob.getFileName())) {
                    File rmAddStageFile1 = join(Repository.REMOVE_STAGE, str);
                    Repository.createNewFile(rmAddStageFile1);
                    NotherUtils.rm(rmAddStageFile1);
                    flg2 = false;
                }
            }
        }
        //flg2为true才创建
        if (flg2) {
            File rmAddStageFile2 = join(Repository.ADD_STAGE, blob.blobId());
            Utils.writeObject(rmAddStageFile2, blob.blobId());
            Repository.createNewFile(rmAddStageFile2);
        }
    }

    public static void remove(Blob removeBlob) {
        Blob blob = removeBlob;
        Commit parentCommit = getHeadBranchCommitId();
        List<String> addStageList = Utils.plainFilenamesIn(Repository.ADD_STAGE);
        //获取相对路径的value
        String trackBlobId = parentCommit.getTracked().get(blob.getFilePath());
        List<String> removeStageList = Utils.plainFilenamesIn(Repository.REMOVE_STAGE);
        File newFile = Paths.get(blob.getFileName().getName()).isAbsolute()
                ? new File(blob.getFileName().getName())
                : join(Repository.CWD, blob.getFileName().getName());
        Repository.createNewFile(newFile);
        if (trackBlobId != null) {
            //blobid相等 commit有引用，添加到removeStage 删除目录中的文件
            //removeStage不包含直接添加
            if (!removeStageList.contains(blob.blobId())) {
                File rmAddStageFile2 = join(Repository.REMOVE_STAGE, blob.blobId());
                Utils.writeObject(rmAddStageFile2, blob.blobId());
                Repository.createNewFile(rmAddStageFile2);
                NotherUtils.rm(newFile);
            }
        }
    }

    public static Map<String, String> commit(Map<String, String> tracked) {
        //遍历addStage,将blob的文件名和blobId做出hashMap进行映射
        List<String> addStageList = Utils.plainFilenamesIn(Repository.ADD_STAGE);
        Map<String, String> parentTracked = tracked;
        if ((Repository.ADD_STAGE.exists())) {
            for (String addStageFile : addStageList) {
                //根据blobid直接创建bolb文件
                Blob blobFile = Blob.fromFile(addStageFile);
                //增加缓存去的blobId添加到tracked
                parentTracked.put(blobFile.getFilePath(), blobFile.getId());
                File addFile = join(Repository.ADD_STAGE, addStageFile);
                //删除addStage下的暂存文件
                NotherUtils.rm(addFile);
            }
        }
        //如果删除区存在
        List<String> removeStageList = Utils.plainFilenamesIn(Repository.REMOVE_STAGE);
        if ((Repository.REMOVE_STAGE.exists())) {
            for (String str : removeStageList) {
                File removeFile = join(Repository.REMOVE_STAGE, str);
                //创建bolb文件
                Blob blobFile = Blob.fromFile(str);
                if (parentTracked != null) {
                    parentTracked.remove(blobFile.getFilePath());
                    //删除removeStage下的暂存文件
                    //NotherUtils.rm(removeFile);
                }
            }
        }
        return parentTracked;
    }

    public static String getConflictContent(String currentBlobId, String targetBlobId) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("<<<<<<< HEAD").append("\n");
        if (currentBlobId != null) {
            Blob currentBlob = Blob.fromFile(currentBlobId);
            contentBuilder.append(getBytes(currentBlob.getBytes()));
        }
        contentBuilder.append("=======").append("\n");
        if (targetBlobId != null) {
            Blob targetBlob = Blob.fromFile(targetBlobId);
            contentBuilder.append(getBytes(targetBlob.getBytes()));
        }
        contentBuilder.append(">>>>>>>").append("\n");
        return contentBuilder.toString();
    }

}



