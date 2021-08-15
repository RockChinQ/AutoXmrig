package test;

import server.Summary;

public class SpyTest {
    public static void main(String[] args) throws Exception{
        Summary.makeSummary();
        Thread.sleep(10000);
        Summary.makeSummary();
    }
}
