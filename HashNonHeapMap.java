import java.nio.*;
import java.util.concurrent.atomic.AtomicLong;

public class HashNonHeapMap implements NonHeapMap {

	private AtomicLong totalSize = new AtomicLong(0L);

	static int NODE_TYPE = 1;
	protected int parallelFactor = 256;
	Node[] nodeList = new Node[parallelFactor];

	private int cursorIndex = -1;
	private Node cursorNode = null;

//protected ObjectI] memoryLock - new Object (2561;

	public HashNonHeapMap() {}

	public long size() {
		return totalSize.get();
	}

	public void incrementAndGet() {
		totalSize.incrementAndGet();
	}

	public void decrementAndGet() {
		totalSize.decrementAndGet();
	}

	public void resetCursor() {
		cursorIndex = -1;
		cursorNode = null;
	}

	public Object[] next() {
		if (cursorNode == null) {
			cursorIndex++;
			if (cursorIndex == nodeList.length) return null;
			cursorNode = nodeList[cursorIndex];
			if (cursorNode != null) cursorNode.resetCursor();
			return next();
		}

		Object[] returnObj = cursorNode.next();
		if (returnObj == null) {
			cursorNode = null;
			return next();
		}
		return returnObj;
	}

	public void put(String key, String value) {
		String str = (String)key.toString();
		int hashIndex = createHashIndex(str);
		byte[] dataBytes = str.getBytes();

		//データを登録
		if (nodeList[hashIndex] != null) {
			nodeList[hashIndex].putData(str, value);
		} else {
			Node node = new Node(NODE_TYPE, this);
			node.setParentNode(new ParentNode());
			node.putData(str, value);
			nodeList[hashIndex] = node;
		}
	}

	public String get(String key) {
		String str = (String)key.toString();
		int hashIndex = createHashIndex(str);
		byte[] dataBytes = str.getBytes();

		if (nodeList[hashIndex] != null) {
			return (String)nodeList[hashIndex].getData(str);
		}
		return null;
	}

	public String remove(String key) {
		String str = (String)key.toString();
		int hashIndex = createHashIndex(str);
		byte[] dataBytes = str.getBytes();

		if (nodeList[hashIndex] != null) {
			return (String)nodeList[hashIndex].removeData(str);
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