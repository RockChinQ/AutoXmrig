package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

public class Command extends Thread{
    public void run(){
        BufferedReader clir=new BufferedReader(new InputStreamReader(System.in));
        while (true){
            try {
                String cmd= clir.readLine().replaceAll("\n","");
                String[] cmdSpt = cmd.split(" ");
                cc:switch (cmdSpt[0]){
                    case "list":{
                        int idx=0;
                        System.out.println("[LIST-TABLE]cptPass\t10s\t60s\t15m\tshares\tcheckTime\tstartTime");
                        int t10sRate=0,t60sRate=0,t15mRate=0,tShares=0;
                        for (Listener.AXClientConn conn: Listener.conns){
                            t10sRate+=conn.rate10s;
                            t60sRate+=conn.rate60s;
                            t15mRate+=conn.rate15m;
                            tShares+=conn.shares;
                            System.out.println("[LIST-"+idx+++"]"+String.format("%-14s", conn.pass)+"\t"+conn.rate10s+"\t"+conn.rate60s+"\t"+conn.rate15m+"\t"+conn.shares+"\t"+conn.lsUpdateTime+"\t"+conn.startTime);
                        }
                        System.out.println("[LIST-TOTAL]dev "+Listener.conns.size()+"\t"+t10sRate+"\t"+t60sRate+"\t"+t15mRate+"\t"+ServerMain.totalShares);
                        System.out.println("[INFO]since"+ServerMain.serverStart+" now"+TimeUtil.millsToMMDDHHmmSS(new Date().getTime())+" @"+Listener.focused.pass);
                        break;
                    }
                    case "all":{
                        String msg=cmd.substring(4);
                        for(Listener.AXClientConn conn:Listener.conns){
                            conn.writeNonBlocked(msg);
                        }
                        break;
                    }
                    case "quiet":{
                        Listener.printMode=Listener.QUIET;
                        System.out.println("[COMMAND]quiet");
                        break;
                    }
                    case "every":{
                        Listener.printMode=Listener.EVERY;
                        System.out.println("[COMMAND]every");
                        break;
                    }
                    case "one":{
                        Listener.printMode=Listener.ONE;
                        System.out.println("[COMMAND]one");
                        break;
                    }
                    case "test":{
                        long now=new Date().getTime();
                        for (Listener.AXClientConn conn:Listener.conns){
                            if (now-conn.lsResponseTime>ServerMain.alivePeriod*3){
                                conn.kill();
                            }
                        }
                        break;
                    }
                    case "@":{
                        if (cmdSpt[1].startsWith("&")){
                            Listener.focused=Listener.conns.get(Integer.parseInt(cmdSpt[1].substring(1)));
                            System.out.println("[COMMAND]@"+Listener.focused.pass);
                        }else{
                            for (Listener.AXClientConn conn:Listener.conns){
                                if (conn.pass.startsWith(cmdSpt[1])){
                                    Listener.focused=conn;
                                    System.out.println("[COMMAND]@"+Listener.focused.pass);
                                    break cc;
                                }
                            }
                            System.out.println("[COMMAND]no such conn");
                        }
                        break;
                    }
                    default:{
                        Listener.focused.writeNonBlocked(cmd);
                        break;
                    }
                }
            }catch (Exception e){
                System.out.println("[SERVER]err at command");
            }
        }
    }
}
