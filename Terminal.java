import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Terminal{
    final static String _home = System.getProperty("user.dir") + File.separatorChar;
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
            case LIST_DIR:
                if(args.length == 0){
                    exec = this.ls(getCurrentDir());
                }else if(args.length == 1){
                    exec = this.ls(args[0]);
                }else{
                    exec.exit_code = Execution.ExitCode.INVALID_ARGUMENTS;
                }
                break;
            case COPY:
                if(args.length == 2){
                    exec = this.cp(args[0], args[1]);
                }else{
                    exec.exit_code = Execution.ExitCode.INVALID_ARGUMENTS;
                }
                break;
            case MOVE:
                if(args.length == 2){
                    exec = this.mv(args[0], args[1]);
                }else{
                    exec.exit_code = Execution.ExitCode.INVALID_ARGUMENTS;
                }
                break;
            case CLEAR_SCREEN:
                exec = this.clear();
                break;
            case DELETE_FILE:
                if(args.length == 1)
                    exec = this.rm(args[0]);
                else
                    exec.exit_code = Execution.ExitCode.INVALID_ARGUMENTS;
                break;
            case PRINT_DATE:
            	if(args.length != 0)
            		exec.exit_code = Execution.ExitCode.INVALID_ARGUMENTS;
            	else
            		exec = date();
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
    Execution ls(String path) {
        Execution exec = new Execution ();
        if(Paths.get(expandPath(path)).toFile().isDirectory()){
            exec.exit_code = Execution.ExitCode.SUCCESS;
            final File folder = new File(expandPath(path));
            File[] listOfFiles = folder.listFiles();
            exec.output = "";
            for(int i = 0; i < listOfFiles.length; ++i) {
                exec.output += listOfFiles[i].getName()+ '\n';
            }
            exec.output += '\n';
        }else{
            exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
            exec.output = "Path specified is not a valid directory\n";
        }
        return exec;
    }
    Execution cp(String oldPath, String newPath){
        Execution exec = new Execution ();
        
        File src = new File(oldPath);
        File dist;
                
        if(newPath.charAt(newPath.length() - 1) == File.separatorChar ) {
        	///copy file to newPath with the same name
        	dist = new File(newPath + src.getName());
        	try {
				dist.createNewFile();
			} catch (IOException e) {
				exec.output = "Please Provide a Valid File Directory\n";
				exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
				
			}
        }else if(Paths.get(expandPath(oldPath)).toFile().isFile() 
        		&& Paths.get(expandPath(newPath)).toFile().isDirectory()){
        	
            try{
            	dist = new File(newPath);
            	dist.createNewFile();
            	
            	FileInputStream copyFile = new FileInputStream(expandPath(oldPath));
            	FileOutputStream pasteFile = new FileOutputStream(dist.getAbsolutePath());
            	
                byte[] buffer = new byte[1024];
                int length;
                
                while ((length = copyFile.read(buffer)) > 0) {
                    pasteFile.write(buffer, 0, length);
                }
                exec.exit_code = Execution.ExitCode.SUCCESS;
                exec.output = "File Copied successfully\n";
                
                copyFile.close();
                pasteFile.close();
            }catch(IOException e){
            	exec.output = "Please Provide a Valid File name\n";
                exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
            }
        }else{
            exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
            exec.output = "Path specified is not a valid directory\n";
        }
        return exec;
    }

    Execution mv(String oldPath, String newPath){
        Execution exec = this.cp(oldPath, newPath);
        if(exec.exit_code.equals(Execution.ExitCode.READ_WRITE_ERROR)) return exec;
        File file = new File(expandPath(oldPath));
        if(file.delete()) {
            exec.output = "File moved successfully\n";
            exec.exit_code = Execution.ExitCode.SUCCESS;
        } else {
            exec.output = "Failed to move the file\n";
            exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
        }
        return exec;
    }
    Execution clear() {
        Execution exec = new Execution();
        exec.exit_code = Execution.ExitCode.SUCCESS;
        exec.output = new String(new char[1000]).replace('\0', '\n');;
        return exec;
    }
    Execution rm(String path) {
        Execution exec = new Execution();
        if(Paths.get(expandPath(path)).toFile().isFile()){
            final File file = new File(expandPath(path));
            exec.exit_code = Execution.ExitCode.SUCCESS;
            exec.output = "";
            exec.output += file.getName() + " was removed successfully.\n";
            file.delete();
        }
        else{
            exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
            exec.output = "Path specified is not a valid file.\n";
        }
        return exec;
    }
    Execution date() {
    	Execution exec = new Execution();
    	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
    	LocalDateTime now = LocalDateTime.now();  
		exec.output = dtf.format(now) + '\n';
		exec.exit_code = Execution.ExitCode.SUCCESS;
		return exec;
    }
};
