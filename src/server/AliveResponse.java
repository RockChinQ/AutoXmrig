package server;

import universal.Out;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

public class AliveResponse extends TimerTask {
	public void run(){
		try{
			for (Listener.AXClientConn conn:Listener.conns){
				conn.writeNonBlocked("!request");
			}
//			Thread.sleep(3000);
			long now=new Date().getTime();
			ArrayList<Listener.AXClientConn> kill=new ArrayList<>();
			for (Listener.AXClientConn conn:Listener.conns){
				if (now-conn.lsResponseTime>ServerMain.alivePeriod*30){
					Out.sayWithTimeLn("[ALIVE]close conn pass:"+conn.pass);
					kill.add(conn);
				}
			}
			for (Listener.AXClientConn conn:kill){
				conn.kill();
			}
		}catch (Exception ignored){
		}
	}
}
