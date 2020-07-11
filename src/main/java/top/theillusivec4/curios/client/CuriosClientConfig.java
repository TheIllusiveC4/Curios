package top.theillusivec4.curios.client;

public class CuriosClientConfig {

  public enum ButtonCorner {
    TOP_LEFT(26, -75, 73, -62), TOP_RIGHT(61, -75, 95, -62), BOTTOM_LEFT(26, -20, 73,
        -29), BOTTOM_RIGHT(61, -20, 95, -29);

    final int xoffset;
    final int yoffset;
    final int creativeXoffset;
    final int creativeYoffset;

    ButtonCorner(int x, int y, int creativeX, int creativeY) {
      xoffset = x;
      yoffset = y;
      creativeXoffset = creativeX;
      creativeYoffset = creativeY;
    }

    public int getXoffset() {
      return xoffset;
    }

    public int getYoffset() {
      return yoffset;
    }

    public int getCreativeXoffset() {
      return creativeXoffset;
    }

    public int getCreativeYoffset() {
      return creativeYoffset;
    }
  }
}
