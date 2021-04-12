package test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class SignalTest {
    public static void main(String[] args)throws Exception {
        Process p=Runtime.getRuntime().exec("D:\\xmrig\\xmrig.exe");
        InputStream is=p.getInputStream();
        OutputStream os=p.getOutputStream();
        new Thread(()-> {
            try {
                byte[] buf = new byte[1024];
                int len = 0;
                while ((len = is.read(buf)) != -1) {
                    System.out.print(new String(buf, StandardCharsets.UTF_8));
                    buf=new byte[1024];
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
        while(true){
            String s=javax.swing.JOptionPane.showInputDialog("type:");
            if (s.equalsIgnoreCase("!exit")){
                p.destroy();
                System.exit(0);
            }
            System.out.println("typed:"+s.toCharArray()[0]);
            os.write(s.toCharArray()[0]);
            os.flush();
        }
    }
}
