package server;

import java.util.Date;
import java.util.TimerTask;

public class AliveResponse extends TimerTask {
	public void run(){
		try{
			long now=new Date().getTime();
			for (Listener.AXClientConn conn:Listener.conns){
				conn.writeNonBlocked("!request");
				if (now-conn.lsResponseTime>ServerMain.alivePeriod*3){
					conn.kill();
				}
			}
		}catch (Exception ignored){

		}
	}
}
