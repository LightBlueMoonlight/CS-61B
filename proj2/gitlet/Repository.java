package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
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

    public static final File COMMIT = join(OBJECTS, "commit");

    public static final File BLOB = join(OBJECTS, "blob");

    public static final File REFS = join(GITLET_DIR, "refs");

    public static final File HEADS = join(REFS, "heads");

    public static final File HEAD = join(GITLET_DIR, "HEAD");

    public static final File ADD_STAGE = join(GITLET_DIR, "addStage");

    public static final File REMOVE_STAGE = join(GITLET_DIR, "removeStage");

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
            Utils.message("A Gitlet version-control system already exists "
                    + "in the current directory.");
            System.exit(0);
        }
        //创建.gitlet目录
        GITLET_DIR.mkdir();
        //创建objects目录 用来存储commit和blob
        OBJECTS.mkdir();
        COMMIT.mkdir();
        BLOB.mkdir();
        //创建refs目录
        REFS.mkdir();
        HEADS.mkdir();
        ////创建默认的master分支 在HEAD目录记录master分支
        Commit initCommit = new Commit();
        createNewFile(initCommit.getFile());
        //CommitID存储在master分支下
        makeBranch(MASTER, initCommit.getCommitID());
        //HEAD存储master的分支名
        Utils.writeContents(HEAD, MASTER);
    }

    //在heads文件夹内存有多个文件，每个文件的名字即为分支名字
    /*
     *      |--refs
     *          |--heads
     *               |--master
     *               |--61abc
     */
    private static void makeBranch(String branch, String commit) {
        File masterFile = join(HEADS, branch);
        if (!masterFile.exists()) {
            createNewFile(masterFile);
            //尝试
            Utils.writeContents(masterFile, commit);
        }
    }

    public static void createNewFile(File newFile) {
        try {
            File fileParent = newFile.getParentFile();
            if (!fileParent.exists()){
                fileParent.mkdir();
            }
            newFile.createNewFile();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage() + "<<:<<" + excp.toString()
                    + "<<:<<" + excp.getClass().toString() + "<<:<<" + excp.getLocalizedMessage());
        }
    }

    /**
     * 添加操作
     * add操作会将所有未存储的文件以blob的形式存起来
     * 注意，所有static标签标记的变量都不会被存储，所以声明变量时不要加static
     * 除此之外，所有存储起来的文件名以及对应的blobID都要写入到addstage中，以便于下次commit的时候读取
     *
     * @param
     */
    public static void setAdd(String addFile) {
        File newFile = Paths.get(addFile).isAbsolute()
                ? new File(addFile)
                : join(CWD, addFile);
        //判断添加的文件是否存在工作目录中，不存在则报错
        if (!newFile.exists()) {
            NotherUtils.message("File does not exist.");
        }

        //读取HEAD下的分支 例如：master
        String headFileString = Utils.readContentsAsString(HEAD);
        //因为分支都在heads下，所以用HEAD读取到的分支名做一个拼接，用来读取当前分支下的内容
        File headBranch = join(HEADS, headFileString);
        //读取headBranch下的内容
        String headBranchText = Utils.readContentsAsString(headBranch);
        //根据commitId生成commit文件
        Commit parentCommit = Commit.fromFile(headBranchText);
        //如果addStage目录不存在就创建
        if (!ADD_STAGE.exists()){
            ADD_STAGE.mkdir();
        }
        //更据添加文件名创建bolb文件
        Blob blob = new Blob(newFile);
        //如果file和当前commit中跟踪的文件相同（blob的hashCode相同），则不将其添加到staging中
        //Tracked的键值对是相对路径--blobId
        //获取相对路径的value
        String trackBlobId = parentCommit.getTracked().get(blob.getFilePath());
        //不为null说明当前commit文件包含当前添加blob文件路径
        if (trackBlobId != null){
            //blobid相等就不用添加了，删除目录下的添加文件
            if (trackBlobId.equals(blob.blobId())){
                //删除目录下的add文件
                restrictedDelete(newFile);
            }else {
                //blobid不相等就等commit下替换相对路径的value。因为还没commit，所以放到暂存区
                //遍历addStage下的blob，如果有相同的相对路径，则删除掉之前的blob，替换成当前的blob
                List<String> addStageList = Utils.plainFilenamesIn(ADD_STAGE);
                //遍历addStage中的文件与当前添加的文件做比较
                for (String str : addStageList){
                    //根据blobId还原blob文件
                    Blob blobFromFile = Blob.fromFile(str);
                    //如果addStage里的相对路径等于添加文件的相对路径
                    if (blobFromFile.getFilePath().equals(newFile.getPath())){
                        //获取之前addStage的文件名
                        File rmAddStageFile = join(ADD_STAGE,str);
                        //删除之前的blob文件
                        restrictedDelete(rmAddStageFile);
                        //当前blob添加到addStage目录
                        File rmAddStageFile2 = join(ADD_STAGE,blob.blobId());
                        Utils.writeObject(rmAddStageFile2, blob.blobId());
                        createNewFile(rmAddStageFile2);
                    }
                }
                //删除目录下的add文件
                restrictedDelete(newFile);
            }
        }else{
            //遍历addStage下的blob，如果有相同的相对路径，则删除掉之前的blob，替换成当前的blob
            List<String> addStageList = Utils.plainFilenamesIn(ADD_STAGE);
            //不包含则将其添加到staging中
            for (String str : addStageList){
                //根据blobId还原blob文件
                Blob blobFromFile = Blob.fromFile(str);
                //如果addStage里的相对路径等于添加文件的相对路径
                if (blobFromFile.getFilePath().equals(newFile.getPath())){
                    //获取之前addStage的文件名
                    File rmAddStageFile = join(ADD_STAGE,str);
                    //删除之前的blob文件
                    restrictedDelete(rmAddStageFile);
                    //当前blob添加到addStage目录
                    File rmAddStageFile2 = join(ADD_STAGE,blob.blobId());
                    Utils.writeObject(rmAddStageFile2, blob.blobId());
                    createNewFile(rmAddStageFile2);
                }
            }
        }
    }

    /*
     *   .gitlet
     *      |--objects
     *      |--HEAD
     *   a.txt
     */
    //检查运行的命令是否在.gitlet目录同级下运行，比如a.txt就和.gitlet是同级.或者是在.gitlet下运行
    public static void checkDir() {
        if (!GITLET_DIR.exists()) {
            NotherUtils.message("Not in an initialized Gitlet directory.");
        }
    }

    //判断目录下是否包含bilb文件，不包含则创建
    public static void containsBlob(File fileName, Blob blob) {
        if (!fileName.exists()) {
            fileName.mkdir();
        }
        //获取目录下所有文件名
        List<String> list = Utils.plainFilenamesIn(fileName);
        //要在blob目录中创建文件
        createNewFile(blob.getBlobSaveFileName());
        String bolbString = blob.getId();
        if (!list.contains(bolbString)) {
            File saveFile = Utils.join(fileName, bolbString);
            createNewFile(saveFile);
            List<String> list2 = Utils.plainFilenamesIn(fileName);
            System.out.println(fileName.getPath() + "<<<<<:" + list2);
        }
    }

    //comit时的操作
    public static void setCommit(String message) {
        //查看添加暂存区下目录
        List<String> addStageList = Utils.plainFilenamesIn(ADD_STAGE);
        //查看删除暂存区下目录
        List<String> removeStageList = Utils.plainFilenamesIn(REMOVE_STAGE);
        //判断暂存区是否存在，或为空
        if ((!ADD_STAGE.exists() || addStageList == null)
                && (!REMOVE_STAGE.exists() || removeStageList == null)) {
            //报错
            NotherUtils.message("No changes added to the commit.");
        }
        //读取HEAD下的分支 例如：master
        String headFileString = Utils.readContentsAsString(HEAD);
        //因为分支都在heads下，所以用HEAD读取到的分支名做一个拼接，用来读取当前分支下的内容
        File headBranch = join(HEADS, headFileString);
        //读取headBranch下的内容
        String headBranchText = Utils.readContentsAsString(headBranch);
        //根据commitId生成commit文件
        Commit parentCommit = Commit.fromFile(headBranchText);


        //遍历addStage,将blob的文件名和blobId做出hashMap进行映射
        Map<String, String> tracked = new HashMap<>();
        Map<String, String> parentTracked = parentCommit.getTracked();
        if ((ADD_STAGE.exists())) {
            for (String addStageFile : addStageList) {
                //根据blobid直接创建bolb文件
                Blob blobFile = Blob.fromFile(addStageFile);
                //增加缓存去的blobId添加到tracked
                parentTracked.put(blobFile.getFilePath(), blobFile.getId());
                File addFile = join(ADD_STAGE, addStageFile);
                //删除addStage下的暂存文件
                Utils.restrictedDelete(addFile);
            }
        }
        //如果删除区存在
        if ((REMOVE_STAGE.exists())) {
            for (String str : removeStageList) {
                File removeFile = join(REMOVE_STAGE, str);
                //创建bolb文件
                Blob blobFile = Blob.fromFile(str);
                parentTracked.remove(blobFile.getId());
                //删除addStage下的暂存文件
                Utils.restrictedDelete(removeFile);
            }
        }
        List<String> list = parentCommit.getParent();
        list.add(parentCommit.getCommitID());
        //创建新的commit
        Commit newCommit = new Commit(message, list, parentTracked);
        //先删除在创建
        Utils.restrictedDelete(headBranch);
        //重新创建
        File newHeadBranch = join(HEADS, headFileString);
        createNewFile(newHeadBranch);
        //将新生成的commitId在写入head
        System.out.println("将新生成的commitId在写入head");
        Utils.writeObject(newHeadBranch, newCommit.getCommitID());
        String newHeadBranchText = Utils.readContentsAsString(newHeadBranch);
        System.out.println("开始写入:" + newHeadBranchText);
    }

    public static void setRM(String removeFile) {
        File newFile = Paths.get(removeFile).isAbsolute()
                ? new File(removeFile)
                : join(CWD, removeFile);
        //读取HEAD下的分支 例如：master
        String headFileString = Utils.readContentsAsString(HEAD);
        //因为分支都在heads下，所以用HEAD读取到的分支名做一个拼接，用来读取当前分支下的内容
        File headBranch = join(HEADS, headFileString);
        //读取headBranch下的内容
        String headBranchText = Utils.readContentsAsString(headBranch);
        //根据commitId生成commit文件
        Commit parentCommit = Commit.fromFile(headBranchText);
        //判断删除暂存区是否存在，不存在就创建
        if (!REMOVE_STAGE.exists()) {
            //创建removeStage文件目录
            REMOVE_STAGE.mkdir();
        }
        //如果文件在stage for add区域，则将其中缓存区删除；
        //如果文件被当前commit跟踪，则将其存入stage for removal区域。如果该文件存在于工作目录
        //更据删除文件名创建bolb文件
        Blob blob = new Blob(newFile);
        //如果file和当前commit中跟踪的文件相同（blob的hashCode相同），则将其添加到removeStaging中
        //Tracked的键值对是相对路径--blobId
        //读取添加暂存区
        boolean flg = true;
        List<String> addStageList = Utils.plainFilenamesIn(ADD_STAGE);
        //遍历addStage中的文件与当前添加的文件做比较
        for (String str : addStageList){
            //如果addStage里的相对路径等于添加文件的相对路径
            if (str.equals(blob.getId()) ){
                //当前blob添加到removeStaging目录
                File rmAddStageFile2 = join(REMOVE_STAGE,blob.blobId());
                createNewFile(rmAddStageFile2);
                if (newFile.exists()){
                    //删除目录下的remove文件
                    restrictedDelete(newFile);
                }
                flg = false;
            }
        }
        //获取相对路径的value
        String trackBlobId = parentCommit.getTracked().get(blob.getFilePath());
        //不为null说明当前commit文件包含当前删除blob文件路径
        if (trackBlobId != null){
            //blobid相等就不用添加了，删除目录下的添加文件
            if (trackBlobId.equals(blob.blobId())){
                //当前blob添加到removeStaging目录
                File rmAddStageFile2 = join(REMOVE_STAGE,blob.blobId());
                createNewFile(rmAddStageFile2);
                if (newFile.exists()){
                    //删除目录下的remove文件
                    restrictedDelete(newFile);
                }
                flg = false;
            }
        }
        //如果文件既没有被 暂存也没有被 head commit跟踪，打印错误信息No reason to remove the file.
        if (flg) {
            NotherUtils.message("No reason to remove the file.");
        }
    }

    public static void setLog() {
        //读取HEADcommit
        String addFileString = Utils.readContentsAsString(HEAD);
        printLog(addFileString);
    }

    private static void printLog(String addFileString) {
        Commit parentCommit = Commit.fromFile(addFileString);
        System.out.printf("===");
        System.out.printf("commit " + parentCommit.commitId());
        //对于合并提交（那些有两个父提交的提交），在第一个提交的正下方添加一行
        if (parentCommit.getParent().size() == 2) {
            Commit parentCommit2 = Commit.fromFile(parentCommit.getParent().get(1));
            //Merge:”后面的两个十六进制数字由第一个和第二个父项的提交 ID 的前七位组成
            System.out.printf("Merge: " + parentCommit.commitId().substring(0, 7) + " "
                    + parentCommit2.commitId().substring(0, 7));
        }
        System.out.printf("Date: " + parentCommit.getDate());
        System.out.printf(parentCommit.getMessage());
        System.out.println();
        if (parentCommit.getParent().size() != 0) {
            printLog(parentCommit.getParent().get(0));
        }
    }

    public static void setGlobalLog() {
        List<String> commitList = Utils.plainFilenamesIn(COMMIT);
        //打印所有的Commit而不关心顺序
        for (String str : commitList) {
            printLog(str);
        }
    }

    public static void setFind(String message) {
        List<String> commitList = Utils.plainFilenamesIn(COMMIT);
        //默认存在此信息
        boolean flg = true;
        for (String str : commitList) {
            Commit parentCommit = Commit.fromFile(str);
            if (message.equals(parentCommit.getMessage())) {
                Utils.message(parentCommit.commitId());
                flg = false;
            }
        }
        if (flg) {
            Utils.message("Found no commit with that message.");
        }
    }

    public static void setBranch(String branch) {
        //如果具有给定名称的分支已经存在，则打印错误消息A branch with that name already exists.
        List<String> branchList = Utils.plainFilenamesIn(HEADS);
        if (branchList.contains(branch)) {
            NotherUtils.message("A branch with that name already exists.");
        }
/**
        //将HEAD全部内容作为字符串返回。其实也就是前一个返回
        String headString = Utils.readContentsAsString(HEAD);
        File addFile = join(ADD_STAGE, addStageFile);
        //创建bolb文件
        Blob blob = new Blob(addFile);
        //将blobId和相对
        tracked.put(Blob.getBlobId(blob.getBlobSaveFileName()), blob.getBlobSaveFileName().getPath()
        //创建新的commit
        Commit newCommit = new Commit(message, parentCommit.getParent(), tracked);
        //读取父commit
        makeBranch(branch, headString);*/
    }
}

