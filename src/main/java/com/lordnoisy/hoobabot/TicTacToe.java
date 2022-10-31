package com.lordnoisy.hoobabot;

import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import discord4j.common.util.Snowflake;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TicTacToe {

    /**
     * Start a game of tic-tac-toe
     * @param fighter the person who initiated the game
     * @param opponent the opponent
     * @return the message to be created
     */
    public MessageCreateSpec startTicTacToe(Member fighter, Snowflake opponent) {
        String name = fighter.getDisplayName();
        String url = fighter.getAvatarUrl();
        return MessageCreateSpec.builder()
                .addEmbed(createTicTacToeEmbed(name, url, url))
                .addAllComponents(createButtonRows(fighter.getId().asString(), opponent.asString()))
                .build();
    }

    public EmbedCreateSpec createTicTacToeEmbed(String name, String url, String iconUrl) {
        String title = name + " has initiated tic-tac-toe!";
        return EmbedCreateSpec.builder()
                .author(title, url, iconUrl)
                .description("<insert game of tic-tac-toe>")
                .color(EmbedBuilder.getStandardColor())
                .timestamp(Instant.now())
                .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconURL() + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
    }

    /**
     * Create all the buttons for tic-tac-toe
     * @param currentTurnUser the user who's taking their turn
     * @param nextTurnUser the user whose turn it isn't
     * @return all tic-tac-toe buttons in a List of ActionRows
     */
    public List<LayoutComponent> createButtonRows (String currentTurnUser, String nextTurnUser) {
        ArrayList<LayoutComponent> buttons = new ArrayList<>();
        for (int y = 1; y <= 3; y++) {
            ArrayList<Button> tempButtonRow = new ArrayList<>();
            for (int x = 1; x <= 3; x++) {
                Button newButton = this.createButton(x, y, currentTurnUser, nextTurnUser, false);
                tempButtonRow.add(newButton);
            }
            ActionRow actionRow = ActionRow.of(tempButtonRow);
            buttons.add(actionRow);
        }
        return buttons;
    }

    /**
     * Create a tic-tac-toe button
     * @param x the x coordinate the button relates to
     * @param y the y coordinate the button relates to
     * @param currentTurnUser the user who's taking their turn
     * @param nextTurnUser the user whose turn it isn't
     * @param isDisabled if a button is disabled
     * @return a valid tic-tac-toe button
     */
    public Button createButton (int x, int y, String currentTurnUser, String nextTurnUser, boolean isDisabled) {
        return Button.primary("tic_tac_toe:"+x+","+y+":"+currentTurnUser+":"+nextTurnUser, "Here!");
    }
}
