package gitlet;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


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

    public static void rm(File file) {
        if (!file.delete()) {
            throw new IllegalArgumentException(String.format("rm: %s: Failed to delete.", file.getPath()));
        }
    }
}
