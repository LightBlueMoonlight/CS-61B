package gitlet;

import java.io.Serializable;

public class Blob implements Serializable {
    private static final long userId = 134252411453L;
    private String Type;
    private long size;
    private byte[] content;
    private String hash;
}
