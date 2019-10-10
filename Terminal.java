public class Terminal{
    private String dir;
    Terminal(){
        // Set home to shell location
        this.dir = System.getProperty("user.dir");
    }

    String pwd(){
        return this.dir;
    }
};
