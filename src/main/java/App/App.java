package App;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import static java.lang.Boolean.parseBoolean;
import static spark.Spark.*;

public class App{

    public static String CLIENT_NAME;
    public static String CLIENT_STARTER;
    public static String LINKS_TYPE;

    public static void main (String args[]) {
        CLIENT_NAME = args[0];
        LINKS_TYPE = args[1];

        if(args.length != 2) throw new IllegalArgumentException("2 arguments must be provided: client name and links type");

        setUp();

        //Client and server are the same (p2p), so we need to detect which client is the one starting the communication and which is listening to requests
        if(App.CLIENT_NAME.equals(App.CLIENT_STARTER)) {
            Controller.initProcess();
            try {
                FileWriter f = new FileWriter("client1/logs/state.log", true);
                f.write(CLIENT_NAME);
                f.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }

        else Controller.manageRequests();

    }

    public static void setUp() {

        if(CLIENT_NAME.equals("client2")) port(4568);

        HashMap<String, String> config = Service.obtainConfig();

        if(parseBoolean(config.get("starts"))) CLIENT_STARTER = CLIENT_NAME;
        else CLIENT_STARTER = config.get("peer");
    }

}
