package nhcollection.util;

import java.nio.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hash値利用した連想配列実装.<br>
 * Key,Valueを指定することでKeyはJavaヒープに、Valueはオフヒープに保存されます<br>
 *<br>
 * また、スレッドセーフとして実装されているためマルチスレッド環境で利用可能です<br>
 * ただし、カーソル処理であるresetCursor()、next()メソッド呼び出し時に並列スレッドにてアクセスを行うと
 * 意図しない動作が発生します<br>
 *<br>
 * また、Key値となるObjectのhashCode()メソッドを使っているため、全て同一の値を返してくるhashCodeメソッドなどを
 * 実装したKey値を使うと処理効率が低下します<br>
 * 現時点でput()メソッドは指定されたValueのtoString()メソッドを呼び出した値を保存するため、get()メソッドの戻り値は
 * Stringのみとなります<br>
 *<br>
 * 予め保存するValueの値が予想可能な場合はそのバイトサイズをコンストラクタに指定するとメモリ効率が上がります<br>
 *
 * @license Apache v2 License
 * @author okuyamaoo
 */
public class HashNonHeapMap implements NonHeapMap {

	private AtomicLong totalSize = new AtomicLong(0L);

	static int NODE_TYPE = 1;
	protected int parallelFactor = 256;
	Node[] nodeList = new Node[parallelFactor];

	private int settingChunkSize = 	256;
	private int settingNumberOfChunk = 4096;
	private int settingRebuildExpansionFactor = settingNumberOfChunk;

	private int cursorIndex = -1;
	private Node cursorNode = null;

	protected Integer[] memoryLock = new Integer[parallelFactor];

	/**
	 * コンストラクタ.<br>
	 * 1データサイズはbyteサイズで256byteに最適化される<br>
	 */
	public HashNonHeapMap() {
		this(256);
	}

	/**
	 * コンストラクタ.<br>
	 * 1データサイズのbyteサイズを指定することが可能<br>
	 *
	 * @param valueByteSize 予想される保存するValueのbyteサイズを指定
	 */
	public HashNonHeapMap(int valueByteSize) {
		this(valueByteSize, 4096, 4096);
	}

	/**
	 * コンストラクタ.<br>
	 * 1データサイズが大きい場合や極端に小さい場合は本コンストラクタで初期化<br>
	 *
	 * @param valueByteSize 予想される保存するValueのbyteサイズを指定
	 * @param numberOfChunk ここで指定する数値×valueByteSize×(1~256)　バイトがデータ登録の過程で確保される
	 * @param rebuildExpansionFactor ヒープ外メモリ-が不足すると確保されるメモリ量を指定(計算式:valueByteSize * (numberOfChunk = numberOfChunk +  rebuildExpansionFactor)) * (1~256)
	 */
	public HashNonHeapMap(int valueByteSize, int numberOfChunk, int rebuildExpansionFactor) {
		settingChunkSize = valueByteSize;
		settingNumberOfChunk = numberOfChunk;
		settingRebuildExpansionFactor = rebuildExpansionFactor;
		for (int idx = 0; idx < parallelFactor; idx++) {
			memoryLock[idx] = new Integer(idx);
		}
	}

	/**
	 * 保存している要素数を取得<br>
	 *
	 * @return 要素数
	 */
	public long size() {
		return totalSize.get();
	}

	public void incrementAndGet() {
		totalSize.incrementAndGet();
	}

	public void decrementAndGet() {
		totalSize.decrementAndGet();
	}

	/**
	 * ループ処理にて全ての要素<KEY, VALUE>を取得する際にループ前に一度呼び出す初期化メソッド<br>
	 * 呼び出し間にカーソルの位置が初期化され、1件目からの取得が可能<br>
	 * 本メソッド呼び出し時は他スレッドからの操作をブロックすること<br>
	 *
	 */
	public void resetCursor() {
		cursorIndex = -1;
		cursorNode = null;
	}

