package server;

import java.util.Date;

public class ServerMain {
    static Listener listener=new Listener(1030);
    static Command command=new Command();
    static int totalShares=0;
    static String serverStart=TimeUtil.millsToMMDDHHmmSS(new Date().getTime());
    public static void main(String[] args) {
        listener.start();
        command.start();
        System.out.println("[MAIN]server started");
    }
}
