package test;

import universal.FileIO;

public class PantheraAlgoRemTest {
	public static void main(String[] args)throws Exception {
		String filePath=javax.swing.JOptionPane.showInputDialog("Path");
		String source=FileIO.read(filePath);
		String result=source.replaceFirst("panthera","panthMASKera")
				.replaceAll("\"panthera\": *","\"panthera\": 0\n")
				.replace("panthMASKera","panthera");
		System.out.println(result);
		FileIO.write(filePath,result);
	}
}
