package com.blauadvisors.diui;
//NOT WORKING!
import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class Intd_Spinner_ArrayAdapter<T> extends ArrayAdapter<T>
{
	private float text_size;
	private int text_style;
	
	public Intd_Spinner_ArrayAdapter(Context context, int textViewResourceId, T[] items)
	{
		super(context, textViewResourceId);
		setText_size(15);
		setText_style(Typeface.NORMAL);
	}

	public TextView getView(int position, View convertView, ViewGroup parent)
	{
		TextView v = (TextView) super.getView(position, convertView, parent);
		v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
		v.setTypeface(null, text_style);

		return v;
	}

	public TextView getDropDownView(int position, View convertView, ViewGroup parent)
	{
		TextView v = (TextView) super.getView(position, convertView, parent);
		v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
		v.setTypeface(null, text_style);

		return v;
	}

	public void setText_size(float text_size){ this.text_size = text_size; }
	public float getText_size(){ return text_size; }
	public void setText_style(int text_style){ this.text_style = text_style; }
	public int getText_style(){ return text_style; }

}
