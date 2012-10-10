package com.blauadvisors.diui;

public class Server_info
{
	private String server, user, pwd;

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

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	} 
	
	public String toString()
	{
		if( server == null )
			return "- Select server -";
		//else
			return user+"@"+server;
	}
	
	public byte[] toFile(){
		return (server+"\t"+user+"\t"+pwd+"\n").getBytes();
	}
}
