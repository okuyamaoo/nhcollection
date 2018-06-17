import java.nio.*;
import java.lang.reflect.*;

public class NHBuffer { 

	// Heap外メモリ
	// メモリ上に128バイトで1データとした領域を使いそこにデータを格納する
	protected ByteBuffer buf = null;
	private ByteBuffer oldBuf = null;
	private int nowLastWritePoinst = 0;

	protected int chunkSize = 128;
	protected int numberOfChunk = 256;
	protected int rebuildExpansionFactor = 1024;
	protected int bufferSize = chunkSize * numberOfChunk;
	protected int[] removeAddrList = null;
	protected int[][] addConversionList = null;

	//設定デフォルト
	public NHBuffer() {
		buf = ByteBuffer.allocateDirect(bufferSize);
	}

	//設定設定
	public NHBuffer(int settingChunkSize, int settingNumberOfChunk, int settingRebuildExpansionFactor) {
		chunkSize = settingChunkSize;
		numberOfChunk = settingNumberOfChunk;
		rebuildExpansionFactor =settingRebuildExpansionFactor;

		bufferSize = chunkSize * numberOfChunk;

		buf = ByteBuffer.allocateDirect(bufferSize);
	}

	protected int[] writeData(byte[] data) {
		int reUseBufferPoint = getReuseBuffer();
		if(reUseBufferPoint < 0 && !isMyBufferMargin()) rebuildMyBuffer();
		int writePoint = nowLastWritePoinst;

		// 再利用チャンクがあれば利用
		if (reUseBufferPoint > -1) writePoint = reUseBufferPoint;

		buf.position(writePoint*chunkSize);
		buf.put(data, 0, data.length);
		int[] ret = new int[2];
		ret[0] = writePoint;
		ret [1] = data.length;

		// 再利用Chunkを使わずに後続に書き出した場合は最終ポイントを1つ進める
		if (reUseBufferPoint < 0) nowLastWritePoinst = nowLastWritePoinst + 1;
		return ret;
	}

	protected int[] writeData(int point, byte[] data) {
		buf.position(point * chunkSize);
		buf.put(data, 0, data.length);
		int[] ret = new int[2];
		ret[0] = point;
		ret[1] = data.length;

		return ret;
	}

	protected byte[] readData(int[] point) {
		byte[] data = new byte[point[1]];
		buf.position(point[0] * chunkSize);
		buf.get(data);

		return data;
	}

	protected void removeData(int point) {
		if (removeAddrList == null) {
			removeAddrList = new int[5];
			removeAddrList[0] =-1;
			removeAddrList[1] =-1;
			removeAddrList[2] =-1;
			removeAddrList[3] =-1;
			removeAddrList[4] =-1;
		}

		boolean removeRegist = false;
		for (int i = 0; i < removeAddrList.length; i++) {
			if (removeAddrList[i] == -1) {
				removeRegist = true;
				removeAddrList[i] = point;
				break;
			}
		}


		if (!removeRegist) {
			int rebuildFactor = 5;
			int[] newRemoveAddrList = new int[removeAddrList.length + rebuildFactor];
			int i= 0;
			for ( ; i < removeAddrList.length; i++) {
				newRemoveAddrList[i] = removeAddrList[i];
			}
			for ( ; i < removeAddrList.length; i++) {
				newRemoveAddrList[i] = -1;
			}

			newRemoveAddrList[removeAddrList.length] = point;
			removeAddrList = newRemoveAddrList;
		}
	}


	protected int getReuseBuffer() {
		if (removeAddrList == null) return -1;

		for (int i = 0; i < removeAddrList.length; i++) {
			if (removeAddrList[i] > -1) {
				int reusePoint = removeAddrList[i];
				removeAddrList[i] = -1;
				return reusePoint;
			}
		}
		removeAddrList = null;
		return -1;
	}

	//自身のByteBufferがまだデータを格納出来るか確認
	protected boolean isMyBufferMargin() {
		if (nowLastWritePoinst < numberOfChunk) return true;
		return false;
	}

	// 自身のByteBufferを拡張
	protected boolean rebuildMyBuffer() {

		numberOfChunk = numberOfChunk + rebuildExpansionFactor;
		ByteBuffer newBuf = ByteBuffer.allocateDirect(chunkSize * numberOfChunk);
		buf.position(0);
		newBuf.put(buf);

		oldBuf = buf;
		buf = newBuf;
		releaseBuffer();
		return true;
	}

	private void releaseBuffer() {
		try {
			Method cleaner = oldBuf.getClass().getMethod("cleaner");
			cleaner.setAccessible(true);

			Object cleanerObj = cleaner.invoke(oldBuf);
			Method clean = cleanerObj.getClass().getMethod("clean");
			clean.setAccessible(true);
			clean. invoke(cleanerObj);
		} catch (Exception e) {
			// メソッドエラー発生時はGCに任せる
			oldBuf = null;
		}
	}
}