package myplayer;

import ap25.Move;

public interface Strategy{
    Move search(BitBoard board);
}