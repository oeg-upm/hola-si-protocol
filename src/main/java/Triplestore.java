

import spark.Request;
import spark.Response;
import spark.Route;

import java.net.URI;
import java.net.URISyntaxException;

import sparql.streamline.core.SparqlEndpoint;
import sparql.streamline.core.SparqlEndpointConfiguration;

import org.apache.jena.sparql.resultset.ResultsFormat;

import java.io.ByteArrayOutputStream;


public class Triplestore {

    private static URI updateEnpoint = null;
    private static URI queryEnpoint = null;
    private static String username = null;
    private static String password = null;

    public Triplestore( String queryEnpoint, String updateEnpoint) throws URISyntaxException {
        this.updateEnpoint = new URI(updateEnpoint);
        this.queryEnpoint = new URI(queryEnpoint);
        username = "admin";
        password = "pw123";

    }

    public Triplestore( URI queryEnpoint, URI updateEnpoint) {
        this.updateEnpoint = updateEnpoint;
        this.queryEnpoint = queryEnpoint;
    }

    public static SparqlEndpoint getSparqlEndpoint() {
        return new SparqlEndpoint(new SparqlEndpointConfiguration(queryEnpoint.toString(), updateEnpoint.toString(), username, password));
    }

    public static ResultsFormat guess(String str) {
        return ResultsFormat.lookup(str);
    }

    public static ByteArrayOutputStream query(String sparql, ResultsFormat format) throws Exception {
        try {
            return getSparqlEndpoint().query(sparql, format);
        } catch (Exception e) {
            throw new Exception(e.toString());
        }
    }

    public static void update(String sparql) throws Exception{
        try {
            getSparqlEndpoint().update(sparql);
        } catch (Exception e) {
            throw new Exception(e.toString());
        }
    }

}