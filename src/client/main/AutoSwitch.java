package client.main;

import universal.FileIO;
import universal.Out;

import java.awt.*;
import java.util.TimerTask;

public class AutoSwitch extends TimerTask {
    private static Point lsMousePoint=MouseInfo.getPointerInfo().getLocation();
    boolean running=false;
    int count=0;
    public AutoSwitch(){
    }
    @Override
    public void run(){
        Point nowLoc=MouseInfo.getPointerInfo().getLocation();
        if (AXMain.printToStdout){
            Out.sayWithTimeLn("Check mouse location");
        }
        if (nowLoc.distance(lsMousePoint)>10){
            updateState(false);
            count=0;
        }else if (nowLoc.distance(lsMousePoint)<2){
            if (count==0){
                count++;
            }else if (count==1) {
                updateState(true);
            }
        }
        lsMousePoint.setLocation(nowLoc);
    }
    public void updateState(boolean running){
        if (running==this.running)
            return;
        try {
            if (running) {
                String fileContent = FileIO.read("D:\\xmrig\\config.json");
                FileIO.write("D:\\xmrig\\config.json", fileContent.replace("\"cpu\": {\n" +
                        "        \"enabled\": false,", "\"cpu\": {\n" +
                        "        \"enabled\": true,"));
                AXMain.connect.writeNonBlocked("state mining");
            } else {
                String fileContent = FileIO.read("D:\\xmrig\\config.json");
                FileIO.write("D:\\xmrig\\config.json", fileContent.replace("\"cpu\": {\n" +
                        "        \"enabled\": true,", "\"cpu\": {\n" +
                        "        \"enabled\": false,"));
                AXMain.connect.writeNonBlocked("state hang-up");
            }
            this.running=running;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}