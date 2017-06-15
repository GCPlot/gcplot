package com.gcplot.utils;

public abstract class BinaryUtils {

	public static long bytesToLong(byte[] b) {
		return ((((long) b[0]) << 56) | (((long) b[1] & 0xff) << 48) | (((long) b[2] & 0xff) << 40)
	            | (((long) b[3] & 0xff) << 32) | (((long) b[4] & 0xff) << 24) | (((long) b[5] & 0xff) << 16)
	            | (((long) b[6] & 0xff) << 8) | (((long) b[7] & 0xff)));
	}
	
	public static void longToBytes(long l, byte[] b) {
		b[0] = (byte) (l >> 56);
		b[1] = (byte) (l >> 48);
		b[2] = (byte) (l >> 40);
		b[3] = (byte) (l >> 32);
		b[4] = (byte) (l >> 24);
		b[5] = (byte) (l >> 16);
		b[6] = (byte) (l >> 8);
		b[7] = (byte) l;
	}
	
	public static int bytesToInt(byte[] b) {
		return ((((int) b[0] & 0xff) << 24) | (((int) b[1] & 0xff) << 16)
	            | (((int) b[2] & 0xff) << 8) | (((int) b[3] & 0xff)));
	}
	
	public static byte[] intToBytes(int l) {
		return new byte[] {
		        (byte) (l >>> 24),
		        (byte) (l >>> 16),
		        (byte) (l >>> 8),
		        (byte) l
		    };
	}
	
	public static final int intlongToBytes(int value, long lval, byte[] data) {
		int result = 1;
		
		data[0] = (byte)(value >>> 24);
		result = 31 * result + data[0];
		data[1] = (byte)(value >>> 16);
		result = 31 * result + data[1];
		data[2] = (byte)(value >>> 8);
		result = 31 * result + data[2];
		data[3] = (byte)value;
		result = 31 * result + data[3];
		data[4] = (byte)(lval >>> 56);
		result = 31 * result + data[4];
		data[5] = (byte)(lval >>> 48);
		result = 31 * result + data[5];
		data[6] = (byte)(lval >>> 40);
		result = 31 * result + data[6];
		data[7] = (byte)(lval >>> 32);
		result = 31 * result + data[7];
		data[8] = (byte)(lval >>> 24);
		result = 31 * result + data[8];
		data[9] = (byte)(lval >>> 16);
		result = 31 * result + data[9];
		data[10] = (byte)(lval >>> 8);
		result = 31 * result + data[10];
		data[11] = (byte)(lval);
		result = 31 * result + data[11];
		
		return result;
	}
	
}
