package gitlet;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static gitlet.Utils.join;
import static gitlet.Utils.restrictedDelete;

public class NotherUtils {
    /**
     * 输出报错信息并退出
     */
    public static void message(String mesage) {
        Utils.message(mesage);
        System.exit(0);
    }

    public static void clearFile(File file)  {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void addStageFile(File newFile,File ADD_STAGE,Blob blob){
        //获取addStage中的文件
        List<String> addStageList = Utils.plainFilenamesIn(ADD_STAGE);
        //遍历addStage中的文件与当前添加的文件做比较
        //为空就直接添加
        if (addStageList == null || addStageList.isEmpty()){
            System.out.println("第一次");
            File rmAddStageFile2 = join(ADD_STAGE,blob.blobId());
            Utils.writeObject(rmAddStageFile2, blob.blobId());
            Repository.createNewFile(rmAddStageFile2);
        }

        //不为空则遍历
        if (addStageList != null && !addStageList.isEmpty()) {
            System.out.println("第二次");
            //文件名相同，内容不同，要添加addStage
            for (String str : addStageList) {
                //根据blobId还原blob文件
                Blob blobFromFile = Blob.fromFile(str);
                //如果addStage里的相对路径等于添加文件的相对路径
                if (blobFromFile.getFilePath().equals(newFile.getPath())) {
                    //获取之前addStage的文件名
                    File rmAddStageFile = join(ADD_STAGE, str);
                    System.out.println("rmAddStageFile:" + rmAddStageFile.getName());
                    //删除之前的blob文件
                    rm(rmAddStageFile);
                    //当前blob添加到addStage目录
                    File rmAddStageFile2 = join(ADD_STAGE, blob.blobId());
                    Utils.writeObject(rmAddStageFile2, blob.blobId());
                    Repository.createNewFile(rmAddStageFile2);
                    System.out.println("rmAddStageFile2:" + rmAddStageFile2.getName());
                }
            }
        }
        //删除目录下的add文件
        rm(newFile);
    }

    public static void rm(File file) {
        if (!file.delete()) {
            throw new IllegalArgumentException(String.format("rm: %s: Failed to delete.", file.getPath()));
        }
    }
}
