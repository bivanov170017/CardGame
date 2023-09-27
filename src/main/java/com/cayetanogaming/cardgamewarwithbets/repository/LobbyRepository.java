package com.cayetanogaming.cardgamewarwithbets.repository;

import com.cayetanogaming.cardgamewarwithbets.model.Lobby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LobbyRepository extends JpaRepository<Lobby, Long> {
    @Query(value = "SELECT * FROM LOBBY LIMIT 1", nativeQuery = true)
    Optional<Lobby> findTheLobby();
}
