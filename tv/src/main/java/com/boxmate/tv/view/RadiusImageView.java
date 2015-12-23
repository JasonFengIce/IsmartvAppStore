package com.boxmate.tv.view;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RadiusImageView extends ImageView {
	
	private int raduisDefault = 10;
	public RadiusImageView(Context context) {
		super(context);
	}

	public RadiusImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RadiusImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}
	public void setImageResource(int resId) {  
        Bitmap originalImage = BitmapFactory.decodeResource(  
                getResources(), resId);  
        Bitmap nowImageBitmap = toRoundCorner(originalImage, raduisDefault);
		setImageBitmap(nowImageBitmap);
    }  
	public void setDefaultRadius(int r) {
		raduisDefault = r;
	}
	public void makeRadius(int radius) {
		Bitmap originalImage = ((BitmapDrawable)this.getDrawable()).getBitmap();  
		Bitmap nowImageBitmap = toRoundCorner(originalImage, radius);
		setImageBitmap(nowImageBitmap);
	}

	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}
}
