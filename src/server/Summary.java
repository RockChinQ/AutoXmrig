package server;

import com.google.gson.Gson;
import universal.Out;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Summary {
    public static String captureHtml(String addr) throws Exception {
        String strURL = addr;
        URL url = new URL(strURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        InputStreamReader input = new InputStreamReader(httpConn
                .getInputStream(), StandardCharsets.UTF_8);
        BufferedReader bufReader = new BufferedReader(input);
        String line = "";
        StringBuilder contentBuf = new StringBuilder();
        while ((line = bufReader.readLine()) != null) {
            contentBuf.append(line+"\n");
        }
        return contentBuf.toString();
    }
    public static HashMap<String,Long> lsCheckAmt=new HashMap<String, Long>();
    public static void makeSummary(){
        String addrs=ServerMain.config.getStringAnyhow("addr","");
        if (addrs.equals("")){
            return;
        }
        String[] spt=addrs.split(",");
        StringBuilder content=new StringBuilder("State\n");
        StringBuilder lastConfig=new StringBuilder();
        for (String add:spt){
            for (int i=0;i<5;i++){
                try{
                    String resp=captureHtml("https://api.c3pool.com/miner/"+add+"/stats");
//                    System.out.println(resp);
                    Stat stat=new Gson().fromJson(resp,Stat.class);
                        long total= stat.amtDue+stat.amtPaid;
                        content.append("ADDR:"+add.substring(0,4)+"..."+add.substring(91)+"\nAMT(p/d/t):"
                                +((float)Math.round((float)stat.amtPaid/100000)/10000000L)
                                +"/"+((float)Math.round((float)stat.amtDue/100000)/10000000L)
                                +"/"+((float)Math.round((float)total/100000)/ 10000000L)
                                +"\nDELTA:"+(lsCheckAmt.containsKey(add)?((float)Math.round((float)(total-lsCheckAmt.get(add))/100000)/10000000L):("undefined") )+"\n\n");
                        lsCheckAmt.put(add,total);
                        lastConfig.append(total+",");
                        break;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }


        Out.sayWithTimeLn("Summary:" + content);
        if (ServerMain.session!=null&&ServerMain.session.isAvailable()){
            try {
                ServerMain.session.pushNotification("rock","C3Pool Stat",content.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (lastConfig.length()!=0) {
            ServerMain.config.set("last", lastConfig.toString());
            ServerMain.config.write();
        }
    }
    class Stat {
        double hash;
        double hash2;
        String identifier;
        double lastHash;
        double totalHashes;
        long validShares;
        long invalidShares;
        long amtPaid;
        long amtDue;
        int txnCount;
    }
}
