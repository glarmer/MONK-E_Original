package com.lordnoisy.hoobabot.game;

import com.lordnoisy.hoobabot.Coordinates;
import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;

import java.time.Instant;
import java.util.HashMap;

public class Checkers {
    private final String whiteSquare = ":white_large_square:";
    private final String blackSquare = "<:black_tile:1041110531563606088>";
    private final String blackPlayer = "<:black_player:1041110531563606088>";
    private final String whitePlayer = "<:white_player:1041110531563606088>";
    private final String[] letters = new String[]{"A","B","C","D","E","F","G","H"};
    private final String[] lettersUnicode = new String[]{"\ud83c\udde6", "\ud83c\udde7", "\ud83c\udde8", "\ud83c\udde9",
            "\ud83c\uddea", "\ud83c\uddeb", "\ud83c\uddec", "\ud83c\udded"};

    private final HashMap<Coordinates, String> startBoardState = constructStartBoardState();
    private final String hashMapKeyBlackPlayer = "pb";
    private final String hashKeyWhitePlayer = "pw";
    private final String hashKeyEmptyBlack = "eb";
    private final String hashKeyEmptyWhite = "ew";

    public Checkers() {
    }

    /**
     * Start a game of checkers
     * @param fighter the person who initiated the game
     * @param opponent the opponent
     * @return the message to be created
     */
    public MessageCreateSpec startCheckers(Member fighter, Snowflake opponent) {
        String startingBoard = this.constructBoard(this.startBoardState);
        return MessageCreateSpec.builder()
                .addEmbed(createCheckersEmbed(fighter, startingBoard, opponent.asString(), fighter.getId().asString(), opponent.asString(), null))
                .build();
    }

    /**
     * Create the checkers embed
     * @param fighter the user who started the game
     * @param currentBoard the current board
     * @param opponentId the ID of the opponent
     * @param currentTurnUser the current turn user's ID
     * @param nextTurnUser the next turn user's ID
     * @param winner the winners ID
     * @return
     */
    public EmbedCreateSpec createCheckersEmbed(Member fighter, String currentBoard, String opponentId, String currentTurnUser, String nextTurnUser, String winner) {
        String title = fighter.getDisplayName() + " has initiated a game of checkers!";
        String url = fighter.getAvatarUrl();
        String extraDescription;
        String endDescription = "";
        if (winner != null) {
            String loser = currentTurnUser;
            if (winner.equals(currentTurnUser)) {
                loser = nextTurnUser;
            }
            loser = "<@" + loser + ">\n\n";
            extraDescription = "<@" + winner + "> has won the game, congratulations!\n\n";
            endDescription = "\nBetter luck next time " + loser;
        } else {
            extraDescription = "You have been challenged <@" + opponentId + ">!\n\n";
            endDescription = "\nIt is currently <@" + currentTurnUser + ">'s turn";
        }
        return EmbedCreateSpec.builder()
                .author(title, url, url)
                .description(extraDescription + currentBoard + endDescription)
                .color(EmbedBuilder.getStandardColor())
                .timestamp(Instant.now())
                .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconURL() + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
    }

    /**
     * Convert a hashmap representation of the play board into a string
     * @param boardState the current board state
     * @return a string version of the board
     */
    public String constructBoard(HashMap<Coordinates, String> boardState) {
        String board = "\u2198\u0031\u20E3\u0032\u20E3\u0033\u20E3\u0034\u20E3\u0035\u20E3\u0036\u20E3\u0037\u20E3\u0038\u20E3\n";
        for (int y = 1; y <= 8; y++) {
            board = board.concat(lettersUnicode[y-1]);
            for (int x = 1; x <= 8; x++) {
                Coordinates currentCoordinate = new Coordinates(x, y);
                System.out.println("CURRENT X Y" + x + " " + y + " " + boardState.get(currentCoordinate));
                switch (boardState.get(currentCoordinate)) {
                    case hashKeyEmptyWhite:
                        board = board.concat(whiteSquare);
                        break;
                    case hashKeyEmptyBlack:
                        board = board.concat(blackSquare);
                        break;
                    case hashKeyWhitePlayer:
                        board = board.concat(whitePlayer);
                        break;
                    case hashMapKeyBlackPlayer:
                        board = board.concat(blackPlayer);
                        break;
                }
            }
            board = board.concat("\n");
        }
        return board;
    }

    /**
     * Create a start board state
     * @return the start board state
     */
    public HashMap<Coordinates, String> constructStartBoardState() {
        HashMap<Coordinates, String> boardState = new HashMap<>();
        for (int y = 1; y <= 8; y++) {
            for (int x = 1; x <= 8; x++) {
                Coordinates currentCoordinate = new Coordinates(x,y);
                System.out.println("CURRENT START X Y " + x + " " + y);
                if ((x+y-1) % 2 == 0) {
                    //It is a black square
                    if (y <= 3) {
                        //Black player
                        boardState.put(currentCoordinate, hashMapKeyBlackPlayer);
                    } else if (y >= 6) {
                        //White player
                        boardState.put(currentCoordinate, hashKeyWhitePlayer);
                    } else {
                        boardState.put(currentCoordinate, hashKeyEmptyBlack);
                    }
                } else {
                    //It is a white square
                    boardState.put(currentCoordinate, hashKeyEmptyWhite);
                }
                System.out.println(boardState.get(currentCoordinate));
            }
        }
        return boardState;
    }
}
