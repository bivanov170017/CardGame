package com.cayetanogaming.cardgamewarwithbets.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "lobby")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lobby {
    @Id
    @SequenceGenerator(
            name = "lobby_sequence",
            sequenceName =  "lobby_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "lobby_sequence"
    )
    private Long id;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "deck_cards", joinColumns = @JoinColumn(name = "lobby_id"))
    private List<Card> cardsDeck;
    @Embedded
    private Card currentCard;
    @Column()
    private int playerBalance;
}
