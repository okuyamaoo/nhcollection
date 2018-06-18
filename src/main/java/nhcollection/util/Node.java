package nhcollection.util;

import java.util.*;

public class Node {

	public static long myInstatnceCnt[];
	protected ParentNode parentNode = null;
	protected Map<Object, int[]> keyMap;
	private NonHeapMap masterMap;
	private Iterator cursorIte = null;
	private int NODE_TYPE = 0;

	public Node(int nodeType, NonHeapMap masterMap) {
		this.masterMap = masterMap;

		if (nodeType == 1) {
			NODE_TYPE = 1;
			keyMap = new HashMap(128);
		} else if (nodeType == 2) {
			NODE_TYPE = 2;
			keyMap = new TreeMap();
		}
	}

	public void setParentNode(ParentNode pNode) {
		parentNode = pNode;
	}

	public ParentNode getParentNode() {
		return parentNode;
	}

	public void resetCursor() {
		cursorIte = null;
	}

	public Object[] next() {
		if (cursorIte == null) cursorIte = keyMap.keySet().iterator();
		if(cursorIte.hasNext()) {
			Object key = cursorIte.next();
			Object value = getData(key);
			Object[] result = new Object[2];
			result[0] = key;
			result[1] = value;
			return result;
		} else {
			cursorIte = null;
			return null;
		}
	}

	//仮にObjectはStringとする
	public void putData(Object obj, String value) {
		int[] exsistData = keyMap.get(obj);
		if (exsistData != null) removeData(obj);

		masterMap.incrementAndGet();
		keyMap.put(obj, parentNode.writeData(value.getBytes()));
	}



	public String getData(Object obj) {
		int[] key = keyMap.get(obj);
		byte[] nodeResult = null;
		if (key != null) {
			nodeResult = parentNode.readData(key);
			if (nodeResult == null) return null;
			return new String(nodeResult);
		}
		return null;
	}

	// 登録されたkeyを探しデータを削除する
	public String removeData(Object obj) {

		int[] key = keyMap.remove(obj);
		if (key != null) {
			masterMap. decrementAndGet();
			Object removeResult = parentNode.removeData(key);
			return null;
		}

		return null;
	}

	public int size() {
		return keyMap.size();
	}
}
