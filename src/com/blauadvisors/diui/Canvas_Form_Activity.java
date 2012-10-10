package com.blauadvisors.diui;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.blauadvisors.diui.IntalioWS.IntalioWSBinder;

public class Canvas_Form_Activity extends Activity
{
	protected Element task_output;	//It's also the task_input (initial output)
	protected int input_count = 0;
	protected LinearLayout layout;
	protected ScrollView scroll;
	protected String dialog_msg;
	protected static int CANNOT_COMPLETE_DIALOG = 1;

	protected IntalioWS intalio = null;
	protected String task_id = null, task_type = null, task_url = null, task_ns = null;

	protected Map<String, Integer> radio_buttons;	//String == XML id, Integer == Android.View ID
	protected DisplayMetrics metrics;
	
	public Canvas_Form_Activity( )
	{
		super();
		radio_buttons = new HashMap<String, Integer>();
	}

	public void onCreate( Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		getWindow().setBackgroundDrawableResource( R.drawable.background_bw_1280x768_jpg );

		layout = new LinearLayout( this );
		layout.setOrientation( LinearLayout.VERTICAL );
		scroll = new ScrollView( this );
		scroll.addView( layout );
		//To convert display pixels (DP) to pixels (PX)
		metrics = getResources().getDisplayMetrics();

		Bundle extras = getIntent().getExtras();
		//String action = getIntent().getAction();
		//if( action.compareTo("start_task")==0 )
		//{
		setTask_id( extras.getString("task_id") );
		setTask_type( extras.getString("task_type") );
		setTask_url( extras.getString("task_url") );
		setTask_ns( extras.getString("task_ns") );
		//}

		getApplicationContext().bindService(
            new Intent(getApplicationContext(), IntalioWS.class),
            mConnection,
            Context.BIND_AUTO_CREATE
        );
	}
	
	protected void parse_canvas( BufferedReader canvas ) throws Exception
	{
		canvas.readLine();	//Skip XML preamble
        Element parent = new SAXBuilder().build( canvas ).getRootElement();
        Namespace canvas_ns  = parent.getNamespace();

		Iterator<?> it = parent.getChildren( "object", canvas_ns ).iterator();		//Non-objects are ignored.
		while( it.hasNext() )	//There's only one root object, the loop is not needed, it's left just in case...
			parse_object( (Element)it.next(), layout, task_output, canvas_ns, null );

/**
		//Funny way of doing the same:
		for(
			Iterator<?> it = parent.getChildren( "object", canvas_ns ).iterator();
			it.hasNext();
			parse_object( (Element)it.next(), layout, task_output, canvas_ns)
		);
*/	

		//Final bar separator
		TextView label = new TextView( this );
		label.setBackgroundColor( Color.parseColor("#BCC0C3") );	//Blau gray
		label.setText( " " );		//If not set, no bar is showed
		layout.addView( label );

	}
	
