package client.main;

import java.io.BufferedReader;

/**
 * 获取命令行输出的异常并输出
 * @author Rock Chin
 */
public class CmdError extends Thread{
    BufferedReader errReader;
    ProcessCmd processCmd;
    StringBuffer buffer=new StringBuffer("");
    public CmdError(BufferedReader errReader, ProcessCmd processCmd){
        this.errReader=errReader;
        this.processCmd=processCmd;
    }
    public void run(){
        try{
            String line;
            while (true){
                line=errReader.readLine();
                if (AXMain.printToStdout)
                    System.out.println(line);
                AXMain.connect.writeIgnoreExce(line);
            }
        }catch (Exception e){}
    }
}
