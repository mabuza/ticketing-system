package system.ticketing.utilities.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ncube on 2/5/17.
 */
public class RefGen {
    private static Map<String, Integer> counters = new HashMap<String, Integer>();

    private static final int MAX_COUNT = 99;
    private static final int MAX_STAN = 999999;
    private static Date anchorDate;

    private static Date getAnchorDate() {
        if (anchorDate == null) {
            try {
                SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
                anchorDate = df.parse("01012015");
            }
            catch (ParseException e) {
                // will not happen with fixed strings ...
            }
        }

        return anchorDate;
    }
    /**
     * @param args id
     */
    public static void main(String[] args) {
        String ref = "";
        Set<String> uniqueRefs = new HashSet<String>();
        for(int i=0; i<1; i++){

            if(i%2==0)
                ref = getReference("E");
            else
                ref = getReference("T");
            uniqueRefs.add(ref);

            System.out.println(ref+" -> "+ref.length());
        }

        System.out.println("Unique Refs: "+uniqueRefs.size());

        //System.out.println(pad("1234", "0", 7));

    }

    public static synchronized String getReference(String prefix){
        //1. Add the prefix to the id
        String reference = prefix;


        //2. Add Days after 1 January 2015
        reference += pad(calculateDays()+"", "0", 4);

        //3. Add seconds after midnight
        reference += pad(calculateSeconds()+"", "0", 5);

        //4. Add add counter
        Integer counter = counters.get(prefix);


        if(counter==null || counter>=MAX_COUNT) counter = 0;

        counter++;

        counters.put(prefix, counter);
        reference += pad(""+counter, "0", 2);

        return reference;
    }

    public static synchronized long getSTAN(){
        String prefix = "STAN";

        Integer counter = counters.get(prefix);


        if(counter==null || counter>=MAX_STAN) counter = 0;

        counter++;

        counters.put(prefix, counter);
        return counter;
    }

    private static long calculateDays(){
        Date now = new Date();
        //return TimeUnit.DAYS.convert(now.getTime()-getAnchorDate().getTime(), TimeUnit.MILLISECONDS);
        return TimeUnit.MILLISECONDS.toDays(now.getTime()-getAnchorDate().getTime());
    }

    private static long calculateSeconds(){
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date old = c.getTime();

        //return TimeUnit.SECONDS.convert(now.getTime()-old.getTime(), TimeUnit.MILLISECONDS);
        return TimeUnit.MILLISECONDS.toSeconds(now.getTime()-old.getTime());
    }

    private static String pad(String text, String padChar, int requiredLength){
        StringBuffer sb = new StringBuffer(text);
        while(sb.length()<requiredLength){
            sb.insert(0, padChar);
        }

        return sb.toString();
    }
}