	protected void parse_object
	( Element canvas, LinearLayout layout, Element task_output, Namespace canvas_ns, TextView label ) throws Exception
	{
		String object_type = canvas.getAttributeValue("type");

		/**
		Do parse_XYZ( canvas, canvas_ns, layout, task_output );
		Not layout.addView( parse_XYZ( canvas, canvas_ns, task_output ) );
		This way, the object can be added to the layout sooner and 
		it will have some properties (layout.getLayoutParams() )
		**/
		if( object_type.compareTo("com.intalio.ria.Field") == 0 )
			parse_field( canvas, canvas_ns, layout, task_output, label );

		else if ( object_type.compareTo("jsx3.gui.Block") == 0 )
			parse_block( canvas, canvas_ns, layout, task_output, label );

		//Only sections have fieldset? All sections have?
		else if ( object_type.compareTo("com.intalio.ria.Section") == 0 )
			parse_section( canvas, canvas_ns, layout, task_output, label );

		/** Fix all this functions. See note above **/
		else if ( object_type.compareTo("jsx3.gui.TextBox") == 0 )
			layout.addView( parse_textBox( canvas, task_output, canvas_ns, label ) );

		else if( object_type.compareTo("jsx3.gui.CheckBox") == 0)
			//Els checkbox són independents
			layout.addView( parse_checkBox( canvas, task_output, canvas_ns, label ) );

		else if( object_type.compareTo("jsx3.gui.RadioButton") == 0)
			//Els radiobuttons NO són independents!
			parse_radioButton( canvas, task_output, canvas_ns, layout, label );

		else if( object_type.compareTo("jsx3.gui.Select") == 0)
			layout.addView( parse_select( canvas, task_output, canvas_ns, label ) );

		else if ( object_type.compareTo("jsx3.gui.DatePicker") == 0)
			layout.addView( parse_datePicker( canvas, task_output, canvas_ns, label ) );
		
		else if( object_type.compareTo("com.intalio.ria.Title") == 0)
			layout.addView( parse_Title( canvas, task_output, canvas_ns ) );

		//"jsx3.gui.LayoutGrid", "jsx3.gui.Matrix", "jsx3.gui.Matrix.Column",
		//"com.intalio.ria.FileUpload"
	}
	/*
	protected void parse_object
	( Element canvas, LinearLayout layout, Element task_output, Namespace canvas_ns ) throws Exception
	{
		parse_object(canvas, layout, task_output, canvas_ns, null);
	}
	*/
	protected void parse_field
	( Element field, Namespace canvas_ns, LinearLayout layout, Element task_output, TextView label ) throws Exception
	{
		Element strings = field.getChild("strings", canvas_ns);		//Never null?
		String text_value;
		
		//Log.d("Block", new XMLOutputter().outputString(task_output));
		//Log.d("Block", "Value: "+value);
		//Log.d("Block", new XMLOutputter().outputString(input));
		
		List<?> objects = field.getChildren("object", canvas_ns );
		int objects_size = objects.size();
		if( objects_size == 0 ) //Make a single textView
		{
			if( strings.getAttributeValue("riaLabelText") != null )		//I'm not sure
				layout.addView( parse_textView_common( field, canvas_ns ) );	//Always make a textView?

		}else{		//Label+field(s): make a LinearLayout with label+fields
			//Labels with "-field" ended jsxname do not add a parent element
			//Labels with other ends adds a parent element (Intalio rulez!)
			//TODO: That's FALSE!
			if( !strings.getAttributeValue("jsxname").endsWith("-field") )
				task_output = add_field_output( strings, task_output );

			text_value = strings.getAttributeValue("riaLabelText");
			if( text_value != null )
			{
				LinearLayout label_layout = new LinearLayout( this );
				label_layout.setOrientation( LinearLayout.HORIZONTAL );

				strings.setAttribute("jsxtext", text_value );
				TextView text_label = parse_textView_common( field, canvas_ns );
				text_label.setMinWidth( (int)metrics.density*250 );
				text_label.setGravity(Gravity.RIGHT);
				text_label.setTypeface(null, Typeface.BOLD);
				label_layout.addView( text_label );
				
				//TODO: This are "paddings" and "margins". Bad way!
				TextView space = new TextView(this);
				space.setWidth(15);
				space.setHeight(15);
				label_layout.addView( space );

				layout.addView( label_layout );
				
				 if( objects_size > 1 )		//More than one field: make a LinearLayout with all them
				 {
					LinearLayout right_layout = new LinearLayout( this );
					String direction = strings.getAttributeValue("riaLayoutDirection");
					if( direction == null || direction.compareTo("horizontal") == 0 )
						right_layout.setOrientation( LinearLayout.HORIZONTAL );
					else
						right_layout.setOrientation( LinearLayout.VERTICAL );
					label_layout.addView( right_layout );

					Iterator<?> it = objects.iterator();
					while( it.hasNext() )
						parse_object( (Element)it.next(), right_layout, task_output, canvas_ns, text_label );
				 }else
					 parse_object( (Element)objects.iterator().next(), label_layout, task_output, canvas_ns, text_label );

			}else{
				Iterator<?> it = objects.iterator();
				while( it.hasNext() )
					parse_object( (Element)it.next(), layout, task_output, canvas_ns, null );	//null text_label
			}
		}
	}

