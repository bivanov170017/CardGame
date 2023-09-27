package com.cayetanogaming.cardgamewarwithbets.service;

import com.cayetanogaming.cardgamewarwithbets.enums.Rank;
import com.cayetanogaming.cardgamewarwithbets.enums.Suit;
import com.cayetanogaming.cardgamewarwithbets.exception.CardGameException;
import com.cayetanogaming.cardgamewarwithbets.model.Card;
import com.cayetanogaming.cardgamewarwithbets.model.Lobby;
import com.cayetanogaming.cardgamewarwithbets.repository.LobbyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class LobbyServiceImpl implements LobbyService{

    private final LobbyRepository lobbyRepository;

    @Autowired
    public LobbyServiceImpl(LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
    }

    @Override
    @Transactional
    public String getStarted(int balance) {
        if (lobbyRepository.findAll().isEmpty()) {
            return initializeLobby(balance);
        }

        return topUpBalance(balance);
    }

    @Override
    @Transactional
    public String shuffleDeck() {
        Lobby lobby = lobbyRepository.findTheLobby()
                .orElseThrow(() -> new CardGameException("You need to start the game first!"));

        List<Card> deck = initializeDeck();
        shuffle(deck);

        lobby.setCardsDeck(deck);
        lobby.setCurrentCard(lobby.getCardsDeck().remove(0));

        return lobby.getCurrentCard().toString();
    }

    @Override
    @Transactional
    public String placeBet(int bet, boolean isBigger) {
        Lobby lobby = lobbyRepository.findTheLobby()
                .orElseThrow(() -> new CardGameException("You need to start the game first!"));

        if (lobby.getCardsDeck().isEmpty()) {
            return "No more cards! Please shuffle the deck.";
        }

        if (lobby.getPlayerBalance() < bet) {
            return String.format("Insufficient balance! Your current balance is %S. Please make your bet according to your bank balance or start the game with more money.", lobby.getPlayerBalance());
        }

        Card nextCard = lobby.getCardsDeck().remove(0);
        int nextCardRankValue = nextCard.getRank().ordinal();
        int currentCardRankValue = lobby.getCurrentCard().getRank().ordinal();
        lobby.setCurrentCard(nextCard);

        if (currentCardRankValue == nextCardRankValue) {
            return String.format("The next card is: %s! It is a draw! Your current balance is: %s", lobby.getCurrentCard(), lobby.getPlayerBalance());
        } else if ((currentCardRankValue > nextCardRankValue && isBigger) || (currentCardRankValue < nextCardRankValue && !isBigger)) {
            lobby.setPlayerBalance(lobby.getPlayerBalance() + bet);
            return String.format("The next card is: %s! You win! Your current balance is: %s", lobby.getCurrentCard(), lobby.getPlayerBalance());

        } else {
            lobby.setPlayerBalance(lobby.getPlayerBalance() - bet);
            return String.format("The next card is: %s! Unfortunately you lose! Your current balance is: %s", lobby.getCurrentCard(), lobby.getPlayerBalance());
        }
    }

    private String initializeLobby(int balance) {
        List<Card> deck = initializeDeck();
        shuffle(deck);
        Card currentCard = deck.remove(0);
        Lobby lobby = Lobby.builder()
                .cardsDeck(deck)
                .currentCard(currentCard)
                .playerBalance(balance)
                .build();
        lobbyRepository.save(lobby);

        return currentCard.toString();
    }

    private List<Card> initializeDeck() {
        List<Card> deck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                deck.add(new Card(rank, suit));
            }
        }
        return deck;
    }

    private void shuffle(List<Card> deck) {
        Collections.shuffle(deck);
    }

    public String topUpBalance(int balance) {
        Lobby lobby = lobbyRepository.findTheLobby().get();
        int newBalance = lobby.getPlayerBalance() + balance;
        lobby.setPlayerBalance(newBalance);
        Card currentCard = lobby.getCurrentCard();
        return currentCard.toString();
    }
}
