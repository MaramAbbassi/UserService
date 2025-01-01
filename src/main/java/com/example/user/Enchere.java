package com.example.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;



public class Enchere  {

    private Long id;
    private Long userId;
    private Long pokemonId;
    private double startingPrice; //prix de debut d'enchére
    private double highestBid; //enchere le plus haut
    private Long highestBidderId;
    private LocalDateTime dateExpiration ;//date d'expiration
    private String Status; //cloturé actif..

    private List<Bid> bids;

    public Enchere() {
    }

    public Enchere(Long userid, long pokenmonid, double startingPrice, double highestBid, Long highestBidderId, LocalDateTime dateExpiration, String Status) {
        this.userId = userid;
        this.pokemonId = pokenmonid;
        this.startingPrice = startingPrice;
        this.highestBid = highestBid;
        this.highestBidderId = highestBidderId;
        this.dateExpiration = dateExpiration;
        this.Status = Status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPokemonId() {
        return pokemonId;
    }

    public void setPokemonId(Long pokemonId) {
        this.pokemonId = pokemonId;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public double getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(double highestBid) {
        this.highestBid = highestBid;
    }

    public Long getHighestBidderId() {
        return highestBidderId;
    }

    public void setHighestBidderId(Long highestBidderId) {
        this.highestBidderId = highestBidderId;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
    public List<Bid> getBids() {
        return bids;
    }

    public void setBids(List<Bid> bids) {
        this.bids = bids;
    }
}