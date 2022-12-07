package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 * TODO: It's a good idea to give a description here of what else this Class
 * does at a high level.
 *
 * @author TODO
 */

/**
 * 表示gitlet存储库。
 * TODO:在这里描述这个类的其他内容是个好主意
 * 在高水平上。
 */
public class Repository implements Serializable {

    /**
     * 当前工作目录
     */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    /**
     * .gitlet目录
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File OBJECTS = join(GITLET_DIR, "objects");

    public static final File STAGE = join(GITLET_DIR, "stage");

    public static final File REFS = join(GITLET_DIR, "refs");

    public static final File HEAD = join(GITLET_DIR, "HEAD");

    public static final File ADDSTAGE = join(GITLET_DIR, "addstage");

    public static final File REMOVESSTAGE = join(GITLET_DIR, "removestage");

    public static final File INIT_COMMIT = join(".objects", "initCommit");


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
            NotherUtils.message("A Gitlet version-control system already exists in the current directory.");
        }
        //创建.gitlet目录
        GITLET_DIR.mkdir();
        //创建objects目录 用来存储commit和blob
        OBJECTS.mkdir();
        //创建refs目录
        REFS.mkdir();
        //stage   #保存暂存区信息，在执行git init 的时候，这个文件还没有
        Commit initCommit = new Commit();
    }
//    {
//        //代替parent和blobID
//        List<String> blackList = new ArrayList<>();
//        //initCommit的parent和blobId应该是空的,不能为null，sha1方法会报错
//        Commit initCommit = new Commit("Init Commit", blackList, blackList);
//        //通过commit对象算出commit的id
//        String id = Utils.sha1(initCommit);
//        //文件名是算出commit的id
//        File saveFile = Utils.join(INIT_COMMIT, id);
//        saveFile.mkdir();
//        //对象都通过序列化写入到objects文件夹中，每个对象对应一个文件，文件名即为40位的对象ID。
//        Utils.writeObject(saveFile, initCommit);
//    }

    //添加操作
    public static void setAdd(Object Object) {
        //判断加入的是否文件
        if (Object instanceof File) {
            //需要判断是否有.gitlet
            List<String> fileList = Utils.plainFilenamesIn(CWD);
            if (fileList == null || !fileList.contains(".gitlet")) {
                if (fileList == null || !fileList.contains("index")) {

                }

                if (fileList.contains("index")) {
                    //将内容加入index 判断是否存在
                    Utils.sha1();
                }
            } else {
                //操作数不正确。
                Utils.message("Incorrect operands.");
                System.exit(0);
            }

        }

        //设置暂存区
//        public static void stage (Object Object){
//            //算出要进入暂存区的文件hash值
//            String hashKey = Utils.sha1(Object);
//            //判断暂存区的文件是否包含这个文件
//            List<String> fileList = Utils.plainFilenamesIn("index");
//            if (!fileList.contains(hashKey)) {
//                //添加暂存区，同时要放入objects
//            }
//        }
    }
}
