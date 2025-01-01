package com.example.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Pokemon {

    private Long id;
    private String name;
    private List<String> abilities;
    private List<String> evolution_chain;
    private Map<String, String> stats;
    private List<Enchere> History;

    // Constructeur par défaut
    public Pokemon() {
        this.stats = Map.of(
                "hp", "",
                "attack", "",
                "defense", "",
                "special-attack", "",
                "special-defense", "",
                "speed", ""
        );
        this.History = new ArrayList<>();
    }

    // Constructeur avec paramètres
    public Pokemon(Long id, String name, List<String> abilities, List<String> evolution_chain, Map<String, String> stats) {
        this.id = id;
        this.name = name;
        this.abilities = abilities;
        this.evolution_chain = evolution_chain;
        this.stats = stats;
        this.History=new ArrayList<>();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAbilities() {
        return abilities;
    }

    public void setAbilities(List<String> abilities) {
        this.abilities = abilities;
    }

    public List<String> getEvolution_chain() {
        return evolution_chain;
    }

    public void setEvolution_chain(List<String> evolution_chain) {
        this.evolution_chain = evolution_chain;
    }

    public Map<String, String> getStats() {
        return stats;
    }

    public void setStats(Map<String, String> stats) {
        this.stats = stats;
    }

    public void miseAjourHistory(Enchere e) {
        History.add(e);
    }
    public List<Enchere> getHistory() {
        return History;
    }
}