	protected void parse_block
	( Element block, Namespace canvas_ns, LinearLayout layout, Element task_output, TextView label ) throws Exception
	{
		//Add input (if any) into the jsxtext string's attribute before parsing the textView.
		Element strings = block.getChild("strings", canvas_ns);		//Never null?
		String value = strings.getAttributeValue("jsxname");
		Element input = task_output.getChild(value, task_output.getNamespace() );
		if( input != null )
		{
			strings.removeAttribute("jsxtext");		//If there's no "jsxtext" happens nothing or Exception?
			strings.setAttribute("jsxtext", input.getValue() );		//Is it set in the block element?
		}
		List<?> objects = block.getChildren("object", canvas_ns );
		int objects_size = objects.size();
		if( objects_size == 0 )		//Make a textView
		{
			if( strings.getAttributeValue("jsxtext") != null )		//I'm not sure
			{
				String text_class = strings.getAttributeValue("jsxclassname");
				if( text_class != null )
				{
					if( text_class.compareTo("form-title") == 0 )
						layout.addView( get_formTitle( block, canvas_ns ) );
				}else		//What other text class exists?
					layout.addView( parse_textView_common( block, canvas_ns ) );
			}
			
		}else if( objects_size == 1 )	//Make an horizontal linear layout
		{
			if( strings.getAttributeValue("jsxtext") != null )
			{
				TextView text_label = parse_textView_common( block, canvas_ns );
				text_label.setMinWidth( (int)metrics.density*250 );

				LinearLayout label_layout = new LinearLayout( this );
				label_layout.setOrientation( LinearLayout.HORIZONTAL );
				label_layout.addView( text_label );

				layout.addView( label_layout );
				
				parse_object( (Element)objects.iterator().next(), label_layout, task_output, canvas_ns, text_label );
			}else
				parse_object( (Element)objects.iterator().next(), layout, task_output, canvas_ns, null );

		}else if( objects_size > 1 )	//Make an horizontal linear layout + <canvas_orientation> linear layout
		{
			if( strings.getAttributeValue("jsxtext") != null )
			{
				TextView text_label = parse_textView_common( block, canvas_ns );
				text_label.setMinWidth( (int)metrics.density*250 );

				LinearLayout label_layout = new LinearLayout( this );
				label_layout.setOrientation( LinearLayout.HORIZONTAL );
				label_layout.addView( text_label );

				layout.addView( label_layout );
				
				LinearLayout right_layout = new LinearLayout( this );
				String direction = strings.getAttributeValue("riaLayoutDirection");
				if( direction == null || direction.compareTo("horizontal") == 0 )
					right_layout.setOrientation( LinearLayout.HORIZONTAL );
				else
					right_layout.setOrientation( LinearLayout.VERTICAL );
				label_layout.addView( right_layout );

				Iterator<?> it = objects.iterator();
				while( it.hasNext() )
					parse_object( (Element)it.next(), right_layout, task_output, canvas_ns, text_label );
			}else{
				Iterator<?> it = objects.iterator();
				while( it.hasNext() )
					parse_object( (Element)it.next(), layout, task_output, canvas_ns, null );
				
			}
		}
	}

	protected void parse_section
	( Element parent_element, Namespace canvas_ns, LinearLayout parent_layout, Element task_output, TextView label ) throws Exception
	{
		LinearLayout section = new LinearLayout( this );		//Always new layout? no empty sections?
		section.setOrientation( LinearLayout.VERTICAL );
		parent_layout.addView( section );

		Iterator<?> it = parent_element.getChildren("object", canvas_ns).iterator();
		Element strings = parent_element.getChild("strings", canvas_ns);		//Error if it does not exists
		String value = strings.getAttributeValue("jsxclassname");
		if( value != null && value.compareTo("fieldset") == 0 )
		{
			section.setOrientation(LinearLayout.VERTICAL);
			TextView text_label = new TextView( this );
			text_label.setTextColor(Color.BLACK);
			text_label.setBackgroundColor( Color.parseColor("#BCC0C3") );		//Blau gray
			text_label.setText( strings.getAttributeValue("riaTitleText") );		//Check null value?
			section.addView( text_label );

			task_output = add_field_output( strings, task_output );
			
			while( it.hasNext() )
				parse_object( (Element)it.next(), section, task_output, canvas_ns, text_label );
		}else while( it.hasNext() )
			parse_object( (Element)it.next(), section, task_output, canvas_ns, null );

	}
	
