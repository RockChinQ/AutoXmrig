package server;

import universal.Out;

import java.util.Date;
import java.util.Timer;

public class ServerMain {
    static Listener listener=new Listener(1030);
    static Command command=new Command();
    static int totalShares=0;
    static String serverStart=TimeUtil.millsToMMDDHHmmSS(new Date().getTime());
    public static long alivePeriod=60000;
    public static void main(String[] args) {
        listener.start();
        command.start();
        new Timer().schedule(new AliveResponse(),new Date(),alivePeriod);
        Out.sayWithTimeLn("[MAIN]server started");
    }
}
