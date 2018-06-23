package nhcollection.util;

import java.util.*;
import java.io.*;

public class Node {

	public static long myInstatnceCnt[];
	protected ParentNode parentNode = null;
	protected Map<Object, int[]> keyMap;
	private NonHeapMap masterMap;
	private Iterator cursorIte = null;
	private boolean cursorDescOrder = false;
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
		cursorDescOrder = false;
		cursorIte = null;
	}

	public void resetCursor(boolean descOrder) {
		if (descOrder) {
			cursorDescOrder = true;
		} else{
			cursorDescOrder = false;
		}
		cursorIte = null;
	}


	private void coursourNext() {
		if (cursorIte == null) {
			if (!cursorDescOrder) {
				cursorIte = keyMap.keySet().iterator();
			} else {
				if (NODE_TYPE == 2) {
					// TreeMap指定で降順指定の場合は降順のKeySetからIteratorを取り出す
					cursorIte = ((TreeMap)keyMap).descendingKeySet().iterator();
				} else {
					// 通常のHashMapの場合は降順のKeySetは無効
					cursorIte = keyMap.keySet().iterator();
				}
			}
		}
	}

	public Object[] next()  throws IOException, ClassNotFoundException {
		coursourNext(); // カーソルを進める

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

	public Object nextKey() {
		coursourNext(); // カーソルを進める

		if(cursorIte.hasNext()) {
			Object key = cursorIte.next();
			return key;
		} else {
			cursorIte = null;
			return null;
		}
	}

	//仮にObjectはStringとする
	public void putData(Object obj, Object value) throws IOException {
		int[] exsistData = keyMap.get(obj);
		byte[] data = serialize(value);
		if (exsistData != null) removeData(obj);

		masterMap.incrementAndGet();
		keyMap.put(obj, parentNode.writeData(data));
	}

	public Object getData(Object obj) throws IOException, ClassNotFoundException {
		int[] key = keyMap.get(obj);
		byte[] nodeResult = null;
		if (key != null) {
			nodeResult = parentNode.readData(key);
			if (nodeResult == null) return null;
			return deserialize(nodeResult);
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

	public boolean hasData(Object obj) {
		int[] key = keyMap.get(obj);
		if (key != null) return true;
		return false;
	}

	public int size() {
		return keyMap.size();
	}

	private byte[] serialize(Object obj) throws IOException {
		byte type = 0;
		byte[] result = null;
		byte[] dataBytes = null;
		if (obj instanceof String) {
			type = 1;
		} else if (obj instanceof Integer) {
			type = 2;
		} else if (obj instanceof Long) {
			type = 3;
		} else if (obj instanceof Double) {
			type = 4;
		} else if (obj instanceof Short) {
			type = 5;
		} else if (obj instanceof Float) {
			type = 6;
		} else if (obj instanceof Boolean) {
			type = 7;
		} else if (obj instanceof Character) {
			type = 8;
		} else if (obj instanceof Byte) {
			type = 9;
		} else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			dataBytes = baos.toByteArray();
		}

		if (type > 0 &&  type < 10) {
			dataBytes = obj.toString().getBytes();
		}

		result = new byte[1 + dataBytes.length];
		result[0] = type;
		System.arraycopy(dataBytes, 0, result, 1, dataBytes.length);

		return result;
	}

	private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		Object result = null;
		byte[] resultBytes = null;
		if (data[0] == 0) {
			resultBytes = new byte[data.length - 1];
			System.arraycopy(data, 1, resultBytes, 0, resultBytes.length);
			result = new ObjectInputStream(new ByteArrayInputStream(resultBytes)).readObject();
		} else if (data[0] == 1) {
			resultBytes = new byte[data.length - 1];
			System.arraycopy(data, 1, resultBytes, 0, resultBytes.length);
			result = new String(resultBytes);
		} else if (data[0] == 2) {
			resultBytes = new byte[data.length - 1];
			System.arraycopy(data, 1, resultBytes, 0, resultBytes.length);
			result = new Integer(new String(resultBytes));
		} else if (data[0] == 3) {
			resultBytes = new byte[data.length - 1];
			System.arraycopy(data, 1, resultBytes, 0, resultBytes.length);
			result = new Long(new String(resultBytes));
		} else if (data[0] == 4) {
			resultBytes = new byte[data.length - 1];
			System.arraycopy(data, 1, resultBytes, 0, resultBytes.length);
			result = new Double(new String(resultBytes));
		} else if (data[0] == 5) {
			resultBytes = new byte[data.length - 1];
			System.arraycopy(data, 1, resultBytes, 0, resultBytes.length);
			result = new Short(new String(resultBytes));
		} else if (data[0] == 6) {
			resultBytes = new byte[data.length - 1];
			System.arraycopy(data, 1, resultBytes, 0, resultBytes.length);
			result = new Float(new String(resultBytes));
		} else if (data[0] == 7) {
			resultBytes = new byte[data.length - 1];
			System.arraycopy(data, 1, resultBytes, 0, resultBytes.length);
			result = new Boolean(new String(resultBytes));
		} else if (data[0] == 8) {
			resultBytes = new byte[data.length - 1];
			System.arraycopy(data, 1, resultBytes, 0, resultBytes.length);
			result = new Character((new String(resultBytes)).charAt(0));
		} else if (data[0] == 9) {
			resultBytes = new byte[data.length - 1];
			System.arraycopy(data, 1, resultBytes, 0, resultBytes.length);
			result = new Byte(new String(resultBytes));
		}




		return result;
	}
}
