package com.cayetanogaming.cardgamewarwithbets.repository;

import com.cayetanogaming.cardgamewarwithbets.enums.Rank;
import com.cayetanogaming.cardgamewarwithbets.enums.Suit;
import com.cayetanogaming.cardgamewarwithbets.model.Card;
import com.cayetanogaming.cardgamewarwithbets.model.Lobby;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
class LobbyRepositoryTest {

    @Autowired
    private LobbyRepository lobbyRepositoryUnderTest;

    @AfterEach
    void tearDown() {
        lobbyRepositoryUnderTest.deleteAll();
    }

    @Test
    void itShouldReturnTheLobby() {
        Card expectedCard = new Card(Rank.ACE, Suit.SPADES);
        Lobby lobby = Lobby.builder()
                .currentCard(expectedCard)
                .build();
        lobbyRepositoryUnderTest.save(lobby);
        Lobby actualLobby = lobbyRepositoryUnderTest.findTheLobby().get();
        assertThat(actualLobby.getCurrentCard()).isEqualTo(expectedCard);
    }

    @Test
    void itShouldThrowErrorWhenNoLobbyIsAdded() {
        assertThatThrownBy(() -> {
            lobbyRepositoryUnderTest.findTheLobby().get();
        }).isInstanceOf(NoSuchElementException.class);
    }

}