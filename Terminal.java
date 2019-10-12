import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    Execution run(String cmd, String stdin) throws IOException{
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
            	if(args.length == 0){
                    exec = this.rm(getCurrentDir());
                }else if(args.length == 1){
                    exec = this.rm(args[0]);
                }else{
                    exec.exit_code = Execution.ExitCode.INVALID_ARGUMENTS;
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
    Execution cp(String oldPath, String newPath) throws IOException {
    	Execution exec = new Execution ();
    	int lastIndex = newPath.lastIndexOf('/');
    	FileInputStream copyFile;
		FileOutputStream pasteFile;
    	String pasteFileName = newPath.substring(lastIndex + 1);
    	newPath = newPath.substring(0, lastIndex+1);

		if(lastIndex == newPath.length() ) {
			exec.output = "Please Provide a Valid File name\n";
			exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
		}else if(Paths.get(expandPath(oldPath)).toFile().isFile() 
				&& Paths.get(expandPath(newPath)).toFile().isDirectory()){
			
			copyFile = new FileInputStream(oldPath);
			pasteFile = new FileOutputStream(newPath + pasteFileName);
	        byte[] buffer = new byte[1024];
	        int length;
	        
	        while ((length = copyFile.read(buffer)) > 0) {
	            pasteFile.write(buffer, 0, length);
	        }
	        
			exec.exit_code = Execution.ExitCode.SUCCESS;
			exec.output = "File Copied successfully";
		}else{
			exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
			exec.output = "Path specified is not a valid directory\n";
		}
		
    	  return exec;
    	
    }
    
    Execution mv(String oldPath, String newPath) throws IOException {
    	Execution exec = this.cp(oldPath, newPath);
    	File file = new File(oldPath); 
        
        if(file.delete()) 
        { 
            exec.output = ("File deleted successfully"); 
            exec.exit_code = Execution.ExitCode.SUCCESS;
        } 
        else
        { 
        	exec.output = ("Failed to delete the file"); 
        	exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
        } 
		return exec;
    	
    }
    Execution clear() {
    	Execution exec = new Execution();
            exec.exit_code = Execution.ExitCode.SUCCESS;
            exec.output =  "\033["+ "2J"; 
		return exec;
    	
    }
    @SuppressWarnings("unused")
	Execution rm(String path) {
    	Execution exec = new Execution();
    	if(Paths.get(expandPath(path)).toFile().isDirectory()){
    		final File folder = new File(expandPath(path));
    		if(folder != null){
    			exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
                exec.output = folder.getName() + " can't be removed\n";
    		}
    		else{
	            exec.exit_code = Execution.ExitCode.SUCCESS;
	            exec.output = "";
	            exec.output += folder.getName() + " was removed successfully\n";
	            folder.delete();
    		}
        }
    	else{
            exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
            exec.output = "Path specified is not a valid directory\n";
        }
        return exec;
    }
};
