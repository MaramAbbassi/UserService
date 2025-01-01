package com.example.user;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.*;
import java.util.List;

@RegisterRestClient
@Path("/pokemons")
@Produces("application/json")
@Consumes("application/json")
public interface PokemonClient {
    @GET
    @Path("/user/{userId}")
    List<Pokemon> getPokemonsByUserId(@PathParam("userId") Long userId);

    @POST
    @Path("/add/{userId}")
    void addPokemonToUser(@PathParam("userId") Long userId, Pokemon pokemon);
}
