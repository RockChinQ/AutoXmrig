package server;

import conn.ESNSession;
import conn.ISessionListener;
import packs.PackRespNotification;
import packs.PackResult;
import universal.Out;
import util.Debug;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ServerMain {
    static Listener listener=new Listener(1030);
    static Command command=new Command();
    static int totalShares=0;
    static String serverStart=TimeUtil.millsToMMDDHHmmSS(new Date().getTime());
    public static long alivePeriod=60000;
    public static ESNSession session;
    public static Config config=new Config("autoxmrig.conf");
    public static Timer summaryTimer=new Timer();
    public static void main(String[] args) {
//        Debug.debug=true;

        initESNSession();


        listener.start();
        command.start();
        new Timer().schedule(new AliveResponse(),new Date(),alivePeriod);
        Out.sayWithTimeLn("[MAIN]server started");

        if (!config.getStringAnyhow("addr","undefined").equals("undefined")){
            String[] addrs=config.getStringValue("addr").split(",");
            if (!config.getStringAnyhow("last","undefined").equals("undefined")){
                String[] lasts=config.getStringValue("last").split(",");
                int index=0;
                for (String s:addrs){
                    Summary.lsCheckAmt.put(s,Long.parseLong(lasts[index++]));
                }
            }
        }

        summaryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Calendar calendar=Calendar.getInstance();
                    if (calendar.get(Calendar.HOUR_OF_DAY)==config.getIntValue("hour")&&calendar.get(Calendar.MINUTE)==config.getIntValue("minute")) {
                        Summary.makeSummary();
                    }
                }catch (Exception e){}
            }
        },new Date(),60000);
    }
    public static void initESNSession(){
        try {
            session=new ESNSession("39.100.5.139:3003", "autoxmrig", "000112rock.,.", 5000, new ISessionListener() {
                @Override
                public void notificationReceived(PackRespNotification packRespNotification) {

                }

                @Override
                public void sessionLogout(PackResult packResult) {
                    while(true) {
                        try {
                            Debug.debug("reconn");
                            session.reConnect("39.100.5.139:3003", "autoxmrig", "000112rock.,.");
                            break;
                        } catch (Exception e) {
//                            e.printStackTrace();
                        }
                        try{
                            Thread.sleep(20000);
                        }catch (Exception ignored){
                            break;
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
