package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     * *在此处列出Commit类的所有实例变量
     * *上面的注释描述了该变量代表什么以及如何
     * *变量。我们为“message”提供了一个示例。
     */

    /** The message of this Commit. */
    private String message;//提交信息
    private String id;//commit对象的ID
    private List<String> parent;//父提交
    private Date date;//提交日期
    //private List<String> blobID;//文件id
    private  File file;//用SHA1 生成的id创建新的commit文件
    private  Map<String, String> tracked;//跟踪的文件以文件路径为关键字，SHA1 id为值进行映射。  =blobID？


    public Commit(String message, List<String> parent, Map<String, String> tracked){
        date = new Date();
        this.message = message;
        this.parent = parent;
        this.tracked = tracked;
        id = commitId();
        file = Utils.join(Repository.GITLET_DIR,id);
    }
    
    public Commit(){
        date = new Date(0);
        message = "initial commit";
        parent = new ArrayList<>();
        tracked = new HashMap<>();
        id = commitId();
        file = Utils.join(Repository.GITLET_DIR,id);
    }



    //根据commit对象算出他的commitId
    private String commitId() {
        return Utils.sha1(getTimestamp(),message,parent.toString(),tracked.toString());
    }

    //Gradescope指定日期格式
    public String getTimestamp() {
        // Thu Jan 1 00:00:00 1970 +0000
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        return dateFormat.format(date);
    }

    //创建commit文件
    public void makeCommitFile(File file) {
        //获取文件的上层目录
        File dir = file.getParentFile();
        Utils.writeObject(dir, this);
    }

}
