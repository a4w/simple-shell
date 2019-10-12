import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Terminal{
    final static String _home = System.getProperty("user.dir") + '/';
    private Path dir;

    static class Execution{
        enum ExitCode{
            SUCCESS, ERROR, COMMAND_NOT_FOUND, INVALID_ARGUMENTS, SYNTAX_ERROR, READ_WRITE_ERROR
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

    Execution run(String cmd, String stdin){
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

            case CHANGE_DIR:
                if(args.length == 0){
                    exec = this.cd(_home);
                }else if(args.length == 1){
                    exec = this.cd(args[0]);
                }else{
                    exec.exit_code = Execution.ExitCode.INVALID_ARGUMENTS;
                }
                break;

            case MORE:
                if(stdin == null && args.length == 0){
                    exec.exit_code = Execution.ExitCode.INVALID_ARGUMENTS;
                }else{
                    exec = this.more(args, stdin);
                }
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

    Execution cd(String path){
        Execution exec = new Execution();
        if(Paths.get(expandPath(path)).toFile().isDirectory()){
            exec.exit_code = Execution.ExitCode.SUCCESS;
            this.dir = Paths.get(expandPath(path));
        }else{
            exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
            exec.output = "Path specified is not a valid directory\n";
        }
        return exec;
    }

    Execution more(String[] files, String stdin){
        final String bufferSeparator = "\n*********************************************\n*********************EOF*********************\n*********************************************\n\n";
        Execution exec = new Execution();
        exec.exit_code = Execution.ExitCode.SUCCESS;
        // Show stdin, then files one by one
        exec.output = stdin == null ? "" : stdin + bufferSeparator;
        for(int i = 0; i < files.length; ++i){
            Path f = Paths.get(expandPath(files[i]));
            if(!f.toFile().exists() || !f.toFile().isFile()){
                exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
                exec.output = "Arguments specified are not readable files\n";
                break;
            }
            try{
                exec.output += new String(Files.readAllBytes(f));
                exec.output += bufferSeparator;
            }catch(IOException e){
                exec.exit_code = Execution.ExitCode.ERROR;
                break;
            }
        }
        return exec;
    }
};
