package nhcollection.util;

import java.nio.*;
import java.util.concurrent.atomic.AtomicLong;

public class HashNonHeapMap implements NonHeapMap {

	private AtomicLong totalSize = new AtomicLong(0L);

	static int NODE_TYPE = 1;
	protected int parallelFactor = 256;
	Node[] nodeList = new Node[parallelFactor];

	private int cursorIndex = -1;
	private Node cursorNode = null;

	protected Integer[] memoryLock = new Integer[parallelFactor];

	public HashNonHeapMap() {
		for (int idx = 0; idx < parallelFactor; idx++) {
			memoryLock[idx] = new Integer(idx);
		}
	}

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

		public void put(Object key, String value) {
			String str = key.toString();
			int hashIndex = createHashIndex(str);

			synchronized(memoryLock[hashIndex]) {

				//データを登録
				if (nodeList[hashIndex] != null) {
					nodeList[hashIndex].putData(key, value);
				} else {
					Node node = new Node(NODE_TYPE, this);
					node.setParentNode(new ParentNode());
					node.putData(key, value);
					nodeList[hashIndex] = node;
				}
			}
		}

		public String get(Object key) {
			String str = key.toString();
			int hashIndex = createHashIndex(str);

			synchronized(memoryLock[hashIndex]) {
				if (nodeList[hashIndex] != null) {
					return (String)nodeList[hashIndex].getData(key);
				}
			}
			return null;
		}

		public String remove(Object key) {
			String str = key.toString();
			int hashIndex = createHashIndex(str);

			synchronized(memoryLock[hashIndex]) {
				if (nodeList[hashIndex] != null) {
					return (String)nodeList[hashIndex].removeData(key);
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
