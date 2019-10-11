import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class Parser{
    public enum Command{
        NONE, EXIT, COPY, MOVE, CHANGE_DIR, LIST_DIR, CONCATENATE, MORE, CREATE_DIR, DELETE_DIR, DELETE_FILE, PRINT_ARGS, PRINT_DATE, PRINT_HELP, PRINT_WORKING_DIR, CLEAR_SCREEN,
        PIPE, OUTPUT_REDIRECT, OUTPUT_REDIRECT_APPEND, NOT_REGISTERED
    };

    private Command cmd;
    private String[] args;

    Parser(){
        this.cmd = null;
        this.args = null;
    }

    public boolean parse(String input){
        /*
         * Precendence: (|) (>,>>) (commands)
         */
        // Partition command into words
        int pipe = -1, oredirect = Integer.MAX_VALUE, aredirect = Integer.MAX_VALUE;
        input += '.'; // Used to prevent out of bounds
        ArrayList<String> parts = new ArrayList<String>();
        String current = "";
        Character escape = null;
        for(int i = 0; i < input.length() - 1; ++i){
            // Handle quoted partitions
            if(input.charAt(i) == '\'' || input.charAt(i) == '"'){
                if(escape != null && input.charAt(i) == escape){
                    escape = null;
                    if(!current.equals("")) parts.add(current);
                    current = "";
                }else if(escape != null){
                    current += input.charAt(i);
                }else{
                    escape = input.charAt(i);
                }
            }else if(escape != null){
                current += input.charAt(i);
            }else{
                // Word boundaries are <Space>, "|", ">", ">>"
                if(input.charAt(i) == ' '){
                    if(!current.equals("")) parts.add(current);
                    current = "";
                }else if(input.charAt(i) == '|'){
                    pipe = i;
                    break;
                }else if(input.charAt(i) == '>'){
                    if(input.charAt(i+1) == '>')
                        aredirect = i;
                    else
                        oredirect = i;
                    break;
                }else{
                    current += input.charAt(i);
                }
            }
        }
        if(escape != null) return false;
        if(!current.equals("")) parts.add(current);
        current = "";
        if(pipe != -1){
            this.args = new String[2];
            this.args[0] = input.substring(0, pipe);
            this.args[1] = input.substring(pipe+1, input.length() - 1);
            this.cmd = Command.PIPE;
        }else if(oredirect != Integer.MAX_VALUE || aredirect != Integer.MAX_VALUE){
            // Precendence left to right
            if(oredirect < aredirect){
                this.args = new String[2];
                this.args[0] = input.substring(0, oredirect);
                this.args[1] = input.substring(oredirect+1, input.length() - 1);
                this.cmd = Command.OUTPUT_REDIRECT;
            }else if(aredirect < oredirect){
                this.args = new String[2];
                this.args[0] = input.substring(0, aredirect);
                this.args[1] = input.substring(aredirect+2, input.length() - 1);
                this.cmd = Command.OUTPUT_REDIRECT_APPEND;
            }
        }else{
            if(parts.isEmpty())
                parts.add("__NO__COMMAND__");
            this.args = new String[parts.size() - 1];
            for(int i = 1; i < parts.size(); ++i) this.args[i-1] = parts.get(i);

            switch(parts.get(0)){
                case "cp":
                    this.cmd = Command.COPY;
                    break;
                case "mv":
                    this.cmd = Command.MOVE;
                    break;
                case "cd":
                    this.cmd = Command.CHANGE_DIR;
                    break;
                case "ls":
                    this.cmd = Command.LIST_DIR;
                    break;
                case "cat":
                    this.cmd = Command.CONCATENATE;
                    break;
                case "more":
                    this.cmd = Command.MORE;
                    break;
                case "mkdir":
                    this.cmd = Command.CREATE_DIR;
                    break;
                case "rmdir":
                    this.cmd = Command.DELETE_DIR;
                    break;
                case "rm":
                    this.cmd = Command.DELETE_FILE;
                    break;
                case "args":
                    this.cmd = Command.PRINT_ARGS;
                    break;
                case "date":
                    this.cmd = Command.PRINT_DATE;
                    break;
                case "help":
                    this.cmd = Command.PRINT_HELP;
                    break;
                case "pwd":
                    this.cmd = Command.PRINT_WORKING_DIR;
                    break;
                case "clear":
                    this.cmd = Command.CLEAR_SCREEN;
                    break;
                case "__NO__COMMAND__":
                    this.cmd = Command.NONE;
                    break;
                case "exit":
                    this.cmd = Command.EXIT;
                    break;
                default:
                    this.cmd = Command.NOT_REGISTERED;
                    break;
            }
        }
        return true;
    }

    public Command getCommand(){
        if(this.cmd == null)
            return Command.NONE;
        return this.cmd;
    }

    public String[] getArguments(){
        return this.args;
    }
};
