package universal;

import server.TimeUtil;

public class Out {
    public static void sayWithTimeLn(String msg){
        sayWithTime(msg+"\n");
    }
    public static void sayWithTime(String msg){
        System.out.print(TimeUtil.nowMMDDHHmmSS()+"|"+msg);
    }
}
