public class Terminal{
    final static String _home = System.getProperty("user.dir");
    private String dir;

    enum ExitCode{
        SUCCESS, ERROR, COMMAND_NOT_FOUND, INVALID_ARGUMENT, TERMINATE
    };

    class Return{
        String output;
        ExitCode exit_code;
    };

    Terminal(){
        // Set home to shell location
        this.dir = _home;
    }

    Return run(String cmd){
        Return r = new Return();
        r.exit_code = ExitCode.SUCCESS;

        Parser parser = new Parser();

        try{
            // Attempt to parse command
            parser.parse(cmd);
        }catch(Exception e){
            r.exit_code = ExitCode.COMMAND_NOT_FOUND;
            return r;
        }

        switch(parser.getCommand()){
            default:
                r.exit_code = ExitCode.ERROR;
                break;
            case EXIT:
                System.out.println("Bye");
                r.exit_code = ExitCode.TERMINATE;
                break;
            case COPY:
                // TODO: Implement cp
                break;
            case PWD:
                r.output = this.pwd();
                break;
        }
        return r;
    }

    String pwd(){
        return this.dir + '\n';
    }
};
