package test;

public class TestMain {
	public static float rate10s=0,rate60s=0,rate15m=0;
	public static void main(String[] args) {

		String msg="[CONN-386class-new][2021-04-12 14:24:15.092]  miner    speed 10s/60s/15m 512.5 n/a n/a H/s max 512.5 H/s";
		System.out.println(msg);
		int speedIdx=msg.indexOf("15m ");
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
