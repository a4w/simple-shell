import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Terminal{
    final static String _home = System.getProperty("user.dir") + '/';
    private Path dir;

    static class Execution{
        enum ExitCode{
            SUCCESS, ERROR, COMMAND_NOT_FOUND, INVALID_ARGUMENTS, SYNTAX_ERROR
        };
        String output;
        ExitCode exit_code;
    };

    Terminal(){
        // Set home to shell location
        this.dir = Paths.get(_home);
    }

    String getCurrentDir(){
        return this.dir.toString();
    }

    String expandPath(String path){
        Path p = Paths.get(path);
        if(!p.isAbsolute()){
            p = Paths.get(getCurrentDir() + File.separatorChar + path);
        }
        return p.normalize().toAbsolutePath().toString();
    }

    Execution run(String cmd){
        Parser parser = new Parser();
        Execution exec = new Execution();
        if(!parser.parse(cmd)){
            exec.exit_code = Execution.ExitCode.SYNTAX_ERROR;
            return exec;
        }
        // Inside each case, validate arguments, if no match, return ExitCode.INVALID_ARGUMENTS and any extra errors in output else return execution
        String[] args = parser.getArguments();
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
        exec.output = getCurrentDir() + '\n';
        return exec;
    }
};
