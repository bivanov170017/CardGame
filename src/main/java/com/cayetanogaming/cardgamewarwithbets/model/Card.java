package com.cayetanogaming.cardgamewarwithbets.model;

import com.cayetanogaming.cardgamewarwithbets.enums.Rank;
import com.cayetanogaming.cardgamewarwithbets.enums.Suit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Card {
    @Enumerated
    private Rank rank;
    @Enumerated
    private Suit suit;


    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }


    @Override
    public String toString() {
        return String.format("%S OF %S", this.rank, this.suit);
    }
}
