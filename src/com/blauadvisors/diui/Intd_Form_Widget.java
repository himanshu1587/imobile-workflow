package com.blauadvisors.diui;

import org.jdom.Element;

public interface Intd_Form_Widget
{
/**
 	private Pattern pattern;
	private boolean required = false;
	private String name;
	private int width = 0;
	private int height = 0;
*/
	String getString();
	Element getElement();		//Element of the JDom XML tree, linked to the tree.
	void setElementText();
	void setElement(Element element);
	void setRequired( boolean required );
	void setName( String name );
	String getName();
	boolean hasValidInput();

}