	/**
	 * ループ処理にて全ての次の要素<KEY, VALUE>を取得する.<br>
	 * 要素が存在しない場合はnullが返却される<br>
	 * 呼び出し間にカーソルの位置が1件進む<br>
	 * 返却される要素はObjectの配列となり、インデックス0がKey値(Object型 putメソッドへ渡したKeyのObject)、インデックス1がValue値(String型 putメソッドへ渡したValuのObjectのtoString値)<br>
	 * 本メソッド呼び出し時は他スレッドからの操作をブロックすること<br>
	 *
	 * @return インデックス0がKey値(Object型 putメソッドへ渡したKeyのObject)、インデックス1がValue値(String型 putメソッドへ渡したValuのObjectのtoString値)
	 */
	public Object[] next() {
		if (cursorNode == null) {
			cursorIndex++;
			if (cursorIndex == nodeList.length) return null;
			cursorNode = nodeList[cursorIndex];
			if (cursorNode != null) cursorNode.resetCursor();
			return next();
		}
		Object[] returnObj = null;
		synchronized(memoryLock[cursorIndex]) {
			returnObj = cursorNode.next();
		}
		if (returnObj == null) {
			cursorNode = null;
			return next();
		}
		return returnObj;
	}

	/**
	 * Key,Valueを保存する.<br>
	 * Keyがnull,Valueがnullの場合NullPointerExceptionがthrowsされる<br>
	 * keyは引き渡されたインスタンスが保存され、ValueはtoStringメソッドの結果が保存される<br>
	 *
	 *
	 * @param key 保存するKey値
	 * @param value　保存するValue値(実際に保存されるのはtoStringの値)
	 * @throw NullPointerException keyもしくはvalueがnull
	 */
	public void put(Object key, Object value) {
		if (key == null || value == null) throw new NullPointerException("key or value is null");
		String valueStr = value.toString();
		String str = key.toString();
		int hashIndex = createHashIndex(str);

		synchronized(memoryLock[hashIndex]) {

			//データを登録
			if (nodeList[hashIndex] != null) {
				nodeList[hashIndex].putData(key, valueStr);
			} else {
				Node node = new Node(NODE_TYPE, this);
				node.setParentNode(new ParentNode(settingChunkSize, settingNumberOfChunk, settingRebuildExpansionFactor));
				node.putData(key, valueStr);
				nodeList[hashIndex] = node;
			}
		}
	}

	/**
	 * Keyを指定しValueを取得する.<br>
	 * Keyがnullの場合NullPointerExceptionがthrowsされる<br>
	 * 値が存在しない場合はnullが返却される<br>
	 * Valueはputメソッド呼び出し時のValueのtoStringメソッドの結果が返却される<br>
	 *
	 * @param key 取得するKey値
	 * @return　保存されたValue値のtoString文字列
	 * @throw NullPointerException keyがnull
	 */
	public String get(Object key) {
		if (key == null) throw new NullPointerException("key is null");
		String str = key.toString();
		int hashIndex = createHashIndex(str);

		synchronized(memoryLock[hashIndex]) {
			if (nodeList[hashIndex] != null) {
				return nodeList[hashIndex].getData(key);
			}
		}
		return null;
	}

	/**
	 * Keyを指定し要素を削除する.<br>
	 * Keyがnullの場合NullPointerExceptionがthrowsされる<br>
	 * 値が存在しない場合はnullが返却される<br>
	 * 削除が出来た場合は削除したValueが返却される<br>
	 *
	 * @param key 削除するKey値
	 * @return　削除が成功した場合は保存されていたValue値のtoString文字列。要素が存在しない場合はnull
	 * @throw NullPointerException keyがnull
	 */
	public String remove(Object key) {
		if (key == null) throw new NullPointerException("key is null");
		String str = key.toString();
		int hashIndex = createHashIndex(str);

		synchronized(memoryLock[hashIndex]) {
			if (nodeList[hashIndex] != null) {
				String removeValue = nodeList[hashIndex].getData(key);
				nodeList[hashIndex].removeData(key);
				return removeValue;
			}
		}
		return null;
	}

	private int createHashIndex(String str) {
		int hashCode = str.hashCode();
		if (hashCode < 0) hashCode=-hashCode;

		return hashCode % parallelFactor;
	}

	public void dump() {}
	}
