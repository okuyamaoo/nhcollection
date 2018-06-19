package nhcollection.util;

import java.io.*;
import java.nio.*;
import java.util.concurrent.atomic.AtomicLong;

public interface NonHeapMap {
	public long size();

	public void incrementAndGet();
	public void decrementAndGet();

	public void resetCursor();
	public Object[] next() throws IOException, ClassNotFoundException;
}
