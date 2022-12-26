package gitlet;
import jdk.swing.interop.SwingInterOpUtils;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.*;
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
            if (!fileParent.exists()) {
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
        //更据添加文件名创建bolb文件
        Blob blob = new Blob(newFile);
        String trackBlobId = parentCommit.getTracked().get(blob.getFilePath());
        //如果addStage目录不存在就创建
        if (!ADD_STAGE.exists()) {
            ADD_STAGE.mkdir();
        }
        boolean flg = true;
        boolean flg2 = false;
        if (trackBlobId != null) {
            if (trackBlobId.equals(blob.getId())) {
                flg = false;
                if (newFile.exists()) {
                    NotherUtils.rm(newFile);
                }
            }
        }
        //查看添加暂存区下目录
        List<String> addStageList = Utils.plainFilenamesIn(ADD_STAGE);
        if (!addStageList.contains(blob.blobId()) && flg) {
            flg2 = true;
        }
        List<String> removeStageList = Utils.plainFilenamesIn(REMOVE_STAGE);
        if (removeStageList != null && !removeStageList.isEmpty()) {
            for (String str : removeStageList) {
                Blob blob1 = Blob.fromFile(str);
                if (blob1.getFileName().equals(blob.getFileName())) {
                    File rmAddStageFile1 = join(REMOVE_STAGE, str);
                    createNewFile(rmAddStageFile1);
                    NotherUtils.rm(rmAddStageFile1);
                    flg2 = false;
                }
            }
        }
        //flg2为true才创建
        if (flg2) {
            File rmAddStageFile2 = join(ADD_STAGE, blob.blobId());
            Utils.writeObject(rmAddStageFile2, blob.blobId());
            createNewFile(rmAddStageFile2);
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
        //如果addStage目录不存在就创建
        if (!ADD_STAGE.exists()) {
            ADD_STAGE.mkdir();
        }
        if (!REMOVE_STAGE.exists()) {
            REMOVE_STAGE.mkdir();
        }
        //暂存区为空
        if (NotherUtils.isHeadBranch()) {
            NotherUtils.message("No changes added to the commit.");
        }
        //查看添加暂存区下目录
        List<String> addStageList = Utils.plainFilenamesIn(Repository.ADD_STAGE);
        //查看删除暂存区下目录
        List<String> removeStageList = Utils.plainFilenamesIn(Repository.REMOVE_STAGE);
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
        if ((ADD_STAGE.exists())) {
            for (String addStageFile : addStageList) {
                //根据blobid直接创建bolb文件
                Blob blobFile = Blob.fromFile(addStageFile);
                //增加缓存去的blobId添加到tracked
                parentTracked.put(blobFile.getFilePath(), blobFile.getId());
                File addFile = join(ADD_STAGE, addStageFile);
                //删除addStage下的暂存文件
                NotherUtils.rm(addFile);
            }
        }
        //如果删除区存在
        if ((REMOVE_STAGE.exists())) {
            for (String str : removeStageList) {
                File removeFile = join(REMOVE_STAGE, str);
                //创建bolb文件
                Blob blobFile = Blob.fromFile(str);
                if (parentTracked != null) {
                    parentTracked.remove(blobFile.getFilePath());
                    //删除removeStage下的暂存文件
                    NotherUtils.rm(removeFile);
                }
            }
        }
        List<String> list = new ArrayList<>();
        list.add(parentCommit.getCommitID());
        //创建新的commit
        Commit newCommit = new Commit(message, list, parentTracked);
        //先删除在创建
        NotherUtils.rm(headBranch);
        //重新创建
        makeBranch(headFileString, newCommit.getCommitID());
    }
    public static void setRM(String removeFile) {
        File newFile = Paths.get(removeFile).isAbsolute()
                ? new File(removeFile)
                : join(CWD, removeFile);
        createNewFile(newFile);
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
        Blob blob = new Blob(newFile);
        //如果file和当前commit中跟踪的文件相同（blob的hashCode相同），则将其添加到removeStaging中
        //Tracked的键值对是相对路径--blobId
        //读取添加暂存区
        boolean flg = true;
        List<String> addStageList = Utils.plainFilenamesIn(ADD_STAGE);
        //获取相对路径的value
        String trackBlobId = parentCommit.getTracked().get(blob.getFilePath());
        List<String> removeStageList = Utils.plainFilenamesIn(REMOVE_STAGE);
        //文件刚被add进addstage而没有commit，直接删除addstage中的Blob就可以
        if (addStageList != null && !addStageList.isEmpty()) {
            for (String str : addStageList) {
                Blob blob1 = Blob.fromFile(str);
                if (blob1.getFileName().equals(blob.getFileName())) {
                    File rmAddStageFile1 = join(ADD_STAGE, str);
                    createNewFile(rmAddStageFile1);
                    NotherUtils.rm(rmAddStageFile1);
                    flg = false;
                }
            }
        }
        //文件被当前Commit追踪并且存在于工作目录中，那么就将及放入removestage并且在工作目录中删除此文件。在下次commit中进行记录。
        //文件被当前Commit追踪并且不存在于工作目录中，那么就将及放入removestage并即可
        //不为null说明当前commit文件包含当前删除blob文件路径
        if (trackBlobId != null) {
            //blobid相等 commit有引用，添加到removeStage 删除目录中的文件
            flg = false;
            //removeStage不包含直接添加
            if (!removeStageList.contains(blob.blobId())) {
                File rmAddStageFile2 = join(REMOVE_STAGE, blob.blobId());
                Utils.writeObject(rmAddStageFile2, blob.blobId());
                createNewFile(rmAddStageFile2);
                NotherUtils.rm(newFile);
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
        //因为分支都在heads下，所以用HEAD读取到的分支名做一个拼接，用来读取当前分支下的内容
        File headBranch = join(HEADS, addFileString);
        //读取headBranch下的内容
        String headBranchText = Utils.readContentsAsString(headBranch);
        printLog(headBranchText);
    }
    private static void printLog(String addFileString) {
        //根据commitId生成commit文件
        Commit parentCommit = Commit.fromFile(addFileString);
        Utils.message("===");
        Utils.message("commit " + parentCommit.commitId());
        //对于合并提交（那些有两个父提交的提交），在第一个提交的正下方添加一行
        if (parentCommit.getParent().size() > 1) {
            Commit parentCommit2 = Commit.fromFile(parentCommit.getParent().get(1));
            Commit parentCommit1 = Commit.fromFile(parentCommit.getParent().get(0));
            //Merge:”后面的两个十六进制数字由第一个和第二个父项的提交 ID 的前七位组成
            Utils.message("Merge: " + parentCommit1.commitId().substring(0, 7) + " "
                    + parentCommit2.commitId().substring(0, 7));
        }
        Utils.message("Date: " + parentCommit.getDate());
        Utils.message(parentCommit.getMessage());
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
        File newBranch = join(HEADS, branch);
        Utils.writeContents(newBranch, headBranchText);
        //createNewFile(newBranch);
    }
    public static void setStatus() {
        Utils.message("=== Branches ===");
        //读取HEAD下的分支 例如：master
        String headFileString = Utils.readContentsAsString(HEAD);
        List<String> branchList = Utils.plainFilenamesIn(HEADS);
        for (String branchName : branchList) {
            if (branchName.equals(headFileString)) {
                Utils.message("*" + branchName);
                continue;
            }
            Utils.message(branchName);
        }
        System.out.println();
        Utils.message("=== Staged Files ===");
        List<String> addStageList = Utils.plainFilenamesIn(ADD_STAGE);
        if (addStageList != null && !addStageList.isEmpty()) {
            for (String branchName : addStageList) {
                //根据blobId还原blob文件
                Blob blobFromFile = Blob.fromFile(branchName);
                Utils.message(blobFromFile.getFileName().getName());
            }
        }
        System.out.println();
        Utils.message("=== Removed Files ===");
        List<String> removeStageList = Utils.plainFilenamesIn(REMOVE_STAGE);
        if (removeStageList != null && !removeStageList.isEmpty()) {
            for (String branchName : removeStageList) {
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
        if (head.equals(text)) {
            NotherUtils.message("Cannot remove the current branch.");
        }
        File headBranch = join(HEADS, text);
        createNewFile(headBranch);
        NotherUtils.rm(headBranch);
    }
    public static void checkout(String fileName) {
        File newFile = Paths.get(fileName).isAbsolute()
                ? new File(fileName)
                : join(CWD, fileName);
        //createNewFile(newFile);
        //如果文件被当前commit所跟踪，则其放入工作目录中（如果工作目录中有同名文件，则替代它）；
        Commit parentCommit = NotherUtils.getHeadBranchCommitId();
        String trackBlobId = parentCommit.getTracked().get(newFile.getPath());
        if (trackBlobId != null) {
            List<String> cwdList = Utils.plainFilenamesIn(CWD);
            if (cwdList.contains(fileName)) {
                File rmAddStageFile2 = join(CWD, fileName);
                //createNewFile(rmAddStageFile2);
                NotherUtils.clearFile(rmAddStageFile2);
            }
            File newBranch = join(CWD, fileName);
            Blob blob = Blob.fromFile(trackBlobId);
            Utils.writeContents(newBranch, NotherUtils.getBytes(blob.getBytes()));
            createNewFile(newBranch);
        } else {
            NotherUtils.message("File does not exist in that commit.");
        }
    }
    public static void checkout(String commitId, String fileName) {
        File newFile = Paths.get(fileName).isAbsolute()
                ? new File(fileName)
                : join(CWD, fileName);
        createNewFile(newFile);
        List<String> commitList = Utils.plainFilenamesIn(COMMIT);
        if (!commitList.contains(commitId)) {
            NotherUtils.message("No commit with that id exists.");
        }
        Commit parentCommit = Commit.fromFile(commitId);
        String trackBlobId = parentCommit.getTracked().get(newFile.getPath());
        if (trackBlobId != null) {
            Blob blob = Blob.fromFile(trackBlobId);
            List<String> cwdList = Utils.plainFilenamesIn(CWD);
            if (cwdList.contains(fileName)) {
                File rmAddStageFile2 = join(CWD, fileName);
                createNewFile(rmAddStageFile2);
                NotherUtils.rm(rmAddStageFile2);
            }
            File newBranch = join(CWD, fileName);
            Utils.writeContents(newBranch, NotherUtils.getBytes(blob.getBytes()));
            createNewFile(newBranch);
        } else {
            NotherUtils.message("File does not exist in that commit.");
        }
    }
    public static void checkoutBranch(String branch) {
        //如果checked branch不存在，输出错误信息
        List<String> headsList = Utils.plainFilenamesIn(HEADS);
        if (!headsList.contains(branch)) {
            NotherUtils.message("No such branch exists.");
        }
        String headFileString = Utils.readContentsAsString(HEAD);
        if (headFileString.equals(branch)) {
            NotherUtils.message("No need to checkout "
                    + "the current branch.");
        }
        //未切换前的分支
        Commit parentCommit3A = NotherUtils.getHeadBranchCommitId();
        File newBranch = join(HEADS, branch);
        NotherUtils.clearFile(HEAD);
        Utils.writeContents(HEAD, branch);
        //newBranCh的commitID
        String commitId1 = Utils.readContentsAsString(newBranch);
        //newbranch
        Commit parentCommit3B = Commit.fromFile(commitId1);
        for (String key : parentCommit3A.getTracked().keySet()) {
            //被两个commit共同跟踪（用checked branch中的blobs覆写这些文件）
            if (parentCommit3B.getTracked().containsKey(key)) {
                Blob blob = Blob.fromFile(parentCommit3B.getTracked().get(key));
                File blobFile = new File(blob.getFilePath());
                if (blobFile.exists()) {
                    NotherUtils.rm(blobFile);
                }
                Utils.writeContents(blobFile, NotherUtils.getBytes(blob.getBytes()));
            } else {
                //仅被当前跟踪
                Blob blob3A = Blob.fromFile(parentCommit3A.getTracked().get(key));
                File blob3AFile = new File(blob3A.getFilePath());
                if (blob3AFile.exists()) {
                    NotherUtils.rm(blob3AFile);
                }
            }
        }
        for (String key1 : parentCommit3B.getTracked().keySet()) {
            //被两个commit共同跟踪（用checked branch中的blobs覆写这些文件）
            if (parentCommit3A.getTracked().containsKey(key1)) {
                Blob blob = Blob.fromFile(parentCommit3B.getTracked().get(key1));
                File blobFile = new File(blob.getFilePath());
                if (blobFile.exists()) {
                    NotherUtils.rm(blobFile);
                }
                Utils.writeContents(blobFile, NotherUtils.getBytes(blob.getBytes()));
            } else {
                //仅被当前跟踪
                Blob blob3B = Blob.fromFile(parentCommit3B.getTracked().get(key1));
                List<String> cwdlist = Utils.plainFilenamesIn(CWD);
                if (cwdlist.contains(blob3B.getFileName().getName())) {
                    NotherUtils.message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                } else {
                    Utils.writeContents(blob3B.getFileName()
                            , NotherUtils.getBytes(blob3B.getBytes()));
                }
            }
        }
        //更改HEAD指向Commit3B，最后清空缓存区。
        List<String> addStageList = Utils.plainFilenamesIn(ADD_STAGE);
        List<String> removeStageList = Utils.plainFilenamesIn(REMOVE_STAGE);
        if ((ADD_STAGE.exists())) {
            for (String addStageFile : addStageList) {
                File addFile = join(ADD_STAGE, addStageFile);
                //删除addStage下的暂存文件
                NotherUtils.rm(addFile);
            }
        }
        if ((REMOVE_STAGE.exists())) {
            for (String str : removeStageList) {
                File addFile = join(REMOVE_STAGE, str);
                //删除addStage下的暂存文件
                NotherUtils.rm(addFile);
            }
        }
    }
    public static void setReset(String resetCommitId) {
        List<String> commitList = Utils.plainFilenamesIn(COMMIT);
        if (!commitList.contains(resetCommitId)) {
            NotherUtils.message("No commit with that id exists.");
        }
        String branch = Utils.readContentsAsString(HEAD);
        File newBranch = join(HEADS, branch);
        String commitId1 = Utils.readContentsAsString(newBranch);
        NotherUtils.clearFile(newBranch);
        Utils.writeContents(newBranch, resetCommitId);
        Commit parentCommit3A = Commit.fromFile(commitId1);
        //未切换前的分支
        Commit parentCommit3B = Commit.fromFile(resetCommitId);
        for (String key : parentCommit3A.getTracked().keySet()) {
            //被两个commit共同跟踪（用checked branch中的blobs覆写这些文件）
            if (parentCommit3B.getTracked().containsKey(key)) {
                Blob blob = Blob.fromFile(parentCommit3B.getTracked().get(key));
                File blobFile = new File(blob.getFilePath());
                if (blobFile.exists()) {
                    NotherUtils.rm(blobFile);
                }
                Utils.writeContents(blobFile, NotherUtils.getBytes(blob.getBytes()));
            } else {
                //仅被当前跟踪
                Blob blob3A = Blob.fromFile(parentCommit3A.getTracked().get(key));
                File blob3AFile = new File(blob3A.getFilePath());
                if (blob3AFile.exists()) {
                    NotherUtils.rm(blob3AFile);
                }
            }
        }
        for (String key1 : parentCommit3B.getTracked().keySet()) {
            //被两个commit共同跟踪（用checked branch中的blobs覆写这些文件）
            if (parentCommit3A.getTracked().containsKey(key1)) {
                Blob blob = Blob.fromFile(parentCommit3B.getTracked().get(key1));
                File blobFile = new File(blob.getFilePath());
                if (blobFile.exists()) {
                    NotherUtils.rm(blobFile);
                }
                Utils.writeContents(blobFile, NotherUtils.getBytes(blob.getBytes()));
            } else {
                //仅被当前跟踪
                Blob blob3B = Blob.fromFile(parentCommit3B.getTracked().get(key1));
                List<String> cwdlist = Utils.plainFilenamesIn(CWD);
                if (cwdlist.contains(blob3B.getFileName().getName())) {
                    NotherUtils.message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                } else {
                    Utils.writeContents(blob3B.getFileName()
                            , NotherUtils.getBytes(blob3B.getBytes()));
                }
            }
        }
        //更改HEAD指向Commit3B，最后清空缓存区。
        NotherUtils.clearFile(HEAD);
        Utils.writeContents(HEAD, branch);
        List<String> addStageList = Utils.plainFilenamesIn(ADD_STAGE);
        List<String> removeStageList = Utils.plainFilenamesIn(REMOVE_STAGE);
        if ((ADD_STAGE.exists())) {
            for (String addStageFile : addStageList) {
                File addFile = join(ADD_STAGE, addStageFile);
                //删除addStage下的暂存文件
                NotherUtils.rm(addFile);
            }
        }
        if ((REMOVE_STAGE.exists())) {
            for (String str : removeStageList) {
                File addFile = join(REMOVE_STAGE, str);
                //删除addStage下的暂存文件
                NotherUtils.rm(addFile);
            }
        }
    }
    public static void setMerge(String text) {
        //如果给定的branch不存在，输出错误信息：
        List<String> headsList = Utils.plainFilenamesIn(HEADS);
        if (!headsList.contains(text)) {
            NotherUtils.message("A branch with that name does not exist.");
        }
        //如果给定的branch和当前branch相同，输出错误信息
        String head = Utils.readContentsAsString(HEAD);
        if (head.equals(text)) {
            NotherUtils.message("Cannot merge a branch with itself.");
        }
        ////如果缓存区还有blob（文件存在），输出错误信息：
        //暂存区为空
        if (!NotherUtils.isHeadBranch()) {
            NotherUtils.message("You have uncommitted changes.");
        }
        //HEAD
        Commit commitA = NotherUtils.getHeadBranchCommitId();
        //OTHER
        Commit commitB = NotherUtils.getBranch(text);
        Map<String, Integer> commAMap = new HashMap<>();
        Map<String, Integer> commBMap = new HashMap<>();
        Map<String, Integer> finSplitMap = new HashMap<>();
        finSplit(finSplitMap, commitA, commitB, commAMap, commBMap);
        //如果split point和HEAD分支的Commit相同，意味着otherbranch与HEAD在一个分支上并且超前于HEAD
        //此时直接将HEAD更新到otherbranch的当前Commit，并且输出Current branch fast-forwarded.
        for (String splitKey: finSplitMap.keySet()) {
            if (commitA.commitId().equals(splitKey)) {
                String headFileString = Utils.readContentsAsString(HEAD);
                File masterFile = join(HEADS, headFileString);
                Utils.writeContents(masterFile, commitB.commitId());
                if (!masterFile.exists()) {
                    createNewFile(masterFile);
                }
                for (String path : commitA.getTracked().keySet()) {
                    if (!commitB.getTracked().keySet().contains(path)) {
                        File cwdFile = new File(path);
                        createNewFile(cwdFile);
                        NotherUtils.rm(cwdFile);
                    }
                }
                NotherUtils.message("Current branch fast-forwarded.");
            }
            if (commitB.commitId().equals(splitKey)) {
                NotherUtils.message("Given branch is an ancestor of the current branch.");
            }
        }
        //id为key filename为value
        Map<String, String> allfileMap = new HashMap<>();
        Map<String, String> masterMap = new HashMap<>();
        Map<String, String> otherMap = new HashMap<>();
        Map<String, String> splitMap = new HashMap<>();
        for (String splitKey : finSplitMap.keySet()) {
            if (!splitKey.equals("0")) {
                Commit splitCommit = Commit.fromFile(splitKey);
                for (String str : splitCommit.getTracked().keySet()) {
                    String splitvalue = splitCommit.getTracked().get(str);
                    allfileMap.put(splitvalue, str);
                    splitMap.put(splitvalue, str);
                }
            }
        }
        for (String masterKey : commitA.getTracked().keySet()) {
            String mastevalue = commitA.getTracked().get(masterKey);
            allfileMap.put(mastevalue, masterKey);
            masterMap.put(mastevalue, masterKey);
        }
        for (String otherKey : commitB.getTracked().keySet()) {
            String mastevalue = commitB.getTracked().get(otherKey);
            allfileMap.put(mastevalue, otherKey);
            otherMap.put(mastevalue, otherKey);
        }
        boolean conflict = false;
        List<String> list = new ArrayList<>();
        list.add(commitA.getCommitID());
        list.add(commitB.getCommitID());
        String headFileString = Utils.readContentsAsString(HEAD);
        String message = "Merged" + " " + text + " "
                + "into" + " " + headFileString + ".";
        compareFile(allfileMap, masterMap, otherMap, splitMap,
                commitA.getTracked(), conflict, message, list, headFileString);
    }
    private static void compareFile(Map<String, String> allfileMap,
                                    Map<String, String> masterMap, Map<String, String> otherMap,
                                    Map<String, String> splitMap, Map<String, String> parentTracked,
                                    boolean conflict, String message, List<String> list, String headFileString) {
        //遍历allfileMap中的keyset，判断其余三个Map中的文件存在以及修改情况，就能够判断出上述7种不同情况
        //然后对每个文件进行删除、覆写、直接写入等操作，这样就完成了merge操作。
        if (!REMOVE_STAGE.exists()) {
            //创建removeStage文件目录
            REMOVE_STAGE.mkdir();
        }
        if (!ADD_STAGE.exists()) {
            //创建removeStage文件目录
            ADD_STAGE.mkdir();
        }
        for (String blobId : allfileMap.keySet()) {
            Blob compareBlib = Blob.fromFile(blobId);
            //根据value获取对应的key
            String masterKey = NotherUtils.getKey(masterMap, compareBlib.getFilePath());
            String otherKey = NotherUtils.getKey(otherMap, compareBlib.getFilePath());
            String splitKey = NotherUtils.getKey(splitMap, compareBlib.getFilePath());
            if (splitKey != null && masterKey != null && otherKey != null) {
                //没改变继续引用
                if (splitKey.equals(masterKey) && splitKey.equals(otherKey)) {
                    File cwdFile = join(CWD, compareBlib.getFileName().getName());
                    if (cwdFile.exists()) {
                        NotherUtils.rm(cwdFile);
                    }
                    Utils.writeContents(cwdFile, NotherUtils.getBytes(compareBlib.getBytes()));
                }
                //文件内容是other的
                if (splitKey.equals(masterKey) && !splitKey.equals(otherKey)) {
                    File cwdFile = join(CWD, compareBlib.getFileName().getName());
                    Blob blob = Blob.fromFile(otherKey);
                    if (cwdFile.exists()) {
                        NotherUtils.rm(cwdFile);
                    }
                    Utils.writeContents(cwdFile, NotherUtils.getBytes(blob.getBytes()));
                    NotherUtils.add(blob);
                }
                //文件内容master
                if (!splitKey.equals(masterKey) && splitKey.equals(otherKey)) {
                    File cwdFile = join(CWD, compareBlib.getFileName().getName());
                    Blob blob = Blob.fromFile(masterKey);
                    if (cwdFile.exists()) {
                        NotherUtils.rm(cwdFile);
                    }
                    Utils.writeContents(cwdFile, NotherUtils.getBytes(blob.getBytes()));
                }
                //文件内容master
                if (!splitKey.equals(masterKey) && !splitKey.equals(otherKey)
                        && masterKey.equals(otherKey)) {
                    File cwdFile = join(CWD, compareBlib.getFileName().getName());
                    Blob blob = Blob.fromFile(masterKey);
                    if (cwdFile.exists()) {
                        NotherUtils.rm(cwdFile);
                    }
                    Utils.writeContents(cwdFile, NotherUtils.getBytes(blob.getBytes()));
                }
                //文件内容冲突
                if (!splitKey.equals(masterKey) && !splitKey.equals(otherKey)
                        && !masterKey.equals(otherKey)) {
                    //File cwdFile = join(CWD, compareBlib.getFileName().getName());
                    if (compareBlib.getFileName().exists()) {
                        NotherUtils.rm(compareBlib.getFileName());
                    }
                    String conflictContent = NotherUtils.getConflictContent(masterKey, otherKey);
                    writeContents(compareBlib.getFileName(), conflictContent);
                    Blob blobId2 = new Blob(compareBlib.getFileName());
                    NotherUtils.add(blobId2);
                    conflict = true;
                }
            }
            if (splitKey != null && masterKey != null && otherKey == null) {
                if (splitKey.equals(masterKey)) {
                    Blob blob = Blob.fromFile(masterKey);
                    List<String> removeStageList = Utils.plainFilenamesIn(Repository.REMOVE_STAGE);
                    if (!removeStageList.contains(blob.blobId())) {
                        File rmAddStageFile2 = join(Repository.REMOVE_STAGE, blob.blobId());
                        Utils.writeObject(rmAddStageFile2, blob.blobId());
                        Repository.createNewFile(rmAddStageFile2);
                    }
                    File cwdFile = join(CWD, blob.getFileName().getName());
                    if (cwdFile.exists()) {
                        NotherUtils.rm(cwdFile);
                    }
                }
                if (!splitKey.equals(masterKey)) {
                    File cwdFile = join(CWD, compareBlib.getFileName().getName());
                    Blob blob = Blob.fromFile(masterKey);
                    if (cwdFile.exists()) {
                        NotherUtils.rm(cwdFile);
                    }
                    String conflictContent = NotherUtils.getConflictContent(masterKey, otherKey);
                    writeContents(compareBlib.getFileName(), conflictContent);
                    Blob blobId2 = new Blob(compareBlib.getFileName());
                    NotherUtils.add(blobId2);
                    conflict = true;
                }
            }

            if (splitKey != null && masterKey == null && otherKey != null) {
                if (splitKey.equals(otherKey)) {
                    Blob blob = Blob.fromFile(otherKey);
                    List<String> removeStageList = Utils.plainFilenamesIn(Repository.REMOVE_STAGE);
                    if (!removeStageList.contains(blob.blobId())) {
                        File rmAddStageFile2 = join(Repository.REMOVE_STAGE, blob.blobId());
                        Utils.writeObject(rmAddStageFile2, blob.blobId());
                        Repository.createNewFile(rmAddStageFile2);
                    }
                    File cwdFile = join(CWD, compareBlib.getFileName().getName());
                    if (cwdFile.exists()) {
                        NotherUtils.rm(cwdFile);
                    }
                    //删除了Utils.writeContents(cwdFile, NotherUtils.getBytes(compareBlib.getBytes()));
                }
                if (!splitKey.equals(otherKey)) {
                    File cwdFile = join(CWD, compareBlib.getFileName().getName());
                    Blob blob = Blob.fromFile(otherKey);
                    if (cwdFile.exists()) {
                        NotherUtils.rm(cwdFile);
                    }
                    Utils.writeContents(cwdFile, NotherUtils.getBytes(blob.getBytes()));
                    String conflictContent = NotherUtils.getConflictContent(masterKey, otherKey);
                    writeContents(compareBlib.getFileName(), conflictContent);
                    Blob blobId2 = new Blob(compareBlib.getFileName());
                    NotherUtils.add(blobId2);
                    conflict = true;
                }
            }
            if (splitKey != null && masterKey == null && otherKey == null) {
                File cwdFile = join(CWD, compareBlib.getFileName().getName());
                if (cwdFile.exists()) {
                    NotherUtils.rm(cwdFile);
                }
            }
            if (splitKey == null && masterKey != null && otherKey != null) {
                if (!masterKey.equals(otherKey)) {
                    if (compareBlib.getFileName().exists()) {
                        NotherUtils.rm(compareBlib.getFileName());
                    }
                    String conflictContent = NotherUtils.getConflictContent(masterKey, otherKey);
                    writeContents(compareBlib.getFileName(), conflictContent);
                    Blob blobId2 = new Blob(compareBlib.getFileName());
                    NotherUtils.add(blobId2);
                    conflict = true;
//                    System.out.println("compareBlib.getFileName().getName():" + compareBlib.getFileName().getName());
//                    System.out.println("conflictContent:" + conflictContent);
                }
                if (masterKey.equals(otherKey)) {
                    File cwdFile = join(CWD, compareBlib.getFileName().getName());
                    Blob blob = Blob.fromFile(masterKey);
                    if (cwdFile.exists()) {
                        NotherUtils.rm(cwdFile);
                    }
                    //Utils.writeContents(cwdFile, NotherUtils.getBytes(blob.getBytes()));
                }
            }
            //可以了
            if (splitKey == null && masterKey == null && otherKey != null) {
                //仅被当前跟踪
                Blob blob3B = Blob.fromFile(otherKey);
                List<String> cwdlist = Utils.plainFilenamesIn(CWD);
                if (cwdlist.contains(blob3B.getFileName().getName())) {
                    NotherUtils.message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                } else {
                    Utils.writeContents(blob3B.getFileName(), NotherUtils.getBytes(blob3B.getBytes()));
                }
            }
            //可以了
            if (splitKey == null && masterKey != null && otherKey == null) {
                Blob blob3B = Blob.fromFile(masterKey);
                List<String> cwdlist = Utils.plainFilenamesIn(CWD);
                if (cwdlist.contains(blob3B.getFileName().getName())) {
                    NotherUtils.message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                } else {
                    Utils.writeContents(blob3B.getFileName(), NotherUtils.getBytes(blob3B.getBytes()));
                }
//                File cwdFile = join(CWD, compareBlib.getFileName().getName());
//                Blob blob = Blob.fromFile(masterKey);
//                if (blob.getFileName().exists()) {
//                    NotherUtils.rm(cwdFile);
//                }
                //Utils.writeContents(cwdFile, NotherUtils.getBytes(blob.getBytes()));
            }
        }

        parentTracked = NotherUtils.commit(parentTracked);
        //String message = "Merged other into master";
        Commit newCommit = new Commit(message, list, parentTracked);
        //如果工作目录存在仅被merge commit跟踪，且将被覆写的文件，输出错误信息：
        File masterFile = join(HEADS, headFileString);
        Utils.writeContents(masterFile, newCommit.commitId());
        if (!masterFile.exists()) {
            createNewFile(masterFile);
        }
        if (conflict) {
            NotherUtils.message("Encountered a merge conflict.");
        }
    }
    private static void finSplit(Map<String, Integer> finSplitMap,
                                 Commit commitA, Commit commitB, Map<String, Integer> commAMap, Map<String, Integer> commBMap) {
        int n = 0;
        while (commitA.getParent() != null && !commitA.getParent().isEmpty()) {
            n = n + 1;
            commAMap.put(commitA.getCommitID(), n);
            commitA = Commit.fromFile(commitA.getParent().get(0));
        }
        int m = 0;
        while (commitB.getParent() != null && !commitB.getParent().isEmpty()) {
            m = m + 1;
            commBMap.put(commitB.getCommitID(), m);
            commitB = Commit.fromFile(commitB.getParent().get(0));
        }
        String key = "0";
        int value = 999999999;
        for (String str : commAMap.keySet()) {
            if (commBMap.containsKey(str)) {
                if (commAMap.get(str) < value) {
                    value = commAMap.get(str);
                    key = str;
                }
            }
        }
        finSplitMap.put(key, value);
    }
}