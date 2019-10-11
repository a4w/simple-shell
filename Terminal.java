import java.io.File;
import java.nio.file.Path;

public class Terminal{
    final static String _home = System.getProperty("user.dir") + '/';
    private String dir;

    static class Execution{
        enum ExitCode{
            SUCCESS, ERROR, COMMAND_NOT_FOUND, INVALID_ARGUMENTS, SYNTAX_ERROR
        };
        String output;
        ExitCode exit_code;
    };

    Terminal(){
        // Set home to shell location
        this.dir = _home;
    }

    String expandPath(String path){
        File file = new File(path);
        if(file.isAbsolute()){
            return path;
        }else{
            return this.dir + path;
        }
    }

    Execution run(String cmd){
        Parser parser = new Parser();
        Execution exec = new Execution();
        if(!parser.parse(cmd)){
            exec.exit_code = Execution.ExitCode.SYNTAX_ERROR;
            return exec;
        }
        // Inside each case, validate arguments, if no match, return ExitCode.INVALID_ARGUMENTS and any extra errors in output else return execution
        switch(parser.getCommand()){
            case EXIT:
                exec = null; // Special case to end program
                break;
            case PRINT_WORKING_DIR:
                exec = this.pwd();
                break;
            default:
                exec.exit_code = Execution.ExitCode.COMMAND_NOT_FOUND;
                break;
        }
        return exec;
    }

    Execution pwd(){
        Execution exec = new Execution();
        exec.exit_code = Execution.ExitCode.SUCCESS;
        exec.output = this.dir + '\n';
        return exec;
    }
};
