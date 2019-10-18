package yanagishima.servlet.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/")
public class DefaultPath {
    @GET
    public Response get() {
        return Response.status(Status.OK).build();
    }
}
