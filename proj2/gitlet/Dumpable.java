package gitlet;

import java.io.Serializable;

/** An interface describing dumpable objects.
 *  @author P. N. Hilfinger
 */
interface Dumpable extends Serializable {
    /** 在System.out上打印有关此对象的有用信息。 */
    void dump();
}
