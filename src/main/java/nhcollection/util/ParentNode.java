package nhcollection.util;

import java.nio.*;

public class ParentNode {

	private NHBuffer defaultBuf = null;
	//private NHBuffer largeBuf nul;

	public ParentNode(){

		defaultBuf = new NHBuffer();
	}

	//データをBufferへ書き込む
	//返却値はintの配列
	//配列の内容は偶数(0含む)IndexはBuffer上の読み込み開始Chunk位置の数値(バイト位置ではない)
	//奇数Indexは当該Chunk上に書き込んだbyte数
	//書き込むデータサイズが1chunkサイズを超える場合は上記のルールで書き込ん
	//例) int [0] = 0,int [1] = 128
	//    int [2] 1,int[3] 128
	//    int[4] 2,int[5] 56
	protected int[] writeData(byte[] data) {
		int[] ret = null;

		//データサイズに応じて処理分岐
		if (data.length <= defaultBuf.chunkSize) {
			//1チャンクサイズと同サイズか以下
			return defaultBuf.writeData(data);
		} else {
			//1チャンクサイズ以上
			int loopCount = data.length/ defaultBuf.chunkSize;
			int rmder = data.length % defaultBuf.chunkSize;
			if (rmder > 0) loopCount = loopCount + 1;

			ret = new int[loopCount * 2];
			// ここで128byteよりも大きいデータを書き込んでいく
			for (int writeIdx = 0; writeIdx < loopCount; writeIdx++) {

				byte[] writeData = null;
				if (rmder > 0 && loopCount == (writeIdx + 1)) {

					// チャンクサイズで割り切れない最後のループ
					writeData = new byte[rmder];
				} else {
					writeData = new byte[defaultBuf.chunkSize];
				}
				//
				System.arraycopy(data, (writeIdx * defaultBuf.chunkSize), writeData, 0, writeData.length);
				int[] writeResult = defaultBuf.writeData(writeData);

				ret[writeIdx*2] = writeResult[0];
				ret[writeIdx*2+1] = writeResult[1];
			}
		}

		return ret;
	}

	protected int[] writeData(int point, byte[] data) {
		return defaultBuf.writeData(point, data);
	}


	protected Object readData(int[] point) {

		int loopCount = point.length / 2;
		int totalReadSize = 0;
		byte[] allReadData = null;

		// 読み取りが複数Chunkの可能性があるのでまず読み取り総データを求める
		//
		//
		for (int loop = 0; loop < loopCount; loop++) {
			totalReadSize = totalReadSize + point[loop *2 + 1];
		}

		//そもそも1chunkの場合は1回のreadDataで終了
		if (totalReadSize <= defaultBuf.chunkSize) {

			allReadData =  defaultBuf.readData(point);
		} else {

			// 複数Chunk処理
			allReadData = new byte[totalReadSize];

			for (int loop = 0; loop < loopCount; loop++) {
				int[] readInfo = new int[2];
				readInfo[0] = point[loop * 2];
				readInfo[1] = point[loop * 2+ 1];
				byte[] oneChunkResult = defaultBuf.readData(readInfo);
				System.arraycopy(oneChunkResult, 0 ,allReadData, (loop * defaultBuf.chunkSize), oneChunkResult.length);
			}
		}

		return new String(allReadData);
	}

	protected Object removeData(int[] point) {

		int loopCount = point.length / 2;

		//読み取り範囲が複数chunkの可能性があるので.まず読み取りサイズを求める
		//処理としてはwirtaDataの戻り値である読み込み開始位置と書き込んでいるbyteのサイズが渡ってくるので
		//サイズ部分を足しこんでいる
		for (int loop = 0; loop < loopCount; loop++) {
			defaultBuf.removeData(point[loop*2]);
		}
		return null;
	}
}
