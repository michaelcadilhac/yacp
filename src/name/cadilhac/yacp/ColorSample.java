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
import android.os.Parcelable;
import android.os.Parcel;

public class ColorSample extends ImageView {

    private Path mSquiggle;
    private Paint mSquigglePaint;
    private Brush mBrush;

    static class SavedState extends BaseSavedState {
        Brush brush;

        SavedState(Parcelable superState) {
            super(superState);
        }
        
        private SavedState(Parcel in) {
            super(in);
	    brush = new Brush (in.readInt (), in.readFloat ());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel (out, flags);
            out.writeInt (brush.getColor ());
            out.writeFloat (brush.getStrokeWidth ());
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState () {
      Parcelable superState = super.onSaveInstanceState();
      SavedState ss = new SavedState(superState);
        
      ss.brush = mBrush;
      return ss;
    }


    @Override
    public void onRestoreInstanceState (Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState (ss.getSuperState());
        
        setBrush (ss.brush);
    }


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

      mBrush = new Brush ();
      mSquigglePaint = new Paint (Paint.ANTI_ALIAS_FLAG);
      setColor (a.getColor (R.styleable.ColorSample_android_gestureColor, 0xFF000000));
      mSquigglePaint.setStyle (Paint.Style.STROKE);
      mSquigglePaint.setStrokeCap (Paint.Cap.ROUND);
      setStrokeWidth (a.getFloat (R.styleable.ColorSample_android_gestureStrokeWidth, 0.f));
      a.recycle();
    }

    public ColorSample setColor (int color) {
      mSquigglePaint.setColor (color);
      mBrush.setColor (color);
      invalidate ();
      return this;
    }

    public ColorSample setStrokeWidth (float strokeWidth) {
      mSquigglePaint.setStrokeWidth (strokeWidth);
      mBrush.setStrokeWidth (strokeWidth);
      invalidate ();
      return this;
    }

    public ColorSample setBrush (Brush brush) {
      mSquigglePaint.setColor (brush.getColor ());
      mSquigglePaint.setStrokeWidth (brush.getStrokeWidth ());
      mBrush = brush;
      invalidate ();
      return this;
    }

    public int getColor () {
      return mBrush.getColor ();
    }

    public float getStrokeWidth () {
      return mBrush.getStrokeWidth ();
    }

    public Brush getBrush () {
      return mBrush;
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

    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
      super.onLayout (changed, left, top, right, bottom);
    }
}
