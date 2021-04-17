package client.main;

import server.TimeUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

public class Connect extends Thread{
    public Socket socket;
    BufferedWriter tcpw;
    BufferedReader tcpr;
    public void run(){
        while(true) {
            try {
                socket=new Socket("39.100.5.139",1030);
                tcpw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                tcpr=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writeNonBlocked("pass "+AXMain.pass);
                writeNonBlocked("start "+TimeUtil.millsToMMDDHHmmSS(new Date().getTime()));
                try {
                    writeNonBlocked("state " + (AXMain.autoSwitch.running ? "mining" : "hang-up"));
                }catch (Exception ignored){}
                while(true){
                    String[] msgSpt=tcpr.readLine().split(" ");
                    try {
                        switch (msgSpt[0]) {
                            case "!exit": {
                                AXMain.killAllMiner();
                                System.exit(0);
                                break;
                            }
                            case "!stdout": {
                                AXMain.printToStdout=Boolean.parseBoolean(msgSpt[1]);
                                writeNonBlocked("[CONNECT]stdout "+AXMain.printToStdout);
                                break;
                            }
                            case "!request":{
                                writeNonBlocked("response");
                                break;
                            }
                        }
                    }catch (Exception e){
                        AXMain.connect.writeNonBlocked("[CONNECT]command error");
                    }
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            try{Thread.sleep(10000);}catch (Exception ignored){}
        }
    }
    public void writeIgnoreExce(String str){
        try{
            tcpw.write(str);
            if (!str.endsWith("\n"))
                tcpw.newLine();
            tcpw.flush();
        }catch (Exception ignored){}
    }
    public void writeNonBlocked(String str){
        new Thread(()->{
            writeIgnoreExce(str);
        }).start();
    }
}
