package server;

import universal.Out;

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
                    case "ls":{
                        int idx=0;
                        Out.sayWithTimeLn("[LIST-TABLE]cptPass\ths10s\tshares\tstate\tcheckTime");
                        int t10sRate=0,tShares=0,tMining=0;
                        for (Listener.AXClientConn conn: Listener.conns){
                            if (conn.state.equals("mining")) {
                                t10sRate += conn.rate10s<0?0:conn.rate10s;
                            }
                            tShares+=conn.shares;
                            tMining+=conn.state.equalsIgnoreCase("mining")?1:0;
                            Out.sayWithTimeLn("[LIST-"+idx+++"]"+String.format("%-14s", conn.pass)
                                    +"\t"+(conn.rate10s<0?"n/a":conn.rate10s)
                                    +"\t"+conn.shares
                                    +"\t"+conn.state
                                    +"\t"+conn.lsUpdateTime);
                        }
                        Out.sayWithTimeLn("[LIST-TOTAL]devCou "+Listener.conns.size()+"\t"+t10sRate+"\t"+ServerMain.totalShares+"\t"+tMining);
                        Out.sayWithTimeLn("[INFO]since"+ServerMain.serverStart+" now"+TimeUtil.millsToMMDDHHmmSS(new Date().getTime())+" @"+(Listener.focused==null?"":Listener.focused.pass));
                        break;
                    }
                    case "lsd":{
                        int idx=0;
                        Out.sayWithTimeLn("[LIST-TABLE]cptPass\t10s\t60s\t15m\tshares\tstate\tcheckTime\tstartTime\trlsNote");
                        int t10sRate=0,t60sRate=0,t15mRate=0,tShares=0,tMining=0;
                        for (Listener.AXClientConn conn: Listener.conns){
                            if (conn.state.equals("mining")) {
                                t10sRate += (conn.rate10s<0?0:conn.rate10s);
                                t60sRate += (conn.rate60s<0?0:conn.rate60s);
                                t15mRate += (conn.rate15m<0?0:conn.rate15m);
                            }
                            tShares+=conn.shares;
                            tMining+=conn.state.equalsIgnoreCase("mining")?1:0;
                            Out.sayWithTimeLn("[LIST-"+idx+++"]"+String.format("%-14s", conn.pass)
                                    +"\t"+(conn.rate10s<0?"n/a":conn.rate10s)
                                    +"\t"+(conn.rate60s<0?"n/a":conn.rate60s)
                                    +"\t"+(conn.rate15m<0?"n/a":conn.rate15m)
                                    +"\t"+conn.shares
                                    +"\t"+conn.state
                                    +"\t"+conn.lsUpdateTime
                                    +"\t"+conn.startTime
                                    +"\t"+conn.rlsNote);
                        }
                        Out.sayWithTimeLn("[LIST-TOTAL]devCount "+Listener.conns.size()+"\t"+t10sRate+"\t"+t60sRate+"\t"+t15mRate+"\t"+ServerMain.totalShares+"\t"+tMining);
                        Out.sayWithTimeLn("[INFO]since"+ServerMain.serverStart+" now"+TimeUtil.millsToMMDDHHmmSS(new Date().getTime())+" @"+Listener.focused.pass);
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
                        Out.sayWithTimeLn("[COMMAND]quiet");
                        break;
                    }
                    case "every":{
                        Listener.printMode=Listener.EVERY;
                        Out.sayWithTimeLn("[COMMAND]every");
                        break;
                    }
                    case "one":{
                        Listener.printMode=Listener.ONE;
                        Out.sayWithTimeLn("[COMMAND]one");
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
                    case "exit":{
                        if (cmdSpt.length>1) {
                            try {
                                Listener.exitAllClient = Boolean.parseBoolean(cmdSpt[1]);
                                Out.sayWithTimeLn("[COMMAND]exit any:" + Listener.exitAllClient);
                            } catch (Exception e) {
                                Out.sayWithTimeLn("[COMMAND]syntax err");
                            }
                        }else {
                            Out.sayWithTimeLn("[COMMAND]exit any:"+Listener.exitAllClient);
                        }
                        break;
                    }
                    case "@":{
                        if (cmdSpt[1].startsWith("&")){
                            Listener.focused=Listener.conns.get(Integer.parseInt(cmdSpt[1].substring(1)));
                            Out.sayWithTimeLn("[COMMAND]@"+Listener.focused.pass);
                        }else{
                            for (Listener.AXClientConn conn:Listener.conns){
                                if (conn.pass.startsWith(cmdSpt[1])){
                                    Listener.focused=conn;
                                    Out.sayWithTimeLn("[COMMAND]@"+Listener.focused.pass);
                                    break cc;
                                }
                            }
                            Out.sayWithTimeLn("[COMMAND]no such conn");
                        }
                        break;
                    }
                    default:{
                        Listener.focused.writeNonBlocked(cmd);
                        break;
                    }
                }
            }catch (Exception e){
                Out.sayWithTimeLn("[SERVER]err at command");
            }
        }
    }
}
