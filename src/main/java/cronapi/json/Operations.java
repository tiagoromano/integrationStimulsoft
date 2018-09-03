package cronapi.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.ParamMetaData;
import cronapi.Utils;
import cronapi.Var;
import cronapi.database.DataSource;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

@CronapiMetaData(category = CategoryType.JSON, categoryTags = {"Json"})
public class Operations {

  public static final Configuration GSON_CONFIGURATION = Configuration
      .builder()
      .mappingProvider(new GsonMappingProvider())
      .jsonProvider(new GsonJsonProvider())
      .build();

  @CronapiMetaData(type = "function", name = "{{createObjectJson}}", nameTags = {
      "createObjectJson"}, description = "{{functionToCreateObjectJson}}", returnType = ObjectType.JSON)
  public static final Var createObjectJson() throws Exception {
    Var value = new Var(new JsonObject());
    return value;
  }

  @CronapiMetaData(type = "function", name = "{{getJsonOrMapField}}", nameTags = {
      "getJsonOrMapField"}, description = "{{functionToGetJsonOrMapField}}", returnType = ObjectType.OBJECT)
  public static final Var getJsonOrMapField(
      @ParamMetaData(type = ObjectType.OBJECT, description = "{{mapOrJsonVar}}") Var mapVar,
      @ParamMetaData(type = ObjectType.STRING, description = "{{pathKey}}") Var keyVar)
      throws Exception {
    Var value = Var.VAR_NULL;
    Object obj = mapVar.getObject();
    String key = keyVar.toString();

    if (obj instanceof DataSource) {
      obj = ((DataSource) obj).getObject();
    }

    if (key.startsWith("$")) {
      JsonElement jsonToBeSearched = mapVar.getObjectAsJson();
      Object result = JsonPath.using(GSON_CONFIGURATION).parse(jsonToBeSearched).read(key);
      return Var.valueOf(result);
    }

    String[] path = key.toString().split("\\.");
    for (int i = 0; i < path.length; i++) {
      String k = path[i];
      if (obj != null) {
        if (i == path.length - 1) {
          value = Var.valueOf(Utils.mapGetObjectPathExtractElement(obj, k, false));
        } else {
          obj = Utils.mapGetObjectPathExtractElement(obj, k, false);
        }
      }
    }
    return value;
  }

  @CronapiMetaData(type = "function", name = "{{setJsonOrMapField}}", nameTags = {
      "setJsonOrMapField"}, description = "{{functionToSetJsonOrMapField}}", returnType = ObjectType.VOID)
  public static final void setJsonOrMapField(
      @ParamMetaData(type = ObjectType.OBJECT, description = "{{mapOrJsonVar}}") Var mapVar,
      @ParamMetaData(type = ObjectType.STRING, description = "{{pathKey}}") Var keyVar,
      @ParamMetaData(type = ObjectType.OBJECT, description = "{{valueToBetSet}}") Var value)
      throws Exception {
    Object obj = mapVar.getObject();
    Object key = keyVar.getObject();

    if (obj instanceof DataSource) {
      obj = ((DataSource) obj).getObject();
    }

    String[] path = key.toString().split("\\.");
    for (int i = 0; i < path.length; i++) {
      String k = path[i];
      if (obj != null) {
        if (i == path.length - 1) {
          Utils.mapSetObject(obj, k, value);
        } else {
          obj = Utils.mapGetObjectPathExtractElement(obj, k, true);
        }
      }
    }
  }

  @CronapiMetaData(type = "function", name = "{{toJson}}", nameTags = {
      "toJson"}, description = "{{functionToJson}}", returnType = ObjectType.JSON)
  public static final Var toJson(
      @ParamMetaData(type = ObjectType.OBJECT, description = "{{valueToBeRead}}") Var valueToBeRead)
      throws Exception {
    return Var.valueOf(valueToBeRead.getObject(JsonElement.class));
  }

  @CronapiMetaData(type = "function", name = "{{JSONtoList}}", nameTags = {"toList",
      "Para Lista"}, description = "{{functionToList}}", returnType = ObjectType.LIST)
  public static final Var toList(
      @ParamMetaData(type = ObjectType.OBJECT, description = "{{valueToBeRead}}") Var valueToBeRead)
      throws Exception {
    return toMap(valueToBeRead);
  }

  @CronapiMetaData(type = "function", name = "{{toMap}}", nameTags = {"toMap",
      "Para Mapa"}, description = "{{functionToMap}}", returnType = ObjectType.MAP)
  public static final Var toMap(
      @ParamMetaData(type = ObjectType.OBJECT, description = "{{valueToBeRead}}") Var valueToBeRead)
      throws Exception {
    Object obj = null;
    String content = "";
    Gson c = new Gson();
    if (valueToBeRead.getObject() instanceof String) {
      content = valueToBeRead.getObjectAsString();
    } else if (valueToBeRead.getObject() instanceof FileInputStream) {
      content = cronapi.io.Operations.fileReadAll(valueToBeRead).getObjectAsString();
    }

    if (content.startsWith("[")) {
      obj = c.fromJson(content, List.class);
    } else {
      obj = c.fromJson(content, Map.class);
    }

    return Var.valueOf(obj);
  }

}
