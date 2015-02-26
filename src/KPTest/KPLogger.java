package KPTest;

import java.text.SimpleDateFormat;
import java.util.Date;

public class KPLogger {
    private String name;
    private boolean withDate = false;
    public KPLogger(String name){
        this.name = name;
    }
    public void log(Object strObj){
        Date dNow = new Date();
        SimpleDateFormat dFormat = new SimpleDateFormat("[MM/dd:hh:mm:ss]");

        String ss = String.format("[%s]: %s\n", this.name, strObj);
        if (withDate) ss = String.format("%s[%s]: %s\n", dFormat.format(dNow),this.name, strObj);
        System.out.format(ss);
    }
    public String getName(){ return this.name;}
    public void setName(String name){
        this.name = name;
    }
    public void withDate(boolean wd){
        this.withDate = wd;
    }
    public boolean getWithDate() {return this.withDate;}
}
