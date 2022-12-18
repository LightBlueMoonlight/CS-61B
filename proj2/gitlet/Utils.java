package gitlet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/** Assorted utilities.
 *
 * Give this file a good read as it provides several useful utility functions
 * to save you some time.
 *
 *  @author P. N. Hilfinger
 */
class Utils {

    /** The length of a complete SHA-1 UID as a hexadecimal numeral.完整SHA-1 UID的长度（十六进制数字）。 */
    static final int UID_LENGTH = 40;

    /*SHA-1哈希值*/
    /**返回VAL串联的SHA-1哈希
     *是字节数组和字符串的任意组合*/
    static String sha1(Object... vals) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (Object val : vals) {
                if (val instanceof byte[]) {
                    md.update((byte[]) val);
                } else if (val instanceof String) {
                    md.update(((String) val).getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new IllegalArgumentException("improper type to sha1");
                }
            }
            Formatter result = new Formatter();
            for (byte b : md.digest()) {
                result.format("%02x", b);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException excp) {
            throw new IllegalArgumentException("System does not support SHA-1");
        }
    }

    /** Returns the SHA-1 hash of the concatenation of the strings in  返回中字符串串联的SHA-1哈希值
     *  VALS. */
    static String sha1(List<Object> vals) {
        return sha1(vals.toArray(new Object[vals.size()]));
    }

    /* FILE DELETION */

    /** Deletes FILE if it exists and is not a directory.  Returns true
     *  if FILE was deleted, and false otherwise.  Refuses to delete FILE
     *  and throws IllegalArgumentException unless the directory designated by
     *  FILE also contains a directory named .gitlet. */
    /*文件删除*/
    /**如果文件存在且不是目录，则删除该文件。返回true
     *如果FILE被删除，则为false，否则为false。拒绝删除FILE
     *并抛出IllegalArgumentException，除非
     *FILE还包含一个名为.gitlet的目录*/
    static boolean restrictedDelete(File file) {
        if (!(new File(file.getParentFile(), ".gitlet")).isDirectory()) {
            throw new IllegalArgumentException("not .gitlet working directory");
        }
        if (!file.isDirectory()) {
            return file.delete();
        } else {
            return false;
        }
    }

    /** Deletes the file named FILE if it exists and is not a directory.
     *  Returns true if FILE was deleted, and false otherwise.  Refuses
     *  to delete FILE and throws IllegalArgumentException unless the
     *  directory designated by FILE also contains a directory named .gitlet. */
    /**删除名为file的文件（如果该文件存在且不是目录）。
     *如果FILE已删除，则返回true，否则返回false。拒绝
     *删除FILE并抛出IllegalArgumentException，除非
     *FILE指定的目录还包含一个名为.gitlet的目录*/
    static boolean restrictedDelete(String file) {
        return restrictedDelete(new File(file));
    }

    /* READING AND WRITING FILE CONTENTS */

    /** Return the entire contents of FILE as a byte array.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    /*读取和写入文件内容*/
    /**将FILE的全部内容作为字节数组返回。FILE必须
     *是一个普通文件。引发非法争论异常
     *如果出现问题*/
    static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            System.out.println(file.getPath());
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Return the entire contents of FILE as a String.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    /**将FILE的全部内容作为字符串返回。FILE必须
     *是一个普通文件。引发非法争论异常
     *如果出现问题*/
    static String readContentsAsString(File file) {
        return new String(readContents(file), StandardCharsets.UTF_8);
    }

    /** Write the result of concatenating the bytes in CONTENTS to FILE,
     *  creating or overwriting it as needed.  Each object in CONTENTS may be
     *  either a String or a byte array.  Throws IllegalArgumentException
     *  in case of problems. */
    /**将CONTENTS中的字节连接到FILE的结果写入，
     *根据需要创建或覆盖它。CONTENTS中的每个对象可以是
     *字符串或字节数组。引发非法争论异常
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
    /**返回从FILE读取的T类型的对象，将其强制转换为EXPECTEDCLASS。
     *出现问题时抛出IllegalArgumentException*/
    static <T extends Serializable> T readObject(File file,
                                                 Class<T> expectedClass) {
        if (file.exists()){
            System.out.println("file.exists");
        }
        if (file.isFile()){
            System.out.println("file.isFile");
        }
        System.out.println(file.getParentFile());
        System.out.println(file.getPath());
        try {
            InputStream in1 = new FileInputStream(file);
            System.out.println("kaishi");
            if (in1 == null){
                System.out.println("因为我");
            }
            ObjectInputStream in =
                new ObjectInputStream(in1);
            System.out.println("jieshu");
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

    /* DIRECTORIES */

    /** Filter out all but plain files. */
    /*目录*/
    /**过滤掉除普通文件外的所有文件*/
    private static final FilenameFilter PLAIN_FILES =
        new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        };

    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    /**返回目录DIR中所有普通文件的名称列表
     *字典顺序为Java字符串。如果DIR为空，则返回null
     *不表示目录*/
    static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    /**返回目录DIR中所有普通文件的名称列表
     *字典顺序为Java字符串。如果DIR为空，则返回null
     *不表示目录*/
    static List<String> plainFilenamesIn(String dir) {
        return plainFilenamesIn(new File(dir));
    }

    /* OTHER FILE UTILITIES */
    /*其他文件实用程序*/

    static File join(String first, String... others) {
        return Paths.get(first, others).toFile();
    }


    static File join(File first, String... others) {
        //file.getPath返回此抽象路径名的路径名字符串形式  例如当前文件的文件名为 test.txt  返回test.txt
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
    /** Return a GitletException whose message is composed from MSG and ARGS as
     *  for the String.format method. */
    /*消息和错误报告*/
    /**返回一个GitletException，其消息由MSG和ARGS组成
     *字符串。格式方法*/
    static GitletException error(String msg, Object... args) {
        return new GitletException(String.format(msg, args));
    }

    /** Print a message composed from MSG and ARGS as for the String.format
     *  method, followed by a newline. */
    /**按String.format打印由MSG和ARGS组成的消息
     *方法，后跟换行符*/
    static void message(String msg, Object... args) {
        System.out.printf(msg, args);
        System.out.println();
    }

    /**
     *
     * 转换成编译器指定的日期格式
     * @param date
     * @return
     */
    private static String dateToTimeStamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(date);
    }
}
