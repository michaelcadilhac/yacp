package name.cadilhac.yacp;

import java.lang.Math;

import android.util.Log;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.RadialGradient;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.view.LayoutInflater;
import android.util.AttributeSet;
import android.content.res.TypedArray;
import android.graphics.ComposeShader;
import android.graphics.Xfermode;
import android.view.View.MeasureSpec;

public class ColorWheel extends View {
    public interface OnColorChangedListener {
        public void onColorChanged (int color);
    }

		private Paint mSweepPaint = null;
		private OnColorChangedListener mListener = null;

		private int   mColor = 0;
		private float mHue = 0.f; // mHue is mAngle in degrees.
		private float mVal = 0.f;
		private float mSat = 0.f;

		private boolean mWheeling = false;
		private boolean mLuming = false;

		private float WHEEL_WIDTH;
		private float WHEEL_RADIUS;
		private float WHEEL_X;
		private float WHEEL_Y;
		private float WHEEL_INSIDE_RADIUS;
		private float WHEEL_TRIANGLE_SIDE;
		private float SEL_CIRCLE_WIDTH;
		private float SEL_CIRCLE_RADIUS;
		private float BW_SPAN;

		public ColorWheel (Context context) {
			this (context, null, 0);
		}

		public ColorWheel (Context context, AttributeSet attrs) {
			this (context, attrs, 0);
		}

		public ColorWheel (Context context, AttributeSet attrs, int defStyle) {
			super (context, attrs, defStyle);

			TypedArray a = context.obtainStyledAttributes(attrs,
																										R.styleable.ColorWheel,
																										defStyle, 0);
			mColor = a.getColor (R.styleable.ColorWheel_android_color, mColor);
			a.recycle();
		}

		public void setColor (int color)
		{
			float hsv[] = {0.f, 0.f, 0.f};
			Color.colorToHSV (color, hsv);
			mHue = hsv[0];
			mSat = hsv[1];
			mVal = hsv[2];

			invalidate ();
		}

		public int getColor () {
			return mColor;
		}

		public void setOnColorChangedListener (OnColorChangedListener listener) {
			mListener = listener;
		}

		@Override 
		protected void onDraw(Canvas canvas) {
			// Wheel:
			canvas.drawCircle(WHEEL_X, WHEEL_Y, WHEEL_RADIUS, mSweepPaint);

			// Triangle:
			// We work in a bitmap because of the way we clip a triangle out of
			// the shading (see below).  Note that we draw a rotated bitmap and
			// print it as-is, rather than drawing a non rotated bitmap and
			// print it rotated, for antialiasing purposes.

			Bitmap bitmap = Bitmap.createBitmap((int) (WHEEL_INSIDE_RADIUS * 2.f),
																					(int) (WHEEL_INSIDE_RADIUS * 2.f),
																					Bitmap.Config.ARGB_8888);
			Canvas triangleCanvas = new Canvas(bitmap);
			triangleCanvas.translate (WHEEL_INSIDE_RADIUS / 2.f,
																WHEEL_INSIDE_RADIUS - WHEEL_TRIANGLE_SIDE / 2);
			triangleCanvas.rotate (mHue, WHEEL_INSIDE_RADIUS / 2.f,
														 WHEEL_TRIANGLE_SIDE / 2.f);

			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.FILL_AND_STROKE);

			// Draw the shades
			float hsv[] = { mHue, 1.f, 1.f };
			int color = Color.HSVToColor (hsv);
			// This shade is slightly inexact w.r.t. HSV.
			paint.setShader (
				new ComposeShader (
					new LinearGradient (0, 0, 0, WHEEL_TRIANGLE_SIDE,
															0xFF000000, 0xFFFFFFFF, Shader.TileMode.CLAMP),
					new LinearGradient (0, 0, 2.f * WHEEL_INSIDE_RADIUS, 0,
															0x00FFFFFF & color, color, Shader.TileMode.CLAMP),
					(Xfermode) null));
			triangleCanvas.drawRect (0, 0,
															 (3.f / 2.f * WHEEL_INSIDE_RADIUS), WHEEL_TRIANGLE_SIDE,
															 paint);

			// Clip to a triangle:
			// This is a bit ackward because clipPath is buggy on most
			// hardware, as is BitmapShader.  I didn't want to disable hardware
			// rendering, hence the following.
			Path triangle = new Path ();
			triangle.moveTo (0, 0);
			triangle.lineTo (3.f / 2.f * WHEEL_INSIDE_RADIUS, WHEEL_TRIANGLE_SIDE / 2.f);
			triangle.lineTo (0, WHEEL_TRIANGLE_SIDE);
			triangle.close ();
			triangle.toggleInverseFillType ();
			paint = new Paint (Paint.ANTI_ALIAS_FLAG);
			paint.setStyle (Paint.Style.FILL_AND_STROKE);
			paint.setColor (0x00000000);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
			triangleCanvas.drawPath (triangle, paint);