	protected TextView parse_textView_common( Element canvas, Namespace canvas_ns )
	{
		TextView label = new TextView( this );
		
		Element strings = canvas.getChild("strings", canvas_ns);
		Element variants = canvas.getChild("variants", canvas_ns);
		String attribute;

		/** Size attributes:  */
		
		//TODO: Can be "25%" or "25" (pixels?)
		attribute = variants.getAttributeValue("jsxheight");
		if( attribute != null )
			label.setMinHeight( (int)metrics.density*Integer.valueOf(attribute) );

		//TODO: Can be "25%" or "25" (pixels?)
		attribute = variants.getAttributeValue("jsxwidth");
		if( attribute != null )
			label.setMinWidth( (int)metrics.density*Integer.valueOf(attribute) );
		//else
			//label.setMinWidth( (int)metrics.density*250 );

		/*
		It isn't in any layout, can not get layout parameters :-(
		attribute = variants.getAttributeValue("jsxoverflow");
		if( attribute == null || attribute.compareTo("3") == 0 )
			label.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
		*/
		attribute = variants.getAttributeValue("jsxfontsize");
		if( attribute != null )
			label.setTextSize( TypedValue.COMPLEX_UNIT_DIP, (int)(Integer.valueOf(attribute)*1.1) );
		else
			label.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 15 );
		
		/** Text-style attributes */
		
		attribute = strings.getAttributeValue("jsxfontweight");
		if( attribute != null )
		{
			if( attribute.compareTo("bold") == 0 )
				label.setTypeface(null, Typeface.BOLD);

			else if ( attribute.compareTo("normal") == 0 )
				label.setTypeface(null, Typeface.NORMAL);
		}//else label.setTypeface(null, Typeface.NORMAL);	//Default
		
		attribute = strings.getAttributeValue("jsxcolor");
		if( attribute != null && attribute.compareTo("") != 0 )
		//try{
			label.setTextColor( Color.parseColor(attribute) );
		//}catch (Exception e ){ e.printStackTrace(); }

		attribute = strings.getAttributeValue("jsxtext");
		if( attribute != null )
			label.setText( Html.fromHtml(attribute) );	//Do not set it before Typeface.BOLD
		/**
		else return null?
		No! riaLabelText have the text in another attribute
		Yes! add "jsxtext" attribute if it's called diferent.
		Or the text could be added outside this function after calling it (it could have input!)...
		*/

