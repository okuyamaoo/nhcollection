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
      try {
        boolean result = true;
        //long start = System.nanoTime();

        HashNonHeapMap nonHeapMap = new HashNonHeapMap();
        Map tmpMap = new HashMap();
        tmpMap.put("aaaaa", "bbbbb");
        tmpMap.put("ccccc", "ddddd");
        nonHeapMap.put("key1", tmpMap);
        System.out.println(nonHeapMap.get("key1"));
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
        // containsKey
        for (int i = 0; i < 100000; i++) {
          if(!nonHeapMap.containsKey(key + i)) result = false;
        }
        // SIZE
        System.out.println("size() = [" + nonHeapMap.size() + "]");
        if (nonHeapMap.size() != 100000L)  result = false;

        //　カーソル
        nonHeapMap.resetCursor();
        int hashMapCount = 0;
        for (Object[] obj = (Object[])nonHeapMap.next(); obj != null; obj = (Object[])nonHeapMap.next()) {
          //System.out.println("key=" + obj[0]); // key
          //System.out.println("value=" + obj[1].toString()); // value
          hashMapCount++;
        }
        if (hashMapCount != 100000) result = false;
        nonHeapMap.resetCursor();
        hashMapCount = 0;
        for (Object[] obj = (Object[])nonHeapMap.next(); obj != null; obj = (Object[])nonHeapMap.next()) {
          //System.out.println("key=" + obj[0]); // key
          //System.out.println("value=" + obj[1].toString()); // value
          hashMapCount++;
        }
        if (hashMapCount != 100000) result = false;

        // TreeMapのテスト
        TreeNonHeapMap treeNonHeapMap = new TreeNonHeapMap();

        key = "key";
        val = "AvaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevalueX";
        /*for (int i = 0; i < 1000000; i++) {
          nonHeapMap.put(key + i, val + i);
          if ((i % 20000) == 0) System.out.println(i);
        }
        long end = System.nanoTime();
        System.out.println(((end - start) / 1000 /1000) + "ms");
        System.exit(1);*/
        // PUT
        for (int i = 0; i < 100000; i++) {
          treeNonHeapMap.put(key + i, val + i);
          if ((i % 20000) == 0) System.out.println(i);
        }
        // GET
        for (int i = 0; i < 100000; i++) {
          String testVal = (String)treeNonHeapMap.get(key + i);
          if (testVal == null || !testVal.equals(val + i)) result = false;
        }
        // REMOVE
        for (int i = 0; i < 50000; i++) {
          treeNonHeapMap.remove(key + i);
          if ((i % 10000) == 0) System.out.println(i);
        }
        // REMOVE -> GET
        for (int i = 0; i < 50000; i++) {
          String testVal = (String)treeNonHeapMap.get(key + i);
          if (testVal != null) result = false;;
        }
        val = "valuevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevalue";
        // Larget String PUT
        for (int i = 0; i < 50000; i++) {
          treeNonHeapMap.put(key + i, val+val+ i);
          if ((i % 10000) == 0) System.out.println(i);
        }
        // GET
        for (int i = 0; i < 50000; i++) {
          String testVal = (String)treeNonHeapMap.get(key + i);
          if (testVal == null || !testVal.equals(val+val + i)) result = false;
        }
        // containsKey
        for (int i = 0; i < 100000; i++) {
          if(!treeNonHeapMap.containsKey(key + i)) result = false;
        }
        // SIZE
        System.out.println("size() = [" + treeNonHeapMap.size() + "]");
        if (treeNonHeapMap.size() != 100000L)  result = false;


        //　カーソル 昇順・降順テスト
        System.out.println("Tree - Test - coursour - 1");
        treeNonHeapMap.clear();
        for (int i = 0; i < 500; i++) {

          treeNonHeapMap.put(i, val + i);
          if ((i % 20000) == 0) System.out.println(i);
        }

        for (int tt = 0; tt < 10; tt++) {
          if ((tt % 2) == 0) {
            System.out.println("Tree - Test - coursour - 2");
            treeNonHeapMap.resetCursor();
            System.out.println("Tree - Test - coursour - 3");
            Integer beforKey = null;
            Integer nowKey = null;
            int objectCount = 0;
            for (Object[] obj = (Object[])treeNonHeapMap.next(); obj != null; obj = (Object[])treeNonHeapMap.next()) {
              if (beforKey == null) {
                beforKey = (Integer)obj[0];
              } else {
                nowKey = (Integer)obj[0];
                if (beforKey.intValue() >= nowKey.intValue()) {
                  System.out.println("昇順 - error");
                  result = false;
                  break;
                  //System.out.println("key=" + obj[0]); // key
                  //System.out.println("value=" + obj[1].toString()); // value
                } else {
                  //System.out.println("key=" + obj[0]); // key
                  //System.out.println("value=" + obj[1].toString()); // value
                }
                beforKey = nowKey;
              }
              objectCount++;
            }
            if (objectCount != 500) result = false;
            System.out.println("Tree - Test - coursour - 4" + " obj count=" + objectCount);
          } else {
            // Tree カーソル　降順
            System.out.println("Tree - Test - coursour - 2 - 降順");
            treeNonHeapMap.resetDescCursor();
            System.out.println("Tree - Test - coursour - 3");
            Integer beforKey = null;
            Integer nowKey = null;
            int objectCount = 0;

            for (Object[] obj = (Object[])treeNonHeapMap.next(); obj != null; obj = (Object[])treeNonHeapMap.next()) {
              if (beforKey == null) {
                beforKey = (Integer)obj[0];
              } else {
                nowKey = (Integer)obj[0];
                if (beforKey.intValue() <= nowKey.intValue()) {
                  System.out.println("降順 - error");
                  result = false;
                  break;
                  //System.out.println("key=" + obj[0]); // key
                  //System.out.println("value=" + obj[1].toString()); // value
                } else {
                  //System.out.println("key=" + obj[0]); // key
                  //System.out.println("value=" + obj[1].toString()); // value
                }
                beforKey = nowKey;
              }
              objectCount++;
            }
            if (objectCount != 500) result = false;
            System.out.println("Tree - Test - coursour - 4" + " obj count=" + objectCount);
          }
        }

        assertTrue( result );
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
}
