package com.example.user;

import com.example.user.exceptions.UserNotFoundException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import com.example.utils.JwtUtils;



@ApplicationScoped


public class UserService {

    @Inject
    EntityManager em;

    public List<User> getAllUsers() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    public User findUserById(Long id) {
        User user = em.find(User.class, id);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + id + " not found.");
        }
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
    public void addLimCoins(Long userId, int amount) {
        User user = findUserById(userId);
        user.setLimCoins(user.getLimCoins() + amount);
        em.merge(user);
    }


    @Transactional
    public void deductLimCoins(Long userId, int amount) {
        User user = findUserById(userId);
        if (user.getLimCoins() < amount) {
            throw new IllegalArgumentException("Insufficient LimCoins for user " + userId);
        }
        user.setLimCoins(user.getLimCoins() - amount);
        em.merge(user);
    }

    @Transactional
    public void addPokemonToUser(Long userId, Long pokemonId) {
        User user = findUserById(userId);
        System.out.printf("Pokémon %d has been added to user %d%n", pokemonId, userId);
        // Replace with actual implementation later.
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


}