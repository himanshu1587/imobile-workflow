package com.blauadvisors.diui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class Intd_Spinner extends Spinner implements Intd_Form_Widget
{
	private Element element;
	private Map<String, Integer> map_list;
	private String array_list[];
	private String name;

	public Intd_Spinner(Context context){ super(context); }

	public Intd_Spinner(Context context, List<Element> ls)
	{
		super(context);
		setElementsList(ls);
	}
	
	public Intd_Spinner(Context context, Element element, List<Element> ls)
	{
		super(context);
		this.element = element;
		setElementsList(ls);
	}

	@Override
	public Element getElement()
	{
		setElementText();
		return element;
	}

	public String getString(){ return array_list[getSelectedItemPosition()]; }

	public void setElement(Element element)
	{
		this.element = element;
		String value = element.getValue();
		if( value != null )
			setSelected( value );
	}
	public void setElementText(){ element.setText(getString()); }
	public boolean hasValidInput(){ return true; }

/**
	<?xml version="1.0"?>
	<data>
	  <record jsxid="AU" jsxtext="Automobile"/>
	  <record jsxid="BO" jsxtext="Boat"/>
	  <record jsxid="BU" jsxtext="Bus"/>
	  <record jsxid="HE" jsxtext="Helicopter"/>
	  <record jsxid="MO" jsxtext="Motorcycle"/>
	  <record jsxid="PL" jsxtext="Plane"/>
	  <record jsxid="SP" jsxtext="Spaceship"/>
	  <record jsxid="TR" jsxtext="Train"/>
	</data>
*/	
	public void setElementsList(List<?> ls)
	{
		Element row;
		Iterator<?> it = ls.iterator();
		String items[] = new String[ls.size()];
		array_list = new String[ls.size()];
		map_list = new HashMap<String, Integer>();

		for( int i=0; it.hasNext(); i++ )
		{
			row = (Element)it.next();
			items[i] = row.getAttributeValue("jsxtext");

			map_list.put( row.getAttributeValue("jsxid"), i );
			array_list[i] = row.getAttributeValue("jsxid");
		}
		setAdapter( new /*Intd_Spinner_*/ArrayAdapter<String> (
			getContext(),
			android.R.layout.simple_spinner_item,
			items
		));

	}
	
	public void setSelected( String value )
	{
		Integer id = map_list.get( value );
		if( id != null )
			setSelection( id );
	}

	public void setRequired( boolean required ){ }	//Can be empty?
	public void setName( String name ){ this.name = name;  }
	public String getName(){ return name; }
}
