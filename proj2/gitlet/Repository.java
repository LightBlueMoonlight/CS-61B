package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
/**表示gitlet存储库。
 *TODO:在这里描述这个类的其他内容是个好主意
 *在高水平上。
 */
public class Repository {

    /**当前工作目录*/
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    /**.gitlet目录*/
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File INDEX = join(".gitlet", "index");

    public static final File OBJECTS = join(".gitlet", "objects");

    public static final File INIT_COMMIT = join(".objects", "initCommit");


    //设置.git和存储仓库
    public static void setInit() {
        //如果当前目录下没有存储库就创建.gitlet
        List<String> fileList = Utils.plainFilenamesIn(CWD);
        if(fileList == null || !fileList.contains(".gitlet")){
            //创建.gitlet目录
            GITLET_DIR.mkdir();
            //生成.git要在.git下创建一个objets文件用来存储commit和blob
            OBJECTS.mkdir();
            //代替parent和blobID
            List<String> blackList = new ArrayList<>();
            //initCommit的parent和blobId应该是空的,不能为null，sha1方法会报错
            Commit initCommit = new Commit("Init Commit",blackList,blackList);
            //通过commit对象算出commit的id
            String id = Utils.sha1(initCommit);
            //文件名是算出commit的id
            File saveFile = Utils.join(INIT_COMMIT, id);
            //对象都通过序列化写入到objects文件夹中，每个对象对应一个文件，文件名即为40位的对象ID。
            Utils.writeObject(saveFile,initCommit);
        }

        //当前目录中已经有一个 Gitlet 版本控制系统
        if(fileList.contains(".gitlet")){
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        //index   #保存暂存区信息，在执行git init 的时候，这个文件还没有
        //如果用户输入的命令需要在初始化的 Gitlet 工作目录（即包含.gitlet子目录的目录）中，但不在这样的目录中，则打印消息
    }

    //添加操作
    public static void setAdd(Object Object) {
        //判断加入的是否文件
        if(Object instanceof File){
            //需要判断是否有.gitlet
            List<String> fileList = Utils.plainFilenamesIn(".gitlet");
            if(fileList == null || !fileList.contains("index")){
                INDEX.mkdir();
                //将内容加入index
                stage(Object);
            }

            if(fileList.contains("index")){
                //将内容加入index 判断是否存在
                Utils.sha1();
            }
        }else{
            //操作数不正确。
            Utils.message("Incorrect operands.");
            System.exit(0);
        }

    }

    //设置暂存区
    public static void stage(Object Object) {
        //算出要进入暂存区的文件hash值
        String hashKey = Utils.sha1(Object);
        //判断暂存区的文件是否包含这个文件
        List<String> fileList = Utils.plainFilenamesIn("index");
        if(!fileList.contains(hashKey)){
            //添加暂存区，同时要放入objects
        }
    }


}
