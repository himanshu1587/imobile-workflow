package com.blauadvisors.diui;

import java.util.ArrayList;

import org.jdom.Element;

import android.content.Context;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class Intd_RadioGroup extends RadioGroup implements Intd_Form_Widget
{
	private Element element;
	private static int OFFSET = 1000;
	private int num_buttons = OFFSET;
	private ArrayList<String> values;
	private boolean required;
	private String name = "";

	public Intd_RadioGroup(Context context)
	{
		super(context);
		values = new ArrayList<String>();
		//getLayoutParams().width = 150;
	}

	public Intd_RadioGroup(Context context, Element element )
	{
		super(context);
		this.element = element;
		values = new ArrayList<String>();
	}
	
	public void add_button( Element strings, Element variants )
	{
		RadioButton button = new RadioButton( getContext() );
		button.setText( strings.getAttributeValue("jsxtext") );
		//button.setTypeface(null, Typeface.NORMAL);
		button.setId( num_buttons );
		
		String element_text = element.getText();
		String button_value = strings.getAttributeValue("jsxvalue"); 
		/*
		if( element_text.compareTo("") == 0 )	//How to distinguish from empty and not set?
			button.setChecked( variants.getAttributeValue("jsxselected").compareTo("1") == 0 );
		else
		*/
			button.setChecked( element_text.compareTo( button_value ) == 0 );

		values.add( num_buttons-OFFSET, button_value );
		addView( button, num_buttons-OFFSET );
		num_buttons++;
	}

	public Element getElement()
	{
		element.setText(getString());
		return element;
	}

	public String getString()
	{
		int button_id = getCheckedRadioButtonId();
		if( button_id >= 0 )	//Is any button checked?
			return values.get( button_id-OFFSET );
		else
			return "";		//Or null?
	}
	
	public void setElement(Element element)
	{
		this.element = element;
		int id;
		String value = element.getValue();
		for(
			id = values.size()-1;
			id >= 0 && values.get(id).compareTo(value) != 0 ;
			id--
		);
		if( id != -1 )
			check(id+OFFSET);
	}

	public boolean hasValidInput() { return (!required || getCheckedRadioButtonId() >= 0 ); }
	public void setElementText(){ element.setText(getString()); }
	public void setRequired( boolean required ){ this.required = required; }
	public void setName( String name ){ this.name = name;  }
	public String getName(){ return name; }
}
