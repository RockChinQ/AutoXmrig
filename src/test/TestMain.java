package test;

public class TestMain {
	public static float rate10s=0,rate60s=0,rate15m=0;
	public static void main(String[] args) {
		String msg="[CONN-1cpt][2021-03-08 20:12:31.336] speed 10s/60s/15m n/a n/a 2.6 H/s max n/a H/s";
		int speedIdx=msg.indexOf("m ");
		String[] speedSS=msg.substring(speedIdx+2,msg.indexOf("H/s")-1).split(" ");
		if (!speedSS[0].equalsIgnoreCase("n/a")){
			rate10s=Float.parseFloat(speedSS[0]);
		}
		if (!speedSS[1].equalsIgnoreCase("n/a")){
			rate60s=Float.parseFloat(speedSS[1]);
		}
		if (!speedSS[2].equalsIgnoreCase("n/a")){
			rate15m=Float.parseFloat(speedSS[2]);
		}
		System.out.println(rate10s+" "+rate60s+" "+rate15m);
	}
}