		/*
		//That should be set on the Layout widget! :-(
		attribute = strings.getAttributeValue("jsxmargin");
		if( attribute != null )
		{
			String pads[] = attribute.split(" ");
			label.setPadding(
				Integer.valueOf(pads[3])+2,	//Left. +2 is also set on WebBrowser
				Integer.valueOf(pads[0])+2,	//Top
				Integer.valueOf(pads[1]),	//Right
				Integer.valueOf(pads[2])	//Bottom
			);
		}
		// */		
		return label;
	}

	protected Element add_field_output( Element strings, Element task_input )
	{
		String value = strings.getAttributeValue("jsxname");
		if( value != null )
		{
			Element child = task_input.getChild(value, task_output.getNamespace());
			if( child == null )
			{
				child = new Element(value);
				task_input.addContent(child);
			}
			return child;
		}else
			return null;
	}

	protected Element add_task_output( Element strings, Element task_input )
	{
		String value = strings.getAttributeValue("jsxname");
		//Log.d("Block", new XMLOutputter().outputString(task_output));
		//Log.d("Block", "Value: "+value);
		//Log.d("Block", new XMLOutputter().outputString(input));
		if( value != null )
		{
			Element child = task_input.getChild(value, task_output.getNamespace());
			if( child == null )
			{
				child = new Element(value);
				child.setText( strings.getAttributeValue("jsxvalue") );
				task_input.addContent(child);
			}
			return child;
		}else
			return null;
	}
	
	protected TextView get_formTitle( Element object, Namespace canvas_ns )
	{
		TextView title = parse_textView_common( object, canvas_ns );
		
		Element strings = object.getChild("strings", canvas_ns);
		Element variants = object.getChild("variants", canvas_ns);
		String attribute;
		
		attribute = strings.getAttributeValue("jsxfontweight");
		if( attribute == null )
			title.setTypeface(null, Typeface.BOLD);		//Bold by default

		attribute = variants.getAttributeValue("jsxfontsize");
		if( attribute == null )
			title.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 30 );
		
		attribute = strings.getAttributeValue("jsxcolor");
		if( attribute == null )
			//title.setTextColor( Color.parseColor("#00457D") );		//Blau blue
			title.setTextColor( Color.parseColor("#BCC0C3") );		//Blau gray
			
		return title;
	}
	
	protected TextView parse_Title( Element object, Element task_output, Namespace canvas_ns )
	{
		TextView title = new TextView( this );
		
		Element strings = object.getChild("strings", canvas_ns);
		String attribute;
				
		title.setTypeface(null, Typeface.BOLD);		//All labels are bold?
		//title.setGravity(Gravity.RIGHT); 
		attribute = strings.getAttributeValue("riaTitleSize");
		if( attribute != null )
			title.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 26-Integer.valueOf(attribute)*3 );
		
		attribute = strings.getAttributeValue("riaTitleText");
		if( attribute != null )
			title.setText( Html.fromHtml(attribute) );	//Do not set it before Typeface.BOLD
		
		return title;
	}

	protected Intd_EditText parse_textBox( Element object, Element task_output, Namespace canvas_ns, TextView label )
	{
		Intd_EditText text_box = new Intd_EditText( this );
		//Intd_EditText text_box = parse_textView_common( object, canvas_ns );

		Element output;
		String attribute, pattern = null;
		Element strings = object.getChild("strings", canvas_ns);
		Element variants = object.getChild("variants", canvas_ns);

		if( label != null )
			text_box.setName( label.getText().toString() );
		
		output = add_task_output( strings, task_output );
		if( output != null )
		{
			text_box.setId( input_count++ );
			((Intd_Form_Widget)text_box).setElement(output);
		}//else what?
		
		attribute = variants.getAttributeValue("jsxreadonly");
		text_box.setEnabled( attribute == null || attribute.compareTo("1") != 0 );		//Changes aspect
		//if( attribute != null && attribute.compareTo("1") == 0 )			//Less intrussive
			//text_box.setFocusable(false);
			//text_box.setInputType( TYPE_NULL );

		attribute = variants.getAttributeValue("jsxrequired");
		if( attribute != null && attribute.compareTo("1") == 0 )
		{
			text_box.setRequired( true );
			if( label != null && !label.getText().toString().endsWith( " (*)"))
				label.append(" (*)");
		}

		text_box.setGravity( Gravity.TOP );		//Does variants have text alignement/gravity attributes?
		attribute = variants.getAttributeValue("jsxtype");
		if( attribute != null )
		{
			if( attribute.compareTo("1") == 0 )		//TextBox (vs TextInput)
				text_box.setLines(5);

			else if( attribute.compareTo("2") == 0 )	//Password field
				//InputType.IME_FLAG_NO_FULLSCREEN
				text_box.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD  );

		}

		/** Only "validationexpression" xor "validationtype" */
		attribute = strings.getAttributeValue("jsxvalidationtype");
		if( attribute != null )
		{
			if( attribute.compareTo("email") == 0 )
				pattern = "^[\\w\\-]+(\\.[\\w\\-]+)*@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,4}$";
				// ewilliams@example.com --> insert into domiions (direccion) values ( 'example' ) 
				//
				//pattern = "([a-zA-Z0-9_~\\-\\.]+)@([a-zA-Z0-9]+).[a-zA-Z0-9]{2,}";		//Intalio original

			else if( attribute.compareTo("number") == 0 )
				pattern = "\\d+";
			//else if: "Social security", "phone", "letter", "US zipcode", "Money"
		}

		attribute = strings.getAttributeValue("jsxvalidationexpression");
		if( attribute != null )
			pattern = attribute;
		
		if( pattern != null )
			text_box.setPattern( pattern );

		attribute = variants.getAttributeValue("jsxmaxlength");
		if( attribute != null )
			text_box.setMaxSize( Integer.valueOf(attribute) );

		//text_box.setWidth(250);
