package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author TODO
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            gitlet.Utils.message("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        String text;
        if (firstArg.equals("rm-branch")){
            System.out.println("有输入");
            System.out.println("args："+args);
            System.out.println("args1："+args[1]);
        }
        switch (firstArg) {
            case "init":
                validateNumArgs(args, 1);
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
                if(args.length == 1){
                    NotherUtils.message("Please enter a commit message.");
                }
                text = args[1];
                if (text.length() == 0) {
                    NotherUtils.message("Please enter a commit message.");
                }
                Repository.setCommit(text);
                break;
            case "log":
                Repository.checkDir();
                validateNumArgs(args, 1);
                Repository.setLog();
                break;
            case "rm":
                Repository.checkDir();
                validateNumArgs(args, 2);
                text = args[1];
                Repository.setRM(text);
                break;
            case "find":
                Repository.checkDir();
                validateNumArgs(args, 2);
                text = args[1];
                Repository.setFind(text);
                break;
            case "global-log":
                Repository.checkDir();
                validateNumArgs(args, 1);
                Repository.setGlobalLog();
                break;
            case "branch":
                Repository.checkDir();
                validateNumArgs(args, 2);
                text = args[1];
                Repository.setBranch(text);
                break;
            case "rm-branch":
                System.out.println("什么情况");
                Repository.checkDir();
                validateNumArgs(args, 2);
                text = args[1];
                System.out.println("text"+text);
                Repository.setRmBranch(text);
                break;
            case "status":
                Repository.checkDir();
                validateNumArgs(args, 1);
                Repository.setStatus();
                break;
            case "reset":
                Repository.checkDir();
                validateNumArgs(args, 2);
                String resetCommitId = args[1];
                Repository.setReset(resetCommitId);
                break;
            case "checkout":
                Repository.checkDir();
                if (args.length == 3){
                    if (!args[1].equals("--")) {
                        NotherUtils.message("Incorrect operands.");
                    }
                    String fileName = args[2];
                    Repository.checkout(fileName);
                    break;
                }
                else if (args.length == 4){
                    if (!args[2].equals("--")) {
                        NotherUtils.message("Incorrect operands.");
                    }
                    String commitId = args[1];
                    String fileName2 = args[3];
                    Repository.checkout(commitId, fileName2);
                    break;
                }
                else if (args.length == 2){
                    String branch = args[1];
                    Repository.checkoutBranch(branch);
                    break;
                }else{
                    NotherUtils.message("Incorrect operands.");
                }
            default:
                NotherUtils.message("No command with that name exists.");
                break;
        }
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            NotherUtils.message("Incorrect operands.");
        }
    }
}
