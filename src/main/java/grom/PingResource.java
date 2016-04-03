package grom;
import javax.ws.rs.*;

@Path("/ping")
public class PingResource {
    @GET
    public String ping() {
        return "pong";
    }
}
