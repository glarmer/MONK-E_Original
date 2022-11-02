package com.lordnoisy.hoobabot;

import com.lordnoisy.hoobabot.utility.DiscordUtilities;
import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class TicTacToe {
    private final String fighterSymbol = "\u274E";
    private final String opponentSymbol = "\uD83C\uDD7E";
    private final String emptySymbol = ":black_large_square:";

    /**
     * Start a game of tic-tac-toe
     * @param fighter the person who initiated the game
     * @param opponent the opponent
     * @return the message to be created
     */
    public MessageCreateSpec startTicTacToe(Member fighter, Snowflake opponent) {
        String name = fighter.getDisplayName();
        String url = fighter.getAvatarUrl();
        String[][] moves = new String[3][3];
        String startingBoard = createDescription(moves);
        return MessageCreateSpec.builder()
                .addEmbed(createTicTacToeEmbed(name, url, startingBoard, opponent.asString(), fighter.getId().asString(), opponent.asString(), null, false))
                .addAllComponents(createButtonRows(fighter.getId().asString(), opponent.asString(), "1", null, moves, false))
                .build();
    }

    /**
     * Get the edit for tic-tac-toe
     * @param message the tic-tac-toe message
     * @param buttonId the buttonId that has been pressed
     * @param buttonPresserId the ID of the user who pressed the button
     * @return the edit spec
     */
    public MessageEditSpec updateTicTacToe(Message message, String buttonId, String buttonPresserId) {
        if (!DiscordUtilities.isBotMessage(message.getClient(), message)) {
            return null;
        }
        if (message.getEmbeds().size() != 1) {
            return null;
        }
        Embed ticTacToeEmbed = message.getEmbeds().get(0);
        Embed.Author author = ticTacToeEmbed.getAuthor().orElse(null);
        if (author == null) {
            return null;
        }
        String authorName = author.getName().orElse("");
        if (!authorName.contains("tic-tac-toe")) {
            return null;
        }
        String name = authorName.split("has")[0].trim();
        String url = author.getUrl().orElse("");

        String[] buttonInfo = buttonId.split(":");

        //Make sure only the allowed user is making a turn
        String currentTurnUser = buttonInfo[2];
        String nextTurnUser = buttonInfo[3];
        if (!currentTurnUser.equals(buttonPresserId)) {
            return null;
        }
        boolean botMatch = false;
        boolean isBotTurn = false;
        String botId = message.getClient().getSelfId().asString();
        if (nextTurnUser.equals(botId)) {
            botMatch = true;
        }
        if (currentTurnUser.equals(botId)) {
            isBotTurn = true;
        }


        String[][] moves = new String[3][3];

        //Add freshly pressed option
        int newX = Integer.parseInt(buttonInfo[1].split(",")[0])-1;
        int newY = Integer.parseInt(buttonInfo[1].split(",")[1])-1;
        String symbol;
        int turnNumber = Integer.parseInt(buttonInfo[4]);
        if (turnNumber%2==1) {
            symbol = fighterSymbol;
        } else {
            symbol = opponentSymbol;
        }

        moves[newX][newY] = symbol;

        // Calculate board
        String boardState = "";
        for (int i = 5; i < buttonInfo.length; i++) {
            int currentIteration = i-5;
            int y;
            int x;
            if (currentIteration == 0) {
                y = 0;
                x = 0;
            } else {
                y = Math.floorDiv(currentIteration, 3);
                x = currentIteration - (y*3);
            }
            if (newX == x && newY == y) {
                if (symbol.equals(fighterSymbol)) {
                    boardState = boardState.concat("x:");
                } else {
                    boardState = boardState.concat("o:");
                }
                continue;
            }
            String currentPlace = buttonInfo[i];
            switch (currentPlace) {
                case "e":
                    boardState = boardState.concat("e:");
                    break;
                case "x":
                    boardState = boardState.concat("x:");
                    moves[x][y] = fighterSymbol;
                    break;
                case "o":
                        boardState = boardState.concat("o:");
                        moves[x][y] = opponentSymbol;
                    break;
            }
        }

        boolean gameFinished = false;
        if (turnNumber==9){
            gameFinished = true;
            System.out.println("DETECTED GAME FINISHED");
        }

        String winner = getWinner(moves);
        boolean hasWon = false;
        if (winner != null) {
            hasWon = true;
            winner = currentTurnUser;
        }

        String opponentId = ticTacToeEmbed.getDescription().orElse("b\nn").split("challenged")[1].replace("@","").replace("<","").replace(">","").split("!")[0].trim();

        boolean stalemate = gameFinished && !hasWon;
        System.out.println("STALEMATE IS " + stalemate);

        String playBoard = createDescription(moves);
        List<EmbedCreateSpec> embeds;
        if (botMatch) {
            embeds = List.of(createTicTacToeEmbed(name, url, playBoard, opponentId, currentTurnUser, nextTurnUser, winner, stalemate));
        } else {
            embeds = List.of(createTicTacToeEmbed(name, url, playBoard, opponentId, nextTurnUser, currentTurnUser, winner, stalemate));
        }
        List<LayoutComponent> buttons = createButtonRows(nextTurnUser, currentTurnUser, String.valueOf(turnNumber+1), boardState, moves, hasWon);
        if (stalemate) {
            buttons.clear();
        }
        if (botMatch && !isBotTurn && !hasWon && !stalemate) {
            int[] botCoordinates = getMove(moves);
            int botX = botCoordinates[0];
            int botY = botCoordinates[1];

            int number = botY*3 + botX;
            String[] board = boardState.split(":");
            boardState = "";
            for (int i = 0; i < board.length; i++) {
                if (i!=number) {
                    boardState = boardState.concat(board[i]+":");
                } else {
                    boardState = boardState.concat("o:");
                }
            }
            String fakeButtonId = "tic_tac_toe:"+(botX+1)+","+(botY+1)+":"+nextTurnUser+":"+currentTurnUser+":"+(turnNumber+1)+":"+boardState;
            return updateTicTacToe(message, fakeButtonId, botId);
        } else {
            return MessageEditSpec.builder()
                    .addAllEmbeds(embeds)
                    .addAllComponents(buttons)
                    .build();
        }
    }

    /**
     * Get a move
     * @param moves the current moves
     * @return the new move
     */
    public int[] getMove(String[][] moves) {
        Random random = new Random();
        int chance = random.nextInt(1,4);
        if (chance == 2) {
            return getRandomMove(moves);
        }
        int[] winningMove = getCalculatedMove(moves, opponentSymbol);
        if (winningMove[0]!=-1) {
            int winningChance = random.nextInt(1,11);
            if (winningChance < 10) {
                return winningMove;
            }
        }

        int[] calculatedMove = getCalculatedMove(moves, fighterSymbol);
        if (calculatedMove[0]==-1) {
            return getRandomMove(moves);
        } else {
            return calculatedMove;
        }
    }

    /**
     * Get a calculated move
     * @param moves the current moves
     * @param symbol the player symbol
     * @return coordinates for a move
     */
    public int[] getCalculatedMove(String[][] moves, String symbol) {
        ArrayList<Coordinates> potentialMoves = new ArrayList<>();
        boolean isCentreTakenByFighter = false;
        boolean haveNonCentreMovesBeenMade = false;
        if (Objects.equals(moves[1][1], symbol)) {
            isCentreTakenByFighter = true;
        }
        for (int y = 0; y <= 2; y++) {
            int numberOfFighterSymbolsRow = 0;
            int lastFreeSpaceRow = -1;
            int numberOfFighterSymbolsColumn = 0;
            int lastFreeSpaceColumn = -1;
            for (int x = 0; x <= 2; x++) {
                //Checks rows
                if (Objects.equals(moves[x][y], symbol)) {
                    numberOfFighterSymbolsRow++;
                } else if (moves[x][y]==null) {
                    lastFreeSpaceRow = x;
                }
                //Checks columns
                if (Objects.equals(moves[y][x], symbol)) {
                    numberOfFighterSymbolsColumn++;
                } else if (moves[y][x]==null) {
                    lastFreeSpaceColumn = x;
                }

                if (!(moves[y][x] == null || moves[x][y] == null)&&(x!=1 && y!=1)) {
                    haveNonCentreMovesBeenMade = true;
                }
            }
            if (numberOfFighterSymbolsRow == 2 && lastFreeSpaceRow != -1) {
                Coordinates potentialMoveRow = new Coordinates(lastFreeSpaceRow, y);
                potentialMoves.add(potentialMoveRow);
            }
            if (numberOfFighterSymbolsColumn == 2 && lastFreeSpaceColumn != -1) {
                Coordinates potentialMoveColumn = new Coordinates(y, lastFreeSpaceColumn);
                potentialMoves.add(potentialMoveColumn);
            }
        }

        //Make it pick a corner if the centre is the only move played
        if (isCentreTakenByFighter && !haveNonCentreMovesBeenMade) {
            potentialMoves.add(new Coordinates(0, 0));
            potentialMoves.add(new Coordinates(0, 2));
            potentialMoves.add(new Coordinates(2, 0));
            potentialMoves.add(new Coordinates(2, 2));
        }

        //Calculate diagonals
        if (isCentreTakenByFighter) {
            for (int x = 0; x <= 2; x = x + 2) {
                // 0 0, 0 2| 2 0, 2 2
                for (int y = 0; y <= 2; y = y + 2) {
                    if (Objects.equals(moves[x][y], symbol)) {
                        int newX;
                        int newY;
                        if (x == 0) {
                            newX = 2;
                        } else {
                            newX = 0;
                        }
                        if (y == 0) {
                            newY = 2;
                        } else {
                            newY = 0;
                        }
                        if (moves[newX][newY] == null) {
                            potentialMoves.add(new Coordinates(newX, newY));
                        }
                    }
                }
            }
        }

        if (potentialMoves.size()>0) {
            Random random = new Random();
            int calculatedMoveIndex = random.nextInt(0,potentialMoves.size());
            int botX = potentialMoves.get(calculatedMoveIndex).getX();
            int botY = potentialMoves.get(calculatedMoveIndex).getY();
            return new int[]{botX, botY};
        } else {
            return new int[]{-1, -1};
        }
    }

    /**
     * Get a random move that hasn't been taken
     * @param moves the current moves
     * @return coordinates for a move
     */
    public int[] getRandomMove(String[][] moves) {
        Random random = new Random();
        int botX = random.nextInt(0,3);
        int botY = random.nextInt(0,3);
        if (moves[botX][botY] == null) {
            moves[botX][botY] = opponentSymbol;
        } else {
            return getRandomMove(moves);
        }

        return new int[]{botX, botY};
    }

    /**
     * Calculates if a user has won
     * @param moves the array of current moves
     * @return
     */
    public String getWinner(String[][] moves) {
        for (int xy = 0; xy < 3; xy++) {
            //Check all columns
            if (!Objects.equals(moves[xy][0], null)) {
                if (Objects.equals(moves[xy][0], moves[xy][1]) && Objects.equals(moves[xy][0], moves[xy][2])) {
                    return moves[xy][0];
                }
            }
            if (!Objects.equals(moves[0][xy], null)) {
                if (Objects.equals(moves[0][xy], moves[1][xy]) && Objects.equals(moves[0][xy], moves[2][xy])) {
                    return moves[0][xy];
                }
            }
        }
        if (moves[1][1]!=null) {
            if (Objects.equals(moves[1][1], moves[0][0]) && Objects.equals(moves[1][1], moves[2][2])) {
                return moves[1][1];
            }
            if (Objects.equals(moves[1][1], moves[2][0]) && Objects.equals(moves[1][1], moves[0][2])) {
                return moves[1][1];
            }
        }
        return null;
    }

    /**
     * Creates the tic-tac-toe embed
     * @param name the challengers name
     * @param url the challengers profile picture url
     * @param currentBoard the current state of the board
     * @return the embed
     */
    public EmbedCreateSpec createTicTacToeEmbed(String name, String url, String currentBoard, String opponentId, String currentTurnUser, String nextTurnUser, String winner, boolean stalemate) {
        String title = name + " has initiated tic-tac-toe!";
        String extraDescription;
        String endDescription = "";
        if (!stalemate) {
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
        } else {
            extraDescription = "The game has ended in a stalemate :(\n\n";
            endDescription = "\nBetter luck next time to both players!";
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
     * Turn a moves array into a valid board
     * @param moves the 2d array of moves
     * @return a valid board
     */
    public String createDescription(String[][] moves) {
        String description = "";
        for (int y = 0; y <= 2; y++) {
            for (int x = 0; x <= 2; x++) {
                if (moves[x][y]==null) {
                    description = description.concat(emptySymbol);
                } else {
                    description = description.concat(moves[x][y]);
                }
            }
            description = description.concat("\n");
        }
        return description;
    }

    /**
     * Create all the buttons for tic-tac-toe
     * @param currentTurnUser the user who's taking their turn
     * @param nextTurnUser the user whose turn it isn't
     * @return all tic-tac-toe buttons in a List of ActionRows
     */
    public List<LayoutComponent> createButtonRows (String currentTurnUser, String nextTurnUser, String turnNumber, String boardState, String[][] moves, boolean isFinished) {
        ArrayList<LayoutComponent> buttons = new ArrayList<>();
        int numberOfDisabledButtons = 0;
        for (int y = 1; y <= 3; y++) {
            ArrayList<Button> tempButtonRow = new ArrayList<>();
            for (int x = 1; x <= 3; x++) {
                boolean isDisabled = moves[x - 1][y - 1] != null;
                if (isFinished) {
                    isDisabled = true;
                    numberOfDisabledButtons++;
                }
                Button newButton = this.createButton(x, y, currentTurnUser, nextTurnUser, turnNumber, isDisabled, boardState);
                tempButtonRow.add(newButton);
            }
            ActionRow actionRow = ActionRow.of(tempButtonRow);
            buttons.add(actionRow);
        }
        if (numberOfDisabledButtons==9) {
            buttons.clear();
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
    public Button createButton (int x, int y, String currentTurnUser, String nextTurnUser, String turnNumber, boolean isDisabled, String boardState) {
        if (boardState==null) {
            boardState = "e:e:e:e:e:e:e:e:e";
        }
        return Button.primary("tic_tac_toe:"+x+","+y+":"+currentTurnUser+":"+nextTurnUser+":"+turnNumber+":"+boardState, "Here!").disabled(isDisabled);
    }
}
