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
        this.id = blobId();
        this.blobSaveFileName = Utils.join(Repository.BLOB, id);
    }

    public String getId() {
        return id;
    }

    //文件名（key）：根据源文件内容生成的SHA1哈希值。（只与文件内容有关，与文件名等无关）
    public String blobId() {
        return Utils.sha1(bytes);
    }

    //算出调用文件的blobid
    public static String getBlobId(File file) {
        Repository.createNewFile(file);
        byte[] bytes2 = Utils.readContents(file);
        return Utils.sha1( bytes2);
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
        return fileBlob;
    }

    public static Map pathToBlobID(Blob blob) {
        Map<String, String> pathToBlobID = new HashMap<>();
        pathToBlobID.put(blob.filePath, blob.id);
        return pathToBlobID;
    }



}
