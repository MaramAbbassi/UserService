package com.example.user;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.*;
import java.util.List;


@RegisterRestClient
@Path("/encheres")
@Produces("application/json")
@Consumes("application/json")
public interface EnchereClient {
    @GET
    @Path("/user/{userId}")
    List<Enchere> getEncheresByUserId(@PathParam("userId") Long userId);

    @POST
    @Path("/place/{userId}")
    void placeBid(@PathParam("userId") Long userId, Enchere enchere);
}