package name.cadilhac.yacp;

public class Brush {
    int mColor;
    float mStrokeWidth;

    public Brush () {
      this (0, 0.f);
    }

    public Brush (int color, float strokeWidth) {
      mColor = color;
      mStrokeWidth = strokeWidth;
    }

    public int getColor () {
      return mColor;
    }

    public float getStrokeWidth () {
      return mStrokeWidth;
    }

    public Brush setColor (int color) {
      mColor = color;
      return this;
    }

    public Brush setStrokeWidth (float strokeWidth) {
      mStrokeWidth = strokeWidth;
      return this;
    }
}
