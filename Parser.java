public class Parser{
    public enum Command{
        NONE, EXIT, COPY, MOVE, MKDIR, PWD
    };

    private Command cmd;
    private String[] args;

    Parser(){
        this.cmd = null;
        this.args = null;
    }

    public boolean parse(String input){
        this.cmd = Command.PWD;
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
