package App;

import java.util.HashMap;

import static java.lang.Boolean.parseBoolean;
import static spark.Spark.*;

public class App{

    public static String CLIENT_NAME;
    public static String CLIENT_TARGET_NAME;
    public static String CLIENT_STARTER;

    public static void main (String args[]) {
        CLIENT_NAME = args[0];

        setUp();

        //Client and server are the same (p2p), so we need to detect which client is the one starting the communication and which is listening to requests
        if(App.CLIENT_NAME.equals(App.CLIENT_STARTER)) Controller.initProcess();
        Controller.manageRequests();

    }

    public static void setUp() {

        if(CLIENT_NAME.equals("client2")) port(4568);

        HashMap<String, String> config = Service.obtainConfig();
        CLIENT_TARGET_NAME = config.get("peer");

        if(parseBoolean(config.get("starts"))) CLIENT_STARTER = CLIENT_NAME;
        else CLIENT_STARTER = config.get("peer");
    }

}
