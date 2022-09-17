import static spark.Spark.get;


public class Controller {

    public static void manageRequests(){

        /*path("/api", () -> {

            get("/parliament", Service.parliament);

            path("/exchange", () -> {
                post("/ontology", Service.exchangeOntology);
                put("/alignments", Service.exchangeAlignments);
            });

        });*/
        get("/nanana", Service.parliament);
        //while(true);
    }

}
