package com.cayetanogaming.cardgamewarwithbets.service;


import com.cayetanogaming.cardgamewarwithbets.enums.Rank;
import com.cayetanogaming.cardgamewarwithbets.enums.Suit;
import com.cayetanogaming.cardgamewarwithbets.exception.CardGameException;
import com.cayetanogaming.cardgamewarwithbets.model.Card;
import com.cayetanogaming.cardgamewarwithbets.model.Lobby;
import com.cayetanogaming.cardgamewarwithbets.repository.LobbyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LobbyServiceImplTest {

    @Mock
    private LobbyRepository lobbyRepository;
    private LobbyServiceImpl lobbyServiceImplUnderTest;

    @BeforeEach
    void setUp() {
        lobbyServiceImplUnderTest = new LobbyServiceImpl(lobbyRepository);
    }

    @Test
    void canStartGame() {
        int initialBalance = 500;
        lobbyServiceImplUnderTest.getStarted(initialBalance);
        ArgumentCaptor<Lobby> lobbyArgumentCaptor = ArgumentCaptor.forClass(Lobby.class);

        verify(lobbyRepository).findAll();
        verify(lobbyRepository).save(lobbyArgumentCaptor.capture());

        Lobby capturedLobby = lobbyArgumentCaptor.getValue();

        assertThat(capturedLobby.getPlayerBalance()).isEqualTo(initialBalance);
    }

    @Test
    void canRestartGameAndRechargeBalance() {
        int balanceAtTime = 15;
        int topUpBalance = 500;

        Lobby lobby = Lobby.builder()
                .playerBalance(balanceAtTime)
                .currentCard(new Card())
                .build();

        List<Lobby> list = new ArrayList<>();
        list.add(lobby);

        given(lobbyRepository.findAll()).willReturn(list);
        given(lobbyRepository.findTheLobby()).willReturn(Optional.ofNullable(lobby));
        lobbyServiceImplUnderTest.getStarted(topUpBalance);

        verify(lobbyRepository).findTheLobby();
        assertThat(lobby.getPlayerBalance()).isEqualTo(balanceAtTime + topUpBalance);
    }

    @Test
    void canNotShuffleBeforeStartAndReturnsException() {
        assertThatThrownBy(() -> {
            lobbyServiceImplUnderTest.shuffleDeck();
        }).isInstanceOf(CardGameException.class)
                .withFailMessage("You need to start the game first!");
    }

    @Test
    void canShuffleDeck() {
        lobbyServiceImplUnderTest.getStarted(500);

        ArgumentCaptor<Lobby> lobbyArgumentCaptor = ArgumentCaptor.forClass(Lobby.class);

        verify(lobbyRepository).findAll();
        verify(lobbyRepository).save(lobbyArgumentCaptor.capture());

        Lobby capturedLobby = lobbyArgumentCaptor.getValue();
        Card cardBeforeShuffle = capturedLobby.getCurrentCard();
        List<Card> deckBeforeShuffle = capturedLobby.getCardsDeck();
        given(lobbyRepository.findTheLobby()).willReturn(Optional.of(capturedLobby));
        lobbyServiceImplUnderTest.shuffleDeck();
        Card cardAfterShuffle = capturedLobby.getCurrentCard();
        List<Card> deckAfterShuffle = capturedLobby.getCardsDeck();


        verify(lobbyRepository).findTheLobby();
        assertThat(cardAfterShuffle).isNotEqualTo(cardBeforeShuffle);
        assertThat(deckAfterShuffle.get(0)).isNotEqualTo(deckBeforeShuffle.get(0));
    }

    @Test
    void canNotPlaceBetsBeforeStartAndReturnsException() {
        assertThatThrownBy(() -> {
            lobbyServiceImplUnderTest.placeBet(500, true);
        }).isInstanceOf(CardGameException.class)
                .withFailMessage("You need to start the game first!");
    }

    @Test
    void canNotPlaceBetsWithEmptyDeck() {
        Lobby lobbyWithEmptyDeckNoBalanceNoCurrentCard = Lobby.builder()
                .cardsDeck(new ArrayList<>())
                .build();
        given(lobbyRepository.findTheLobby()).willReturn(Optional.ofNullable(lobbyWithEmptyDeckNoBalanceNoCurrentCard));
        String actualMessage = lobbyServiceImplUnderTest.placeBet(500, true);
        String expectedMessage = "No more cards! Please shuffle the deck.";

        verify(lobbyRepository).findTheLobby();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }
    @Test
    void canNotPlaceBetsWithInsufficientBalance() {
        Lobby lobbyZeroBalanceDeckNoCurrentCard = Lobby.builder()
                .cardsDeck(List.of(new Card(Rank.ACE, Suit.SPADES)))
                .playerBalance(0)
                .build();
        given(lobbyRepository.findTheLobby()).willReturn(Optional.ofNullable(lobbyZeroBalanceDeckNoCurrentCard));
        String actualMessage = lobbyServiceImplUnderTest.placeBet(500, true);
        String expectedMessage = String.format("Insufficient balance! Your current balance is %S. Please make your bet according to your bank balance or start the game with more money.", lobbyZeroBalanceDeckNoCurrentCard.getPlayerBalance());

        verify(lobbyRepository).findTheLobby();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    void canPlaceBetReturnsDraw() {
        List<Card> deckWithCard = new ArrayList<>();
        deckWithCard.add(new Card(Rank.ACE, Suit.SPADES));
        Lobby lobbyZeroBalanceDeckNoCurrentCard = Lobby.builder()
                .cardsDeck(deckWithCard)
                .playerBalance(500)
                .currentCard(new Card(Rank.ACE, Suit.HEARTS))
                .build();
        given(lobbyRepository.findTheLobby()).willReturn(Optional.ofNullable(lobbyZeroBalanceDeckNoCurrentCard));
        String actualMessage = lobbyServiceImplUnderTest.placeBet(500, true);
        String expectedMessage = String.format("The next card is: %s! It is a draw! Your current balance is: %s", lobbyZeroBalanceDeckNoCurrentCard.getCurrentCard(), lobbyZeroBalanceDeckNoCurrentCard.getPlayerBalance());

        verify(lobbyRepository).findTheLobby();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    void canPlaceBetReturnsWinBigger() {
        List<Card> deckWithCard = new ArrayList<>();
        deckWithCard.add(new Card(Rank.KING, Suit.SPADES));
        Lobby lobbyZeroBalanceDeckNoCurrentCard = Lobby.builder()
                .cardsDeck(deckWithCard)
                .playerBalance(500)
                .currentCard(new Card(Rank.ACE, Suit.HEARTS))
                .build();
        given(lobbyRepository.findTheLobby()).willReturn(Optional.ofNullable(lobbyZeroBalanceDeckNoCurrentCard));
        String actualMessage = lobbyServiceImplUnderTest.placeBet(500, true);
        String expectedMessage = String.format("The next card is: %s! You win! Your current balance is: %s", lobbyZeroBalanceDeckNoCurrentCard.getCurrentCard(), lobbyZeroBalanceDeckNoCurrentCard.getPlayerBalance());

        verify(lobbyRepository).findTheLobby();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    void canPlaceBetReturnsWinSmaller() {
        List<Card> deckWithCard = new ArrayList<>();
        deckWithCard.add(new Card(Rank.ACE, Suit.SPADES));
        Lobby lobbyZeroBalanceDeckNoCurrentCard = Lobby.builder()
                .cardsDeck(deckWithCard)
                .playerBalance(500)
                .currentCard(new Card(Rank.KING, Suit.HEARTS))
                .build();
        given(lobbyRepository.findTheLobby()).willReturn(Optional.ofNullable(lobbyZeroBalanceDeckNoCurrentCard));
        String actualMessage = lobbyServiceImplUnderTest.placeBet(500, false);
        String expectedMessage = String.format("The next card is: %s! You win! Your current balance is: %s", lobbyZeroBalanceDeckNoCurrentCard.getCurrentCard(), lobbyZeroBalanceDeckNoCurrentCard.getPlayerBalance());

        verify(lobbyRepository).findTheLobby();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    void canPlaceBetReturnsLoseBigger() {
        List<Card> deckWithCard = new ArrayList<>();
        deckWithCard.add(new Card(Rank.ACE, Suit.SPADES));
        Lobby lobbyZeroBalanceDeckNoCurrentCard = Lobby.builder()
                .cardsDeck(deckWithCard)
                .playerBalance(500)
                .currentCard(new Card(Rank.KING, Suit.HEARTS))
                .build();
        given(lobbyRepository.findTheLobby()).willReturn(Optional.ofNullable(lobbyZeroBalanceDeckNoCurrentCard));
        String actualMessage = lobbyServiceImplUnderTest.placeBet(500, true);
        String expectedMessage = String.format("The next card is: %s! Unfortunately you lose! Your current balance is: %s", lobbyZeroBalanceDeckNoCurrentCard.getCurrentCard(), lobbyZeroBalanceDeckNoCurrentCard.getPlayerBalance());

        verify(lobbyRepository).findTheLobby();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    void canPlaceBetReturnsLoseSmaller() {
        List<Card> deckWithCard = new ArrayList<>();
        deckWithCard.add(new Card(Rank.KING, Suit.SPADES));
        Lobby lobbyZeroBalanceDeckNoCurrentCard = Lobby.builder()
                .cardsDeck(deckWithCard)
                .playerBalance(500)
                .currentCard(new Card(Rank.ACE, Suit.HEARTS))
                .build();
        given(lobbyRepository.findTheLobby()).willReturn(Optional.ofNullable(lobbyZeroBalanceDeckNoCurrentCard));
        String actualMessage = lobbyServiceImplUnderTest.placeBet(500, false);
        String expectedMessage = String.format("The next card is: %s! Unfortunately you lose! Your current balance is: %s", lobbyZeroBalanceDeckNoCurrentCard.getCurrentCard(), lobbyZeroBalanceDeckNoCurrentCard.getPlayerBalance());

        verify(lobbyRepository).findTheLobby();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }




}
