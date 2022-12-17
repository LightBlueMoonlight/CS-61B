package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.join;
import static gitlet.Utils.readObject;

public class Blob implements Serializable {
    private String id; //blobId
    private byte[] bytes; //文件内容
    private File fileName; //存储的文件
    private String filePath; //存储文件的文件路径
    private File blobSaveFileName; //blob文件的文件名

    //构造函数
    public Blob(File file) {
        this.fileName = file;
        this.filePath = file.getPath();
        this.bytes = Utils.readContents(file);
        this.id = Utils.sha1(filePath, bytes);
        this.blobSaveFileName = Utils.join(Repository.BLOB, id);
    }

    //算出调用文件的blobid
    public static String getBlobId(File file) {
        String filePath2 = file.getPath();
        Repository.createNewFile(file);
        byte[] bytes2 = Utils.readContents(file);
        return Utils.sha1(filePath2, bytes2);
    }

    public File getBlobSaveFileName() {
        return blobSaveFileName;
    }

    public File getFileName() {
        return fileName;
    }

    //根据commitId生成commit文件
    public static Blob fromFile(String id) {
        return readObject(getObjectFile(id), Blob.class);
    }

    public static File getObjectFile(String id) {
        File fileBlob = join(Repository.BLOB, id);
        Repository.createNewFile(fileBlob);
        System.out.println("看看："+fileBlob.getPath());
        return fileBlob;
    }

    public static Map pathToBlobID(Blob blob) {
        Map<String, String> pathToBlobID = new HashMap<>();
        pathToBlobID.put(blob.filePath,blob.id);
        return pathToBlobID;
    }



}