			// Print the triangle into the main canvas.
			canvas.drawBitmap(bitmap,
												WHEEL_X - WHEEL_INSIDE_RADIUS,
												WHEEL_Y - WHEEL_INSIDE_RADIUS,
												new Paint (Paint.ANTI_ALIAS_FLAG));



			canvas.save ();
			canvas.rotate (mHue, WHEEL_X, WHEEL_Y);

			// Pin:
			paint = new Paint (Paint.ANTI_ALIAS_FLAG);
			paint.setStyle (Paint.Style.STROKE);
			paint.setColor (0xFF000000);
			paint.setStrokeWidth (WHEEL_WIDTH / 10.f);
			canvas.drawLine (WHEEL_X + WHEEL_RADIUS - WHEEL_WIDTH / 2.f, WHEEL_Y,
											 WHEEL_X + WHEEL_RADIUS + WHEEL_WIDTH / 2.f, WHEEL_Y,
											 paint);

			// Draw the selection point.
			//
			//   |\\
			//   | \  \
			//   |  \    \
			//   |   \   /
			//   |----/S
			//sY | /__|        tan (pi/2 - pi/3) = sY/sX
			//      sX         cos (pi/2 - pi/3) = sX/(S * side)
			//   hence sX = sqrt(3)/2 * (S * side) = 3/2 * S * radius
			//     and sY = sX / sqrt(3)

			final double sX = 3.f / 2.f * mSat * WHEEL_INSIDE_RADIUS;
			final double sY = sX / Math.sqrt (3);
			final float pX = (float) (WHEEL_X - WHEEL_INSIDE_RADIUS / 2.f +	mVal * sX);
			final float pY = (float) (WHEEL_Y - WHEEL_TRIANGLE_SIDE / 2.f +
																mVal * (WHEEL_TRIANGLE_SIDE - sY));

			paint = new Paint (Paint.ANTI_ALIAS_FLAG);
			paint.setStyle (Paint.Style.STROKE);
			paint.setColor (0xFFFFFFFF);
			paint.setStrokeWidth (SEL_CIRCLE_WIDTH);

			canvas.drawCircle (pX, pY, SEL_CIRCLE_RADIUS, paint);
			paint.setColor (0xFF000000);
			paint.setStrokeWidth (SEL_CIRCLE_WIDTH / 10.f);
			canvas.drawCircle (pX, pY, SEL_CIRCLE_RADIUS - SEL_CIRCLE_WIDTH / 2.f, paint);
			canvas.drawCircle (pX, pY, SEL_CIRCLE_RADIUS + SEL_CIRCLE_WIDTH / 2.f, paint);
			canvas.restore ();
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int wheelSide = 0;

			if (MeasureSpec.getMode (widthMeasureSpec) == MeasureSpec.UNSPECIFIED) 
				if (MeasureSpec.getMode (heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
					// This is a dry run of the calling layout, ignore it.
					super.onMeasure (widthMeasureSpec, heightMeasureSpec);
					return;
				} else 
					wheelSide = MeasureSpec.getSize (heightMeasureSpec);
			else 
				if (MeasureSpec.getMode (heightMeasureSpec) == MeasureSpec.UNSPECIFIED)
					wheelSide = MeasureSpec.getSize (widthMeasureSpec);
				else
					wheelSide = Math.min (MeasureSpec.getSize (widthMeasureSpec),
																MeasureSpec.getSize (heightMeasureSpec));

			setMeasuredDimension (wheelSide, wheelSide);
		}

