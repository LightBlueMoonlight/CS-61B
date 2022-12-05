package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.List;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.在此处添加实例变量。
     *
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
    private List<String> blobID;//文件id


    public Commit(String message, List<String> parent, List<String> blobID){
        this.message = message;
        this.parent = parent;
        this.date = new Date();
        this.blobID = blobID;
        if(parent.size()==0){
            this.date = new Date(0);
        }

        //id的构造等会写
    }



}
