package name.cadilhac.yacptest;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.app.FragmentTransaction;
import name.cadilhac.yacp.BrushPickerDialog;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import android.view.WindowManager;
import java.util.List;
import name.cadilhac.yacp.Brush;
import java.util.ArrayList;
import java.util.Arrays;

//							FragmentTransaction ft = getFragmentManager().beginTransaction();
//				ColorDialog dialog =
//		ColorDialog.newInstance (0xff123123);


//				dialog.show(ft, "color_picker_dialog");


public class MainActivity extends Activity
{
    @Override
    public void onStart () {
      super.onStart ();

			
      // 	View layout = getLayoutInflater ().inflate (R.layout.alerttest, null);

      // AlertDialog alert = new AlertDialog.Builder (this)
      //   .setPositiveButton(R.string.alert_dialog_ok, null)
      //   .setTitle ("Don't cry, will you?")
      //   // .setView (layout) may be done here.
      //   .create();
    
      // // alert.setView (layout) here, or after .show, or I can use the doc's:
    			
      // alert.show ();

      //  FrameLayout fl = (FrameLayout) alert.findViewById (android.R.id.custom);
      //  fl.addView (layout, new LayoutParams(500, 500));
    
      // alert.getWindow ().setLayout (800, 600);
      // // Or use getWindow after show (), or use alert.getWindow ().setAttributes
      // // with a WindowManager.LayoutParams crafted from getWindow ().getAttributes.
      // // Tried before and after show.



      // alert.getWindow ().setLayout (800, 600);

      // //

      // WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

      // lp.copyFrom(alert.getWindow().getAttributes());
      // lp.width = 500;
      // lp.height = 500;
      // lp.x=0;
      // lp.y=0;
      // alert.getWindow().setAttributes(lp);

      // //fl.addView (layout, new LayoutParams(500, 500));


      // AlertDialog.Builder builder = new AlertDialog.Builder(this);
      // //builder.setView(layout);
      // builder.setTitle("Title");
      // AlertDialog alertDialog = builder.create();
      // alertDialog.show();
      // alertDialog.getWindow().setLayout(600, 400); //Controlling width and height.


    }

    ArrayList<Brush> history = new ArrayList<Brush>();
    public void askColor () {
      FragmentTransaction ft = getFragmentManager().beginTransaction();
      final BrushPickerDialog dialog =
	BrushPickerDialog.newInstance (0xff123123, 10.1f, history);
      dialog.setOnBrushPickedListener (new BrushPickerDialog.OnBrushPickedListener () {
	    public void onBrushPicked (Brush brush) {
	      if (dialog.getHistory () != null)
		history = (ArrayList<Brush>) dialog.getHistory ();
	      history.add (brush);
	      askColor ();
	    }});
      dialog.show(ft, "color_picker_dialog");
    }	
      
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      final BrushPickerDialog dialog = (BrushPickerDialog)
	getFragmentManager ().findFragmentByTag ("color_picker_dialog");
      if (dialog == null)
	askColor ();
      else {
	dialog.setOnBrushPickedListener (new BrushPickerDialog.OnBrushPickedListener () {
	      public void onBrushPicked (Brush brush) {
		if (dialog.getHistory () != null)
		  history = (ArrayList<Brush>) dialog.getHistory ();
		history.add (brush);
		askColor ();
	      }});
      }

      // ColorWheel vi = new ColorWheel ((Context) this);
      // setContentView (vi);
    }
}
