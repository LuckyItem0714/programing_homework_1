package ap25;

import java.util.Map;

public enum Color {
  BLACK(1),
  WHITE(-1),
  NONE(0),
  BLOCK(3);

  static Map<Color, String> SYMBOLS =
      Map.of(BLACK, "o", WHITE, "x", NONE, " ", BLOCK, "#");

  private int value;

  private Color(int value) {
    this.value = value;
  }

  public int getValue() {
    return this.value;
  }

  public Color flipped() {
    switch (this) {
    case BLACK: return WHITE;
    case WHITE: return BLACK;
    default: return this;
    }
  }

  public String toString() {
    return SYMBOLS.get(this);
  }

  public Color parse(String str) {
    return Map.of("o", BLACK, "x" , WHITE).getOrDefault(str, NONE);
  }
}
