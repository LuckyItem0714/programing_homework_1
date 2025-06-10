package ap25.league;

import ap25.Player;

class Record implements Comparable<Record> {
  int i;
  int n;
  Player player;
  Match[] matches;
  int win;
  int lose;
  int draw;
  int score;
  float time;

  float ratio() {
    return (float) this.win / (this.win + this.lose + this.draw);
  }

  public int compareTo(Record o) {
    if (this.win != o.win) return o.win - this.win;
    if (this.score != o.score) return o.score - this.score;
    if (this.time != o.time) return (int) (this.time - o.time);
    return 0;
  }
}

