package myplayer;

import ap25.*;

public record MoveScoreBoard(Move move, BitBoard board, int score) implements Comparable<MoveScoreBoard> {
    @Override
    public int compareTo(MoveScoreBoard other) {
        return Integer.compare(this.score, other.score); // 昇順
    }
}
