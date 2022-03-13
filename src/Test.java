import java.time.temporal.ChronoUnit;
import java.sql.Date;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Test {

    public static void main(String []args){
        Date start = Date.valueOf("2005-01-01");
        Date end = Date.valueOf("2006-02-04");

        getRate(start,end,1.5f);
    }

    public static void getRate(Date d1, Date d2, float basicR){

    }
}
