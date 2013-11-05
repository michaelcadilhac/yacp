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

public class BrushPickerDialog extends DialogFragment
  implements ColorWheel.OnColorChangedListener,
  SeekBar.OnSeekBarChangeListener {

    public interface OnBrushPickedListener {
        void onBrushPicked (int color, float strokeWidth);
    }

    private static final float MAX_STROKEWIDTH = 100.f;

    private OnBrushPickedListener mListener = null;
    private int mColor = 0;
    private float mStrokeWidth = 0.f;
    private ColorSample mSample = null;
    private SeekBar mAlphaBar = null;
    private SeekBar mStrokeBar = null;

    public static BrushPickerDialog newInstance (int color, float strokeWidth) {
      BrushPickerDialog frag = new BrushPickerDialog();
      Bundle args = new Bundle();
      args.putInt("color", color);
      args.putFloat("stroke_width", strokeWidth);
      frag.setArguments(args);
      return frag;
    }

    @Override
    public void onAttach (Activity activity) {
      super.onAttach (activity);

      if (mListener == null && (activity instanceof OnBrushPickedListener))
	mListener = (OnBrushPickedListener) activity;
    }

    public void setOnBrushPickedListener (OnBrushPickedListener listener) {
      mListener = listener;
    }

    public void onColorChanged (int color) {
      // The wheel is alpha-agnostic.
      mColor = (color & 0x00FFFFFF) | (mColor & 0xFF000000);
      mSample.setColor (color);
    }

    // One of the two seekBars changed.
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      if (seekBar.getId () == R.id.alpha) {
	mColor = (mColor & 0x00FFFFFF) | ((progress * 255 / 100) << 24);
	mSample.setColor (mColor);
      } else {
	mStrokeWidth = progress * MAX_STROKEWIDTH / 100.f;
	mSample.setStrokeWidth (mStrokeWidth);
      }
    }

    public void onStopTrackingTouch (SeekBar sb) {}
    public void onStartTrackingTouch (SeekBar sb) {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      mColor = getArguments().getInt ("color");
      mStrokeWidth = getArguments().getFloat ("stroke_width");

      final View layout = getActivity ().getLayoutInflater().
	inflate (R.layout.brush_picker_dialog, null);

      final AlertDialog alert = new AlertDialog.Builder (getActivity ())
	.setPositiveButton(R.string.alert_dialog_ok,
			   new DialogInterface.OnClickListener() {
			       public void onClick(DialogInterface dialog, int whichButton) {
				 if (mListener != null)
				   mListener.onBrushPicked (mColor, mStrokeWidth);
			       }
			   }
	  )
	.setNegativeButton(R.string.alert_dialog_cancel, null)
	.setView (layout)
	.create ();

			
      mSample = (ColorSample) layout.findViewById (R.id.sample);
      mSample.setColor (mColor);
      mSample.setStrokeWidth (mStrokeWidth);

      final ColorWheel wheel = (ColorWheel) layout.findViewById (R.id.wheel);
      wheel.setOnColorChangedListener (this);
      // The wheel is not concerned by alpha.
      wheel.setColor (mColor | 0xFF000000);

      mAlphaBar = (SeekBar) layout.findViewById (R.id.alpha);
      mAlphaBar.setProgress ((int) (Color.alpha (mColor) * 100.f / 255.f));
      mAlphaBar.setOnSeekBarChangeListener(this);

      mStrokeBar = (SeekBar) layout.findViewById (R.id.strokewidth);
      if (mStrokeWidth > MAX_STROKEWIDTH)
	mStrokeWidth = MAX_STROKEWIDTH;
      mStrokeBar.setProgress ((int) (mStrokeWidth * 100.f / MAX_STROKEWIDTH));
      mStrokeBar.setOnSeekBarChangeListener(this);

      return alert;
    }
}
