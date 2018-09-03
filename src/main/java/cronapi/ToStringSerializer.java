package cronapi;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.gson.JsonElement;

import cronapi.database.DataSource;

public class ToStringSerializer extends StdSerializer<Object> {

  public ToStringSerializer() {
    super(Object.class);
  }
  
  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    if(value != null) {
        gen.writeRawValue(value.toString());
    } else
      gen.writeObject(null);
  }
}
