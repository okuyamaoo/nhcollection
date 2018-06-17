public class Test {

	public static void main(String[] args) {
		HashNonHeapMap nonHeapMap = new HashNonHeapMap();
		
		nonHeapMap.put("test001", "aaaaa");
		nonHeapMap.put("test002", "bbbbb");
		System.out.println(nonHeapMap.get("test001"));
		System.out.println(nonHeapMap.get("test002"));
		String key = "key";
		String val = "Avaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevalue";
		for (int i = 0; i < 200000; i++) {
			nonHeapMap.put(key + i, val + i);
			if ((i % 100000) == 0) System.out.println(i);
		}

		for (int i = 0; i < 200000; i++) {
			String testVal = nonHeapMap.get(key + i);
			if (testVal == null || !testVal.equals(val + i)) System.out.println("err");
		}

		for (int i = 0; i < 50000; i++) {
			nonHeapMap.remove(key + i);
			if ((i % 10000) == 0) System.out.println(i);
		}
		for (int i = 0; i < 50000; i++) {
			String testVal = nonHeapMap.get(key + i);
			if (testVal != null) System.out.println("err");
		}
		val = "valuevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevaluevalue";
		for (int i = 0; i < 50000; i++) {
			nonHeapMap.put(key + i, val+val+ i);
			if ((i % 10000) == 0) System.out.println(i);
		}
		for (int i = 0; i < 50000; i++) {
			String testVal = nonHeapMap.get(key + i);
			if (testVal == null || !testVal.equals(val+val + i)) System.out.println(testVal);
		}

	}
}