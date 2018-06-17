import java.util.*;


public class Node {

	public static long myInstatnceCnt[];
	protected ParentNode parentNode = null;
	protected Map<Object, int[]> keyMap;
	private NonHeapMap masterMap;
	private Iterator cursorIte = null;

	public Node(int nodeType, NonHeapMap masterMap) {
		this.masterMap = masterMap;
		if (nodeType == 1) {
			keyMap = new HashMap();
		} else if (nodeType == 2) {
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
			result[1] = result;
			return result;
		} else {
			cursorIte = null;
			return null;
		}
	}

	//仮にObjectはStringとする
	public void putData(Object obj, Object value) {
		int[] exsistData = keyMap.get(obj);
		if (exsistData != null) removeData(obj);

		masterMap.incrementAndGet();
		keyMap.put(obj, parentNode.writeData(((String)value).getBytes()));
	}



	public Object getData(Object obj) {
		int[] key = keyMap.get(obj);
		if (key != null) return parentNode.readData(key);
		return null;
	}

	// 登録されたkeyを探しデータを削除する
	public Object removeData(Object obj) {

		int[] key = keyMap.remove(obj);
		if (key != null) {
			masterMap. decrementAndGet();
			return parentNode.removeData(key);
		}
		
		return null;
	}

	public int size() {
		return keyMap.size();
	}
}