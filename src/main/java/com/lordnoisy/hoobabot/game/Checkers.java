package com.lordnoisy.hoobabot.game;

import com.lordnoisy.hoobabot.Coordinate;
import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Checkers {
    private final String whiteSquare = ":white_large_square:";
    private final String yellowSquare = ":yellow_square:";
    private final String blackSquare = "<:black_tile:1041099325981343886>";
    private final String blackPlayer = "<:black_player:1041099324890820638>";
    private final String whitePlayer = "<:white_player:1041099327277383680>";
    private final String[] letters = new String[]{"A","B","C","D","E","F","G","H"};
    private final String[] lettersUnicode = new String[]{"\ud83c\udde6", "\ud83c\udde7", "\ud83c\udde8", "\ud83c\udde9",
            "\ud83c\uddea", "\ud83c\uddeb", "\ud83c\uddec", "\ud83c\udded"};

    private final HashMap<Coordinate, String> startBoardState = constructStartBoardState();
    private final String hashMapKeyBlackPlayer = "p";
    private final String hashMapKeyWhitePlayer = "o";
    private final String hashMapKeyEmptyBlack = "b";
    private final String hashMapKeyEmptyWhite = "w";
    private final String hashMapKeyPossibleMove = "y";

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
        String boardData = boardStateToButtonInfo(startBoardState);
        List<LayoutComponent> buttonRows = createAxisButtonRows(true, null, fighter.getId().asString(), opponent.asString(), 1, boardData);
        return MessageCreateSpec.builder()
                .addEmbed(createCheckersEmbed(fighter.getId().asString(), fighter.getDisplayName(), fighter.getAvatarUrl(), startingBoard, opponent.asString(), 1, null))
                .addAllComponents(buttonRows)
                .build();
    }

    public MessageEditSpec getYVersion(String buttonID, Embed embed) {
        String name = embed.getAuthor().get().getName().orElse("Unknown d").split(" ")[0];
        String url = embed.getAuthor().get().getUrl().orElse(" ");
        String[] buttonData = buttonID.split(":");
        String selection = buttonData[1];
        int modulo = Integer.parseInt(buttonData[2]);
        String currentFighter = new BigInteger(buttonData[3], 36).toString();
        String nextFighter = new BigInteger(buttonData[4], 36).toString();
        String boardData = buttonData[5];
        List<LayoutComponent> buttonRows = createAxisButtonRows(false, selection, currentFighter, nextFighter, modulo, boardData);

        String embedBoard = constructBoard(buttonInfoToBoardState(buttonID));
        List<EmbedCreateSpec> embeds = List.of(createCheckersEmbed(currentFighter, name, url, embedBoard, nextFighter, modulo, null));
        return MessageEditSpec.builder()
                .addAllEmbeds(embeds)
                .addAllComponents(buttonRows)
                .build();
    }

    public MessageEditSpec getPossibleMovesVersion(String buttonID, Embed embed) {
        String name = embed.getAuthor().get().getName().orElse("Unknown d").split(" ")[0];
        String url = embed.getAuthor().get().getUrl().orElse(" ");
        String[] buttonData = buttonID.split(":");
        String selection = buttonData[1];
        Coordinate coordinateSelection = Coordinate.stringToCoordinate(selection);
        int modulo = Integer.parseInt(buttonData[2]);
        String currentFighter = new BigInteger(buttonData[3], 36).toString();
        String nextFighter = new BigInteger(buttonData[4], 36).toString();
        String boardData = buttonData[5];

        HashMap<Coordinate, String> boardMap = buttonInfoToBoardState(boardData);
        String currentSymbol;
        int move;
        if(modulo == 1) {
            currentSymbol = blackPlayer;
            move = 1;
        } else {
            currentSymbol = whitePlayer;
            move = -1;
        }
        int newY = coordinateSelection.getY() + move;
        int newX1 = coordinateSelection.getX() + 1;
        int newX2 = coordinateSelection.getX() - 1;

        Coordinate possibleMove1 = new Coordinate(newX1, newY);
        Coordinate possibleMove2 = new Coordinate(newX2, newY);

        boardMap.replace(possibleMove1, hashMapKeyPossibleMove);
        boardMap.replace(possibleMove2, hashMapKeyPossibleMove);

        String embedBoard = constructBoard(boardMap);
        List<EmbedCreateSpec> embeds = List.of(createCheckersEmbed(currentFighter, name, url, embedBoard, nextFighter, modulo, null));
        return MessageEditSpec.builder()
                .addAllEmbeds(embeds)
                .build();

    }

    /**
     * Create the checkers embed
     * @param fighterId the id of the fighter
     * @param fighterName the name of the fighter
     * @param fighterUrl the url of the fighter
     * @param currentBoard the current board layout
     * @param opponentId the id of the opponent
     * @param turnModulo the current turn modulo by 2
     * @param winner the winner if any
     * @return the embed create spec
     */
    public EmbedCreateSpec createCheckersEmbed(String fighterId, String fighterName, String fighterUrl, String currentBoard, String opponentId, int turnModulo, String winner) {
        String currentTurnUser;
        String nextTurnUser;
        String currentSymbol;
        if(turnModulo == 1) {
            currentTurnUser = fighterId;
            nextTurnUser = opponentId;
            currentSymbol = blackPlayer;
        } else {
            currentTurnUser = opponentId;
            nextTurnUser = fighterId;
            currentSymbol = whitePlayer;
        }
        String title = fighterName + " has initiated a game of checkers!";
        String url = fighterUrl;
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
            endDescription = "\nIt is currently <@" + currentTurnUser + ">'s turn ("+currentSymbol+")";
        }
        return EmbedCreateSpec.builder()
                .author(title, url, url)
                .description(extraDescription + currentBoard + endDescription)
                .color(EmbedBuilder.getStandardColor())
                .timestamp(Instant.now())
                .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconUrl() + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
    }

    /**
     * Convert a hashmap representation of the play board into a string
     * @param boardState the current board state
     * @return a string version of the board
     */
    public String constructBoard(HashMap<Coordinate, String> boardState) {
        String board = "\u2198\u0031\u20E3\u0032\u20E3\u0033\u20E3\u0034\u20E3\u0035\u20E3\u0036\u20E3\u0037\u20E3\u0038\u20E3\n";
        for (int y = 1; y <= 8; y++) {
            board = board.concat(lettersUnicode[y-1]);
            for (int x = 1; x <= 8; x++) {
                Coordinate currentCoordinate = new Coordinate(x, y);
                switch (boardState.get(currentCoordinate)) {
                    case hashMapKeyEmptyWhite:
                        board = board.concat(whiteSquare);
                        break;
                    case hashMapKeyEmptyBlack:
                        board = board.concat(blackSquare);
                        break;
                    case hashMapKeyPossibleMove:
                        board = board.concat(yellowSquare);
                        break;
                    case hashMapKeyWhitePlayer:
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
    public HashMap<Coordinate, String> constructStartBoardState() {
        HashMap<Coordinate, String> boardState = new HashMap<>();
        for (int y = 1; y <= 8; y++) {
            for (int x = 1; x <= 8; x++) {
                Coordinate currentCoordinate = new Coordinate(x,y);
                if ((x+y-1) % 2 == 0) {
                    //It is a black square
                    if (y <= 3) {
                        //Black player
                        boardState.put(currentCoordinate, hashMapKeyBlackPlayer);
                    } else if (y >= 6) {
                        //White player
                        boardState.put(currentCoordinate, hashMapKeyWhitePlayer);
                    } else {
                        boardState.put(currentCoordinate, hashMapKeyEmptyBlack);
                    }
                } else {
                    //It is a white square
                    boardState.put(currentCoordinate, hashMapKeyEmptyWhite);
                }
                System.out.println(boardState.get(currentCoordinate));
            }
        }
        return boardState;
    }

    /**
     * Turns the board state into info that can be stored in a button ID
     * @param boardState the board state
     * @return the button info
     */
    public String boardStateToButtonInfo(HashMap<Coordinate, String> boardState) {
        String buttonInfo = "";
        for(int y = 1; y <= 8; y++){
            for(int x = 1; x <= 8; x++){
                buttonInfo = buttonInfo.concat(boardState.get(new Coordinate(x, y)));
            }
        }
        return buttonInfo;
    }

    /**
     * Turn a button's custom ID into a board state
     * @param buttonInfo the button's custom ID
     * @return the board state
     */
    public HashMap<Coordinate, String> buttonInfoToBoardState(String buttonInfo) {
        HashMap<Coordinate, String> boardState = new HashMap<>();
        String[] buttonInfoArray = buttonInfo.split(":");
        String boardInfo = buttonInfoArray[buttonInfoArray.length-1];
        for(int i = 0; i < boardInfo.length(); i++) {
            char currentChar = boardInfo.charAt(i);
            Coordinate currentCoordinate = oneDimensionToCoordinate(i+1, 8);
            String value = String.valueOf(currentChar);
            boardState.put(currentCoordinate, value);
        }
        return boardState;
    }

    /**
     * Turns a one dimensional position into a coordinate
     * @param pos the position in the list
     * @param width the width to conform to
     * @return the new coordinate
     */
    public Coordinate oneDimensionToCoordinate(double pos, double width) {
        int y = (int) Math.ceil(pos/width);
        int x = (int) (pos - ((y-1)*width));
        return new Coordinate(x, y);
    }

    /**
     * Create a row of buttons to select position on an axis
     * @param isXAxis if it is for the X axis or not
     * @param selection the previous selection if it exists
     * @param currentUserId the full current user ID
     * @param nextUserId the full next user ID
     * @param turnNumberModulo the turn number modulo by 2
     * @param boardData the current board data
     * @return the row of buttons
     */
    public ArrayList<LayoutComponent> createAxisButtonRows(boolean isXAxis, String selection, String currentUserId, String nextUserId, int turnNumberModulo, String boardData){
        String shortCurrentId = Utilities.convertToBase(currentUserId,36);
        String shortNextId = Utilities.convertToBase(nextUserId,36);
        ArrayList<Button> tempButtonRow = new ArrayList<>();
        ArrayList<Button> tempButtonRow2 = new ArrayList<>();
        for(int i = 1; i <= 4; i++){
            String coordinate;
            String coordinate2;
            if (isXAxis) {
                coordinate = String.valueOf(i);
                coordinate2 = String.valueOf(i+4);
            } else {
                coordinate = letters[i-1];
                coordinate2 = letters[i+3];
            }
            Button newButton = createAxisButton(coordinate, selection, shortCurrentId, shortNextId, turnNumberModulo, boardData, false);
            tempButtonRow.add(newButton);
            Button newButton2 = createAxisButton(coordinate2, selection, shortCurrentId, shortNextId, turnNumberModulo, boardData, false);
            tempButtonRow2.add(newButton2);
        }
        ArrayList<LayoutComponent> components = new ArrayList<>();
        components.add(ActionRow.of(tempButtonRow));
        components.add(ActionRow.of(tempButtonRow2));
        return components;
    }

    /**
     * Create a button to select a point on an axis
     * @param xOrY the X or Y coordinate
     * @param selection the previous button's selection, if there was one
     * @param shortCurrentId the ID of the user whose turn it is, shortened
     * @param shortNextId the ID of the user whose turn it isn't, shortened
     * @param turnNumberModulo the modulo of the turn number by 2
     * @param boardData the current state of the board
     * @param isDisabled if the button is disabled or not
     * @return the constructed button
     */
    public Button createAxisButton(String xOrY, String selection, String shortCurrentId, String shortNextId, int turnNumberModulo, String boardData, boolean isDisabled) {
        if(selection == null) {
            selection = "";
        }
        //When selecting X it will just be X, when selecting Y it will be 1A for example
        selection = selection.concat(xOrY);
        System.out.println("BUTTON ID: " + "c:"+selection+":"+turnNumberModulo+":"+shortCurrentId+":"+shortNextId+":"+boardData);
        return Button.primary("c:"+selection+":"+turnNumberModulo+":"+shortCurrentId+":"+shortNextId+":"+boardData, xOrY).disabled(isDisabled);
    }
}
