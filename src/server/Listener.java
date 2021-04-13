package server;

import client.main.AXMain;
import universal.Out;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class Listener extends Thread{
    public static boolean exitAllClient=false;
    public int port=1030;
    public static ArrayList<AXClientConn> conns=new ArrayList<>();
    public static AXClientConn focused;
    public final static int QUIET=0,ONE=1,EVERY=2;
    public static int printMode=0;

    public static class AXClientConn extends Thread{
        public StringBuffer msgs=new StringBuffer();
        public float rate10s=0,rate60s=0,rate15m=0;
        public String lsUpdateTime="undefined";
        public Socket socket;
        BufferedReader tcpr;
        BufferedWriter tcpw;
        public String pass="N/A";
        public int shares=0;
        public String startTime="undefined";
        public long lsResponseTime=0;
        public String state="unknown";
        public AXClientConn(Socket socket)throws Exception{
            this.socket=socket;
            tcpr=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            tcpw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.start();
        }
        public void writeIgnoreExce(String str){
            try{
                tcpw.write(str);
                tcpw.newLine();
                tcpw.flush();
            }catch (Exception ignored){}
        }
        public void writeNonBlocked(String str){
            new Thread(()->{
                writeIgnoreExce(str);
            }).start();
        }

        public void run(){
            try {
                while (true) {
                    String msg=tcpr.readLine();
//                    System.out.println(msg);
                    String[] msgSpt=msg.split(" ");
                    lsResponseTime=new Date().getTime();
                    switch (msgSpt[0]){
                        case "pass":{
                            this.pass=msgSpt[1];
                            Out.sayWithTimeLn("[CONN]new conn pass:"+pass);
                            if (focused==null){
                                focused=this;
                                Out.sayWithTimeLn("[LISTENER]@"+pass);
                            }
                            if (exitAllClient){
                                Out.sayWithTimeLn("[CONN]exit any client.");
                                writeIgnoreExce("!exit");
                            }
                            break;
                        }
                        case "start":{
                            startTime=msgSpt[1];
                            break;
                        }
                        case "response":{
                            break;
                        }
                        case "state":{
                            this.state=msgSpt[1];
                            break;
                        }
                        default:{
                            if (printMode==EVERY||(printMode==ONE&&focused==this))
                                Out.sayWithTime("[CONN-"+pass+"]"+msg+(msg.endsWith("\n")?"":"\n"));
                            if (msg.contains("speed")){
                                int speedIdx=msg.indexOf("15m ");
                                try {
                                    String[] speedSS = msg.substring(speedIdx + 4, msg.indexOf("H/s") - 1).split(" ");
                                    if (!speedSS[0].equalsIgnoreCase("n/a")) {
                                        this.rate10s = Float.parseFloat(speedSS[0]);
                                    } else {
                                        this.rate10s = 0;
                                    }
                                    if (!speedSS[1].equalsIgnoreCase("n/a")) {
                                        this.rate60s = Float.parseFloat(speedSS[1]);
                                    } else {
                                        this.rate60s = 0;
                                    }
                                    if (!speedSS[2].equalsIgnoreCase("n/a")) {
                                        this.rate15m = Float.parseFloat(speedSS[2]);
                                    } else {
                                        this.rate15m = 0;
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                    System.out.println("speedIdx"+speedIdx);
                                }
                            }else if(msg.contains("accepted")){
//                                System.out.println("sub:"+msg.substring(msg.indexOf("d (")+3,msg.indexOf("diff")-4));
//                                this.shares=Integer.parseInt(msg.substring(msg.indexOf("d (")+3,msg.indexOf("diff")-4));
                                this.shares++;
                                ServerMain.totalShares++;
                            }
                            this.lsUpdateTime=TimeUtil.millsToMMDDHHmmSS(new Date().getTime());
                        }
                    }
                }
            }catch (Exception e){
                Out.sayWithTimeLn("[CONN]close conn pass:"+pass);
//                e.printStackTrace();
//                e.printStackTrace();
                kill();
            }
        }

        /**
         * 一个无响应的连接可能是因为主机已关闭或网络问题
         * 解决前者只需要关闭连接，然鹅为了防止后者再次连接则主动发送关闭要求
         */
        public void kill(){
            try {
                writeNonBlocked("!exit");
                Thread.sleep(200);
                socket.close();
            }catch (Exception ignored){}
            try {
                conns.remove(this);
            }catch (Exception ignored){}
        }
    }

    public Listener(int port){
        this.port=port;
    }
    public void run(){
        try {
            ServerSocket socket=new ServerSocket(port);
            while (true) {
                conns.add(new AXClientConn(socket.accept()));
            }
        }catch (Exception e){
            Out.sayWithTimeLn("[LISTENER]cannot continue listening");
        }
    }

}
