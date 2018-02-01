package com.haitao55.spider.common.utils;

import java.util.Arrays;

public class NumberUtils {

  public static long hexToLong(byte[] bytes) {
    if (bytes.length > 16) {
      throw new IllegalArgumentException("Byte array too long (max 16 elements)");
    }
    long v = 0;
    for (int i = 0; i < bytes.length; i += 2) {
      byte b1 = (byte) (bytes[i] & 0xFF);
      b1 -= 48;
      if (b1 > 9) b1 -= 39;
      if (b1 < 0 || b1 > 15) {
        throw new IllegalArgumentException("Illegal hex value: " + bytes[i]);
      }
      b1 <<= 4;
      byte b2 = (byte) (bytes[i + 1] & 0xFF);
      b2 -= 48;
      if (b2 > 9) b2 -= 39;
      if (b2 < 0 || b2 > 15) {
        throw new IllegalArgumentException("Illegal hex value: " + bytes[i + 1]);
      }
      v |= (((b1 & 0xF0) | (b2 & 0x0F))) & 0x00000000000000FFL;
      if (i + 2 < bytes.length) v <<= 8;
    }
    return v;
  }

  public static byte[] longToHex(final long l) {
    long v = l & 0x7FFFFFFFFFFFFFFFL;
    byte[] result = new byte[16];
    Arrays.fill(result, 0, result.length, (byte) 0);
    for (int i = 0; i < result.length; i += 2) {
      byte b = (byte) ((v & 0xFF00000000000000L) >> 56);
      byte b2 = (byte) (b & 0x0F);
      byte b1 = (byte) ((b >> 4) & 0x0F);
      if (b1 > 9) b1 += 39;
      b1 += 48;
      if (b2 > 9) b2 += 39;
      b2 += 48;
      result[i] = (byte) (b1 & 0xFF);
      result[i + 1] = (byte) (b2 & 0xFF);
      v <<= 8;
    }
    return result;
  }

}
