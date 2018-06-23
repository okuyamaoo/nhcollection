### version-0.0.2
* version0.0.2ではValueをオフヒープに保存する連想配列をリリースします。
  * version0.0.2ではSerialize可能な値はValueに入れ、登録時の型でgetメソッドから返却されるようになりました。
    オフヒープにはSerializeされて保存されます。
```
import nhcollection.util.*;
~
~
~
// オフヒープを利用したMapを初期化
HashNonHeapMap<String, String> nonHeapMap = new HashNonHeapMap(); 

// 値を登録
nonHeapMap.put("key1", "value1");
nonHeapMap.put("key2", "value2");
nonHeapMap.put("key3", "value3");

// 値を取得
String value = nonHeapMap.put("key1");

// 値を削除
nonHeapMap.remove("key1");

// ループ
nonHeapMap.resetCursor();
for (Object[] obj = nonHeapMap.next(); obj != null; obj = nonHeapMap.next()) {
  //System.out.println("key=" + obj[0]); // key
  //System.out.println("value=" + obj[1].toString()); // value
}

// HashMapを保存し取り出して表示
Map tmpMap = new HashMap();
tmpMap.put("key", "value");

nonHeapMap.put("key4", tmpMap);
Map getResult = (Map)nonHeapMap.get("key4");

System.out.println(getResult);
```
