package cronapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cronapi.util.Operations;

import java.io.InputStream;
import java.io.InputStreamReader;

public class AppConfig {
  public static boolean FORCE_METADATA = false;
  public static boolean FORCE_LOCAL_ENTITIES = false;
  private static JsonObject JSON;

  static {
    JSON = loadJSON();
  }

  private static JsonObject loadJSON() {
    ClassLoader classLoader = QueryManager.class.getClassLoader();
    try (InputStream stream = classLoader.getResourceAsStream("META-INF/app.config")) {
      InputStreamReader reader = new InputStreamReader(stream);
      JsonElement jsonElement = new JsonParser().parse(reader);
      return jsonElement.getAsJsonObject();
    } catch (Exception e) {
      return new JsonObject();
    }
  }

  public static JsonObject getJSON() {
    if (Operations.IS_DEBUG) {
      return loadJSON();
    } else {
      return JSON;
    }
  }

  public static boolean isNull(JsonElement value) {
    return value == null || value.isJsonNull();
  }

  public static boolean exposeLocalEntities() {
    JsonObject config = getJSON();
    if (!isNull(config.get("odata"))) {
      JsonElement elem = config.get("odata").getAsJsonObject().get("exposeEntities");
      return (!isNull(elem) && elem.getAsBoolean()) || FORCE_LOCAL_ENTITIES;
    }

    return true;
  }

  public static boolean exposeMetadada() {
    JsonObject config = getJSON();
    if (!isNull(config.get("odata"))) {
      JsonElement elem = config.get("odata").getAsJsonObject().get("exposeMetadata");
      return (!isNull(elem) && elem.getAsBoolean()) || FORCE_METADATA;
    }

    return true;
  }

  public static String exposeMetadadaSecurity() {
    JsonObject config = getJSON();
    if (!isNull(config.get("odata"))) {
      JsonElement elem = config.get("odata").getAsJsonObject().get("exposeMetadadaSecurity");
      return !isNull(elem) ? elem.getAsString() : null;
    }

    return null;
  }

  public static String exposeEnitiesSecurity() {
    JsonObject config = getJSON();
    if (!isNull(config.get("odata"))) {
      JsonElement elem = config.get("odata").getAsJsonObject().get("exposeEnitiesSecurity");
      return !isNull(elem) ? elem.getAsString() : null;
    }

    return null;
  }

  public static String token() {
    JsonObject config = getJSON();
    if (!isNull(config.get("security"))) {
      JsonElement elem = config.get("security").getAsJsonObject().get("token");
      if (!isNull(elem)) {
        return elem.getAsString();
      }
    }

    return "9SyECk96oDsTmXfogIieDI0cD/8FpnojlYSUJT5U9I/FGVmBz5oskmjOR8cbXTvoPjX+Pq/T/b1PqpHX0lYm0oCBjXWICA==";
  }

  public static long tokenExpiration() {
    JsonObject config = getJSON();
    if (!isNull(config.get("security"))) {
      JsonElement elem = config.get("security").getAsJsonObject().get("tokenExpiration");
      if (!isNull(elem)) {
        return elem.getAsLong();
      }
    }

    return 3600L;
  }
}
