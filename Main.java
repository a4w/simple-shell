import java.io.IOException;
import java.util.Scanner;

public class Main{
    public static void main(String args[]){
        Scanner reader = new Scanner(System.in);
        boolean running = true;
        Terminal term = new Terminal();
        while(running){
            // Read command from standard input
            System.out.print("$ ");
            String cmd = reader.nextLine();

            Terminal.Execution exec = term.run(cmd, null);

            if(exec == null){
                System.out.println("Exiting...");
                running = false;
                continue;
            }

            if(exec.exit_code == Terminal.Execution.ExitCode.SUCCESS){
                if(exec.output != null)
                    System.out.print(exec.output);
            }else{
                switch(exec.exit_code){
                    case ERROR:
                        System.err.println("Error occurred");
                        break;
                    case INVALID_ARGUMENTS:
                        System.err.println("Invalid number of arguments supplied!!");
                        break;
                    case COMMAND_NOT_FOUND:
                        System.err.println("Command not found");
                        break;
                    case SYNTAX_ERROR:
                        System.err.println("Input is not recognized");
                        break;
                    case READ_WRITE_ERROR:
                        System.err.println("File I/O Error");
                        break;
                    default:
                        System.err.println("Something wrong happened");
                        break;
                }
                if(exec.output != null)
                    System.err.print(exec.output);
            }
        }
        reader.close();
    }
};
