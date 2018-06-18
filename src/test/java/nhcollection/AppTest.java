package nhcollection;

import java.util.*;
import nhcollection.util.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        boolean result = true;
        //long start = System.nanoTime();

        HashNonHeapMap nonHeapMap = new HashNonHeapMap();
        //Map nonHeapMap = new HashMap(828);
        String key = "key";
        String val = "AvaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevalueX";
        /*for (int i = 0; i < 1000000; i++) {
          nonHeapMap.put(key + i, val + i);
          if ((i % 20000) == 0) System.out.println(i);
        }
        long end = System.nanoTime();
        System.out.println(((end - start) / 1000 /1000) + "ms");
        System.exit(1);*/
        // PUT
        for (int i = 0; i < 100000; i++) {
          nonHeapMap.put(key + i, val + i);
          if ((i % 20000) == 0) System.out.println(i);
        }
        // GET
        for (int i = 0; i < 100000; i++) {
          String testVal = (String)nonHeapMap.get(key + i);
          if (testVal == null || !testVal.equals(val + i)) result = false;
        }
        // REMOVE
        for (int i = 0; i < 50000; i++) {
          nonHeapMap.remove(key + i);
          if ((i % 10000) == 0) System.out.println(i);
        }
        // REMOVE -> GET
        for (int i = 0; i < 50000; i++) {
          String testVal = (String)nonHeapMap.get(key + i);
          if (testVal != null) result = false;;
        }
        val = "valuevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevalue";
        // Larget String PUT
        for (int i = 0; i < 50000; i++) {
          nonHeapMap.put(key + i, val+val+ i);
          if ((i % 10000) == 0) System.out.println(i);
        }
        // GET
        for (int i = 0; i < 50000; i++) {
          String testVal = (String)nonHeapMap.get(key + i);
          if (testVal == null || !testVal.equals(val+val + i)) result = false;
        }
        // SIZE
        System.out.println("size() = [" + nonHeapMap.size() + "]");
        if (nonHeapMap.size() != 100000L)  result = false;

        //　カーソル
        nonHeapMap.resetCursor();
        for (Object[] obj = (Object[])nonHeapMap.next(); obj != null; obj = (Object[])nonHeapMap.next()) {
          //System.out.println("key=" + obj[0]); // key
          //System.out.println("value=" + obj[1].toString()); // value
        }

        assertTrue( result );
    }
}
