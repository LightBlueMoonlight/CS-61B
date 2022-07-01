package capers;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;


/** Assorted utilities.
 *  @author P. N. Hilfinger
 */
class Utils {

    /* READING AND WRITING FILE CONTENTS */

    /** Return the entire contents of FILE as a byte array.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    /**以字节数组的形式返回文件的全部内容。文件必须
     *是普通文件。抛出IllegalArgumentException
     *如果出现问题*/
    static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Return the entire contents of FILE as a String.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    /**以字符串形式返回文件的全部内容。文件必须
     *是普通文件。抛出IllegalArgumentException
     *如果出现问题*/
    static String readContentsAsString(File file) {
        return new String(readContents(file), StandardCharsets.UTF_8);
    }

    /** Write the result of concatenating the bytes in CONTENTS to FILE,
     *  creating or overwriting it as needed.  Each object in CONTENTS may be
     *  either a String or a byte array.  Throws IllegalArgumentException
     *  in case of problems. */
    /**将内容中的字节连接到文件的结果写入，
     *根据需要创建或覆盖它。内容中的每个对象可能是
     *字符串或字节数组。抛出IllegalArgumentException
     *如果出现问题*/
    static void writeContents(File file, Object... contents) {
        try {
            if (file.isDirectory()) {
                throw
                        new IllegalArgumentException("cannot overwrite directory");
            }
            BufferedOutputStream str =
                    new BufferedOutputStream(Files.newOutputStream(file.toPath()));
            for (Object obj : contents) {
                if (obj instanceof byte[]) {
                    str.write((byte[]) obj);
                } else {
                    str.write(((String) obj).getBytes(StandardCharsets.UTF_8));
                }
            }
            str.close();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Return an object of type T read from FILE, casting it to EXPECTEDCLASS.
     *  Throws IllegalArgumentException in case of problems. */
    /**返回从文件读取的T类型的对象，将其强制转换为EXPECTEDCLASS。
     *出现问题时引发IllegalArgumentException*/
    static <T extends Serializable> T readObject(File file,
                                                 Class<T> expectedClass) {
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException | ClassCastException
                | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Write OBJ to FILE. */
    /**将OBJ写入文件*/
    static void writeObject(File file, Serializable obj) {
        writeContents(file, serialize(obj));
    }


    /* OTHER FILE UTILITIES */
    /** Return the concatentation of FIRST and OTHERS into a File designator,
     *  analogous to the {@link java.nio.file.Paths#get(String, String[])}
     *  method. */
    /*其他文件实用程序*/
    /**将FIRST和OTHERS的浓度返回到文件指示符中，
     *类似于{@link java.nio.file.Paths#get（String，String[]）}
     *方法*/
    public static File join(String first, String... others) {
        return Paths.get(first, others).toFile();
    }

    /** Return the concatentation of FIRST and OTHERS into a File designator,
     *  analogous to the {@link java.nio.file.Paths#get(String, String[])}
     *  method. */
    /**将FIRST和OTHERS的浓度返回到文件指示符中，
     *类似于{@link java.nio.file.Paths#get（String，String[]）}
     *方法*/
    public static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }


    /* SERIALIZATION UTILITIES */
    /** Returns a byte array containing the serialized contents of OBJ. */
    /*序列化实用程序*/
    /**返回包含OBJ序列化内容的字节数组*/
    static byte[] serialize(Serializable obj) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            throw error("Internal error serializing commit.");
        }
    }



    /* MESSAGES AND ERROR REPORTING */

    /**
     * Prints out MESSAGE and exits with error code -1.
     * Note:
     *     The functionality for erroring/exit codes is different within Gitlet
     *     so DO NOT use this as a reference.
     *     Refer to the spec for more information.
     * @param message message to print
     */
    /**
     *打印出消息并退出，错误代码为1。
     *注：
     *Gitlet中错误/退出代码的功能不同
     *因此，不要将此作为参考。
     *有关更多信息，请参阅规范。
     *@要打印的参数消息消息

     */
    public static void exitWithError(String message) {
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
        System.exit(-1);
    }

    /** Return a RuntimeException whose message is composed from MSG and ARGS as
     *  for the String.format method. */
    /**返回RuntimeException，其消息由MSG和ARGS组成，如下所示
     *对于字符串。格式方法*/
    static RuntimeException error(String msg, Object... args) {
        return new RuntimeException(String.format(msg, args));
    }

}
