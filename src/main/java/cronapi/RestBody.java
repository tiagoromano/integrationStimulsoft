package cronapi;

import java.util.*;

public class RestBody {
  private Var[] inputs;
  private Map<String, Var> fields;
  
  public Var[] getInputs() {
    return inputs;
  }

  public Var getFirtsInput() {
    if (inputs != null && inputs.length > 0)
      return inputs[0];
    return null;
  }

  public Map<?,?> getEntityData() {
    return (Map<?,?>) getFirtsInput().getObject();
  }
  
  public void setInputs(Var[] inputs) {
    this.inputs = inputs;
  }
  
  public Map<String, Var> getFields() {
    return fields;
  }
  
  public void setFields(Map<String, Var> fields) {
    this.fields = fields;
  }

  public static RestBody parseBody(Map rawData) {
    return parseBody(rawData, (rawData.containsKey("inputs") && rawData.containsKey("fields")));
  }

  public static RestBody parseBody(Map rawData, boolean isFromDataSource) {
    if (!isFromDataSource) {
      Map map = rawData;
      rawData = new LinkedHashMap<>();
      rawData.put("fields", new LinkedHashMap<>());
      List list = new LinkedList();
      list.add(map);
      rawData.put("inputs", list);
    }

    return (RestBody) Var.valueOf(rawData).getObject(RestBody.class);
  }
}
