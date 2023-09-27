package com.cayetanogaming.cardgamewarwithbets;

import com.cayetanogaming.cardgamewarwithbets.model.Lobby;
import com.cayetanogaming.cardgamewarwithbets.repository.LobbyRepository;
import org.json.JSONException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.springframework.web.client.RestTemplate;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CardGameWarWithBetsApplicationTests {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost:";

    private static RestTemplate restTemplate;

    @Autowired
    private LobbyRepository lobbyRepository;

    @BeforeAll
    public static void init() {
        restTemplate = new RestTemplate();
    }

    @BeforeEach
    public void setUp() {
        baseUrl += port;
    }

    @AfterEach
    public void tearDown() {
        lobbyRepository.deleteAll();
    }



    @Test
    void testStart() {
        baseUrl += "/start?balance=500";
        String actualResponse = restTemplate.getForObject(baseUrl, String.class);
        Lobby lobby = lobbyRepository.findTheLobby().get();

        assertThat(lobbyRepository.findAll().size()).isEqualTo(1);
        assertThat(lobby.getPlayerBalance()).isEqualTo(500);
        assertThat(lobby.getCardsDeck().size()).isEqualTo(51);
        assertThat(actualResponse).isEqualTo(lobby.getCurrentCard().toString());
    }

    @Test
    void testShuffle() {
        String startURL = baseUrl + "/start?balance=500";
        baseUrl += "/shuffle";
        String responseBeforeShuffle = restTemplate.getForObject(startURL, String.class);
        Lobby lobbyBeforeShuffle = lobbyRepository.findTheLobby().get();
        String actualResponse = restTemplate.getForObject(baseUrl, String.class);
        Lobby lobbyAfterShuffle = lobbyRepository.findTheLobby().get();

        assertThat(lobbyRepository.findAll().size()).isEqualTo(1);
        assertThat(lobbyAfterShuffle.getPlayerBalance()).isEqualTo(500);
        assertThat(lobbyAfterShuffle.getCardsDeck().size()).isEqualTo(51);
        assertThat(lobbyBeforeShuffle.getCardsDeck().get(0)).isNotEqualTo(lobbyAfterShuffle.getCardsDeck().get(0));
        assertThat(actualResponse).isNotEqualTo(responseBeforeShuffle);
    }

    @Test
    void testBet() throws JSONException {
        String startURL = baseUrl + "/start?balance=500";
        baseUrl += "/bet?bet=500&isBigger=false";
        String responseBeforeBet = restTemplate.getForObject(startURL, String.class);
        Lobby lobbyBeforeBet = lobbyRepository.findTheLobby().get();

        String actualResponse = restTemplate.getForObject(baseUrl, String.class);
        Lobby lobbyAfterBet = lobbyRepository.findTheLobby().get();

        assertThat(lobbyRepository.findAll().size()).isEqualTo(1);
        String[] expectedAnswers = {String.format("The next card is: %s! It is a draw! Your current balance is: %s", lobbyAfterBet.getCurrentCard(), lobbyAfterBet.getPlayerBalance()),
                String.format("The next card is: %s! You win! Your current balance is: %s", lobbyAfterBet.getCurrentCard(), lobbyAfterBet.getPlayerBalance()),
                String.format("The next card is: %s! Unfortunately you lose! Your current balance is: %s", lobbyAfterBet.getCurrentCard(), lobbyAfterBet.getPlayerBalance())

        };

        assertThat(lobbyAfterBet.getCardsDeck().size()).isEqualTo(50);
        assertThat(lobbyBeforeBet.getCardsDeck().get(0)).isNotEqualTo(lobbyAfterBet.getCardsDeck().get(0));
        assertThat(actualResponse).isIn(expectedAnswers);

        if (actualResponse.equals(expectedAnswers[0])) {
            assertThat(lobbyAfterBet.getPlayerBalance()).isEqualTo(500);
        } else if (actualResponse.equals(expectedAnswers[1])) {
            assertThat(lobbyAfterBet.getPlayerBalance()).isEqualTo(1000);
        } else if (actualResponse.equals(expectedAnswers[2])) {
            assertThat(lobbyAfterBet.getPlayerBalance()).isEqualTo(0);
        }
    }
}
