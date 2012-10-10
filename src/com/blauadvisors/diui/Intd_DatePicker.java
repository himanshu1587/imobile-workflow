package com.blauadvisors.diui;

import org.jdom.Element;

import android.content.Context;
import android.widget.DatePicker;

public class Intd_DatePicker extends DatePicker implements Intd_Form_Widget
{
	private Element element;
	private String name;
	
	public Intd_DatePicker(Context context)
	{
		super(context);
		create();
	}

	public Intd_DatePicker(Context context, Element element)
	{
		super(context);
		this.element = element;
		create();
	}

	private void create()
	{
		//if( android.os.Build.VERSION.SDK_INT >= 11 )
		//{
		// /*
			setCalendarViewShown( false );
			//Default size is too big
			setScaleX((float) 0.75);
			setScaleY((float) 0.75);
		// */
		//}
	}
	
	@Override
	public String getString()
	{
		return
			((Integer)getYear()).toString()
			+"-"+((Integer)getMonth()).toString()
			+"-"+((Integer)getDayOfMonth()).toString();
	}
	
	public boolean setString( String iso_date )
	{
		try{
			String[] split_date = iso_date.split("-");
			init(
				Integer.valueOf(split_date[0]),	//Year
				Integer.valueOf(split_date[1]),	//Month
				Integer.valueOf(split_date[2]),	//Day
				null
			);

		}catch (Exception e){
			return false;
		}
		return true;
	}

	@Override
	public Element getElement()
	{
		element.setText(getString());
		return element;
	}

	public void setElement(Element element)
	{
		this.element = element;
		setString( element.getValue() );
	}
	
	public void setElementText(){ element.setText(getString()); }		//Must be an iso-date
	public boolean hasValidInput(){ return true; }
	public void setRequired( boolean required ){ }		//A datePicker can not be empty
	public void setName( String name ){ this.name = name;  }
	public String getName(){ return name; }
}
