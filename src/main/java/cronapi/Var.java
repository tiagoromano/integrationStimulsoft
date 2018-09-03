package cronapi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import cronapi.database.DataSource;
import cronapi.i18n.Messages;
import cronapi.json.JsonArrayWrapper;
import cronapi.json.Operations;
import cronapi.serialization.CronappModule;
import cronapi.util.StorageService;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.olingo.odata2.core.ep.producer.OlingoJsonSerializer;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

@JsonAdapter(VarSerializer.class)
public class Var implements Comparable<Var>, JsonSerializable, OlingoJsonSerializer {

  @Override
  public String serializeAsJson() {
    try {
      return new CronapiConfigurator().objectMapperBuilder().build().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static class JsonAdapter {

  }

  public static void main(String[] args) {
    Var var = new Var("Teste");
    System.out.print(new Gson().toJson(var));
  }

    public static final Pattern ISO_PATTERN = Pattern.compile(
      "(\\d{4}-[01]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-5]\\d\\.\\d+([+-][0-2]\\d:[0-5]\\d|Z))|(\\d{4}-[01]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-5]\\d([+-][0-2]\\d:[0-5]\\d|Z))|(\\d{4}-[01]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d([+-][0-2]\\d:[0-5]\\d|Z))");

  ;
  public static final Object[] EMPTY_OBJ_ARRAY = new Object[0];
  public static final Var VAR_NULL = new Var(null, false);
  public static final Var VAR_TRUE = new Var(true, false);
  public static final Var VAR_FALSE = new Var(false, false);
  public static final Var VAR_ZERO = new Var(0, false);
  public static final Var VAR_ONE = new Var(1, false);
  public static final Var VAR_NEGATIVE_ONE = new Var(-1, false);
  public static final Var VAR_EMPTY = new Var("", false);
  public static final Var VAR_DATE_ZERO;
  private static final NumberFormat _formatter = new DecimalFormat("0.00000");
  public static String[] ALLOWED_TYPES = {"text", "datetime", "date", "number", "integer",
      "boolean"};
  public static Class[] MAPPED_TYPES = {java.lang.String.class, java.util.Date.class,
      java.util.Date.class,
      java.lang.Double.class, java.lang.Long.class, java.lang.Boolean.class};

  static {
    Calendar calendar = Calendar.getInstance();
    calendar.set(1980, 1, 1, 0, 0, 0);
    VAR_DATE_ZERO = new Var(calendar, false);
  }

  private String id;
  private Type _type;
  private Object _object;
  private boolean modifiable = true;
  private boolean created = false;

  /**
   * Construct a Var with an NULL type
   */
  public Var() {
    _type = Type.NULL;
    created = true;
  }

  /**
   * Construct a Var and assign its contained object to that specified.
   *
   * @param object The value to set this object to
   */
  public Var(String id, Object object) {
    this.id = id;
    setObject(object);
  }

  /**
   * Construct a Var and assign its contained object to that specified.
   *
   * @param object The value to set this object to
   */
  public Var(Object object) {
    setObject(object);
  }

  public Var(Object object, boolean modifiable) {
    setObject(object);
    this.modifiable = modifiable;
  }

  /**
   * Construct a Var from a given Var
   *
   * @param var var to construct this one from
   */
  public Var(Var var) {
    _type = Type.UNKNOWN;
    if (var != null) {
      this.id = var.id;
      setObject(var.getObject());
    }
  }

  /**
   * Static constructor to make a var from some value.
   *
   * @param val some value to construct a var around
   * @return the Var object
   */
  public static Var valueOf(Object val) {
    if (val instanceof Var) {
      return (Var) val;
    }

    if (val instanceof Boolean) {
      if (((Boolean) val)) {
        return VAR_TRUE;
      } else {
        return VAR_FALSE;
      }
    }

    if (val == null) {
      return VAR_NULL;
    }

    return new Var(val);
  }

  public static Var valueOf(String id, Object val) {
    if (val instanceof Var && Objects.equals(((Var) val).getId(), id)) {
      return (Var) val;
    }

    return new Var(id, val);
  }

  public static String deserializeType(String value) {
    for (int i = 0; i < ALLOWED_TYPES.length; i++) {
      if (value.endsWith("@@" + ALLOWED_TYPES[i])) {
        return ALLOWED_TYPES[i];
      }
    }

    return ALLOWED_TYPES[0];
  }

  public static Object deserialize(String value) {
    if (value == null) {
      return null;
    }

    int type = 0;
    for (int i = 0; i < ALLOWED_TYPES.length; i++) {
      if (value.endsWith("@@" + ALLOWED_TYPES[i])) {
        type = i;
        value = value.substring(0, value.indexOf("@@"));
        break;
      }
    }

    Var var = null;
    if (type == 0 && ISO_PATTERN.matcher(value).matches()) {
      var = Var.valueOf(Var.valueOf(value).getObjectAsDateTime());
    } else {
      var = Var.valueOf(value);
    }

    return var.getObject(MAPPED_TYPES[type]);
  }

  public static Var newMap() {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    return Var.valueOf(map);
  }

  public Var put(Object key, Object value) {
    getObjectAsMap().put(key.toString(), Var.valueOf(value));
    return this;
  }

  public static Var newList() {
    return new Var(new LinkedList<>());
  }

  public static Object[] asObjectArray(Var[] vars) {
    if (vars.length > 0) {
      Object[] objs = new Object[vars.length];
      for (int i = 0; i < vars.length; i++) {
        objs[i] = vars[i].getObject();
      }

      return objs;
    }

    return EMPTY_OBJ_ARRAY;
  }

  /**
   * Get the type of the underlying object
   *
   * @return Will return the object's type as defined by Type
   */
  public Type getType() {
    return _type;
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the contained datasource
   *
   * @return the object
   */
  public DataSource getObjectAsDataSource() {
    return (DataSource) _object;
  }

  public Object getObject() {
    return _object;
  }

  /**
   * Set the value of the underlying object. Note that the type of Var will be determined when
   * setObject is called.
   *
   * @param val the value to set this Var to
   */
  public void setObject(Object val) {
    if (created && !modifiable) {
      throw new RuntimeException(Messages.getString("NotModifiable"));
    }
    this._object = val;
    inferType();
    // make sure each element of List is Var if type is list
    if (_type.equals(Var.Type.LIST)) {
      LinkedList<Var> myList = new LinkedList<>();
      for (Object obj : this.getObjectAsList()) {
        myList.add(Var.valueOf(obj));
      }
      this._object = myList;
    }

    created = true;
  }

  public <T extends Object> T getTypedObject(Class<T> type) {
    Object object = getObject(type);

    if (object == null) {
      return null;
    }

    if (type.isInstance(object)) {
      return type.cast(object);
    } else {
      JsonElement json = getObjectAsJson();
      ObjectMapper mapper = new ObjectMapper();
      try {
        return mapper.readValue(json.toString(), type);
      } catch (IOException e) {
        throw new ClassCastException(
            "Cannot cast " + object.getClass().getName() + " to " + type.getName());
      }
    }
  }

  public Object getObject(Class type) {

    if (type == Var.class) {
      return this;
    } else if (type == String.class || type == StringBuilder.class || type == StringBuffer.class
        || type == Character.class) {
      return getObjectAsString();
    } else if (type == Boolean.class) {
      return getObjectAsBoolean();
    } else if (type == JsonElement.class) {
      return getObjectAsJson();
    } else if (type == Date.class) {
      return getObjectAsDateTime().getTime();
    } else if (type == Calendar.class) {
      return getObjectAsDateTime();
    } else if (type == Long.class) {
      return getObjectAsLong();
    } else if (type == Integer.class) {
      return getObjectAsInt();
    } else if (type == Double.class || type == double.class) {
      return getObjectAsDouble();
    } else if (type == Float.class) {
      return getObjectAsDouble().floatValue();
    } else if (type == BigDecimal.class) {
      return new BigDecimal(getObjectAsDouble());
    } else if (type == BigInteger.class) {
      return BigInteger.valueOf(getObjectAsLong());
    } else if (type == byte[].class) {
      return getObjectAsByteArray();
    } else if (Collection.class.isAssignableFrom(type)) {
      try {
        return getObjectAsRawList(type);
      } catch (Exception e) {
        return getObjectAsRawList(LinkedList.class);
      }
    }  else {
      //create instance for Entity class
      if (Utils.isEntityClass(type) && _object != null
          && !(_object instanceof java.util.LinkedHashMap)
          && !type.equals(_object.getClass())) {
        try {
          List<String> ids = Utils.getFieldsWithAnnotationId(type);
          Object instanceClass = type.newInstance();
          for (String id : ids) {
            Utils.updateField(instanceClass, id, _object);
          }
          return instanceClass;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      //end create instance for Entity class
      else if (_object instanceof Map && type != Map.class) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Var.class, new VarDeserializer());
        mapper.registerModule(module);

        try {
          List<String> fieldsByteHeaderSignature = cronapi.Utils
              .getFieldsWithAnnotationByteHeaderSignature(type);
          for (String fieldToGetByteContent : fieldsByteHeaderSignature) {
            Var content = cronapi.json.Operations.getJsonOrMapField(Var.valueOf(_object),
                Var.valueOf(fieldToGetByteContent));
            if (cronapi.util.StorageService.isTempFileJson(content.getObjectAsString())) {
              byte[] contentByte = StorageService
                  .getFileBytesWithMetadata(content.getObjectAsString());
              cronapi.json.Operations
                  .setJsonOrMapField(Var.valueOf(_object), Var.valueOf(fieldToGetByteContent),
                      Var.valueOf(contentByte));
            }
          }
        } catch (Exception e) {
          //Abafa
        }

        return mapper.convertValue(_object, type);
      }

      return getObject();
    }
  }

  /**
   * Clone Object
   *
   * @return a new object equal to this one
   */
  public Object cloneObject() {
    Var tempVar = new Var(this);
    return tempVar.getObject();
  }

  /**
   * Get object as an int. Does not make sense for a "LIST" type object
   *
   * @return an integer whose value equals this object
   */
  public Integer getObjectAsInt() {
    switch (getType()) {
      case STRING:
        try {
          return Integer.parseInt((String) getObject());
        } catch (Exception e) {
          return ((Double) Double.parseDouble((String) getObject())).intValue();
        }
      case INT:
        return ((Long) getObject()).intValue();
      case BOOLEAN:
        return ((Boolean) getObject()) ? 1 : 0;
      case DOUBLE:
        return new Double((double) getObject()).intValue();
      case DATETIME:
        return (int) (((Calendar) getObject()).getTimeInMillis());
      case LIST:
        return ((List) _object).size();
      default:
        // has no meaning
        break;
    }

    return 0;
  }

  /**
   * Get object as an int. Does not make sense for a "LIST" type object
   *
   * @return an integer whose value equals this object
   */
  public Long getObjectAsLong() {
    switch (getType()) {
      case STRING:
        try {
          return Long.parseLong((String) getObject());
        } catch (Exception e) {
          return ((Double) Double.parseDouble((String) getObject())).longValue();
        }
      case INT:
        return (Long) getObject();
      case BOOLEAN:
        return ((Boolean) getObject()) ? 1L : 0L;
      case DOUBLE:
        return new Double((double) getObject()).longValue();
      case DATETIME:
        return (Long) ((Calendar) getObject()).getTimeInMillis();
      case LIST:
        return Long.valueOf(((List) _object).size());
      default:
        // has no meaning
        break;
    }

    return 0L;
  }

  public File getObjectAsFile() {
    if (_object instanceof File) {
      return (File) _object;
    } else if (_object instanceof Path) {
      return ((Path) _object).toFile();
    } else {
      return new File(toString());
    }
  }

  public JsonElement getObjectAsJson() {
    if (_object != null) {
      if (_object instanceof JsonElement) {
        return (JsonElement) _object;
      } else {
        try {
          if (_object instanceof String) {
            return new Gson().fromJson(_object.toString(), JsonElement.class);
          } else if (_object instanceof InputStream) {
            return new Gson().fromJson(getObjectAsString(), JsonElement.class);
          } else {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(_object);
            return new Gson().fromJson(json, JsonElement.class);
          }
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    }

    return null;
  }

  /**
   * Get object as an boolean.
   *
   * @return an bool whose value equals this object
   */
  public Calendar getObjectAsDateTime() {
    switch (getType()) {
      case STRING:
        String s = (String) getObject();
        return Utils.toCalendar(s, null);
      case INT:
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getObjectAsInt());
        return c;
      case DOUBLE:
        Calendar cd = Calendar.getInstance();
        cd.setTimeInMillis(getObjectAsInt());
        return cd;
      case DATETIME:
        return (Calendar) getObject();
      case LIST:
        // has no meaning
        break;
      default:
        // has no meaning
        break;
    }

    return VAR_DATE_ZERO.getObjectAsDateTime();
  }

  private Object getPrimitiveValue(JsonPrimitive element) {
    if (element.isBoolean()) {
      return element.getAsBoolean();
    } else if (element.isNumber()) {
      return element.getAsBigDecimal();
    } else {
      return element.getAsString();
    }
  }

  /**
   * Get object as an boolean.
   *
   * @return an bool whose value equals this object
   */
  public Boolean getObjectAsBoolean() {
    switch (getType()) {
      case STRING:
        String s = (String) getObject();
        if (s.equals("1") || s.equalsIgnoreCase("true")) {
          s = "true";
        } else {
          s = "false";
        }
        return Boolean.valueOf(s);
      case INT:
        return (Long) getObject() > 0;
      case BOOLEAN:
        return (boolean) getObject();
      case DOUBLE:
        return new Double((double) getObject()).intValue() > 0;
      case DATETIME:
        // has no meaning
        break;
      case LIST:
        // has no meaning
        break;
      default:
        // has no meaning
        break;
    }
    return false;
  }

  public String getMD5() {
    try {
      if (_object instanceof File) {
        return Utils.encodeMD5((File) _object);
      } else if (_object instanceof byte[]) {
        return Utils.encodeMD5((byte[]) _object);
      } else {
        return Utils.encodeMD5(getObjectAsString());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get object as a double. Does not make sense for a "LIST" type object.
   *
   * @return a double whose value equals this object
   */
  public Double getObjectAsDouble() {
    switch (getType()) {
      case STRING:
        return Double.parseDouble((String) getObject());
      case INT:
        return new Long((Long) getObject()).doubleValue();
      case BOOLEAN:
        return ((boolean) getObject()) ? 1.0 : 0.0;
      case DOUBLE:
        return (double) getObject();
      case DATETIME:
        return (double) ((Calendar) getObject()).getTimeInMillis();
      case LIST:
        return Double.valueOf(((List) _object).size());
      default:
        // has no meaning
        break;
    }

    return 0.0;
  }

  /**
   * Get object as a byte array. Does not make sense for a "LIST" type object.
   *
   * @return a byte array whose value equals this object
   */
  public byte[] getObjectAsByteArray() {
    try {
      switch (getType()) {
        case STRING:
          if (cronapi.util.StorageService.isTempFileJson(((String) getObject()))) {
            return StorageService.getFileBytesWithMetadata((String) getObject());
          }
          return java.util.Base64.getDecoder().decode(((String) getObject()).getBytes("UTF-8"));
        default:
          if (_object instanceof File) {
            if (StorageService.isFileImage(_object)) {
              return FileUtils.readFileToByteArray((File) _object);
            } else {
              return StorageService.getFileBytesWithMetadata((File) _object);
            }
          } else if (_object instanceof InputStream) {
            return IOUtils.toByteArray((InputStream) _object);
          }

          return (byte[]) getObject();
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get object as a string.
   *
   * @return The string value of the object. Note that for lists, this is a comma separated list of
   * the form {x,y,z,...}
   */
  public String getObjectAsString() {
    if (isNull())
      return "";

    Object object = getObject();
    if (object == null) {
      return "";
    } else if (object instanceof InputStream) {
      try {
        return org.apache.commons.io.IOUtils.toString((InputStream) getObject());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else if (object instanceof Document) {
      Document document = (Document) object;
      XMLOutputter outputter = new XMLOutputter();
      return outputter.outputString(document);
    } else if (object instanceof Element) {
      Element element = (Element) object;
      XMLOutputter outputter = new XMLOutputter();
      return outputter.outputString(element);
    } else if (object instanceof String) {
      return object.toString();
    }

    return this.getObjectAsJson().toString();
  }

  private LinkedList<Var> getSingleList(Object o) {
    LinkedList<Var> list = new LinkedList<>();
    list.add(Var.valueOf(o));

    return list;
  }

  private LinkedList<Var> toList(List list) {
    LinkedList<Var> myList = new LinkedList<>();
    for (Object obj : list) {
      myList.add(Var.valueOf(obj));
    }

    return myList;
  }

  public Collection getObjectAsRawList(Class clazz) {
    List list = getObjectAsList();

    Collection result;
    try {
      if (!clazz.isInterface()) {
        result = (Collection) clazz.newInstance();
      } else {
        result = new LinkedList();
      }
    } catch (Exception e) {
      result = new LinkedList();
    }
    for (Object o: list) {
      result.add(Var.valueOf(o).getObject());
    }

    return result;
  }

    /**
     * Get the object as a list.
     *
     * @return a LinkedList whose elements are of type Var
     */
  public LinkedList<Var> getObjectAsList() {

    if (getObject() instanceof Map) {
      LinkedList<Var> myList = new LinkedList<>();
      for (Object obj : ((Map) getObject()).values()) {
        myList.add(Var.valueOf(obj));
      }

      return myList;
    } else if (getObject() instanceof JsonArray) {
      LinkedList<Var> myList = new LinkedList<>();
      for (JsonElement element : (JsonArray) getObject())
      {
        myList.add(Var.valueOf(element));
      }
      return myList;
    } else if (getObject() instanceof List) {
      return (LinkedList<Var>) getObject();
    } else if (getObject() instanceof DataSource) {
      return toList(((DataSource) getObject()).getPage().getContent());
    }

    return getSingleList(getObject());
  }

  public Map getObjectAsMap() {
    if (getObject() instanceof Map) {
      return (Map) getObject();
    } else {
      if (getObject() != null) {
        if (isNative()) {
          Map map = new LinkedHashMap();
          if (id == null) {
            map.put(this, this);
          } else {
            map.put(id, this);
          }

          return map;
        } else {
          ObjectMapper mapper = new ObjectMapper();
          mapper.registerModule(new CronappModule(false));

          if (getObject() instanceof DataSource) {
            return (Map) mapper.convertValue(((DataSource) getObject()).getObject(), Map.class);
          } else {
            return (Map) mapper.convertValue(_object, Map.class);
          }
        }
      }
    }

    return new LinkedHashMap();
  }

  public Var getObjectAsPOJOList() {

    LinkedList<Var> myList = null;

    if (getObject() instanceof DataSource) {
      myList = new LinkedList<>();
      DataSource ds = (DataSource) getObject();
      for (Object obj : ds.getPage().getContent()) {
        myList.add(Var.valueOf(obj));
      }
    } else if (getObject() instanceof List) {
      myList = new LinkedList<>();
      for (Var obj : ((LinkedList<Var>) getObject())) {
        myList.add(obj.getPOJO());
      }
    } else if (getObject() instanceof JsonArray) {
      myList = new LinkedList<>();
      JsonArray jsarray = (JsonArray) getObject();
      for (JsonElement jselement : jsarray) {
        myList.add(Var.valueOf(jselement).getPOJO());
      }

    } else {
      myList = getSingleList(getPOJO());
    }

    return Var.valueOf(myList);
  }

  public boolean isNative() {
    switch (getType()) {
      case STRING:
      case INT:
      case DOUBLE:
      case BOOLEAN:
      case DATETIME:
        return true;
    }

    return false;
  }

  public Var getPOJO() {
    if (isNative()) {
      Map map = new LinkedHashMap();
      map.put("value", getObject());
      if (id == null) {
        map.put("id", getObjectAsString());
      } else {
        map.put("id", id);
      }
      return Var.valueOf(map);
    } else {
      return this;
    }
  }

  public Iterator<Var> iterator() {
    return getObjectAsList().iterator();
  }

  /**
   * If this object is a linked list, then calling this method will return the Var at the index
   * indicated
   *
   * @param index the index of the Var to read (0 based)
   * @return the Var at that index
   */
  public Var get(int index) {
    switch (getType()) {
      case LIST: {
        return ((LinkedList<Var>) getObject()).get(index);
      }
    }

    return VAR_NULL;
  }

  /**
   * If this object is a linked list, then calling this method will return the size of the linked
   * list.
   *
   * @return size of list
   */
  public int size() {
    switch (getType()) {
      case NULL:
        return 0;
      case LIST: {
        return ((LinkedList<Var>) getObject()).size();
      }
      default: {
        if (getObject() instanceof Map) {
          return ((Map) getObject()).size();
        }

        else if (getObject() instanceof DataSource) {
          return ((DataSource) getObject()).getPage().getContent().size();
        }
      }
    }

    return getObjectAsString().length();
  }

  public int length() {
    return getObjectAsString().length();
  }

  public void trim() {
    setObject(getObjectAsString().trim());
  }

  /**
   * Set the value of of a list at the index specified. Note that this is only value if this object
   * is a list and also note that index must be in bounds.
   *
   * @param index the index into which the Var will be inserted
   * @param var the var to insert
   */
  public void set(int index, Var var) {
    ((LinkedList<Var>) getObject()).add(index, var);
  }

  /**
   * Add all values from one List to another. Both lists are Var objects that contain linked lists.
   *
   * @param var The list to add
   */
  public void addAll(Var var) {
    ((LinkedList<Var>) getObject()).addAll(var.getObjectAsList());
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 43 * hash + Objects.hashCode(this._type);
    hash = 43 * hash + Objects.hashCode(this._object);
    return hash;
  }

  /**
   * Test to see if this object equals another one. This is done by converting both objects to
   * strings and then doing a string compare.
   *
   * @return true if equals
   */
  @Override
  public boolean equals(Object obj) {
    return this.compareTo(Var.valueOf(obj)) == 0;
  }

  public void inc(Object value) {
    Object result = null;

    switch (getType()) {
      case DATETIME: {
        getObjectAsDateTime().add(Calendar.DAY_OF_MONTH, Var.valueOf(value).getObjectAsInt());
        break;
      }
      case INT: {
        result = getObjectAsLong() + Var.valueOf(value).getObjectAsLong();
        break;
      }
      default: {
        result = getObjectAsDouble() + Var.valueOf(value).getObjectAsDouble();
      }

    }

    if (result != null) {
      setObject(result);
    }
  }

  public void multiply(Object value) {
    Object result = null;

    switch (getType()) {
      case INT: {
        result = getObjectAsLong() * Var.valueOf(value).getObjectAsLong();
        break;
      }
      default: {
        result = getObjectAsDouble() * Var.valueOf(value).getObjectAsDouble();
      }

    }

    if (result != null) {
      setObject(result);
    }
  }

  public Var append(Object value) {
    Object result = getObjectAsString() + (value != null ? value.toString() : "");
    setObject(result);
    return this;
  }

  /**
   * Check to see if this Var is less than some other var.
   *
   * @param var the var to compare to
   * @return true if it is less than
   */
  public boolean lessThan(Var var) {
    return this.compareTo(var) < 0;
  }

  /**
   * Check to see if this var is less than or equal to some other var
   *
   * @param var the var to compare to
   * @return true if this is less than or equal to var
   */
  public boolean lessThanOrEqual(Var var) {
    return this.compareTo(var) <= 0;
  }

  /**
   * Check to see if this var is greater than a given var.
   *
   * @param var the var to compare to.
   * @return true if this object is grater than the given var
   */
  public boolean greaterThan(Var var) {
    return this.compareTo(var) > 0;
  }

  /**
   * Check to see if this var is greater than or equal to a given var
   *
   * @param var the var to compare to
   * @return true if this var is greater than or equal to the given var
   */
  public boolean greaterThanOrEqual(Var var) {
    return this.compareTo(var) >= 0;
  }

  /**
   * Compare this object's value to another
   *
   * @param var the object to compare to
   * @return the value 0 if this is equal to the argument; a value less than 0 if this is
   * numerically less than the argument; and a value greater than 0 if this is numerically greater
   * than the argument (signed comparison).
   */
  @Override
  public int compareTo(Var var) {
    var = Var.valueOf(var);

    try {
      if (getType() == Type.NULL && var.getType() == Type.NULL) {
        return 0;
      } else if (getType() == Type.NULL && var.getType() != Type.NULL) {
        return -1;
      } else {

        if (var == this) {
          return 0;
        }

        if (this.getObject().equals(var.getObject())) {
          return 0;
        }

        switch (getType()) {
          case STRING:
            return this.getObjectAsString().compareTo(var.getObjectAsString());
          case INT:
            if (var.getType().equals(Var.Type.INT)) {
              return ((Long) this.getObjectAsLong()).compareTo(var.getObjectAsLong());
            } else {
              return ((Double) this.getObjectAsDouble()).compareTo(var.getObjectAsDouble());
            }
          case DOUBLE:
            return ((Double) this.getObjectAsDouble()).compareTo(var.getObjectAsDouble());
          case BOOLEAN:
            return this.getObjectAsBoolean().compareTo(var.getObjectAsBoolean());
          case DATETIME:
            return this.getObjectAsDateTime().compareTo(var.getObjectAsDateTime());
          default:
            if (this.getObject() instanceof Comparable) {
              return Comparable.class.cast(this.getObject()).compareTo(var.getObject());
            }
        }
      }
    } catch (Exception e) {
      // Abafa
    }

    return -1;
  }

  /**
   * Convert this Var to a string format.
   *
   * @return the string format of this var
   */
  @Override
  public String toString() {
    switch (getType()) {
      case STRING:
        return getObject().toString();
      case INT:
        Long i = (Long) getObject();
        return i.toString();
      case DOUBLE:
        Double d = (double) _object;
        return _formatter.format(d);
      case DATETIME:
        return Utils.getDateFormat().format(((Calendar) getObject()).getTime());
      case LIST:
        LinkedList<Var> ll = (LinkedList) getObject();
        StringBuilder sb = new StringBuilder();
        if (ll.isEmpty()) {
          return "[]";
        }
        sb.append("[");
        for (Var v : ll) {
          sb.append(v.toString());
          sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
      case NULL:
        return "";
      default:
        if (getObject() == null) {
          return "";
        } else if (getObject() instanceof Map) {
          java.lang.reflect.Type gsonType = new TypeToken<Map<String, Object>>() {
          }.getType();
          return new Gson().toJson((Map) getObject(), gsonType);
        } else {
          return getObject().toString();
        }
    }
  }

  public Var negate() {
    if (getObjectAsBoolean()) {
      return VAR_FALSE;
    }

    return VAR_TRUE;
  }

  /**
   * Internal method for inferring the "object type" of this object. When it is done, it sets the
   * private member value of _type. This will be referenced later on when various method calls are
   * made on this object.
   */
  private void inferType() {
    if (_object == null) {
      _type = Type.NULL;
    } else if (_object instanceof Var) {
      Var oldObj = (Var) _object;
      _type = oldObj.getType();
      _object = oldObj.getObject();
      if (id == null) {
        id = oldObj.id;
      }
    } else if (_object instanceof String || _object instanceof StringBuilder
        || _object instanceof StringBuffer ||
        _object instanceof Character) {
      _type = Type.STRING;
      _object = _object.toString();
    } else if (_object instanceof Boolean) {
      _type = Type.BOOLEAN;
    } else if (_object instanceof Date) {
      Date date = (Date) _object;
      _type = Type.DATETIME;
      _object = Calendar.getInstance();
      ((Calendar) _object).setTime(date);
    } else if (_object instanceof Calendar) {
      _type = Type.DATETIME;
    } else if (_object instanceof Long) {
      _type = Type.INT;
    } else if (_object instanceof Integer) {
      _type = Type.INT;
      _object = Long.valueOf((Integer) _object);
    } else if (_object instanceof Double) {
      _type = Type.DOUBLE;
    } else if (_object instanceof Float) {
      _type = Type.DOUBLE;
      _object = Double.valueOf((Float) _object);
    } else if (_object instanceof BigDecimal) {
      if (((BigDecimal) _object).scale() == 0) {
        _type = Type.INT;
        _object = ((BigDecimal) _object).longValue();
      } else {
        _type = Type.DOUBLE;
        _object = ((BigDecimal) _object).doubleValue();
      }
    } else if (_object instanceof BigInteger) {
      _type = Type.INT;
      _object = ((BigInteger) _object).longValue();
    } else if (_object instanceof LinkedList) {
      _type = Type.LIST;
    } else if (_object instanceof List) {
      _type = Type.LIST;
      _object = new LinkedList<>((List) _object);
    } else if (_object instanceof JsonPrimitive) {
      _object = getPrimitiveValue((JsonPrimitive) _object);
      inferType();
    } else {
      _type = Type.UNKNOWN;
    }
  }

  @Override
  public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (id != null) {
      gen.writeStartObject();
      gen.writeObjectField(id, _object);
      gen.writeEndObject();
    } else {
      gen.writeObject(_object);
    }
  }

  @Override
  public void serializeWithType(JsonGenerator gen, SerializerProvider serializers,
      TypeSerializer typeSer)
      throws IOException {
    if (id != null) {
      gen.writeStartObject();
      gen.writeObjectField(id, _object);
      gen.writeEndObject();
    } else {
      gen.writeObject(_object);
    }
  }

  public LinkedList<String> keySet() {
    LinkedList<String> keys = new LinkedList<>();

    if (getObject() != null) {
      if (getObject() instanceof Map) {
        ((Map) getObject()).keySet().stream().forEach(c -> keys.add(String.valueOf(c)));
      } else if (getObject() instanceof JsonObject) {
        ((JsonObject) getObject()).entrySet().stream().forEach(c -> keys.add(c.getKey()));
      } else {
        try {
          Class<?> clazz = getObject().getClass();
          BeanInfo info = Introspector.getBeanInfo(clazz);
          PropertyDescriptor[] props = info.getPropertyDescriptors();

          for (PropertyDescriptor pd : props) {
            if (!pd.getName().equals("class")) {
              keys.add(pd.getName());
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }

    return keys;
  }

  public Var getField(String field) {
    try {
      return Operations.getJsonOrMapField(this, Var.valueOf(field));
    } catch (Exception e) {
      // Abafa
    }

    return VAR_NULL;
  }

  public String getStringField(String field) {
    return getField(field).getObjectAsString();
  }

  public void setField(String field, Object value) {
    try {
      Operations.setJsonOrMapField(this, Var.valueOf(field), Var.valueOf(value));
    } catch (Exception e) {
      // Abafa
    }
  }

  public void updateWith(Object obj) {
    Var varObj = Var.valueOf(obj);
    for (String key : varObj.keySet()) {
      this.setField(key, varObj.getField(key));
    }
  }

  public Boolean isNull() {
    return (_object == null) || (_object instanceof JsonNull);
  }

  public Boolean isEmpty() {
    if (getType() == Type.STRING) {
      return getObjectAsString().trim().isEmpty();
    }

    return size() == 0;
  }

  public Boolean isEmptyOrNull() {
    return isNull() || isEmpty();
  }

  public enum Type {
    STRING, INT, DOUBLE, LIST, NULL, UNKNOWN, BOOLEAN, DATETIME
  }

}
