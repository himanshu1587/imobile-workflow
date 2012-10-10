package com.blauadvisors.diui;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class IntalioWS extends Service
{
	private final IBinder iwsBinder = new IntalioWSBinder();

	private String server, user, token = null;
	private int port;

	//xmlPost_login( "10.36.174.192", 8080, "intalio\\47760875H", "47760875H" );	//OK
	//xmlPost_login( "bpmstester.blau.lc", 8080, "intalio\\admin", "changeit" );	//OK
	//xmlPost_login( "bpmstester", 8080, "intalio\\admin", "changeit" );			//KO (Android config issue)
	public void xmlPost_login( String pwd ) //throws Exception
	{
		String xml_data =
	"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tok=\"http://tempo.intalio.org/security/tokenService/\">"
+	"<soapenv:Header/>"
+	"<soapenv:Body>"
+		"<tok:authenticateUser>"
+			"<tok:user>"+ user +"</tok:user>"
+			"<tok:password>"+ pwd +"</tok:password>"
+		"</tok:authenticateUser>"
+	"</soapenv:Body>"
+ "</soapenv:Envelope>";
		
		try {
			Element ws_response = ws_call( xml_data, "/axis2/services/TokenService.Service/" );
			Namespace soapenv = ws_response.getNamespace("soapenv");
			Namespace tokenws = ws_response.getNamespace("tokenws");

			token = ws_response.
				getChild("Body", soapenv).
				getChild("authenticateUserResponse", tokenws).
				getChild("token", tokenws).getValue();
		} catch ( Exception e ) {
			token = null;
			e.printStackTrace();
		}

	}

	public ArrayList<Hashtable<String, String>> get_processes_list( ) throws Exception
	{
		ArrayList<Hashtable<String, String>> tasks_list = new ArrayList<Hashtable<String, String>>();
		String xml_request =
	"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tas=\"http://www.intalio.com/BPMS/Workflow/TaskManagementServices-20051109/\">"
+	"<soapenv:Header/>"
+	"<soapenv:Body>"
+		"<tas:getAvailableTasksRequest>"
+			"<tas:participantToken>" + token + "</tas:participantToken>"
+			"<tas:taskType>INIT</tas:taskType>"
+			"<tas:subQuery>ORDER BY t._creationDate ASC</tas:subQuery>"
+			"<tas:first></tas:first>"
+			"<tas:max></tas:max>"
+		"</tas:getAvailableTasksRequest>"
+	"</soapenv:Body>"
+ "</soapenv:Envelope>";

		Element ws_response = ws_call( xml_request, "/axis2/services/TaskManagementServices.TaskManagementServicesSOAP" );
		Namespace soap = ws_response.getNamespace("soapenv");
		Namespace tms = ws_response.getNamespace("tms");
		
		Iterator<?> tasks = ws_response.
			getChild("Body", soap).
			getChild("getAvailableTasksResponse", tms).
			getChildren("task", tms).iterator();

		Element property = new Element("fck");
		Iterator<?> properties_it;
		Hashtable<String, String> task_data;
		Element child;
		String url;
		while( tasks.hasNext() )
		{
			child = (Element)tasks.next();
			properties_it = child.getChildren().iterator();
			task_data = new Hashtable<String, String>();
			while( properties_it.hasNext() )
			{
				property = (Element)properties_it.next();
				task_data.put( property.getName(), property.getValue() );
			}
			url = get_url_form( task_data.get("formUrl") );
			if( url != null )
			{
				task_data.put( "url", get_url_form( task_data.get("formUrl") ) );
				task_data.put("XSD_ok", check_xsd_availability(task_data.get("formUrl")).toString() );
			}else
				task_data.put("XSD_ok", "false");

			tasks_list.add(task_data);
		}
		return tasks_list;
	}

	public ArrayList<Hashtable<String, String>> get_ready_list( String type ) throws Exception
	{
		ArrayList<Hashtable<String, String>> tasks_list = new ArrayList<Hashtable<String, String>>();
		String xml_request = 
	"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tas=\"http://www.intalio.com/BPMS/Workflow/TaskManagementServices-20051109/\">"
+	"<soapenv:Header/>"
+	"<soapenv:Body>"
+		"<tas:getAvailableTasksRequest>"
+			"<tas:participantToken>" + token + "</tas:participantToken>"
+			"<tas:taskType>"+type+"</tas:taskType>"
+			"<tas:subQuery>T._state = TaskState.READY or T._state = TaskState.CLAIMED ORDER BY t._creationDate ASC</tas:subQuery>"
+			"<tas:first></tas:first>"
+			"<tas:max></tas:max>"
+		"</tas:getAvailableTasksRequest>"
+	"</soapenv:Body>"
+ "</soapenv:Envelope>";

		Element ws_response = ws_call( xml_request, "/axis2/services/TaskManagementServices.TaskManagementServicesSOAP" );
		Namespace soap = ws_response.getNamespace("soapenv");
		Namespace tms = ws_response.getNamespace("tms");
		
		Iterator<?> tasks = ws_response.
			getChild("Body", soap).
			getChild("getAvailableTasksResponse", tms).
			getChildren("task", tms).iterator();

		Element property;
		Iterator<?> properties_it;
		Hashtable<String, String> task_data;
		Element child;
		while( tasks.hasNext() )
		{
			child = (Element)tasks.next();
			properties_it = child.getChildren().iterator();
			task_data = new Hashtable<String, String>();
			while( properties_it.hasNext() )
			{
				property = (Element)properties_it.next();
				task_data.put( property.getName(), property.getValue() );
			}

			task_data.put("url", get_url_form(task_data.get("formUrl")) );
			task_data.put("XSD_ok", check_xsd_availability(task_data.get("formUrl")).toString() );

			tasks_list.add(task_data);
		}
		return tasks_list;
	}

	public ArrayList<Hashtable<String, String>> get_readyTasks_list( ) throws Exception
	{
		return get_ready_list("ACTIVITY");
	}

	public ArrayList<Hashtable<String, String>> get_readyNotifs_list( ) throws Exception
	{
		return get_ready_list("NOTIFICATION");
	}

	public Element get_task_input_element( String task_id ) throws Exception
	{
		String xml_request = 
	"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tas=\"http://www.intalio.com/BPMS/Workflow/TaskManagementServices-20051109/\">"
+	"<soapenv:Header/>"
+	"<soapenv:Body>"
+		"<tas:getTaskRequest>"
+			"<tas:taskId>"+task_id+"</tas:taskId>"
+			"<tas:participantToken>"+token+"</tas:participantToken>"
+		"</tas:getTaskRequest>"
+	"</soapenv:Body>"
+ "</soapenv:Envelope>";

		Element ws_response = ws_call( xml_request, "/axis2/services/TaskManagementServices.TaskManagementServicesSOAP" );
		Namespace soap = ws_response.getNamespace("soapenv");
		Namespace tms = ws_response.getNamespace("tms");
	Log.d("GTI", new XMLOutputter().outputString(ws_response));
		Element task_data = ws_response.
			getChild("Body", soap).
			getChild("getTaskResponse", tms).
			getChild("task", tms).
			getChild("input", tms).
			getChild("FormModel", tms);
		task_data.removeNamespaceDeclaration(null);
		//Log.d("Task Data", new XMLOutputter().outputString(task_data));
		return task_data;
	}

	public String get_task_input( String task_id ) throws Exception
	{
		Element task_data = get_task_input_element(task_id);
		return new XMLOutputter().outputString(task_data);
	}

	public Element start_process( String process_id, Element task_input) throws Exception
	{
		//Log.d("START", new XMLOutputter().outputString(task_input));
		return ws_call(
	"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tas=\"http://www.intalio.com/BPMS/Workflow/TaskManagementServices-20051109/\">"
+	"<soapenv:Header/>"
+	"<soapenv:Body>"
+		"<tas:initRequest>"
+			"<tas:taskId>"+process_id+"</tas:taskId>"
+			"<tas:input>"+new XMLOutputter().outputString(task_input)+"</tas:input>"
+			"<tas:participantToken>"+token+"</tas:participantToken>"
+		"</tas:initRequest>"
+	"</soapenv:Body>"
+"</soapenv:Envelope>",
	"/axis2/services/TaskManagementServices.TaskManagementServicesSOAP");
	}

	public Element complete_task( String task_id, Element task_output ) throws Exception
	{
		//TODO: That's a bad way! use JDom api. There are issues with the namespace, JDom and Intalio.
		//See also XSD_Form_Activity.setXsd()
		String xml_output = new XMLOutputter().outputString(task_output).
			replace( "<FormModel xmlns", "<bai:FormModel xmlns:bai").
			replace("</FormModel>", "</bai:FormModel>");
		//String xml_output = new XMLOutputter().outputString(task_output);
		
		String to_send = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ib4p=\"http://www.intalio.com/bpms/workflow/ib4p_20051115\">"
+	"<soapenv:Header/>"
+	"<soapenv:Body>"
+		"<ib4p:completeTaskRequest>"
+			"<ib4p:taskMetaData>"
+				"<ib4p:taskId>"+task_id+"</ib4p:taskId>"
+				"<ib4p:userProcessNamespaceURI>"+task_output.getNamespace().getURI()+"</ib4p:userProcessNamespaceURI>"
+				"<ib4p:userProcessCompleteSOAPAction></ib4p:userProcessCompleteSOAPAction>"
+			"</ib4p:taskMetaData>"
+			"<ib4p:participantToken>"+token+"</ib4p:participantToken>"
+			"<ib4p:taskOutput>"+xml_output+"</ib4p:taskOutput>"
+		"</ib4p:completeTaskRequest>"
+	"</soapenv:Body>"
+"</soapenv:Envelope>";
		return ws_call( to_send, /*"/ode/processes/completeTask"*/ "/ode/processes/completeTask.UIFWPort" );
	}

	public Element complete_notify( String task_id ) throws Exception
	{
		return ws_call(
	"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tas=\"http://www.intalio.com/BPMS/Workflow/TaskManagementServices-20051109/\">"
+	"<soapenv:Header/>"
+	"<soapenv:Body>"
+		"<tas:completeRequest>"
+			"<tas:taskId>"+task_id+"</tas:taskId>"
+		"<tas:participantToken>"+token+"</tas:participantToken>"
+		"</tas:completeRequest>"
+	"</soapenv:Body>"
+"</soapenv:Envelope>",
	"/axis2/services/TaskManagementServices.TaskManagementServicesSOAP");
	}

	public Element ws_call( String xml_request, String ws_path ) throws Exception
	{
		//Setup the HTTP transport:
		URL url = new URL("http://"+server+":"+port+ws_path);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
		con.setRequestProperty("SOAPAction", "\"\"");
		con.setDoOutput(true);

		// Add the webservice XML request and send the message:
		con.getOutputStream().write( xml_request.getBytes() );
		// Get the HTTP response, parse the XML as Document and return it:
		return new SAXBuilder().build( con.getInputStream() ).getRootElement();
	}

	public String get_url_form( String form_url )
	{
		// XForms
		if( form_url.endsWith(".xform") || form_url.substring(0, 3).compareTo("oxf") == 0 )
			return "http://"+server+":"+port+"/xFormsManager/init";
		// GI Forms
		else if( form_url.substring(0, 4).compareTo("http") == 0 )
			return form_url;
		else
			return null;		//Will raise an exception!
	}

	public BufferedReader get_canvas_form( String form_url ) throws Exception
	{
		//http://blau.serveftp.net:8080/gi/apppath/Chained/First_form.gi/IntalioInternal/ServerLaunch.html
		return new BufferedReader( new InputStreamReader(
			new URL(
				//substring().lastIndexOf(): Bad way!
				form_url.substring(0, form_url.lastIndexOf("/IntalioInternal/ServerLaunch.html"))+"/components/appCanvas.xml"
			).openStream()
		));
	}

	public BufferedReader get_xsd_form( String form_url ) throws Exception
	{
		return new BufferedReader( new InputStreamReader(
			new URL(
				//substring().lastIndexOf(): Bad way!
				form_url.substring(0, form_url.lastIndexOf("/"))+"/FormModel.xsd"
			).openStream()
		));
	}

	public String get_xsd_form_string( String form_url ) throws Exception
	{
		URL url = new URL( form_url.substring( 0, form_url.lastIndexOf("/") )+"/FormModel.xsd" );
		DataInputStream dis = new DataInputStream( new BufferedInputStream( url.openStream() ) );
		String xsd = "", buf;

		while( (buf=dis.readLine()) != null )
			xsd += buf;

		return xsd;
	}

	public Boolean check_xsd_availability( String form_url )
	{
		return true;
		/*
		try{
			new URL( form_url.substring( 0, form_url.lastIndexOf("/") )+"/FormModel.xsd" ).openConnection().connect();
			return true;
		}catch ( Exception e ){
			return false;
		}
		*/
	}

	public Hashtable<String, String> parse_response_on_init_complete( Element response )
	{
		//Log.d("SSP", "Parse response: "+new XMLOutputter().outputString( response ));
		
		Hashtable<String, String> response_parsed = new Hashtable<String, String>();
		Namespace soap = response.getNamespace("soapenv");
		//Namespace tms = response.getNamespace("tms");

		/*
		Element process_response = response.
				getChild("Body", soap).
				getChild("initProcessResponse").
				getChild("userProcessResponse").
				getChild("initProcessResponse");
		*/

		Element process_response = (Element)response.getChild("Body", soap).getChildren().get(0); //"initProcessResponse"
		Namespace tms = process_response.getNamespace();
		process_response = process_response.getChild("userProcessResponse", tms);
		//process_response = (Element) process_response.getChildren().get(0);		//"userProcessResponse"
		process_response = (Element) process_response.getChildren().get(0);		//"initProcessResponse"
		Namespace form_ns = process_response.getNamespace();
		
		response_parsed.put("status", process_response.getChild("status", form_ns).getValue());
		response_parsed.put("error_code", process_response.getChild("errorCode", form_ns).getValue());
		response_parsed.put("error_reason", process_response.getChild("errorReason", form_ns).getValue());
		
		Element meta_data = process_response.getChild("taskMetaData", form_ns);
		response_parsed.put("next_task_ID", meta_data.getChild("nextTaskId", form_ns).getValue());
		response_parsed.put("next_task_URL", meta_data.getChild("nextTaskURL", form_ns).getValue());
		response_parsed.put("instance_ID", meta_data.getChild("instanceId", form_ns).getValue());

		Log.d("SSP", "Miserable death!?");
		
		return response_parsed;
	}

	public Hashtable<String, String> parse_response_on_task_complete( Element response )
	{
		Hashtable<String, String> response_parsed = new Hashtable<String, String>();
		
		Namespace soap = response.getNamespace("soapenv");
		Namespace tms = response.getNamespace("b4p");
		Element process_response = response.
			getChild("Body", soap).
			getChild("response", tms);
		
		Namespace form_ns = process_response.getNamespace();
		response_parsed.put("status", process_response.getChild("status", form_ns).getValue());
		response_parsed.put("error_code", process_response.getChild("errorCode", form_ns).getValue());
		response_parsed.put("error_reason", process_response.getChild("errorReason", form_ns).getValue());
		
		Element meta_data = process_response.getChild("taskMetaData", form_ns);
		response_parsed.put("next_task_ID", meta_data.getChild("nextTaskId", form_ns).getValue());
		response_parsed.put("next_task_URL", meta_data.getChild("nextTaskURL", form_ns).getValue());
		
		return response_parsed;
	}

	public class IntalioWSBinder extends Binder
	{
		IntalioWS getService() throws Exception
		{
			return IntalioWS.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return iwsBinder;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	/*
	//Token should always be private.
	//That prevents to call ws_call(), should be always done from this class.
	public String getToken() {
		return token;
	}
	*/
	public boolean isTokenNull(){
		return token == null;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
