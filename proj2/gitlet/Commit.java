package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.join;
import static gitlet.Utils.readObject;

public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     * *在此处列出Commit类的所有实例变量
     * *上面的注释描述了该变量代表什么以及如何
     * *变量。我们为“message”提供了一个示例。
     */

    /**
     * The message of this Commit.
     */
    private String message; //提交信息
    private String id; //commit对象的ID
    private List<String> parent; //父提交
    private Date date; //提交日期
    //private List<String> blobID; //文件id
    private File file;//用SHA1 生成的id创建新的commit文件
    private Map<String, String> tracked;//跟踪的文件以文件路径为关键字，SHA1 id为值进行映射。  =blobID？


    public Commit(String message, List<String> parent, Map<String, String> tracked) {
        date = new Date();
        this.message = message;
        this.parent = parent;
        this.tracked = tracked;
        id = commitId();
        file = Utils.join(Repository.COMMIT, id);
    }

    public Commit() {
        date = new Date(0);
        message = "initial commit";
        parent = new ArrayList<>();
        tracked = new HashMap<>();
        id = commitId();
        file = Utils.join(Repository.COMMIT, id);
    }

    public File getFile() {
        return file;
    }

    public String getCommitID() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return getTimestamp();
    }

    public List<String> getParent() {
        return parent;
    }

    public Map<String, String> getTracked() {
        return tracked;
    }


    //根据commit对象算出他的commitId
    public String commitId() {
        return Utils.sha1(getTimestamp(), message, parent.toString(), tracked.toString());
    }

    //Gradescope指定日期格式
    public String getTimestamp() {
        // Thu Jan 1 00:00:00 1970 +0000
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        return dateFormat.format(date);
    }

    //根据commitId生成commit文件
    public static Commit fromFile(String id) {
        return readObject(getObjectFile(id), Commit.class);
    }

    public static File getObjectFile(String id) {
        return join(Repository.COMMIT, id);
    }

}