/*
		//Also missing in the other parse_ABC() functions
		attribute = strings.getAttributeValue("display");
		if( attribute != null )(int)metrics.density*250
			text_box.setDisplay( attribute );
*/
		
		//TODO: Can be "25%" or "25" (pixels?)
		attribute = variants.getAttributeValue("jsxheight");
		if( attribute != null )
			text_box.setHeight( (int)metrics.density*Integer.valueOf(attribute) );

		//Can be "25%" or "25" (pixels?)
		attribute = variants.getAttributeValue("jsxwidth");
		if( attribute != null )
			text_box.setWidth( (int)metrics.density*Integer.valueOf(attribute) );
		else
			text_box.setWidth( (int)metrics.density*250 );
		
		attribute = variants.getAttributeValue("jsxfontsize");
		if( attribute != null )
			text_box.setTextSize( TypedValue.COMPLEX_UNIT_DIP, (int)(Integer.valueOf(attribute)*1.1) );
		else
			text_box.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 15 );

		attribute = variants.getAttributeValue("jsxfontweight");
		if( attribute != null )
		{
			if( attribute.compareTo("bold") == 0 )		//Isn't there an italic style?
				text_box.setTypeface(null, Typeface.BOLD);
		}

		attribute = strings.getAttributeValue("jsxcolor");
		if( attribute != null )
			text_box.setTextColor( Color.parseColor(attribute) );

		return text_box;
	}

	protected LinearLayout parse_checkBox( Element object, Element task_output, Namespace canvas_ns, TextView label )
	{
		LinearLayout layout = new LinearLayout( this );
		Intd_CheckBox check_box = new Intd_CheckBox( this );
		TextView text_label = new TextView( this );

		layout.setOrientation(LinearLayout.HORIZONTAL);	
		layout.addView( check_box );		//Always first?
		layout.addView( text_label );			//Always second?
		
		Element strings = object.getChild("strings", canvas_ns);
		Element variants = object.getChild("variants", canvas_ns);
		String attribute;
		Element output;

		if( label != null )
			check_box.setName( label.getText().toString() );
		
		attribute = strings.getAttributeValue("jsxtext");
		if( attribute != null )
			text_label.setText( attribute );

		attribute = variants.getAttributeValue("jsxrequired");
		if ( attribute != null && attribute.compareTo("1") == 0 )
		{
			check_box.setRequired( true );
			if( label != null && !label.getText().toString().endsWith( " (*)"))
				label.append(" (*)");
		}
		
		strings.setAttribute(
			"jsxvalue",
			String.valueOf( variants.getAttributeValue("jsxchecked").compareTo("0") != 0 )
		);
		output = add_task_output( strings, task_output );
		if( output != null )
		{
			check_box.setId( input_count++ );
			((Intd_Form_Widget)check_box).setElement(output);
		}

		return layout;
	}

	protected Intd_Spinner parse_select
	( Element object, Element task_output, Namespace canvas_ns, TextView label ) throws Exception
	{
		String attribute;
		Element output;

		Element strings = object.getChild("strings", canvas_ns);
		Element variants = object.getChild("variants", canvas_ns);

		Intd_Spinner spinner = new Intd_Spinner( this );	//Drop-down box
		
		if( label != null )
			spinner.setName( label.getText().toString() );

		attribute = strings.getAttributeValue("jsxxml");
		if( attribute != null )
		{
			//Build the JDom XML object from the "jsxxml" property:
			Element values = new SAXBuilder().build( new StringReader(attribute) ).getRootElement();
			spinner.setElementsList(values.getChildren());
		}
		
		output = add_task_output( strings, task_output );
		if( output != null )
		{
			spinner.setId( input_count++ );
			((Intd_Form_Widget)spinner).setElement(output);
		}

		spinner = (Intd_Spinner)set_variants_attributes( spinner, variants );

		/*
		//Intd_Spinner_ArrayAdapter is not working!
		Intd_Spinner_ArrayAdapter spinner_adapter = ((Intd_Spinner_ArrayAdapter)(spinner.getAdapter()));
		attribute = variants.getAttributeValue("jsxfontsize");
		if( attribute != null )
			spinner_adapter.setText_size((float)(Float.valueOf(attribute)*1.1));

		attribute = variants.getAttributeValue("jsxfontweight");
		if( attribute != null )
		{
			if( attribute.compareTo("bold") == 0 )		//Isn't there an italic style?
				spinner_adapter.setText_style(Typeface.BOLD);
		}
		 */
		//variants.jsxtype="1" means Select-Combo
		return spinner;
	}

	protected Intd_DatePicker parse_datePicker
	( Element object, Element task_output, Namespace canvas_ns, TextView label ) throws Exception
	{
		Intd_DatePicker date_picker = new Intd_DatePicker( this );

		Element strings = object.getChild("strings", canvas_ns);
		Element variants = object.getChild("variants", canvas_ns);
		Element output;

		strings.setAttribute(
			"jsxvalue",
			variants.getAttributeValue("jsxyear")
			+"-"+variants.getAttributeValue("jsxmonth")
			+"-"+variants.getAttributeValue("jsxdate")
		);
		
		if( label != null )
			date_picker.setName( label.getText().toString() );

		output = add_task_output( strings, task_output );
		if( output != null )
		{
			date_picker.setId( input_count++ );
			((Intd_Form_Widget)date_picker).setElement(output);
		}

		date_picker = (Intd_DatePicker)set_variants_attributes( date_picker, variants );

		return date_picker;
	}

	protected void parse_radioButton
	( Element object, Element task_output, Namespace canvas_ns, LinearLayout parent_layout, TextView label ) throws Exception
	{
		Intd_RadioGroup radio_group;
		String attribute;
		
		Element strings = object.getChild("strings", canvas_ns);
		Element variants = object.getChild("variants", canvas_ns);
		
		String group_name = strings.getAttributeValue("jsxgroupname");
		Integer id = radio_buttons.get( group_name );
		if( id == null )
		{
			radio_group = new Intd_RadioGroup( this );

			radio_group.setLayoutParams( parent_layout.getLayoutParams() );
			radio_group.setOrientation( parent_layout.getOrientation() );
			parent_layout.setOrientation( LinearLayout.HORIZONTAL );

			Element group_strings = (Element)strings.clone();
			group_strings.setAttribute( "jsxname" , group_name );
			group_strings.removeAttribute( "jsxvalue" );
			group_strings.setAttribute( "jsxvalue", "" );

			if( label != null )
				radio_group.setName( label.getText().toString() );

			Element output = add_task_output( group_strings, task_output );
			if( output != null )
			{
				radio_group.setId( input_count );
				radio_buttons.put( group_name, input_count );
				input_count++;
				((Intd_Form_Widget)radio_group).setElement(output);
			}
			parent_layout.addView( radio_group );
		}else
			radio_group = (Intd_RadioGroup)parent_layout.findViewById(id);
		
		if( radio_group != null )
		{
			radio_group.add_button( strings, object.getChild("variants", canvas_ns) );
			
			attribute = variants.getAttributeValue("jsxrequired");
			if ( attribute != null && attribute.compareTo("1") == 0 )
			{
				radio_group.setRequired( true );
				if( label != null && !label.getText().toString().endsWith( " (*)"))
					label.append(" (*)");
			}
		}//else error!
	}
	
	protected View set_variants_attributes( View v, Element variants )
	{
		String attribute;

		//TODO: Can be "25%" or "25" (pixels?)
		attribute = variants.getAttributeValue("jsxheight");
		if( attribute != null )
			v.setMinimumHeight( (int)metrics.density*Integer.valueOf(attribute) );

		//Can be "25%" or "25" (pixels?)
		attribute = variants.getAttributeValue("jsxwidth");
		if( attribute != null )
			v.setMinimumWidth( (int)metrics.density*Integer.valueOf(attribute) );

		return v;
	}
	
	//Overrided by child classes
	protected void print_form() throws Exception {}
	
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
        	IntalioWSBinder binder = (IntalioWSBinder) service;
            try{
            	intalio = binder.getService();
            	print_form();

            }catch( Exception e ){
            	e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            intalio = null;
        }
    };
	
	public void setTask_id(String taskId){ task_id = taskId; }
	public void setTask_type(String taskType){ task_type = taskType; }
	public void setTask_url(String taskUrl){ task_url = taskUrl; }
	public void setTask_ns(String taskNs){ task_ns = taskNs; }
	/**
	private boolean in_array( String[] a, String s )
	{
		int i;
		for( i = a.length-1; i >= 0 && a[i].compareTo(s) != 0; i-- );
		return i>=0;
	}
	*/
}
