package ap25.league;

import static ap25.Color.BLACK;
import static ap25.Color.WHITE;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ap25.Color;
import ap25.Player;

class Stat {
  int win;
  int lose;
  int draw;
  int score;
  int time;

  void update(float score, float time) {
    if (score > 0) {
      this.win += 1;
      this.score += score;
    } else if (score < 0) {
      this.lose += 1;
    } else {
      this.draw += 1;
      this.score += 1;
    }
    this.time += time;
  }
}

class Match {
  Player black;
  Player white;
  Map<Color, Player> players;
  List<OfficialBoard> boards = new ArrayList<>();
  List<Game> games = new ArrayList<>();
  long timeLimit;
  Stat blackStat = new Stat();
  Stat whiteStat = new Stat();

  Match(List<OfficialBoard> boards, Player black, Player white, long timeLimit) {
    // this.boards = new ArrayList<>(boards);
    this.boards = boards;
    this.black = black;
    this.white = white;
    this.players = Map.of(BLACK, black, WHITE, white);
    this.timeLimit = timeLimit;
    this.games = this.boards.stream()
        .map(b -> new Game(b, black, white, this.timeLimit)).toList();
  }

  void play() {
    this.games.forEach(g -> {
      g.play();
      update(g);
    });
  }

  void update(Game game) {
    var score = game.board.score();
    this.blackStat.update(score, game.times.get(BLACK));
    this.whiteStat.update(-score, game.times.get(WHITE));
  }
}
