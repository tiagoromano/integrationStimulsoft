package cronapi.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;

public class CronappVirtualBeanPropertyWriter extends BeanPropertyWriter {
  
  private Object value;
  
  public CronappVirtualBeanPropertyWriter(BeanPropertyWriter base, String name, Object value) {
    super(base, new SerializedString(name));
    this.value = value;
  }
  
  @Override
  public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider provider) throws Exception {
    JsonSerializer<Object> ser = null;
    Class<?> cls = value.getClass();
    PropertySerializerMap m = _dynamicSerializers;
    ser = m.serializerFor(cls);
    if(ser == null) {
      ser = _findAndAddDynamic(m, cls, provider);
    }
    
    gen.writeFieldName(_name);
    
    ser.serialize(value, gen, provider);
    
  }
}
