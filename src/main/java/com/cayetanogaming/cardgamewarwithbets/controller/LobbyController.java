package com.cayetanogaming.cardgamewarwithbets.controller;

import com.cayetanogaming.cardgamewarwithbets.service.LobbyServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LobbyController {
    private final LobbyServiceImpl lobbyServiceImpl;

    @Autowired
    public LobbyController(LobbyServiceImpl lobbyServiceImpl){
        this.lobbyServiceImpl = lobbyServiceImpl;
    };

    @GetMapping("/start")
    public String start(@RequestParam int balance) {
        return lobbyServiceImpl.getStarted(balance);
    }

    @GetMapping("/shuffle")
    public String shuffle() {
        return lobbyServiceImpl.shuffleDeck();
    }

    @GetMapping("/bet")
    public String bet(@RequestParam int bet, @RequestParam boolean isBigger) {
        return lobbyServiceImpl.placeBet(bet, isBigger);
    }

}
