package nhcollection.util;

import java.nio.*;
import java.io.*;
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
 * put()メソッドは指定されたValueがシリアライズして保存される。取得時はデシリアライズされるため、型情報を維持します。
 * そのためSerializableな型である必要があります。一部基本型(String, Byte，Short，Integer，Long，Float，Double，Character，Boolean)に関してはtoStringを行って保存されます。<br>
 *<br>
 * 予め保存するValueの値が予想可能な場合はそのバイトサイズをコンストラクタに指定するとメモリ効率が上がります<br>
 *
 * @license Apache v2 License
 * @author okuyamaoo
 */
public class HashNonHeapMap implements NonHeapMap {

	protected AtomicLong totalSize = new AtomicLong(0L);

	protected static int NODE_TYPE = 1;
	protected int parallelFactor = 256;
	protected Node[] nodeList = new Node[parallelFactor];

	protected int settingChunkSize = 	256;
	protected int settingNumberOfChunk = 4096;
	protected int settingRebuildExpansionFactor = settingNumberOfChunk;

	protected int cursorIndex = -1;
	protected Node cursorNode = null;

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
	 * 返却される要素はObjectの配列となり、インデックス0がKey値(Object型 putメソッドへ渡したKeyのObject)、インデックス1がValue値(putメソッドへ渡したValue値の型及び値)<br>
	 * 本メソッド呼び出し時は他スレッドからの操作をブロックすること<br>
	 *
	 * @return インデックス0がKey値(Object型 putメソッドへ渡したKeyのObject)、インデックス1がValue値(putメソッドへ渡したValue値の型及び値)
	 */
	public Object[] next() throws IOException, ClassNotFoundException {
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
	 * @param value　保存するValue値(String, Byte，Short，Integer，Long，Float，Double，Character，Boolean型以外はSerializableである必要がある)
	 * @throw NullPointerException keyもしくはvalueがnull
	 * @throw IOException ValueがSerialize出来ない
	 */
	public void put(Object key, Object value) throws NullPointerException, IOException {
		if (key == null || value == null) throw new NullPointerException("key or value is null");
		int hashIndex = createHashIndex(key);

		synchronized(memoryLock[hashIndex]) {

			//データを登録
			if (nodeList[hashIndex] != null) {
				nodeList[hashIndex].putData(key, value);
			} else {
				Node node = new Node(NODE_TYPE, this);
				node.setParentNode(new ParentNode(settingChunkSize, settingNumberOfChunk, settingRebuildExpansionFactor));
				node.putData(key, value);
				nodeList[hashIndex] = node;
			}
		}
	}

	/**
	 * Keyを指定しValueを取得する.<br>
	 * Keyがnullの場合NullPointerExceptionがthrowsされる<br>
	 * 値が存在しない場合はnullが返却される<br>
	 * Valueはputメソッド呼び出し時のValueの値が返却される<br>
	 *
	 * @param key 取得するKey値
	 * @return　保存されたValue値のtoString文字列
	 * @throw NullPointerException keyがnull
	 * @throw IOException ValueがSerialize出来ない
	 * @throw ClassNotFoundException valueの型復元に失敗
	 */
	public Object get(Object key) throws NullPointerException, IOException, ClassNotFoundException {
		if (key == null) throw new NullPointerException("key is null");
		int hashIndex = createHashIndex(key);

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
	 * @throw IOException ValueがSerialize出来ない
	 * @throw ClassNotFoundException valueの型復元に失敗
	 */
	public Object remove(Object key) throws NullPointerException, IOException, ClassNotFoundException {
		if (key == null) throw new NullPointerException("key is null");
		int hashIndex = createHashIndex(key);

		synchronized(memoryLock[hashIndex]) {
			if (nodeList[hashIndex] != null) {
				Object removeValue = nodeList[hashIndex].getData(key);
				nodeList[hashIndex].removeData(key);
				return removeValue;
			}
		}
		return null;
	}

	/**
	 * 指定したKeyの有無を返却する.<br>
	 * Keyがnullの場合NullPointerExceptionがthrowsされる<br>
	 * 値が存在する場合はtrueが存在しない場合はfalseが返却される<br>
	 *
	 * @param key Key値
	 * @return　true(Key有り)/false(Keyなし)
	 * @throw NullPointerException keyがnull
	 */
	public boolean containsKey(Object key) {
		if (key == null) throw new NullPointerException("key is null");
		int hashIndex = createHashIndex(key);

		synchronized(memoryLock[hashIndex]) {
			if (nodeList[hashIndex] != null) {
				return nodeList[hashIndex].hasData(key);
			}
		}
		return false;
	}

	/**
	 * 全ての要素を削除する.<br>
	 */
	public void clear() {
		nodeList = null;
		totalSize = new AtomicLong(0L);
		nodeList = new Node[parallelFactor];
	}

	private int createHashIndex(Object obj) {
		int hashCode = obj.hashCode();
		if (hashCode < 0) hashCode=-hashCode;

		return hashCode % parallelFactor;
	}

	public void dump() {}
}
