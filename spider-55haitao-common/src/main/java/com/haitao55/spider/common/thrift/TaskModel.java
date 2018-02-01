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

public class TaskModel implements org.apache.thrift.TBase<TaskModel, TaskModel._Fields>, java.io.Serializable, Cloneable, Comparable<TaskModel> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TaskModel");

  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I64, (short)1);
  private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("status", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField CONFIG_FIELD_DESC = new org.apache.thrift.protocol.TField("config", org.apache.thrift.protocol.TType.STRING, (short)4);
  private static final org.apache.thrift.protocol.TField SITE_REGION_FIELD_DESC = new org.apache.thrift.protocol.TField("siteRegion", org.apache.thrift.protocol.TType.STRING, (short)5);
  private static final org.apache.thrift.protocol.TField PROXY_REGION_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("proxyRegionId", org.apache.thrift.protocol.TType.STRING, (short)6);
  private static final org.apache.thrift.protocol.TField LIMIT_FIELD_DESC = new org.apache.thrift.protocol.TField("limit", org.apache.thrift.protocol.TType.I32, (short)7);
  private static final org.apache.thrift.protocol.TField WEIGHT_FIELD_DESC = new org.apache.thrift.protocol.TField("weight", org.apache.thrift.protocol.TType.I32, (short)8);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TaskModelStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TaskModelTupleSchemeFactory());
  }

  public long id; // required
  public String name; // required
  public String status; // required
  public String config; // required
  public String siteRegion; // required
  public String proxyRegionId; // required
  public int limit; // required
  public int weight; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ID((short)1, "id"),
    NAME((short)2, "name"),
    STATUS((short)3, "status"),
    CONFIG((short)4, "config"),
    SITE_REGION((short)5, "siteRegion"),
    PROXY_REGION_ID((short)6, "proxyRegionId"),
    LIMIT((short)7, "limit"),
    WEIGHT((short)8, "weight");

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
        case 1: // ID
          return ID;
        case 2: // NAME
          return NAME;
        case 3: // STATUS
          return STATUS;
        case 4: // CONFIG
          return CONFIG;
        case 5: // SITE_REGION
          return SITE_REGION;
        case 6: // PROXY_REGION_ID
          return PROXY_REGION_ID;
        case 7: // LIMIT
          return LIMIT;
        case 8: // WEIGHT
          return WEIGHT;
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
  private static final int __ID_ISSET_ID = 0;
  private static final int __LIMIT_ISSET_ID = 1;
  private static final int __WEIGHT_ISSET_ID = 2;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.STATUS, new org.apache.thrift.meta_data.FieldMetaData("status", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.CONFIG, new org.apache.thrift.meta_data.FieldMetaData("config", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.SITE_REGION, new org.apache.thrift.meta_data.FieldMetaData("siteRegion", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.PROXY_REGION_ID, new org.apache.thrift.meta_data.FieldMetaData("proxyRegionId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.LIMIT, new org.apache.thrift.meta_data.FieldMetaData("limit", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.WEIGHT, new org.apache.thrift.meta_data.FieldMetaData("weight", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TaskModel.class, metaDataMap);
  }

  public TaskModel() {
  }

  public TaskModel(
    long id,
    String name,
    String status,
    String config,
    String siteRegion,
    String proxyRegionId,
    int limit,
    int weight)
  {
    this();
    this.id = id;
    setIdIsSet(true);
    this.name = name;
    this.status = status;
    this.config = config;
    this.siteRegion = siteRegion;
    this.proxyRegionId = proxyRegionId;
    this.limit = limit;
    setLimitIsSet(true);
    this.weight = weight;
    setWeightIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TaskModel(TaskModel other) {
    __isset_bitfield = other.__isset_bitfield;
    this.id = other.id;
    if (other.isSetName()) {
      this.name = other.name;
    }
    if (other.isSetStatus()) {
      this.status = other.status;
    }
    if (other.isSetConfig()) {
      this.config = other.config;
    }
    if (other.isSetSiteRegion()) {
      this.siteRegion = other.siteRegion;
    }
    if (other.isSetProxyRegionId()) {
      this.proxyRegionId = other.proxyRegionId;
    }
    this.limit = other.limit;
    this.weight = other.weight;
  }

  public TaskModel deepCopy() {
    return new TaskModel(this);
  }

  @Override
  public void clear() {
    setIdIsSet(false);
    this.id = 0;
    this.name = null;
    this.status = null;
    this.config = null;
    this.siteRegion = null;
    this.proxyRegionId = null;
    setLimitIsSet(false);
    this.limit = 0;
    setWeightIsSet(false);
    this.weight = 0;
  }

  public long getId() {
    return this.id;
  }

  public TaskModel setId(long id) {
    this.id = id;
    setIdIsSet(true);
    return this;
  }

  public void unsetId() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __ID_ISSET_ID);
  }

  /** Returns true if field id is set (has been assigned a value) and false otherwise */
  public boolean isSetId() {
    return EncodingUtils.testBit(__isset_bitfield, __ID_ISSET_ID);
  }

  public void setIdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __ID_ISSET_ID, value);
  }

  public String getName() {
    return this.name;
  }

  public TaskModel setName(String name) {
    this.name = name;
    return this;
  }

  public void unsetName() {
    this.name = null;
  }

  /** Returns true if field name is set (has been assigned a value) and false otherwise */
  public boolean isSetName() {
    return this.name != null;
  }

  public void setNameIsSet(boolean value) {
    if (!value) {
      this.name = null;
    }
  }

  public String getStatus() {
    return this.status;
  }

  public TaskModel setStatus(String status) {
    this.status = status;
    return this;
  }

  public void unsetStatus() {
    this.status = null;
  }

  /** Returns true if field status is set (has been assigned a value) and false otherwise */
  public boolean isSetStatus() {
    return this.status != null;
  }

  public void setStatusIsSet(boolean value) {
    if (!value) {
      this.status = null;
    }
  }

  public String getConfig() {
    return this.config;
  }

  public TaskModel setConfig(String config) {
    this.config = config;
    return this;
  }

  public void unsetConfig() {
    this.config = null;
  }

  /** Returns true if field config is set (has been assigned a value) and false otherwise */
  public boolean isSetConfig() {
    return this.config != null;
  }

  public void setConfigIsSet(boolean value) {
    if (!value) {
      this.config = null;
    }
  }

  public String getSiteRegion() {
    return this.siteRegion;
  }

  public TaskModel setSiteRegion(String siteRegion) {
    this.siteRegion = siteRegion;
    return this;
  }

  public void unsetSiteRegion() {
    this.siteRegion = null;
  }

  /** Returns true if field siteRegion is set (has been assigned a value) and false otherwise */
  public boolean isSetSiteRegion() {
    return this.siteRegion != null;
  }

  public void setSiteRegionIsSet(boolean value) {
    if (!value) {
      this.siteRegion = null;
    }
  }

  public String getProxyRegionId() {
    return this.proxyRegionId;
  }

  public TaskModel setProxyRegionId(String proxyRegionId) {
    this.proxyRegionId = proxyRegionId;
    return this;
  }

  public void unsetProxyRegionId() {
    this.proxyRegionId = null;
  }

  /** Returns true if field proxyRegionId is set (has been assigned a value) and false otherwise */
  public boolean isSetProxyRegionId() {
    return this.proxyRegionId != null;
  }

  public void setProxyRegionIdIsSet(boolean value) {
    if (!value) {
      this.proxyRegionId = null;
    }
  }

  public int getLimit() {
    return this.limit;
  }

  public TaskModel setLimit(int limit) {
    this.limit = limit;
    setLimitIsSet(true);
    return this;
  }

  public void unsetLimit() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __LIMIT_ISSET_ID);
  }

  /** Returns true if field limit is set (has been assigned a value) and false otherwise */
  public boolean isSetLimit() {
    return EncodingUtils.testBit(__isset_bitfield, __LIMIT_ISSET_ID);
  }

  public void setLimitIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __LIMIT_ISSET_ID, value);
  }

  public int getWeight() {
    return this.weight;
  }

  public TaskModel setWeight(int weight) {
    this.weight = weight;
    setWeightIsSet(true);
    return this;
  }

  public void unsetWeight() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __WEIGHT_ISSET_ID);
  }

  /** Returns true if field weight is set (has been assigned a value) and false otherwise */
  public boolean isSetWeight() {
    return EncodingUtils.testBit(__isset_bitfield, __WEIGHT_ISSET_ID);
  }

  public void setWeightIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __WEIGHT_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((Long)value);
      }
      break;

    case NAME:
      if (value == null) {
        unsetName();
      } else {
        setName((String)value);
      }
      break;

    case STATUS:
      if (value == null) {
        unsetStatus();
      } else {
        setStatus((String)value);
      }
      break;

    case CONFIG:
      if (value == null) {
        unsetConfig();
      } else {
        setConfig((String)value);
      }
      break;

    case SITE_REGION:
      if (value == null) {
        unsetSiteRegion();
      } else {
        setSiteRegion((String)value);
      }
      break;

    case PROXY_REGION_ID:
      if (value == null) {
        unsetProxyRegionId();
      } else {
        setProxyRegionId((String)value);
      }
      break;

    case LIMIT:
      if (value == null) {
        unsetLimit();
      } else {
        setLimit((Integer)value);
      }
      break;

    case WEIGHT:
      if (value == null) {
        unsetWeight();
      } else {
        setWeight((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ID:
      return Long.valueOf(getId());

    case NAME:
      return getName();

    case STATUS:
      return getStatus();

    case CONFIG:
      return getConfig();

    case SITE_REGION:
      return getSiteRegion();

    case PROXY_REGION_ID:
      return getProxyRegionId();

    case LIMIT:
      return Integer.valueOf(getLimit());

    case WEIGHT:
      return Integer.valueOf(getWeight());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case ID:
      return isSetId();
    case NAME:
      return isSetName();
    case STATUS:
      return isSetStatus();
    case CONFIG:
      return isSetConfig();
    case SITE_REGION:
      return isSetSiteRegion();
    case PROXY_REGION_ID:
      return isSetProxyRegionId();
    case LIMIT:
      return isSetLimit();
    case WEIGHT:
      return isSetWeight();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TaskModel)
      return this.equals((TaskModel)that);
    return false;
  }

  public boolean equals(TaskModel that) {
    if (that == null)
      return false;

    boolean this_present_id = true;
    boolean that_present_id = true;
    if (this_present_id || that_present_id) {
      if (!(this_present_id && that_present_id))
        return false;
      if (this.id != that.id)
        return false;
    }

    boolean this_present_name = true && this.isSetName();
    boolean that_present_name = true && that.isSetName();
    if (this_present_name || that_present_name) {
      if (!(this_present_name && that_present_name))
        return false;
      if (!this.name.equals(that.name))
        return false;
    }

    boolean this_present_status = true && this.isSetStatus();
    boolean that_present_status = true && that.isSetStatus();
    if (this_present_status || that_present_status) {
      if (!(this_present_status && that_present_status))
        return false;
      if (!this.status.equals(that.status))
        return false;
    }

    boolean this_present_config = true && this.isSetConfig();
    boolean that_present_config = true && that.isSetConfig();
    if (this_present_config || that_present_config) {
      if (!(this_present_config && that_present_config))
        return false;
      if (!this.config.equals(that.config))
        return false;
    }

    boolean this_present_siteRegion = true && this.isSetSiteRegion();
    boolean that_present_siteRegion = true && that.isSetSiteRegion();
    if (this_present_siteRegion || that_present_siteRegion) {
      if (!(this_present_siteRegion && that_present_siteRegion))
        return false;
      if (!this.siteRegion.equals(that.siteRegion))
        return false;
    }

    boolean this_present_proxyRegionId = true && this.isSetProxyRegionId();
    boolean that_present_proxyRegionId = true && that.isSetProxyRegionId();
    if (this_present_proxyRegionId || that_present_proxyRegionId) {
      if (!(this_present_proxyRegionId && that_present_proxyRegionId))
        return false;
      if (!this.proxyRegionId.equals(that.proxyRegionId))
        return false;
    }

    boolean this_present_limit = true;
    boolean that_present_limit = true;
    if (this_present_limit || that_present_limit) {
      if (!(this_present_limit && that_present_limit))
        return false;
      if (this.limit != that.limit)
        return false;
    }

    boolean this_present_weight = true;
    boolean that_present_weight = true;
    if (this_present_weight || that_present_weight) {
      if (!(this_present_weight && that_present_weight))
        return false;
      if (this.weight != that.weight)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public int compareTo(TaskModel other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetId()).compareTo(other.isSetId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.id, other.id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetName()).compareTo(other.isSetName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.name, other.name);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetStatus()).compareTo(other.isSetStatus());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStatus()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.status, other.status);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetConfig()).compareTo(other.isSetConfig());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetConfig()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.config, other.config);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSiteRegion()).compareTo(other.isSetSiteRegion());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSiteRegion()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.siteRegion, other.siteRegion);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetProxyRegionId()).compareTo(other.isSetProxyRegionId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetProxyRegionId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.proxyRegionId, other.proxyRegionId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetLimit()).compareTo(other.isSetLimit());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLimit()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.limit, other.limit);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetWeight()).compareTo(other.isSetWeight());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetWeight()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.weight, other.weight);
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
    StringBuilder sb = new StringBuilder("TaskModel(");
    boolean first = true;

    sb.append("id:");
    sb.append(this.id);
    first = false;
    if (!first) sb.append(", ");
    sb.append("name:");
    if (this.name == null) {
      sb.append("null");
    } else {
      sb.append(this.name);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("status:");
    if (this.status == null) {
      sb.append("null");
    } else {
      sb.append(this.status);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("config:");
    if (this.config == null) {
      sb.append("null");
    } else {
      sb.append(this.config);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("siteRegion:");
    if (this.siteRegion == null) {
      sb.append("null");
    } else {
      sb.append(this.siteRegion);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("proxyRegionId:");
    if (this.proxyRegionId == null) {
      sb.append("null");
    } else {
      sb.append(this.proxyRegionId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("limit:");
    sb.append(this.limit);
    first = false;
    if (!first) sb.append(", ");
    sb.append("weight:");
    sb.append(this.weight);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'id' because it's a primitive and you chose the non-beans generator.
    if (name == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'name' was not present! Struct: " + toString());
    }
    if (status == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'status' was not present! Struct: " + toString());
    }
    if (config == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'config' was not present! Struct: " + toString());
    }
    if (siteRegion == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'siteRegion' was not present! Struct: " + toString());
    }
    if (proxyRegionId == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'proxyRegionId' was not present! Struct: " + toString());
    }
    // alas, we cannot check 'limit' because it's a primitive and you chose the non-beans generator.
    // alas, we cannot check 'weight' because it's a primitive and you chose the non-beans generator.
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

  private static class TaskModelStandardSchemeFactory implements SchemeFactory {
    public TaskModelStandardScheme getScheme() {
      return new TaskModelStandardScheme();
    }
  }

  private static class TaskModelStandardScheme extends StandardScheme<TaskModel> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TaskModel struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.id = iprot.readI64();
              struct.setIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.name = iprot.readString();
              struct.setNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // STATUS
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.status = iprot.readString();
              struct.setStatusIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // CONFIG
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.config = iprot.readString();
              struct.setConfigIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // SITE_REGION
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.siteRegion = iprot.readString();
              struct.setSiteRegionIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // PROXY_REGION_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.proxyRegionId = iprot.readString();
              struct.setProxyRegionIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 7: // LIMIT
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.limit = iprot.readI32();
              struct.setLimitIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 8: // WEIGHT
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.weight = iprot.readI32();
              struct.setWeightIsSet(true);
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
      if (!struct.isSetId()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'id' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetLimit()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'limit' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetWeight()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'weight' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TaskModel struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(ID_FIELD_DESC);
      oprot.writeI64(struct.id);
      oprot.writeFieldEnd();
      if (struct.name != null) {
        oprot.writeFieldBegin(NAME_FIELD_DESC);
        oprot.writeString(struct.name);
        oprot.writeFieldEnd();
      }
      if (struct.status != null) {
        oprot.writeFieldBegin(STATUS_FIELD_DESC);
        oprot.writeString(struct.status);
        oprot.writeFieldEnd();
      }
      if (struct.config != null) {
        oprot.writeFieldBegin(CONFIG_FIELD_DESC);
        oprot.writeString(struct.config);
        oprot.writeFieldEnd();
      }
      if (struct.siteRegion != null) {
        oprot.writeFieldBegin(SITE_REGION_FIELD_DESC);
        oprot.writeString(struct.siteRegion);
        oprot.writeFieldEnd();
      }
      if (struct.proxyRegionId != null) {
        oprot.writeFieldBegin(PROXY_REGION_ID_FIELD_DESC);
        oprot.writeString(struct.proxyRegionId);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(LIMIT_FIELD_DESC);
      oprot.writeI32(struct.limit);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(WEIGHT_FIELD_DESC);
      oprot.writeI32(struct.weight);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TaskModelTupleSchemeFactory implements SchemeFactory {
    public TaskModelTupleScheme getScheme() {
      return new TaskModelTupleScheme();
    }
  }

  private static class TaskModelTupleScheme extends TupleScheme<TaskModel> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TaskModel struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI64(struct.id);
      oprot.writeString(struct.name);
      oprot.writeString(struct.status);
      oprot.writeString(struct.config);
      oprot.writeString(struct.siteRegion);
      oprot.writeString(struct.proxyRegionId);
      oprot.writeI32(struct.limit);
      oprot.writeI32(struct.weight);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TaskModel struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.id = iprot.readI64();
      struct.setIdIsSet(true);
      struct.name = iprot.readString();
      struct.setNameIsSet(true);
      struct.status = iprot.readString();
      struct.setStatusIsSet(true);
      struct.config = iprot.readString();
      struct.setConfigIsSet(true);
      struct.siteRegion = iprot.readString();
      struct.setSiteRegionIsSet(true);
      struct.proxyRegionId = iprot.readString();
      struct.setProxyRegionIdIsSet(true);
      struct.limit = iprot.readI32();
      struct.setLimitIsSet(true);
      struct.weight = iprot.readI32();
      struct.setWeightIsSet(true);
    }
  }

}

