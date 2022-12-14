package gitlet;

import java.io.File;
import java.io.Serializable;

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
        this.blobSaveFileName = Utils.join(Repository.OBJECTS, id);
    }

    //算出调用文件的blobid
    public static String getBlobId(File file) {
        String filePath2 = file.getPath();
        byte[] bytes2 = Utils.readContents(file);
        return Utils.sha1(filePath2, bytes2);
    }

    public File getBlobSaveFileName() {
        return blobSaveFileName;
    }

    public File getFileName() {
        return fileName;
    }

    public void makeBlobFile() {
        //获取文件的上层目录
        //返回该文件的父目录的抽象路径名；如果该路径名未命名父目录，则返回null 例如当前文件名为C:\\test.txt  返回C:\
        File dir = blobSaveFileName.getParentFile();
        Utils.writeContents(dir, this);
    }
}
