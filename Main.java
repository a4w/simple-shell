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

            Terminal.Return execution = term.run(cmd);

            if(execution.exit_code == Terminal.ExitCode.TERMINATE)
                running = false;

            if(execution.exit_code == Terminal.ExitCode.SUCCESS)
                System.out.print(execution.output);
            else
                System.err.print(execution.output);
        }
        reader.close();
    }
};
