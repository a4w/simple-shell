import java.util.Scanner;

public class Main{
    public static void main(String args[]){
        Scanner reader = new Scanner(System.in);
        boolean running = true;
        Terminal term = new Terminal();
        while(running){
            // Read command from standard input
            System.out.print("➜ ");
            String cmd = reader.nextLine();

            Terminal.Execution exec = term.run(cmd);

            if(exec == null){
                running = false;
                continue;
            }

            if(exec.exit_code == Terminal.Execution.ExitCode.SUCCESS)
                System.out.print(exec.output);
            else
                System.err.print(exec.output);
        }
        reader.close();
    }
};
