package ap25.league;

import java.util.Arrays;
import ap25.Player;

public class Result {
  final static String NL = System.lineSeparator();
  Player players[];
  Match[][] matrix;
  Record[] records;

  Result(Player[] players, Match[][] matrix) {
    this.players = players;
    this.matrix = matrix;
    this.records = new Record[this.players.length];

    for (var i = 0; i < this.players.length; i++) {
      var r = new Record();
      r.i = i;
      r.player = this.players[i];
      r.matches = this.matrix[i];
      this.records[i] = r;
    }
  }

  void rank() {
    for (var i = 0; i < this.records.length; i++) {
      var r0 = this.records[i];

      for (var j = 0; j < this.records.length; j++) {
        if (i == j) continue;
        var m = r0.matches[j];
        var r1 = this.records[j];
        update(m.blackStat, r0);
        update(m.whiteStat, r1);
      }
    }

    Arrays.sort(this.records);
  }

  void update(Stat s, Record r) {
    r.n += s.win + s.lose + s.draw;
    r.win += s.win;
    r.lose += s.lose;
    r.draw += s.draw;
    r.score += s.score;
    r.time += s.time;
  }

  public String toString() {
    var buf = new StringBuilder();
    makeOverall(buf);
    makeDetail(buf);
    return buf.toString();
  }

  void makeOverall(StringBuilder buf) {
    buf.append("***** RANK *****" + NL);
    buf.append("   ||name| win|lose|draw|| ratio| score|  time ||" + NL);
    buf.append("---++----+----+----+----++------+------+-------++" + NL);

    var i = 1;
    for (var r: this.records) {
      var t = r.time / r.n;
      var s = String.format("%3d||%4s|%4d|%4d|%4d||%5.1f%%|%6d|%6.3fs||",
          i, r.player, r.win, r.lose, r.draw, 100.0 * r.ratio(), r.score, t);
      buf.append(s + NL);
      i += 1;
    }
    buf.append(NL);
  }

  void makeDetail(StringBuilder buf) {
    var line1 = new StringBuilder("   .x");
    var line2 = new StringBuilder("   o+");

    for (var r0: this.records) {
      line1.append(String.format(" %-4s ", r0.player));
      line2.append("=====+");
    }

    buf.append(line1.toString() + NL + line2.toString() + NL);

    for (var r0: this.records) {
      var matches = this.matrix[r0.i];
      buf.append(String.format("%4s|", r0.player));
      makeDetailLine(buf, matches);
    }
  }

  void makeDetailLine(StringBuilder buf, Match[] matches) {
    for (var r1: this.records) {
      var m = matches[r1.i];
      if (m == null) {
        buf.append("- - -|");
      } else {
        var b = m.blackStat.win;
        var w = m.whiteStat.win;
        var s = String.format("%2d-%2d", b, w);
        buf.append(String.format("%5s|", s));
      }
    }
    buf.append(NL);
  }
}
