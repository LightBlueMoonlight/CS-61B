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
            Utils.message("Please enter a command.");
            System.exit(0);
        }

//        if (args.length > 1){
//            Utils.message("Incorrect operands.");
//            System.exit(0);
//        }
        //checkout -- [file name]
        //checkout [commit id] -- [file name]
        String firstArg = args[0];
        Object text;
        switch(firstArg) {
            case "init":
                Repository.setInit();
//                validateNumArgs("story", args, 2);
//                text = args[1];
//                CapersRepository.writeStory(text);
                break;
            case "add":
                // TODO: handle the `add [filename]` command处理“add[filename]”命令
                validateNumArgs("story", args, 2);
                text = args[1];
                Repository.setAdd(text);
                break;
            // TODO: FILL THE REST IN把剩下的填满
            case "commit":
                break;
            case "log":
                break;
            case "rm":
                break;
            case "find":
                break;
            default:
            Utils.message("No command with that name exists.");
            System.exit(0);
                break;
        }
    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}
