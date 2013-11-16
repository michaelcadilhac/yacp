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
import android.widget.Space;
import android.widget.AnalogClock;
import android.widget.GridLayout;
import android.util.TypedValue;
import android.widget.ImageView;
import java.util.List;

public class ColorHistory extends GridLayout {
    public interface OnBrushPickedListener {
        void onBrushPicked (Brush brush);
    }

    private OnBrushPickedListener mListener = null;
    private List<Brush> mHistory;
    private boolean mRelayout = true;
    private float mThicknessRatio = 1.f;
    private ColorSample mSample;

    public ColorHistory (Context context) {
      this (context, null, 0);
    }

    public ColorHistory (Context context, AttributeSet attrs) {
      this (context, attrs, 0);
    }

    public ColorHistory (Context context, AttributeSet attrs, int defStyle) {
      super (context, attrs, defStyle);
      TypedArray a = context.obtainStyledAttributes(attrs,
						    R.styleable.ColorHistory,
						    defStyle, 0);
      mThicknessRatio = a.getFloat (R.styleable.ColorHistory_android_thicknessRatio, mThicknessRatio);
      a.recycle();
    }

    public ColorHistory setHistory (List<Brush> history) {
      if (getChildCount () != 0)
	removeViews (0, getChildCount () - 1);
      mHistory = history;
      mRelayout = true;
      requestLayout ();
      return this;
    }

    public ColorHistory setOnBrushPickedListener (OnBrushPickedListener listener) {
      mListener = listener;
      return this;
    }

    public ColorHistory setThicknessRatio (float ratio) {
      mThicknessRatio = ratio;
      return this;
    }

    @Override
    public void setRowCount (int rowCount) {
      if (getChildCount () != 0)
	removeViews (0, getChildCount () - 1);
      mRelayout = true;
      super.setRowCount (rowCount);
    }
       
    @Override
    public void setColumnCount (int columnCount) {
      if (getChildCount () != 0)
	removeViews (0, getChildCount () - 1);
      mRelayout = true;
      super.setColumnCount (columnCount);
    }

    boolean mAddedFromLayout = false;
    @Override
    public void addView (View child, int index, ViewGroup.LayoutParams params) {
      if (mAddedFromLayout) {
	super.addView (child, index, params);
      } else {
	if (!(ColorSample.class.isInstance (child)))
	  throw new IllegalArgumentException
	    ("ColorHistory should only have a ColorSample child.");
	mSample = (ColorSample) child;
      }
    }

    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
      if (mRelayout) {
	int eightDp = (int)
	  TypedValue.applyDimension (TypedValue.COMPLEX_UNIT_DIP, 8,
				     getContext ().getResources ().getDisplayMetrics ());
	int cellWidth = (right - left) / getColumnCount () - eightDp;
	int cellHeight = (bottom - top) / getRowCount () - eightDp;
	int row = 0;
	int col = 0;

	setUseDefaultMargins (false);
	mRelayout = false;

	if (getChildCount () != 0)
	  removeViews (0, getChildCount ());

	if (mHistory != null)
	  for (int i = Math.max (0, mHistory.size () - getColumnCount () * getRowCount ());
	       i < mHistory.size (); ++i) {
	    GridLayout.LayoutParams lp =
	      new GridLayout.LayoutParams (GridLayout.spec (row), GridLayout.spec (col));

	    lp.width = cellWidth;
	    lp.height = cellHeight;
	    // Add space using margin between the history elements, but leave it
	    // to the Layout to have its own margin.
	    lp.topMargin = (row == 0 ? 0 : eightDp / 2);
	    lp.bottomMargin = (row == getRowCount () - 1 ? 0 : eightDp / 2);
	    lp.leftMargin = (col == 0 ? 0 : eightDp / 2);
	    lp.rightMargin = (col == getColumnCount () - 1 ? 0 : eightDp / 2);

	    ColorSample cs = new ColorSample (getContext ());
	    cs.setColor (mHistory.get (i).getColor ())
	      .setStrokeWidth (mHistory.get (i).getStrokeWidth () * mThicknessRatio)
	      .setOnClickListener (new OnClickListener () {
		    public void onClick (View v) {
		      if (mListener != null) {
			Brush brush = ((ColorSample) v).getBrush ();
			mListener.onBrushPicked (
			  new Brush (brush.getColor (),
				     brush.getStrokeWidth () / mThicknessRatio));
		      }}});

	    // Copy everything useful from mSample.
	    cs.setAdjustViewBounds (mSample.getAdjustViewBounds());
	    cs.setBaseline (mSample.getBaseline ());
	    cs.setBaselineAlignBottom (mSample.getBaselineAlignBottom ());
	    cs.setCropToPadding (mSample.getCropToPadding ());
	    cs.setScaleType (mSample.getScaleType ());
	    cs.setColorFilter (mSample.getColorFilter ());
	    cs.setImageDrawable (mSample.getDrawable ());
	    cs.setImageMatrix (mSample.getImageMatrix ());
	    {
	      mAddedFromLayout = true;
	      super.addView (cs, lp);
	      mAddedFromLayout = false;
	    }
	    if (++col == getColumnCount ()) {
	      ++row; col = 0;
	    }
	  }
      }
      super.onLayout (changed, left, top, right, bottom);
    }
}
