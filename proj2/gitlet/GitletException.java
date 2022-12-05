package gitlet;

/** General exception indicating a Gitlet error.  For fatal errors, the
 *  result of .getMessage() is the error message to be printed.
 *  @author P. N. Hilfinger
 *  指示Gitlet错误的常规异常。对于致命错误etMessage（）的结果是要打印的错误消息。
 */
class GitletException extends RuntimeException {


    /** A GitletException with no message. 没有消息的GitletException。*/
    GitletException() {
        super();
    }

    /** A GitletException MSG as its message. GitletException MSG作为其消息。*/
    GitletException(String msg) {
        super(msg);
    }

}
