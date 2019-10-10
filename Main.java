import java.util.Scanner;

public class Main{
    public static void main(String args[]){
        Scanner reader = new Scanner(System.in);
        boolean running = true;
        Terminal term = new Terminal();
        while(running){
            Parser parser = new Parser();
            // Read command from standard input
            System.out.print("➜ ");
            String cmd = reader.nextLine();

            try{
                // Attempt to parse command
                parser.parse(cmd);
            }catch(Exception e){
                System.out.println("Error parsing command");
                continue;
            }
            switch(parser.getCommand()){
                default:
                    System.out.println("Command not found");
                    break;
                case EXIT:
                    System.out.println("Bye");
                    running = false;
                    break;
                case COPY:
                    // TODO: Implement cp
                    break;
                case PWD:
                    System.out.println(term.pwd());
            }
        }
        reader.close();
    }
};
