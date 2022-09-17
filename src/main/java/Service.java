import spark.Request;
import spark.Response;
import spark.Route;


public class Service {



    public static Route parliament = (Request request, Response response) -> {
        response.type("text/xml");
        response.status(200);
        if(true) { //evaluar el parliament
            return "si";
        }
        return null;
    };

    public static Route exchangeOntology = (Request request, Response response) -> {

        return null;
    };

    public static Route exchangeAlignments = (Request request, Response response) -> {

        return null;
    };

}
