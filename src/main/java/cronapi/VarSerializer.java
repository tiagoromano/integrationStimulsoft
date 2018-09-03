package cronapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

public class VarSerializer implements JsonSerializer<Var> {

  @Override
  public JsonElement serialize(Var var, Type type,
      JsonSerializationContext jsonSerializationContext) {
    return new Gson().toJsonTree(var.getObject());
  }
}
