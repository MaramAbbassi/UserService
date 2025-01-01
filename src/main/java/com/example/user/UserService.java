package com.example.user;

import com.example.user.exceptions.UserNotFoundException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import org.mindrot.jbcrypt.BCrypt;
import com.example.utils.JwtUtils;



@ApplicationScoped


public class UserService {

    @Inject
    EntityManager em;

    @Inject
    PokemonClient pokemonClient;

    @Inject
    EnchereClient enchereClient;


    public List<User> getAllUsers() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    public User findUserById(Long id) {
        User user = em.find(User.class, id);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + id + " not found.");
        }

        List<Pokemon> pokemons = pokemonClient.getPokemonsByUserId(id);
        List<Enchere> encheres = enchereClient.getEncheresByUserId(id);
        user.setPokemons(pokemons);
        user.setEncheres(encheres);

        return user;
    }

    @Transactional
    public void addUser(User user) {
        em.persist(user);
    }

    @Transactional
    public void updateUser(Long id, User updatedUser, String authenticatedRole) {
        if (!"Admin".equals(authenticatedRole)) {
            throw new SecurityException("Only Admins can update users.");
        }

        User existingUser = findUserById(id);
        if (existingUser == null) {
            throw new UserNotFoundException("Cannot update: User not found.");
        }

        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getRole() != null) {
            existingUser.setRole(updatedUser.getRole());
        }

        em.merge(existingUser);
    }

    @Transactional
    public void deleteUser(Long id, String authenticatedRole) {
        if (!"Admin".equals(authenticatedRole)) {
            throw new SecurityException("Only Admins can delete users.");
        }

        User user = findUserById(id);
        if (user == null) {
            throw new UserNotFoundException("Cannot delete: User not found.");
        }

        em.remove(user);
    }



    @Transactional
    public void registerUser(User user) {
        if (user.getUsername() == null || user.getEmail() == null || user.getPassword() == null) {
            throw new IllegalArgumentException("All fields are required.");
        }

        try {
            // Check if username or email already exists
            Long usernameCount = em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                    .setParameter("username", user.getUsername())
                    .getSingleResult();
            if (usernameCount > 0) {
                throw new IllegalArgumentException("Username already exists.");
            }

            Long emailCount = em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                    .setParameter("email", user.getEmail())
                    .getSingleResult();
            if (emailCount > 0) {
                throw new IllegalArgumentException("Email already exists.");
            }

            // Hash the password
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));

            // Set role to "User" if not provided
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("User");
            }

            // Default LimCoins for new users
            user.setLimCoins(1000);

            // Persist the user
            em.persist(user);
        } catch (Exception e) {
            // Add debugging logs
            e.printStackTrace();
            throw new RuntimeException("Error during user registration: " + e.getMessage());
        }
    }



    public String loginUser(String username, String password) {
        try {
            // Normalize input username by trimming spaces
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username cannot be empty.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be empty.");
            }


            // Query the database for the user by username
            User user = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();

            // Validate the password using BCrypt
            if (!BCrypt.checkpw(password, user.getPassword())) {
                throw new IllegalArgumentException("Invalid password.");
            }

            // Generate and return the JWT token
            return  JwtUtils.generateToken(user.getUsername(), user.getRole());

        } catch (NoResultException e) {
            throw new IllegalArgumentException("User not found with the provided username.");
        }
    }

    @Transactional
    public boolean addLimCoins(Long userId, int amount) {
        User user = findUserById(userId);
        if (user == null) {
            return false; // User not found
        }
        user.setLimCoins(user.getLimCoins() + amount);
        em.merge(user);
        return true; // Coins added successfully
    }


    @Transactional
    public boolean deductLimCoins(Long userId, int amount) {
        User user = findUserById(userId);
        if (user == null || user.getLimCoins() < amount) {
            return false; // User not found or insufficient coins
        }
        user.setLimCoins(user.getLimCoins() - amount);
        em.merge(user);
        return true; // Coins deducted successfully
    }

    private void checkForDuplicateUser(User user) {
        Long usernameCount = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.username = :username AND u.id != :id", Long.class)
                .setParameter("username", user.getUsername())
                .setParameter("id", user.getId())
                .getSingleResult();

        if (usernameCount > 0) {
            throw new IllegalArgumentException("Username already exists.");
        }

        Long emailCount = em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.email = :email AND u.id != :id", Long.class)
                .setParameter("email", user.getEmail())
                .setParameter("id", user.getId())
                .getSingleResult();

        if (emailCount > 0) {
            throw new IllegalArgumentException("Email already exists.");
        }
    }

    @Transactional
    public void addPokemonToUser(Long userId, Pokemon pokemon) {
        User user = findUserById(userId);
        pokemonClient.addPokemonToUser(userId, pokemon); // Notify the Pokémon microservice
        user.getPokemons().add(pokemon);
    }

    public List<Pokemon> getUserPokemons(Long userId) {
        return findUserById(userId).getPokemons();
    }

    @Transactional
    public void placeBid(Long userId, Enchere enchere) {
        User user = findUserById(userId);
        enchereClient.placeBid(userId, enchere); // Notify the Enchère microservice
        user.getEncheres().add(enchere);
    }

    public List<Enchere> getUserEncheres(Long userId) {
        return findUserById(userId).getEncheres();
    }

    @Transactional
    public String sellPokemonToSystem(Long userId, Long pokemonId) {
        // Find the user
        User user = findUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found.");
        }

        // Check if the user owns the Pokémon
        Pokemon pokemonToSell = user.getPokemons().stream()
                .filter(pokemon -> pokemon.getId().equals(pokemonId))
                .findFirst()
                .orElse(null);

        if (pokemonToSell == null) {
            throw new IllegalArgumentException("User does not own this Pokémon.");
        }

        int pokemonRealValue = pokemonToSell.getValeurReelle();

        user.setLimCoins(user.getLimCoins() + pokemonRealValue);

        user.getPokemons().remove(pokemonToSell);

        em.merge(user);

        return "Pokémon sold successfully! Real value: " + pokemonRealValue + " LimCoins.";
    }



}