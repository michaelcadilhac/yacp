package name.cadilhac.yacp;

import android.app.DialogFragment;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.SeekBar;
import android.graphics.Color;
import java.util.List;
import java.util.ArrayList;
import android.graphics.PorterDuff;

public class BrushPickerDialog extends DialogFragment
  implements ColorWheel.OnColorChangedListener,
  SeekBar.OnSeekBarChangeListener {

    public interface OnBrushPickedListener {
        void onBrushPicked (Brush brush);
    }

    private static final float MAX_STROKEWIDTH = 100.f;

    private OnBrushPickedListener mListener = null;
    private ColorWheel mWheel = null;
    private ColorSample mSample = null;
    private SeekBar mAlphaBar = null;
    private SeekBar mStrokeBar = null;
    private List<Brush> mHistory = null;

    public static BrushPickerDialog newInstance (int color, float strokeWidth,
						 List<Brush> history) {
      BrushPickerDialog frag = new BrushPickerDialog ();
      Bundle args = new Bundle ();
      args.putInt ("color", color);
      args.putFloat ("stroke_width", strokeWidth);
      if (history != null && history.size () != 0) {
	int[] historyColor = new int[history.size ()];
	float[] historyStrokeWidth = new float[history.size ()];
	int i = 0;
	for (Brush brush : history) {
	  historyColor[i] = brush.getColor ();
	  historyStrokeWidth[i] = brush.getStrokeWidth ();
	  ++i;
	}
	args.putIntArray ("history_color", historyColor);
	args.putFloatArray ("history_stroke_width", historyStrokeWidth);
      }
      frag.setArguments(args);
      return frag;
    }

    @Override
    public void onAttach (Activity activity) {
      super.onAttach (activity);

      if (mListener == null && (activity instanceof OnBrushPickedListener))
	mListener = (OnBrushPickedListener) activity;
    }

    public int getColor () {
      return mSample.getColor ();
    }

    public float getStrokeWidth () {
      return mSample.getStrokeWidth ();
    }

    public Brush getBrush () {
      return mSample.getBrush ();
    }

    public List<Brush> getHistory () {
      return mHistory;
    }

    public BrushPickerDialog setOnBrushPickedListener (OnBrushPickedListener listener) {
      mListener = listener;
      return this;
    }

    public void onColorChanged (int color) {
      // The wheel is alpha-agnostic.
      mSample.setColor ((color & 0x00FFFFFF) | (mSample.getColor () & 0xFF000000));
    }

    // One of the two seekBars changed.
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      if (seekBar.getId () == R.id.alpha)
	mSample.setColor ((mSample.getColor () & 0x00FFFFFF) |
			  ((progress * 255 / 100) << 24));
      else
	mSample.setStrokeWidth (progress * MAX_STROKEWIDTH / 100.f);
    }

    public void onStopTrackingTouch (SeekBar sb) {}
    public void onStartTrackingTouch (SeekBar sb) {}

    private void setBrush (Brush brush) {
      mWheel.setColor (brush.getColor () | 0xFF000000);
      mAlphaBar.setProgress ((int) (Color.alpha (brush.getColor ()) * 100.f / 255.f));
      mStrokeBar.setProgress ((int) (brush.getStrokeWidth () * 100.f / MAX_STROKEWIDTH));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final View layout = getActivity ().getLayoutInflater().
	inflate (R.layout.brush_picker_dialog, null);

      final AlertDialog alert = new AlertDialog.Builder (getActivity ())
	.setPositiveButton(R.string.alert_dialog_ok,
			   new DialogInterface.OnClickListener() {
			       public void onClick(DialogInterface dialog, int whichButton) {
				 if (mListener != null)
				   mListener.onBrushPicked (mSample.getBrush ());
			       }
			   }
	  )
	.setNegativeButton(R.string.alert_dialog_cancel, null)
	.setView (layout)
	.create ();

      mSample = ((ColorSample) layout.findViewById (R.id.sample))
	.setColor (getArguments().getInt ("color"))
	.setStrokeWidth (Math.min (getArguments().getFloat ("stroke_width"),
				   MAX_STROKEWIDTH));

      mWheel = (ColorWheel) layout.findViewById (R.id.wheel);
      mWheel.setOnColorChangedListener (this);

      mAlphaBar = (SeekBar) layout.findViewById (R.id.alpha);
      mAlphaBar.setOnSeekBarChangeListener(this);

      mStrokeBar = (SeekBar) layout.findViewById (R.id.strokewidth);
      mStrokeBar.setOnSeekBarChangeListener(this);

      setBrush (mSample.getBrush ());

      int[] historyColor = getArguments ().getIntArray ("history_color");
      float[] historyStrokeWidth = getArguments ().getFloatArray ("history_stroke_width");
      if (historyColor != null && historyStrokeWidth != null &&
	  historyColor.length != 0 &&
	  historyColor.length == historyStrokeWidth.length) {
	mHistory = new ArrayList<Brush> (historyColor.length);
	for (int i = 0; i < historyColor.length; ++i)
	  mHistory.add (new Brush (historyColor[i], historyStrokeWidth[i]));
      }
      ((ColorHistory) layout.findViewById (R.id.history)).setHistory (mHistory)
	.setOnBrushPickedListener (new ColorHistory.OnBrushPickedListener () {
	      public void onBrushPicked (Brush brush) {
		setBrush (brush);
	      }});

      return alert;
    }
}
