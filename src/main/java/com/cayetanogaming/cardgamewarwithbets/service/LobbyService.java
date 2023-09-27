package com.cayetanogaming.cardgamewarwithbets.service;

public interface LobbyService {
    String getStarted(int balance);
    String shuffleDeck();
    String placeBet(int bet, boolean isBigger);
}
