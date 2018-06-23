i# nhcollection
### nhcollectionはJavaのオフヒープ利用ライブラリです
* Javaヒープではなくオフヒープを利用することでGCの管理外のメモリにデータ置くことが可能です。
ただしオフヒープを使った場合、保存可能なデータはbyte配列になることや領域の確保、開放を独自に実装することが必要となるため、実装が難しくなります。
* そこでnhcollectionではそういった手間をかけずに手軽にオフヒープを活用できることを目指します。

### なぜオフヒープを使うのか？
* 通常のヒープ領域はGCの管理下にあるため短命なオブジェクトや長寿命のオブジェクトも同様に管理されています。そのため大容量メモリを搭載したサーバが主流な現在GCへの負荷も高まっています。そこで大きなデータや大量のデータをなるべくGC管理下から外しGC負荷の低減、アプリケーション処理の効率化を目指して開発しています。

### version-0.0.3
* version0.0.3では自然順序にて値を取得可能なTreeNonHeapMapをリリースします。
　また、java.util.TreeMapは保持可能な最大件数はIntegerの最大値数ですが、TreeNonHeapMapはメモリが許す限りそれ以上の件数を保持することも可能です。
  ValueはHashNonHeapMapと同様にオフヒープに保存されるため例えばKeyにインクリメンタルな数値を指定し、20億〜行のログを並列スレッドで登録し
  昇順/降順で取り出すなどが可能です。get/put/removeなどはHashNonHeapMapと同様の使い勝手です。
* HashNonHeapMapにclearメソッドを追加し、Buffer領域のサイズを初期化時に指定し最適化可能としました。TreeNonHeapMapも同様です。
```
import nhcollection.util.*;
~
~
~
// 自然順序にてアクセス可能なMapを初期化
TreeNonHeapMap treeNonHeapMap = new TreeNonHeapMap();

// 値を登録
treeNonHeapMap.put("key1", "value1");
treeNonHeapMap.put("key2", "value2");
treeNonHeapMap.put("key3", "value3");
treeNonHeapMap.put("key4", "value3");

// 昇順ループ
treeNonHeapMap.resetCursor();
for (Object[] obj = treeNonHeapMap.next(); obj != null; obj = treeNonHeapMap.next()) {
  System.out.println("key=" + obj[0]); // key
  System.out.println("value=" + obj[1]); // value
}

// 降順ループ
treeNonHeapMap.resetDescCursor();
for (Object[] obj = treeNonHeapMap.next(); obj != null; obj = treeNonHeapMap.next()) {
  System.out.println("key=" + obj[0]); // key
  System.out.println("value=" + obj[1]); // value
}
```
