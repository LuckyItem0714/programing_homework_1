package myplayer;

import static ap25.Board.*;
import static ap25.Color.*;
import static ap25.Move.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.Scanner;
import ap25.*;

public class HumanPlayer extends ap25.Player{
    static final String MY_NAME = "Human";
    Move move;
    MyBoard board;
    private Scanner sc = new Scanner(System.in);
    
    public HumanPlayer(Color color){
	this(MY_NAME, color);
    }
    
    public HumanPlayer(String name, Color color){
	super(name, color);
	this.board = new MyBoard();
    }

    public void setBoard(Board board){
	for(int i = 0; i < LENGTH; i++){
	    this.board.set(i, board.get(i));
	}
    }
	
    boolean isBlack(){return getColor() == BLACK;}

    public Move think(Board board){
	this.board = this.board.placed(board.getMove());
	if(this.board.findNoPassLegalIndexes(getColor()).size() == 0){
	    this.move = Move.ofPass(getColor());
	} else{
	    this.move = null;

	    decide(board);
	    this.move = this.move.colored(getColor());
	}

	this.board = this.board.placed(this.move);
	return this.move;
    }
    
    void decide(Board board){
	Move m = null;
	while(m == null){
	    System.out.println("手を入力(例: a1, b1, ...");
	    m = Move.of(sc.next(), getColor());
	    if(!board.findLegalMoves(getColor()).contains(m)){
		System.out.println("その手は選べません。もう一度入力してください");
		m = null;
	    }
	}
	this.move = m;
    }
}
