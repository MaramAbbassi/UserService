package com.example.user;

import com.example.user.exceptions.UserNotFoundException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @Context
    SecurityContext securityContext;

    @GET
    @RolesAllowed("Admin") // Only Admins can access the list of all users
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"User", "Admin"}) // Both Users and Admins can access this
    public Response getUserById(@PathParam("id") Long id) {
        String authenticatedUsername = securityContext.getUserPrincipal().getName();

        // Find the user being requested
        User user = userService.findUserById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
        }

        // Check if the authenticated user matches the requested user or is an Admin
        if (!user.getUsername().equals(authenticatedUsername) && !securityContext.isUserInRole("Admin")) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied").build();
        }

        return Response.ok(user).build();
    }

    @POST
    @RolesAllowed("Admin") // Only Admins can add new users
    public Response addUser(User user) {
        userService.addUser(user);
        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("Admin") // Only Admins can update users
    public Response updateUser(@PathParam("id") Long id, User user) {
        try {
            userService.updateUser(id, user, securityContext.isUserInRole("Admin") ? "Admin" : "User");
            return Response.ok("User updated successfully!").build();
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An unexpected error occurred: " + e.getMessage())
                    .build();
        }
    }


    @POST
    @Path("/{id}/deduct-coins")
    @RolesAllowed({"User", "Admin"}) // Both Users and Admins can deduct coins
    public Response deductCoins(@PathParam("id") Long id, @QueryParam("amount") int amount) {
        String authenticatedUsername = securityContext.getUserPrincipal().getName();

        // Find the user
        User user = userService.findUserById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
        }

        // Check if the authenticated user matches the requested user or is an Admin
        if (!user.getUsername().equals(authenticatedUsername) && !securityContext.isUserInRole("Admin")) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied").build();
        }

        userService.deductLimCoins(id, amount);
        return Response.ok("Coins deducted successfully").build();
    }

    @POST
    @Path("/register")
    public Response register(User user) {
        try {
            // Prevent users from assigning the "Admin" role to themselves
            if ("Admin".equals(user.getRole()) && !securityContext.isUserInRole("Admin")) {
                return Response.status(Response.Status.FORBIDDEN).entity("Only Admins can create other Admins.").build();
            }

            userService.registerUser(user);
            return Response.status(Response.Status.CREATED).entity("User registered successfully!").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred.").build();
        }
    }

    @POST
    @Path("/login")
    public Response login(UserLoginDto loginDto) {
        try {
            String token = userService.loginUser(loginDto.getUsername(), loginDto.getPassword());
            return Response.ok(token).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{id}/add-coins")
    @RolesAllowed({"User", "Admin"}) // Both Users and Admins can add coins
    public Response addLimCoins(@PathParam("id") Long id, @QueryParam("amount") int amount) {
        String authenticatedUsername = securityContext.getUserPrincipal().getName();

        // Find the user
        User user = userService.findUserById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
        }

        // Check if the authenticated user matches the requested user or is an Admin
        if (!user.getUsername().equals(authenticatedUsername) && !securityContext.isUserInRole("Admin")) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied").build();
        }

        userService.addLimCoins(id, amount);
        return Response.ok("Coins added successfully!").build();
    }

    @POST
    @Path("/{id}/spend-coins")
    @RolesAllowed({"User", "Admin"}) // Both Users and Admins can spend coins
    public Response spendLimCoins(@PathParam("id") Long id, @QueryParam("amount") int amount) {
        String authenticatedUsername = securityContext.getUserPrincipal().getName();

        // Find the user
        User user = userService.findUserById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
        }

        // Check if the authenticated user matches the requested user or is an Admin
        if (!user.getUsername().equals(authenticatedUsername) && !securityContext.isUserInRole("Admin")) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied").build();
        }

        userService.deductLimCoins(id, amount);
        return Response.ok("Coins spent successfully!").build();
    }
}
