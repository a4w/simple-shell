import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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
            case DELETE_DIR:
                if(args.length == 1)
                    exec = this.rmdir(args[0]);
                else
                    exec.exit_code = Execution.ExitCode.INVALID_ARGUMENTS;
                break;
            case CREATE_DIR:
                if(args.length == 1)
                    exec = this.mkdir(args[0]);
                else
                    exec.exit_code = Execution.ExitCode.INVALID_ARGUMENTS;
                break;
            case PRINT_HELP:
                if(args.length == 1)
                    exec = this.help(args[0]);
                else
                    exec.exit_code = Execution.ExitCode.INVALID_ARGUMENTS;
                break;
            case PRINT_ARGS:
                if(args.length == 1)
                    exec = this.args(args[0]);
                else
                    exec.exit_code = Execution.ExitCode.INVALID_ARGUMENTS;
                break;

            case CONCATENATE:
                exec = cat(args, stdin);
                break;

            case PIPE:
                exec = pipe(args[0], args[1], stdin);
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
        exec.exit_code = Execution.ExitCode.SUCCESS;
        exec.output = "File Copied successfully\n";
        File src = Paths.get(expandPath(oldPath)).toFile();
        File dist;
        try {
            if(Paths.get(expandPath(oldPath)).toFile().isDirectory()
                && Paths.get(expandPath(newPath)).toFile().isDirectory()) {
                File[] listOfFiles = src.listFiles();
                for(File f : listOfFiles){
                    if(f.isFile()) {
                        dist = new File(newPath + File.separatorChar + f.getName());
                        copy(oldPath + File.separatorChar + f.getName(), dist);                     
                    }
                }                               
                
            }else if(Paths.get(expandPath(oldPath)).toFile().isFile()
                    && Paths.get(expandPath(newPath)).toFile().isDirectory()) {
                dist = new File(newPath + File.separatorChar + src.getName());
                copy(oldPath, dist);                
                
            }else{
                dist = new File(newPath);
                copy(oldPath, dist);
                exec.exit_code = Execution.ExitCode.SUCCESS;
            }
        }catch (Exception e) {
            exec.output = "Please Provide a Valid File name/Directory \n";
            exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
        }
        return exec;
    }
    void copy(String oldPath, File dist) throws IOException {
        FileInputStream copyFile = new FileInputStream(expandPath(oldPath));
        FileOutputStream pasteFile = new FileOutputStream(dist.getAbsolutePath());
        
        byte[] buffer = new byte[1024];
        int length;
        
        while ((length = copyFile.read(buffer)) > 0) {
            pasteFile.write(buffer, 0, length);
        }
        copyFile.close();
        pasteFile.close();          
    
    }
    Execution mv(String oldPath, String newPath){
        Execution exec = new Execution();
        File file = new File(expandPath(oldPath));
        exec.exit_code = Execution.ExitCode.SUCCESS;
        if(file.isDirectory() && file.exists()) {
        	newPath = newPath + File.separatorChar + mkdir(newPath + File.separatorChar + file.getName()).output;        	
            File[] listOfFiles = file.listFiles();
            for(File f: listOfFiles) exec = mv(f.getAbsolutePath(), newPath);
        }
        exec = this.cp(oldPath, newPath);
        if(exec.exit_code.equals(Execution.ExitCode.READ_WRITE_ERROR)) return exec;
        file.delete();
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
    Execution rmdir(String path) {
        Execution exec = new Execution();
        if(Paths.get(expandPath(path)).toFile().isDirectory()) {
            final File directory = new File(expandPath(path));
            try{
                delete(directory);
                exec.exit_code = Execution.ExitCode.SUCCESS;
                exec.output = "";
                exec.output += directory.getName() + " was removed Successfully.\n";
            }
            catch(IOException e){
                exec.exit_code = Execution.ExitCode.ERROR;
                exec.output = "Directory can't be removed.\n";
            }
        }
        else {
            exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
            exec.output = "Path specified is not a valid directory.\n";
        }
        return exec;
    }
    public static void delete(File directory) throws IOException{
        if(directory.isDirectory()) {
            File[] files = directory.listFiles();
            for(File file : files)
                delete(file);
            directory.delete();
        }
        else if(directory.isFile()) {
            directory.delete();
        }
        else
            throw new IOException("error");
    }
    Execution mkdir(String path) {
        Execution exec = new Execution();
        final File directory = new File(expandPath(path));
        if(Paths.get(expandPath(path)).toFile().isDirectory()) {
            exec.exit_code = Execution.ExitCode.ERROR;
            exec.output = "";
            exec.output += directory.getName(); //XD
        }
        else {
            String src = path;
            int len = 0;
            String folder = "";
            ArrayList<String> directories = new ArrayList<String>();
            while(!Paths.get(expandPath(src)).toFile().isDirectory() && src.length() != 0) {
                folder = Paths.get(expandPath(src)).toFile().getName();
                len += folder.length() + 1;
                directories.add(folder);
                src = "";
                for(int i = 0 ; i < path.length() - len ; i++)
                    src += path.charAt(i);
            }
            for(int i = directories.size() - 1 ; i >= 0 ; i--) {
                File newFolder = new File(expandPath(src) + File.separatorChar + directories.get(i));
                newFolder.mkdir();
                src += File.separatorChar + newFolder.getName();
            }
            exec.exit_code = Execution.ExitCode.SUCCESS;
            exec.output = directory.getName(); //Sorry i need it trimmed
        }
        return exec;
    }
    Execution help(String command) {
        Execution exec = new Execution();
        switch(command) {
        case "cd":
            exec.output = command + " : is used to change the current directory.\n";
            break;
        case "ls":
            exec.output = command + " : is used to list information about the files in the current directory.\n";
            break;
        case "cp":
            exec.output = command + " : is used to copy source to destination, or multiple sources to directory.\n";
            break;
        case "cat":
            exec.output = command + " : is used to concatenate files and print on the standard output.\n";
            break;
        case "more":
            exec.output = command + " : is used to view file or standard input one screenful at a time.\n";
            break;
        case "mkdir":
            exec.output = command + " : allows the user to create directories.\n";
            break;
        case "rmdir":
            exec.output = command + " : removes the directory if it is empty.\n";
            break;
        case "mv":
            exec.output = command + " : used to move or rename files.\n";
            break;
        case "rm":
            exec.output = command + " : removes files or directories.\n";
            break;
        case "args":
            exec.output = command + " : lists all parameters on the command line, number of strings for specific command.\n";
            break;
        case "date":
            exec.output = command + " : displays or sets time.\n";
            break;
        case "help":
            exec.output = command + " : displays what a command does.\n";
            break;
        case "pwd":
            exec.output = command + " : prints name of current directory.\n";
            break;
        case "clear":
            exec.output = command + " : clears the terminal screen.\n";
            break;
        default:
            exec.exit_code = Execution.ExitCode.ERROR;
            exec.output = "Command doesn't exist.\n";
            return exec;
        }
        exec.exit_code = Execution.ExitCode.SUCCESS;
        return exec;
    }
    Execution args(String command) {
        Execution exec = new Execution();
        switch(command) {
        case"cd":
            exec.output = "arg1 : SourcePath.\n";
            break;
        case"ls":
            exec.output = "arg1 : SourcePath.\n";
            break;
        case"more":
            exec.output = "arg1 : files, arg2 : stdin.\n";
            break;
        case"mv":
            exec.output = "arg1 : SourcePath, arg2 : DestinationPath.\n";
            break;
        case"clear":
            exec.output = "no arguments.\n";
            break;
        case"rm":
            exec.output = "arg1 : SourcePath.\n";
            break;
        case"date":
            exec.output = "no arguments.\n";
            break;
        case"pwd":
            exec.output = "no arguments.\n";
            break;
        case"cp":
            exec.output = "arg1 : SourcePath, arg2 : DestinationPath.\n";
            break;
        case"cat":
            exec.output = "can have 0 or n arguments.\n";
            break;
        case"rmdir":
            exec.output = "arg1 : SourcePath.\n";
            break;
        case"mkdir":
            exec.output = "arg1 : DestinationPath.\n";
            break;
        case"help":
            exec.output = "arg1 : CommandName.\n";
            break;
        case"args":
            exec.output = "arg1 : CommandName.\n";
            break;
        default:
            exec.exit_code = Execution.ExitCode.ERROR;
            exec.output = "Command doesn't exist.\n";
            return exec;
        }
        exec.exit_code = Execution.ExitCode.SUCCESS;
        return exec;
    }
    
    Execution cat(String[] listOfFiles, String userInput){
        Execution exec = new Execution();
        exec.exit_code = Execution.ExitCode.SUCCESS ;
        exec.output = userInput == null ? "" : userInput + "\n********************-------------*************************\n";
        if(listOfFiles.length == 0 && userInput == null){
            // Take input
            Scanner sc = new Scanner(System.in);
            String line;
            exec.output = "";
            exec.exit_code = Execution.ExitCode.SUCCESS;
            while(sc.hasNextLine()) {
                line = sc.nextLine();
                if(line.equals("fml")) break; //down mentally
                exec.output += line + '\n';
            }
            exec.output += "\n********************-------------*************************\n";
        }
        if(listOfFiles.length > 0) {
            exec.exit_code = Execution.ExitCode.SUCCESS ;
            File tempFile;
            String line;
            BufferedReader in;
            for(int i = 0; i < listOfFiles.length; ++i) {
                tempFile = Paths.get(expandPath(listOfFiles[i])).toFile();
                if(!tempFile.isFile()) {
                    exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
                    exec.output = "Please Provide a Valid File name/Directory \n";
                    break;
                }
                try{
                    in = new BufferedReader(new FileReader(tempFile.getAbsolutePath()));
                    while((line = in.readLine()) != null)
                        exec.output += line + '\n' ;
                    exec.output += "\n********************-------------*************************\n" ;
                    in.close();
                }catch(IOException e){
                    exec.exit_code = Execution.ExitCode.READ_WRITE_ERROR;
                }
            }
        }
        return exec;
    }
    Execution pipe(String leftCommand, String rightCommand, String stdin) {
        Execution exec = new Execution();
        exec = run(leftCommand, stdin);
        exec = run(rightCommand, exec.output);
        return exec;
    }
};
