package ÖvningQuizTest;

import java.io.*;
import java.net.Socket;

class ServerSidePlayer extends Thread {
    ServerSidePlayer opponent;
    Socket socket;
    BufferedReader input;
    ObjectOutputStream output;
    ServerSideGame game;
    String player;


    public ServerSidePlayer(Socket socket, ServerSideGame game, String player) {

        this.socket = socket;
        this.game = game;
        this.player = player;

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject("MESSAGE Waiting for opponent to connect");
        } catch (IOException e) {
            System.out.println("Player died: " + e);
        }
    }

    /**
     * Accepts notification of who the opponent is.
     */
    public void setOpponent(ServerSidePlayer opponent) {
        this.opponent = opponent;
    }

    /**
     * Returns the opponent.
     */
    public ServerSidePlayer getOpponent() {
        return opponent;
    }


    public void run() {

        String userAnswer;
        String pickedCategory = "";
        int rondnr = 0;


        try {

            // Quiz runda
            while (true) {

                output.writeObject(game.categories.get(0).getName() + " " + game.categories.get(1).getName());

                if (this.equals(game.currentPlayer)) {
                    output.writeObject("MESSAGE Select a category");
                    while ((pickedCategory = input.readLine()) != null) {
                        game.setSelectedCategory(pickedCategory);
                        break;
                    }
                } else {
                    output.writeObject("MESSAGE Other player is choosing category ");
                    output.writeObject("DISABLE");
                }

                while (game.getSelectedCategory() != null) {
                    output.writeObject(game.getSelectedCategory().getQuestions().get(rondnr));


                    if ((userAnswer = input.readLine()) != null) {
                        if (userAnswer.equals(game.getSelectedCategory().getQuestions().get(rondnr).getAnswer())) {
                            game.points++;

                        }
                    }
                    game.legalMove(this);
                    rondnr++;
                    Thread.sleep(2000);
                    if (rondnr == 2) {
                        rondnr = 0;
                        output.writeObject("POINTS" + "\n" + player + ": " + game.getMyPoints() + "\n" + opponent.player + ": " + opponent.game.getMyPoints());
                        Thread.sleep(2000);
                        break;
                    }
                }


                /*if (userAnswer.startsWith("MOVE")) {
                    int location = Integer.parseInt(userAnswer.substring(5));
                    if (game.legalMove(location, this)) {
                        output.println("VALID_MOVE");
                        output.println(game.hasWinner() ? "VICTORY"
                                : game.boardFilledUp() ? "TIE"
                                : "");
                    } else {
                        output.println("MESSAGE ?");
                    }
                } else if (userAnswer.startsWith("QUIT")) {
                    return;
                }*/
            }

        } catch (IOException e) {
            System.out.println("Player died: " + e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
