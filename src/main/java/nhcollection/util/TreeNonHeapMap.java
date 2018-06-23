package nhcollection.util;

import java.nio.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 登録した値を自然順序の昇順、降順で取り出すことが可能な連想配列実装.<br>
 * Key,Valueを指定することでKeyはJavaヒープに、Valueはオフヒープに保存されます<br>
 * 内部実装としてはTreeMapを利用しています<br>
 * ただし複数のTreeMapを統合して利用し値を保持可能なため通常のTreeMap以上の件数を保持可能です<br>
 *<br>
 * また、スレッドセーフとして実装されているためマルチスレッド環境で利用可能です<br>
 * ただし、カーソル処理であるresetCursor()、resetDescCursor()、next()メソッド呼び出し時に並列スレッドにてアクセスを行うと
 * 意図しない動作が発生します<br>
 * resetCursor()を呼び出し、next()メソッドにて値を取得するとKeyの昇順で値が順次取得可能です<br>
 * resetDescCursor()を呼び出し、next()メソッドにて値を取得するとKeyの降順で値が順次取得可能です<br>
 *<br>
 * また、Key値となるObjectは順序を保持するため順序比較が不可能な値を混在して登録することはできません<br>
 * put()メソッドは指定されたValueがシリアライズして保存される。取得時はデシリアライズされるため、型情報を維持します。
 * そのためSerializableな型である必要があります。一部基本型(String, Byte，Short，Integer，Long，Float，Double，Character，Boolean)に関してはtoStringを行って保存されます。<br>
 *<br>
 * 予め保存するValueの値が予想可能な場合はそのバイトサイズをコンストラクタに指定するとメモリ効率が上がります<br>
 *
 * @license Apache v2 License
 * @author okuyamaoo
 */
public class TreeNonHeapMap extends HashNonHeapMap {

	private TreeMap<Object, Integer> cursorNextCandidates = null;
	private boolean cursourDescOrder = false;

	/**
	 * コンストラクタ.<br>
	 * 1データサイズはbyteサイズで256byteに最適化される<br>
	 */
	public TreeNonHeapMap() {
		super(256);
		super.NODE_TYPE = 2;
	}

	/**
	 * コンストラクタ.<br>
	 * 1データサイズのbyteサイズを指定することが可能<br>
	 *
	 * @param valueByteSize 予想される保存するValueのbyteサイズを指定
	 */
	public TreeNonHeapMap(int valueByteSize) {
		super(valueByteSize, 4096, 4096);
		super.NODE_TYPE = 2;
	}

	/**
	 * ループ処理にて全ての要素<KEY, VALUE>を降順にて取得する際にループ前に一度呼び出す初期化メソッド<br>
	 * 呼び出し間にカーソルの位置が初期化され、1件目からの取得が可能<br>
	 * 本メソッド呼び出し時は他スレッドからの操作をブロックすること<br>
	 *
	 */
	public void resetDescCursor() {
		cursourDescOrder = true;
		resetOrderCursor();
	}

	/**
	 * ループ処理にて全ての要素<KEY, VALUE>を昇順にて取得する際にループ前に一度呼び出す初期化メソッド<br>
	 * 呼び出し間にカーソルの位置が初期化され、1件目からの取得が可能<br>
	 * 本メソッド呼び出し時は他スレッドからの操作をブロックすること<br>
	 *
	 */
	public void resetCursor() {
		cursourDescOrder = false;
		resetOrderCursor();
	}

	/**
	 * ループ処理にて全ての要素<KEY, VALUE>を取得する際にループ前に一度呼び出す初期化メソッド<br>
	 * 呼び出し間にカーソルの位置が初期化され、1件目からの取得が可能<br>
	 * 本メソッド呼び出し時は他スレッドからの操作をブロックすること<br>
	 *
	 */
	private void resetOrderCursor() {

		cursorNextCandidates = new TreeMap();
		for (int i = 0; i < nodeList.length; i++) {

			if(nodeList[i] != null) {
				nodeList[i].resetCursor(cursourDescOrder);
				Object keyObj = nodeList[i].nextKey();
				if (keyObj != null) {
					cursorNextCandidates.put(keyObj, i);
				}
			}
		}
	}

	/**
	 * ループ処理にて全ての次の要素<KEY, VALUE>を取得する.<br>
	 * 要素が存在しない場合はnullが返却される<br>
	 * 呼び出し間にカーソルの位置が1件進む<br>
	 * 返却される要素はObjectの配列となり、インデックス0がKey値(Object型 putメソッドへ渡したKeyのObject)、インデックス1がValue値(putメソッドへ渡したValue値の型及び値)<br>
	 * 本メソッド呼び出し時は他スレッドからの操作をブロックすること<br>
	 *
	 * @return インデックス0がKey値(Object型 putメソッドへ渡したKeyのObject)、インデックス1がValue値(putメソッドへ渡したValue値の型及び値)
	 */
	public Object[] next() throws IOException, ClassNotFoundException {
		if(cursorNextCandidates.size() == 0) return null;
		Object key = null;
		Integer keyInNodeIdx = null;
		if (!cursourDescOrder) {
			key = cursorNextCandidates.firstKey();
		} else {
			key = cursorNextCandidates.lastKey();
		}

		keyInNodeIdx = cursorNextCandidates.get(key);
		Object value = super.get(key);
		Object[] result = new Object[2];
		result[0] = key;
		result[1] = value;

		// 次の候補は今回の返却対象となったNodeから取り出して候補Mapへ格納
		cursorNextCandidates.remove(key);
		Object nextKey = nodeList[keyInNodeIdx.intValue()].nextKey();
		if (nextKey != null) {
			cursorNextCandidates.put(nextKey, keyInNodeIdx);
		}
		return result;
	}
}
