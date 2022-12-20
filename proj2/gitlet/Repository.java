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
        Utils.writeContents(masterFile, commit);
        if (!masterFile.exists()) {
            createNewFile(masterFile);
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
        //如果file和当前commit中跟踪的文件相同（blob的hashCode相同），则不将其添加到staging中
        //当前文件有被commit引用
        if (trackBlobId != null) {
            if (trackBlobId.equals(blob.getId())) {
                //删除目录下的add文件
                NotherUtils.rm(newFile);
            }

            if (!trackBlobId.equals(blob.getId())) {
                NotherUtils.addStageFile(newFile,ADD_STAGE,blob);
            }
        }
        //当前文件没有被commit引用
        if (trackBlobId == null){
            NotherUtils.addStageFile(newFile,ADD_STAGE,blob);
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
        Map<String, String> parentTracked = parentCommit.getTracked();
        System.out.println("parentTracked0"+parentTracked);
        System.out.println("addStageList"+addStageList);
        if ((ADD_STAGE.exists())) {
            for (String addStageFile : addStageList) {
                //根据blobid直接创建bolb文件
                System.out.println("addStageFile"+addStageFile);
                Blob blobFile = Blob.fromFile(addStageFile);
                //增加缓存去的blobId添加到tracked
                parentTracked.put(blobFile.getFilePath(), blobFile.getId());
                System.out.println("blobFile.getId()"+blobFile.getId());
                System.out.println("blobFile.getFilePath()"+blobFile.getFilePath());
                File addFile = join(ADD_STAGE, addStageFile);
                //删除addStage下的暂存文件
                NotherUtils.rm(addFile);
            }
        }
        System.out.println("parentTracked1"+parentTracked);
        System.out.println("removeStageList"+removeStageList);
        //如果删除区存在
        if ((REMOVE_STAGE.exists())) {
            for (String str : removeStageList) {
                File removeFile = join(REMOVE_STAGE, str);
                //创建bolb文件
                Blob blobFile = Blob.fromFile(str);
                parentTracked.remove(blobFile.getId());
                //删除addStage下的暂存文件
                NotherUtils.rm(removeFile);
            }
        }
        System.out.println("parentTracked2"+parentTracked);
        List<String> list = parentCommit.getParent();

        list.add(parentCommit.getCommitID());
        //创建新的commit
        Commit newCommit = new Commit(message, list, parentTracked);
        //先删除在创建
        NotherUtils.rm(headBranch);
        //重新创建
        File newHeadBranch = join(HEADS, headFileString);
        //将新生成的commitId在写入head
        Utils.writeObject(newHeadBranch, newCommit.getCommitID());
        createNewFile(newHeadBranch);
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
        //读取HEAD下分支名
        String addFileString = Utils.readContentsAsString(HEAD);
        System.out.println("addFileString"+addFileString);
        //因为分支都在heads下，所以用HEAD读取到的分支名做一个拼接，用来读取当前分支下的内容
        File headBranch = join(HEADS, addFileString);
        //读取headBranch下的内容
        String headBranchText = Utils.readContentsAsString(headBranch);
        System.out.println("headBranchText"+headBranchText);
        printLog(headBranchText);
    }

    private static void printLog(String addFileString) {
        //根据commitId生成commit文件
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
            NotherUtils.message("Found no commit with that message.");
        }
    }

    public static void setBranch(String branch) {
        //如果具有给定名称的分支已经存在，则打印错误消息A branch with that name already exists.
        List<String> branchList = Utils.plainFilenamesIn(HEADS);

        if (branchList.contains(branch)) {
            NotherUtils.message("A branch with that name already exists.");
        }
        String head = Utils.readContentsAsString(HEAD);
        //因为分支都在heads下，所以用HEAD读取到的分支名做一个拼接，用来读取当前分支下的内容
        File headBranch = join(HEADS, head);
        //读取headBranch下的内容
        String headBranchText = Utils.readContentsAsString(headBranch);
        //根据commitId生成commit文件
        Commit parentCommit = Commit.fromFile(headBranchText);
        File newBranch = join(HEADS, branch);
        Utils.writeContents(newBranch,parentCommit.commitId());
        createNewFile(newBranch);

    }

    public static void setStatus() {
        Utils.message("=== Branches ===");
        //读取HEAD下的分支 例如：master
        String headFileString = Utils.readContentsAsString(HEAD);
        List<String> branchList = Utils.plainFilenamesIn(HEADS);
        for (String branchName : branchList){
            if (branchName.equals(headFileString)){
                Utils.message("*" + branchName);
                continue;
            }
            Utils.message(branchName);
        }
        Utils.message("=== Staged Files ===");
        List<String> addStageList = Utils.plainFilenamesIn(ADD_STAGE);
        System.out.println("addStageList"+addStageList);
        if (addStageList !=null){
            for (String branchName : addStageList){
                //根据blobId还原blob文件
                Blob blobFromFile = Blob.fromFile(branchName);
                System.out.println("getId()"+blobFromFile.getId());
                System.out.println("getFilePath()"+blobFromFile.getFilePath());
                System.out.println("getFileName()"+blobFromFile.getFileName());
                System.out.println("getBlobSaveFileName()"+blobFromFile.getBlobSaveFileName());
                Utils.message(blobFromFile.getFileName().getName());
            }
        }
        System.out.println();
        Utils.message("=== Removed Files ===");
        List<String> removeStageList = Utils.plainFilenamesIn(REMOVE_STAGE);
        if (removeStageList !=null){
            for (String branchName : removeStageList){
                //根据blobId还原blob文件
                Blob blobFromFile = Blob.fromFile(branchName);
                Utils.message(blobFromFile.getFileName().getName());
            }
        }
        System.out.println();
        Utils.message("=== Modifications Not Staged For Commit ===");
        System.out.println();
        Utils.message("=== Untracked Files ===");
        System.out.println();
    }

    public static void setRmBranch(String text) {
        List<String> branchList = Utils.plainFilenamesIn(HEADS);
        if (!branchList.contains(text)) {
            NotherUtils.message("A branch with that name does not exist.");
        }
        String head = Utils.readContentsAsString(HEAD);
        if (head.equals(text)){
            NotherUtils.message("Cannot remove the current branch.");
        }
        File headBranch = join(HEADS, text);
        NotherUtils.rm(headBranch);
    }

}