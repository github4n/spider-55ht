/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.haitao55.spider.common.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartbeatModel implements org.apache.thrift.TBase<HeartbeatModel, HeartbeatModel._Fields>, java.io.Serializable, Cloneable, Comparable<HeartbeatModel> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("HeartbeatModel");

  private static final org.apache.thrift.protocol.TField TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("time", org.apache.thrift.protocol.TType.I64, (short)1);
  private static final org.apache.thrift.protocol.TField IP_FIELD_DESC = new org.apache.thrift.protocol.TField("ip", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField PROC_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("procId", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField THREAD_COUNT_FIELD_DESC = new org.apache.thrift.protocol.TField("threadCount", org.apache.thrift.protocol.TType.I32, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new HeartbeatModelStandardSchemeFactory());
    schemes.put(TupleScheme.class, new HeartbeatModelTupleSchemeFactory());
  }

  public long time; // required
  public String ip; // required
  public String procId; // required
  public int threadCount; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    TIME((short)1, "time"),
    IP((short)2, "ip"),
    PROC_ID((short)3, "procId"),
    THREAD_COUNT((short)4, "threadCount");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // TIME
          return TIME;
        case 2: // IP
          return IP;
        case 3: // PROC_ID
          return PROC_ID;
        case 4: // THREAD_COUNT
          return THREAD_COUNT;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __TIME_ISSET_ID = 0;
  private static final int __THREADCOUNT_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TIME, new org.apache.thrift.meta_data.FieldMetaData("time", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.IP, new org.apache.thrift.meta_data.FieldMetaData("ip", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.PROC_ID, new org.apache.thrift.meta_data.FieldMetaData("procId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.THREAD_COUNT, new org.apache.thrift.meta_data.FieldMetaData("threadCount", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(HeartbeatModel.class, metaDataMap);
  }

  public HeartbeatModel() {
  }

  public HeartbeatModel(
    long time,
    String ip,
    String procId,
    int threadCount)
  {
    this();
    this.time = time;
    setTimeIsSet(true);
    this.ip = ip;
    this.procId = procId;
    this.threadCount = threadCount;
    setThreadCountIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public HeartbeatModel(HeartbeatModel other) {
    __isset_bitfield = other.__isset_bitfield;
    this.time = other.time;
    if (other.isSetIp()) {
      this.ip = other.ip;
    }
    if (other.isSetProcId()) {
      this.procId = other.procId;
    }
    this.threadCount = other.threadCount;
  }

  public HeartbeatModel deepCopy() {
    return new HeartbeatModel(this);
  }

  @Override
  public void clear() {
    setTimeIsSet(false);
    this.time = 0;
    this.ip = null;
    this.procId = null;
    setThreadCountIsSet(false);
    this.threadCount = 0;
  }

  public long getTime() {
    return this.time;
  }

  public HeartbeatModel setTime(long time) {
    this.time = time;
    setTimeIsSet(true);
    return this;
  }

  public void unsetTime() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TIME_ISSET_ID);
  }

  /** Returns true if field time is set (has been assigned a value) and false otherwise */
  public boolean isSetTime() {
    return EncodingUtils.testBit(__isset_bitfield, __TIME_ISSET_ID);
  }

  public void setTimeIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TIME_ISSET_ID, value);
  }

  public String getIp() {
    return this.ip;
  }

  public HeartbeatModel setIp(String ip) {
    this.ip = ip;
    return this;
  }

  public void unsetIp() {
    this.ip = null;
  }

  /** Returns true if field ip is set (has been assigned a value) and false otherwise */
  public boolean isSetIp() {
    return this.ip != null;
  }

  public void setIpIsSet(boolean value) {
    if (!value) {
      this.ip = null;
    }
  }

  public String getProcId() {
    return this.procId;
  }

  public HeartbeatModel setProcId(String procId) {
    this.procId = procId;
    return this;
  }

  public void unsetProcId() {
    this.procId = null;
  }

  /** Returns true if field procId is set (has been assigned a value) and false otherwise */
  public boolean isSetProcId() {
    return this.procId != null;
  }

  public void setProcIdIsSet(boolean value) {
    if (!value) {
      this.procId = null;
    }
  }

  public int getThreadCount() {
    return this.threadCount;
  }

  public HeartbeatModel setThreadCount(int threadCount) {
    this.threadCount = threadCount;
    setThreadCountIsSet(true);
    return this;
  }

  public void unsetThreadCount() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __THREADCOUNT_ISSET_ID);
  }

  /** Returns true if field threadCount is set (has been assigned a value) and false otherwise */
  public boolean isSetThreadCount() {
    return EncodingUtils.testBit(__isset_bitfield, __THREADCOUNT_ISSET_ID);
  }

  public void setThreadCountIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __THREADCOUNT_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case TIME:
      if (value == null) {
        unsetTime();
      } else {
        setTime((Long)value);
      }
      break;

    case IP:
      if (value == null) {
        unsetIp();
      } else {
        setIp((String)value);
      }
      break;

    case PROC_ID:
      if (value == null) {
        unsetProcId();
      } else {
        setProcId((String)value);
      }
      break;

    case THREAD_COUNT:
      if (value == null) {
        unsetThreadCount();
      } else {
        setThreadCount((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case TIME:
      return Long.valueOf(getTime());

    case IP:
      return getIp();

    case PROC_ID:
      return getProcId();

    case THREAD_COUNT:
      return Integer.valueOf(getThreadCount());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case TIME:
      return isSetTime();
    case IP:
      return isSetIp();
    case PROC_ID:
      return isSetProcId();
    case THREAD_COUNT:
      return isSetThreadCount();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof HeartbeatModel)
      return this.equals((HeartbeatModel)that);
    return false;
  }

  public boolean equals(HeartbeatModel that) {
    if (that == null)
      return false;

    boolean this_present_time = true;
    boolean that_present_time = true;
    if (this_present_time || that_present_time) {
      if (!(this_present_time && that_present_time))
        return false;
      if (this.time != that.time)
        return false;
    }

    boolean this_present_ip = true && this.isSetIp();
    boolean that_present_ip = true && that.isSetIp();
    if (this_present_ip || that_present_ip) {
      if (!(this_present_ip && that_present_ip))
        return false;
      if (!this.ip.equals(that.ip))
        return false;
    }

    boolean this_present_procId = true && this.isSetProcId();
    boolean that_present_procId = true && that.isSetProcId();
    if (this_present_procId || that_present_procId) {
      if (!(this_present_procId && that_present_procId))
        return false;
      if (!this.procId.equals(that.procId))
        return false;
    }

    boolean this_present_threadCount = true;
    boolean that_present_threadCount = true;
    if (this_present_threadCount || that_present_threadCount) {
      if (!(this_present_threadCount && that_present_threadCount))
        return false;
      if (this.threadCount != that.threadCount)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public int compareTo(HeartbeatModel other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetTime()).compareTo(other.isSetTime());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTime()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.time, other.time);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetIp()).compareTo(other.isSetIp());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetIp()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ip, other.ip);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetProcId()).compareTo(other.isSetProcId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetProcId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.procId, other.procId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetThreadCount()).compareTo(other.isSetThreadCount());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetThreadCount()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.threadCount, other.threadCount);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("HeartbeatModel(");
    boolean first = true;

    sb.append("time:");
    sb.append(this.time);
    first = false;
    if (!first) sb.append(", ");
    sb.append("ip:");
    if (this.ip == null) {
      sb.append("null");
    } else {
      sb.append(this.ip);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("procId:");
    if (this.procId == null) {
      sb.append("null");
    } else {
      sb.append(this.procId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("threadCount:");
    sb.append(this.threadCount);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'time' because it's a primitive and you chose the non-beans generator.
    if (ip == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'ip' was not present! Struct: " + toString());
    }
    if (procId == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'procId' was not present! Struct: " + toString());
    }
    // alas, we cannot check 'threadCount' because it's a primitive and you chose the non-beans generator.
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class HeartbeatModelStandardSchemeFactory implements SchemeFactory {
    public HeartbeatModelStandardScheme getScheme() {
      return new HeartbeatModelStandardScheme();
    }
  }

  private static class HeartbeatModelStandardScheme extends StandardScheme<HeartbeatModel> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, HeartbeatModel struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TIME
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.time = iprot.readI64();
              struct.setTimeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // IP
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.ip = iprot.readString();
              struct.setIpIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // PROC_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.procId = iprot.readString();
              struct.setProcIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // THREAD_COUNT
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.threadCount = iprot.readI32();
              struct.setThreadCountIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      if (!struct.isSetTime()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'time' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetThreadCount()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'threadCount' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, HeartbeatModel struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(TIME_FIELD_DESC);
      oprot.writeI64(struct.time);
      oprot.writeFieldEnd();
      if (struct.ip != null) {
        oprot.writeFieldBegin(IP_FIELD_DESC);
        oprot.writeString(struct.ip);
        oprot.writeFieldEnd();
      }
      if (struct.procId != null) {
        oprot.writeFieldBegin(PROC_ID_FIELD_DESC);
        oprot.writeString(struct.procId);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(THREAD_COUNT_FIELD_DESC);
      oprot.writeI32(struct.threadCount);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class HeartbeatModelTupleSchemeFactory implements SchemeFactory {
    public HeartbeatModelTupleScheme getScheme() {
      return new HeartbeatModelTupleScheme();
    }
  }

  private static class HeartbeatModelTupleScheme extends TupleScheme<HeartbeatModel> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, HeartbeatModel struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI64(struct.time);
      oprot.writeString(struct.ip);
      oprot.writeString(struct.procId);
      oprot.writeI32(struct.threadCount);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, HeartbeatModel struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.time = iprot.readI64();
      struct.setTimeIsSet(true);
      struct.ip = iprot.readString();
      struct.setIpIsSet(true);
      struct.procId = iprot.readString();
      struct.setProcIdIsSet(true);
      struct.threadCount = iprot.readI32();
      struct.setThreadCountIsSet(true);
    }
  }

}
