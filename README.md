# nhcollection
### nhcollectionはJavaのオフヒープ利用ライブラリです
* Javaヒープではなくオフヒープを利用することでGCの管理外のメモリにデータ置くことを可能にします。
ただしオフヒープを使った場合保存可能なデータはbyte配列になることや領域の確保、開放を独自に実装することが必要となるため、実装が難しくなります。
* そこでnhcollectionではそういった手間をかけずに手軽にオフヒープを活用できることを目指します。

### なぜオフヒープを使うのか？
* 通常のヒープ領域はGCの管理下にあるため短命なオブジェクトや長寿命のオブジェクトも同様に管理されています。そのため大容量メモリを搭載したサーバが主流な現在GCへの負荷も高まっています。そこで大きなデータや大量のデータをなるべくGC管理下から外しGC負荷の低減、アプリケーション処理の効率化を目指して開発しています。

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

```
