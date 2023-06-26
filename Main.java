package gitlet;

import java.io.File;
import java.util.Arrays;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if(args.length==0){
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args,1);
                GitletRepository.init();
                break;
            case "add":
                init_check();
                validateNumArgs(args,2);
                GitletRepository.add(args[1]);
                break;
            case "commit":
                init_check();
                validateNumArgs(args,2);
                GitletRepository.commit(args[1]);
                break;
            case "rm":
                init_check();
                validateNumArgs(args,2);
                GitletRepository.rm(args[1]);
                break;
            case "log":
                init_check();
                validateNumArgs(args,1);
                GitletRepository.log();
                break;
            case "global-log":
                init_check();
                validateNumArgs(args,1);
                GitletRepository.global_log();
                break;
            case "find":
                init_check();
                validateNumArgs(args,2);
                GitletRepository.find(args[1]);
                break;
            case "status":
                init_check();
                validateNumArgs(args,1);
                GitletRepository.status();
                break;
            case "checkout":
                init_check();
                GitletRepository.checkout(args);
                break;
            case "branch":
                init_check();
                validateNumArgs(args,2);
                GitletRepository.branch(args[1]);
                break;
            case "rm-branch":
                init_check();
                validateNumArgs(args,2);
                GitletRepository.rm_branch(args[1]);
                break;
            case "reset":
                init_check();
                validateNumArgs(args,2);
                GitletRepository.reset(args[1]);
                break;
            case "merge":
                init_check();
                validateNumArgs(args,2);
                GitletRepository.merge(args[1]);
                break;
            case "add-remote":
                init_check();
                validateNumArgs(args,3);
                GitletRepository.add_remote(args[1],args[2]);
                break;
            case "rm-remote":
                init_check();
                validateNumArgs(args,2);
                GitletRepository.rm_remote(args[1]);
                break;
            case "push":
                init_check();
                validateNumArgs(args,3);
                GitletRepository.push(args[1],args[2]);
                break;
            case "fetch":
                init_check();
                validateNumArgs(args,3);
                GitletRepository.fetch(args[1],args[2]);
                break;
            case "pull":
                init_check();
                validateNumArgs(args,3);
                GitletRepository.pull(args[1],args[2]);
                break;
            case "test":
                String path="D:/gitdesk/test/.gitlet";
                //File remote_head_file=Utils.join(path,"HEAD");
                //Head remote_head=Utils.readObject(remote_head_file, Head.class);
                File remote_stage_file=Utils.join(path,"stage");
                Stage remote_head=Utils.readObject(remote_stage_file, Stage.class);

            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    private static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void init_check(){
        if(!Utils.join(".gitlet").exists()){
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }



}
