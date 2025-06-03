package ap25;

public abstract class Player {
  String name;
  Color color;
  Board board;

  public Player(String name, Color color) {
    this.name = name;
    this.color = color;
  }

  public void setBoard(Board board) { this.board = board; }
  public Color getColor() { return this.color; }
  public String toString() { return this.name; }
  public Move think(Board board) { return null; }
}
