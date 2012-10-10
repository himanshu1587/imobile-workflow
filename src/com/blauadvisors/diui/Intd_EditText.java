package com.blauadvisors.diui;

import java.util.regex.Pattern;

import org.jdom.Element;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Intd_EditText extends EditText implements Intd_Form_Widget
{
	private Element element;
	private Pattern pattern;
	private int default_text_color;
	private int max_size = -1;
	private boolean required = false;
	private String name;
	
	public Intd_EditText(Context context)
	{
		super(context);
		pattern = null;
	}

	public Intd_EditText(Context context, String regexp)
	{
		super(context);
		setPattern(regexp);
		default_text_color = getCurrentTextColor();
		
		setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if( !hasFocus )
				 if( getString().compareTo("") != 0 && !hasValidInput() )
				{
					Toast.makeText(v.getContext(), R.string.invalid_input, Toast.LENGTH_LONG).show();
					//setBackgroundColor();
					setTextColor(getResources().getColor(R.color.invalid_input_color));
				}else
					setTextColor(default_text_color);
			}
		});

	}
	
	public Intd_EditText(Context context, Element element)
	{
		super(context);
		this.element = element;
		pattern = null;
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
		setText( element.getValue() );
	}

	public String getString(){ return getText().toString(); }
	public void setElementText(){ element.setText(getString()); }
	public void setMaxSize( int max_size ){ this.max_size = max_size; }
	public void setPattern( String pattern ){ this.pattern = Pattern.compile(pattern); }
	public void setRequired( boolean required ){ this.required = required; }
	public void setName( String name ){ this.name = name;  }
	public String getName(){ return name; }
	
	public boolean hasValidInput()
	{
		String text = getString();
		return
			( max_size == -1 || text.length() <= max_size )
			&& ( !required || text.length() > 0 )
			&& ( pattern == null || text.compareTo("")==0 || pattern.matcher(text).matches() )
		;
	}
	
}