		@Override
		protected void onSizeChanged (int width, int height, int oldwidth, int oldheight) {
			super.onSizeChanged (width, height, oldheight, oldheight);

			int wheelSide = Math.min (width, height);

			// The stroke width is a quarter of the radius, hence:
			// 2 * radius + width = side
			// and then: radius = side / (2 + 1/4)
			WHEEL_RADIUS = wheelSide / 2.25f;
			WHEEL_WIDTH = WHEEL_RADIUS / 4.f;
			WHEEL_X = WHEEL_Y = wheelSide / 2.f;
			WHEEL_INSIDE_RADIUS = WHEEL_RADIUS - WHEEL_WIDTH / 2.f;
			WHEEL_TRIANGLE_SIDE = WHEEL_INSIDE_RADIUS * 3.f / (float) Math.sqrt (3.f);
			SEL_CIRCLE_RADIUS = WHEEL_INSIDE_RADIUS / 10.f;
			SEL_CIRCLE_WIDTH = SEL_CIRCLE_RADIUS / 4.f;
			BW_SPAN = WHEEL_INSIDE_RADIUS * 1.5f;

			final int[] hueRange = { 0xFFFF0000, 0xFFFFFF00,
															 0xFF00FF00, 0xFF00FFFF,
															 0xFF0000FF, 0xFFFF00FF,
															 0xFFFF0000 };
			final Shader sg = new SweepGradient(WHEEL_X, WHEEL_Y, hueRange, null);
			mSweepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mSweepPaint.setStyle(Paint.Style.STROKE);
			mSweepPaint.setShader(sg);
			mSweepPaint.setStrokeWidth(WHEEL_WIDTH);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX() - WHEEL_X;
			float y = event.getY() - WHEEL_Y;
			float distToCenter = (float) (Math.sqrt (x * x + y * y));
			boolean inH = false;
			boolean inSV = false;

			if (distToCenter <= WHEEL_RADIUS + WHEEL_WIDTH / 2.f) {
				if (distToCenter > WHEEL_INSIDE_RADIUS)
					inH = true;            		
				else
					inSV = true;
			}
					
			switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_DOWN:
					if (!mLuming && (inH || mWheeling)) {
						mWheeling = true;
						float angle = (float) Math.atan2(y, x);
						if (angle < 0)
							angle += 2.f * (float) Math.PI;
						mHue = angle * 360.f / (2.f * (float) Math.PI);
						invalidate();
					} 
					else if (!mWheeling && (inSV || mLuming)){
						mLuming = true;

						// Rotate the triangle so that the touch happened in the
						// infinitely extended region 1, that is, angle between 2PI/3
						// and 5PI/3
						//
						// S 
						// |\\     
						// | \  \
						// |  \ 2  \
						// |   \______\  H
						// | 1 / 3    /
						// |  /    /
						// | /  /
						// |//    
						//         
								
						final double hueAngle = mHue / 360.f * (2.f * Math.PI);
						double angle = Math.atan2(y, x) - hueAngle;
						double rotation = 0.f;
						while (angle < 2.f * Math.PI / 3.f)
						{
							angle += 2.f * Math.PI / 3.f;
							rotation += 2.f * Math.PI / 3.f;
						}
						while (angle > 5.f * Math.PI / 3.f)
						{
							angle -= 2.f * Math.PI / 3.f;
							rotation -= 2.f * Math.PI / 3.f;
						}

						// Now apply the same transformation to x, y, so that (rX, rY) belongs to the cone 1.
						double rX = Math.cos (rotation - hueAngle) * x - Math.sin (rotation - hueAngle) * y;
						double rY = Math.sin (rotation - hueAngle) * x + Math.cos (rotation - hueAngle) * y;

						// If the touch happened outside of the triangle, put it back there.
						if (rX < -WHEEL_INSIDE_RADIUS / 2.f)
						{
							rX = -WHEEL_INSIDE_RADIUS / 2.f;
							// These overflows can happen only if rX itself was out of bound.
							if (rY < -WHEEL_TRIANGLE_SIDE / 2.f)
								rY = -WHEEL_TRIANGLE_SIDE / 2.f;
							if (rY > WHEEL_TRIANGLE_SIDE / 2.f)
								rY = WHEEL_TRIANGLE_SIDE / 2.f;
						}

						// Rotate the point back, except for hueAngle, and change the origin to H.
						final double fX = Math.cos (-rotation) * rX - Math.sin (-rotation) * rY - WHEEL_INSIDE_RADIUS;
						final double fY = Math.sin (-rotation) * rX + Math.cos (-rotation) * rY;

						// Compute S.
						//
						//   S
						//   |\\      
						// l |a\  \
						//   |  \    \
						//   |---X      \  H
						//   | l' \     /       X marks the spot.  The value for S is the portion of the side.
						//   |     \ /
						//   |    /             l is side / 2 + fY
						//   | /		mSat				l' is height - abs(fX) = 3 / 2 * WHEEL_INSIDE_RADIUS + fX
						//           

						final double l = WHEEL_TRIANGLE_SIDE / 2.f + fY;
						final double lp = 3.f / 2.f * WHEEL_INSIDE_RADIUS + fX;
						final double a = Math.PI / 4.f - Math.atan ((l - lp) / (l + lp));
						mSat = (float) (Math.sin (a) / Math.sin(a + Math.PI / 3.f));

						// Compute V.
						// Length of [S, mSat]
						final double o = 3.f / 2.f * WHEEL_INSIDE_RADIUS / // = side * sin(pi/3)
							Math.sin (a + Math.PI / 3.f);
						// Length of [S, (fX, fY)]
						final double d = Math.sqrt ((fX + 3.f / 2.f * WHEEL_INSIDE_RADIUS) * (fX + 3.f / 2.f * WHEEL_INSIDE_RADIUS) +
																				(fY + WHEEL_TRIANGLE_SIDE / 2.f) * (fY + WHEEL_TRIANGLE_SIDE / 2.f));
						mVal = (float) (d / o);
						invalidate();
					}
					break;
				case MotionEvent.ACTION_UP:
					mWheeling = mLuming = false;
					break;
			}
			float hsv[] = { mHue, mSat, mVal };
			mColor = Color.HSVToColor (hsv);
			if (mListener != null)
				mListener.onColorChanged (mColor);
			return true;
		}
}
