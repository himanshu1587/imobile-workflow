package com.blauadvisors.diui;

import org.jdom.Element;

import android.content.Context;
import android.widget.CheckBox;

public class Intd_CheckBox extends CheckBox implements Intd_Form_Widget
{
	private Element element;
	private boolean required = false;
	private String name = "";
	
	public Intd_CheckBox(Context context){ super(context); }

	public Intd_CheckBox(Context context, Element element)
	{
		super(context);
		this.element = element;
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
		setChecked( element.getValue().compareTo("true") == 0 );
	}

	public String getString(){ return ((Boolean)isChecked()).toString(); }
	public void setElementText(){ element.setText(getString()); }
	public boolean hasValidInput(){ return (!required || isChecked()); }
	public void setRequired( boolean required ){ this.required = required; }
	public void setName( String name ){ this.name = name;  }
	public String getName(){ return name; }
}
