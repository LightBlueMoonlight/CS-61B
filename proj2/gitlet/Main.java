package gitlet;
/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if(args == null){
            gitlet.Utils.message("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        String text;
        switch(firstArg) {
            case "init":
                validateNumArgs( args, 1);
                Repository.setInit();
                break;
            case "add":
                Repository.checkDir();
                validateNumArgs(args, 2);
                text = args[1];
                Repository.setAdd(text);
                break;
            case "commit":
                Repository.checkDir();
                validateNumArgs(args, 2);
                text = args[1];
                if (text.length()==0){
                    NotherUtils.message("Please enter a commit message.");
                }
                Repository.setCommit(text);
                break;
            case "log":
                Repository.checkDir();
                validateNumArgs(args, 1);
                break;
            case "rm":
                break;
            case "find":
                break;
            default:
                NotherUtils.message("No command with that name exists.");
                break;
        }
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            Utils.message("Incorrect operands.");
            System.exit(0);
        }
    }
}
