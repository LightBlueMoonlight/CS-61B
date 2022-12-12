package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static gitlet.Utils.*;


public class Repository implements Serializable {

    /**
     * 当前工作目录
     */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * .gitlet目录
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File OBJECTS = join(GITLET_DIR, "objects");

    public static final File REFS = join(GITLET_DIR, "refs");

    public static final File HEADS = join(REFS, "heads");

    public static final File HEAD = join(GITLET_DIR, "HEAD");

    public static final File ADD_STAGE = join(GITLET_DIR, "addstage");

    private static final String MASTER = "master";

    /*
     *   .gitlet
     *      |--objects
     *      |     |--commit and blob
     *      |--refs
     *      |    |--heads
     *      |         |--master
     *      |--HEAD
     *      |--addstage
     *      |--removestage
     */
    //初始化.git
    public static void setInit() {
        //如果当前目录下没有存储库就创建.gitlet,有就报错
        if (GITLET_DIR.exists()) {
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        //创建.gitlet目录
        GITLET_DIR.mkdir();
        //创建objects目录 用来存储commit和blob
        OBJECTS.mkdir();
        //创建refs目录
        REFS.mkdir();
        //stage   #保存暂存区信息，在执行git init 的时候，这个文件还没有
        ////创建默认的master分支 在HEAD目录记录master分支
        Commit initCommit = new Commit();
        Utils.writeObject(HEAD, initCommit.getCommitID());
        makeBranch(MASTER,initCommit.getCommitID());
    }

    //在heads文件夹内存有多个文件，每个文件的名字即为分支名字
    /*
     *      |--refs
     *          |--heads
     *               |--master
     *               |--61abc
     */
    private static void makeBranch(String branch ,String commit) {
        File MASTERFILE = join(HEADS, branch);
        Utils.writeObject(MASTERFILE,commit);
    }

    /**
     * 添加操作
     * add操作会将所有未存储的文件以blob的形式存起来
     * 注意，所有static标签标记的变量都不会被存储，所以声明变量时不要加static
     * 除此之外，所有存储起来的文件名以及对应的blobID都要写入到addstage中，以便于下次commit的时候读取
     * @param
     */
    public static void setAdd(String addFile) {
        File newFile = new File(addFile);
        //判断添加的文件是否存在工作目录中，不存在则报错
        if (!newFile.exists()) {
            NotherUtils.message("File does not exist.");
        }
        //判断添加暂存区是否存在，不存在就创建
        if (!ADD_STAGE.exists()) {
            //创建addStage文件目录
            ADD_STAGE.mkdir();
        }
        //直接创建bolb文件
        Blob blob = new Blob(newFile);
        //objects不包含add的文件，则将add文件写入
        containsBlob(OBJECTS, blob);
        //addStatge不包含add的文件，则将add文件写入
        containsBlob(ADD_STAGE, blob);
    }

    /*
     *   .gitlet
     *      |--objects
     *      |--HEAD
     *   a.txt
     */
    //检查运行的命令是否在.gitlet目录同级下运行，比如a.txt就和.gitlet是同级.或者是在.gitlet下运行
    public static void checkDir(){
        if (!GITLET_DIR.exists()) {
            NotherUtils.message("Not in an initialized Gitlet directory.");
        }
    }

    //判断目录下是否包含bilb文件，不包含则创建
    public static void containsBlob(File fileName, Blob blob){
        List<String> list = Utils.plainFilenamesIn(fileName);
        String bolbString = Utils.readContentsAsString(blob.getFileName());
        if (!list.contains(bolbString)){
            blob.makeBlobFile();
        }
    }

    //comit时的操作
    public static void setCommit(String message) {
        //查看添加暂存区下目录
        List<String> addStageList = Utils.plainFilenamesIn(ADD_STAGE);
        //查看删除暂存区下目录
        List<String> removeStageList = Utils.plainFilenamesIn(ADD_STAGE);
        //判断暂存区是否存在，或为空
        if ((!ADD_STAGE.exists() || addStageList.size()==0)) {
            //报错
            NotherUtils.message("No changes added to the commit.");
        }
        //获取head目录下的master的分支
        List<String> headList = Utils.plainFilenamesIn(HEAD);

        //遍历addStage,将blob的文件名和blobId做出hashMap进行映射
        Map<String, String> tracked = new HashMap<>();
        for (String addStageFile : addStageList){
            File addFile = new File(addStageFile);
            //创建bolb文件
            Blob blob = new Blob(addFile);
            //将addFile的全部内容作为字符串返回。
            String addFileString = Utils.readContentsAsString(blob.getBlobSaveFileName());
            tracked.put(Blob.getBlobId(blob.getBlobSaveFileName()),addFileString);
        }
        //清空addStage
        addStageList.removeAll(addStageList);
        //遍历addStage,将blob的文件名和blobId做出hashMap进行映射
        File masterFile = new File(HEAD.getPath()+MASTER);
        String addFileString = Utils.readContentsAsString(masterFile);
        List<String> commitIdParent = new ArrayList<>();
        commitIdParent.add(addFileString);
        //创建新的commit
        Commit newCommit = new Commit(message,commitIdParent,tracked);
        //commit成功后要删除暂存区中的文件
    }

}
