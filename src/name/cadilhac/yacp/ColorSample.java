package name.cadilhac.yacp;

import android.os.Bundle;
import android.util.AttributeSet;
import android.content.res.TypedArray;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.graphics.Path;
import android.content.Context;
import java.util.Random;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.AnalogClock;

public class ColorSample extends View {

    private Path mSquiggle;
    private Paint mSquigglePaint;

    public ColorSample (Context context) {
      this (context, null, 0);
    }

    public ColorSample (Context context, AttributeSet attrs) {
      this (context, attrs, 0);
    }

    public ColorSample (Context context, AttributeSet attrs, int defStyle) {
      super (context, attrs, defStyle);

      TypedArray a = context.obtainStyledAttributes(attrs,
						    R.styleable.ColorSample,
						    defStyle, 0);

      mSquigglePaint = new Paint (Paint.ANTI_ALIAS_FLAG);
      setColor (a.getColor(R.styleable.ColorSample_android_gestureColor, 0xFF000000));
      mSquigglePaint.setStyle (Paint.Style.STROKE);
      mSquigglePaint.setStrokeCap (Paint.Cap.ROUND);
      setStrokeWidth (a.getFloat(R.styleable.ColorSample_android_gestureStrokeWidth, 0.f));

      a.recycle();
    }

    public void setColor (int color) {
      mSquigglePaint.setColor (color);
      invalidate ();
    }

    public void setStrokeWidth (float strokeWidth) {
      mSquigglePaint.setStrokeWidth (strokeWidth);
      invalidate ();
    }

    @Override 
    protected void onDraw (Canvas canvas) {
      super.onDraw (canvas);
      canvas.drawPath (mSquiggle, mSquigglePaint);
    }

    @Override
    protected void onSizeChanged (int width, int height,
				  int oldwidth, int oldheight) {
      super.onSizeChanged (width, height, oldheight, oldheight);

      Random rand = new Random ();
      mSquiggle = new Path ();
      mSquiggle.moveTo (width / 10, height / 6 + rand.nextFloat () * height / 4);
      mSquiggle.cubicTo (width / 3, height / 2 + rand.nextFloat () * height / 2,
			 2 * width / 3, rand.nextFloat () * height / 2,
			 9 * width / 10, 2 * height / 3 + rand.nextFloat () * height / 4);
    }
}
