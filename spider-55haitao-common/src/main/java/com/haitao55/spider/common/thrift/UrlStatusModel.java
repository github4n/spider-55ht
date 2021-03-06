/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.haitao55.spider.common.thrift;


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

public enum UrlStatusModel implements org.apache.thrift.TEnum {
  NEWCOME(0),
  CRAWLING(1),
  CRAWLED_OK(2),
  CRAWLED_ERROR(3),
  DELETING(4),
  DELETED_OK(5),
  DELETED_ERROR(6),
  UNKNOWN(7);

  private final int value;

  private UrlStatusModel(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static UrlStatusModel findByValue(int value) { 
    switch (value) {
      case 0:
        return NEWCOME;
      case 1:
        return CRAWLING;
      case 2:
        return CRAWLED_OK;
      case 3:
        return CRAWLED_ERROR;
      case 4:
        return DELETING;
      case 5:
        return DELETED_OK;
      case 6:
        return DELETED_ERROR;
      case 7:
        return UNKNOWN;
      default:
        return null;
    }
  }
}
