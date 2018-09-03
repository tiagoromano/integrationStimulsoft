package cronapi.database;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JPQLConverter {
  
  private static Map<String, String> operators = new HashMap<String, String>();
  static {
    operators.put("in", "IN (%s)");
    operators.put("not_in", "NOT IN (%s)");
    operators.put("equal", "= %s");
    operators.put("not_equal", "<> %s");
    operators.put("begins_with", "LIKE (%s)");
    operators.put("not_begins_with", "NOT LIKE (%s)");
    operators.put("ends_with", "LIKE (%s)");
    operators.put("not_ends_with", "NOT LIKE (%s)");
    operators.put("contains", "LIKE (%s)");
    operators.put("not_contains", "NOT LIKE (%s)");
    operators.put("is_empty", "= ''");
    operators.put("is_not_empty", "<> ''");
    operators.put("is_null", "IS NULL");
    operators.put("is_not_null", "IS NOT NULL");
    operators.put("less", "< %s");
    operators.put("less_or_equal", "<= %s");
    operators.put("greater", "> %s");
    operators.put("greater_or_equal", ">= %s");
    operators.put("between", "BETWEEN %s");
    operators.put("not_between", "NOT BETWEEN %s");
  }
  private static Map<String, String> functions = new HashMap<String, String>();
  static {
    functions.put("get", "%s");
    functions.put("min", "MIN(%s)");
    functions.put("max", "MAX(%s)");
    functions.put("avg", "AVG(%s)");
    functions.put("sum", "SUM(%s)");
    functions.put("count", "COUNT(%s)");
  }
  
  public static String getAliasFromSql(String sql) {
    String aux = sql.replaceAll("\n", " ").replaceAll("\t", " ").replaceAll("\r", " ");
    String alias = "";
    Pattern pattern = Pattern.compile("\\bfrom\\s*[A-Za-z0-9_.]*\\s*[A-Za-z0-9_.]*");
    Matcher matcher = pattern.matcher(aux);
    if(matcher.find()) {
      String[] splited = matcher.group().split(" ");
      alias = splited[splited.length - 1];
      if(alias.toLowerCase().trim().equals("where"))
        alias = "";
    }
    return alias;
  }
  
  public static String getTableFromSql(String sql) {
    String aux = sql.replaceAll("\n", " ").replaceAll("\t", " ").replaceAll("\r", " ");
    String table = "";
    Pattern pattern = Pattern.compile("(\\bfrom|\\bFROM|\\bupdate|\\bUPDATE)\\s*[A-Za-z0-9_.]*\\s*[A-Za-z0-9_.]*");
    Matcher matcher = pattern.matcher(aux);
    if(matcher.find()) {
      String[] splited = matcher.group().split(" ");
      if(splited.length > 2)
        table = splited[splited.length - 2];
      else
        table = splited[splited.length - 1];
    }
    return table;
  }
  
  public static JsonObject jsonFromTable(String tableName, boolean createAlias, String type) {
    String alias = "";
    if(createAlias)
      alias = tableName.substring(0, 1).toLowerCase();
    JsonObject json = new JsonObject();
    json.addProperty("isValid", true);
    
    JsonArray rulesEntity = new JsonArray();
    JsonObject rulesEntityObject = new JsonObject();
    rulesEntityObject.addProperty("entity", tableName);
    rulesEntityObject.addProperty("alias", alias);
    rulesEntity.add(rulesEntityObject);
    json.add("rulesEntity", rulesEntity);
    
    JsonArray rulesSelect = new JsonArray();
    if(type == "select") {
      JsonObject rulesSelectBody = new JsonObject();
      rulesSelectBody.addProperty("func", "get");
      rulesSelectBody.addProperty("field", alias);
      rulesSelect.add(rulesSelectBody);
    }
    json.add("rulesSelect", rulesSelect);
    
    json.add("rulesGroupBy", new JsonArray());
    json.add("rulesHaving", new JsonArray());
    json.add("rulesOrderBy", new JsonArray());
    
    JsonObject rule = new JsonObject();
    rule.addProperty("condition", "AND");
    rule.addProperty("not", false);
    rule.addProperty("valid", true);
    rule.add("rules", new JsonArray());
    json.add("rules", rule);
    return json;
  }
  
  public static String sqlFromJson(JsonObject jsonObject) {
    
    String type = "select";
    if(jsonObject.get("type") != null && !jsonObject.get("type").isJsonNull())
      type = jsonObject.get("type").getAsString().toLowerCase();
    
    switch(type) {
      case "select":
        return sqlFromJsonSELECT(jsonObject);
      case "update":
        return sqlFromJsonUPDATE(jsonObject);
      case "delete":
        return sqlFromJsonDELETE(jsonObject);
      default:
        return "";
    }
    
  }
  
  private static String sqlFromJsonDELETE(JsonObject jsonObject) {
    String sqlBase = "delete from %s %s";
    String entityWithAlias = getEntities(jsonObject.get("rulesEntity").getAsJsonArray()).trim();
    String where = getCondition(jsonObject.get("rules").getAsJsonObject()).trim();
    
    sqlBase = String.format(sqlBase, entityWithAlias, where);
    return sqlBase.trim();
  }
  
  private static String sqlFromJsonUPDATE(JsonObject jsonObject) {
    String sqlBase = "update %s set %s %s";
    String entityWithAlias = getEntities(jsonObject.get("rulesEntity").getAsJsonArray()).trim();
    String fields = getFieldsForUpdate(jsonObject.get("rulesSelect").getAsJsonArray()).trim();
    String where = getCondition(jsonObject.get("rules").getAsJsonObject()).trim();
    
    sqlBase = String.format(sqlBase, entityWithAlias, fields, where);
    return sqlBase.trim();
  }
  
  private static String sqlFromJsonSELECT(JsonObject jsonObject) {
    String sqlBase = "select %s from %s %s %s %s %s";
    
    String fields = getFields(jsonObject.get("rulesSelect").getAsJsonArray());
    String entityWithAlias = "";
    if(jsonObject.get("entity") != null && !jsonObject.get("entity").isJsonNull()) {
      entityWithAlias = String
              .format("%s %s", jsonObject.get("entity").getAsString(), jsonObject.get("alias").getAsString()).trim();
    }
    else {
      entityWithAlias = getEntities(jsonObject.get("rulesEntity").getAsJsonArray()).trim();
    }
    String where = getCondition(jsonObject.get("rules").getAsJsonObject()).trim();
    String groupBy = getGroup(jsonObject.get("rulesGroupBy").getAsJsonArray()).trim();
    String having = getHaving(jsonObject.get("rulesHaving").getAsJsonArray()).trim();
    String orderBy = getOrder(jsonObject.get("rulesOrderBy").getAsJsonArray()).trim();
    
    sqlBase = String.format(sqlBase, fields, entityWithAlias, where, groupBy, having, orderBy);

    if(jsonObject.get("multiTenant") != null && !jsonObject.get("multiTenant").isJsonNull() && !jsonObject.get("multiTenant").getAsBoolean()) {
      sqlBase = sqlBase.trim() + " /*notenant*/";
    }

    return sqlBase.trim();
  }
  
  private static String getHaving(JsonArray rulesHaving) {
    StringBuilder having = new StringBuilder();
    rulesHaving.forEach(havingBy -> {
      JsonObject havingByObj = havingBy.getAsJsonObject();
      String funcWithField = String.format(functions.get(havingByObj.get("func").getAsString()),
              havingByObj.get("field").getAsString());
      String operatorWithValue = String.format(operators.get(havingByObj.get("operator").getAsString()),
              havingByObj.get("value").getAsString());
      having.append(String.format("%s %s, ", funcWithField, operatorWithValue));
    });
    String havingBy = having.toString();
    if(havingBy.length() > 0) {
      havingBy = havingBy.substring(0, havingBy.length() - 2);
      havingBy = String.format("having %s", havingBy);
    }
    return havingBy;
  }
  
  private static String getOrder(JsonArray rulesOrderBy) {
    StringBuilder order = new StringBuilder();
    rulesOrderBy.forEach(orderBy -> {
      JsonObject orderByObj = orderBy.getAsJsonObject();
      String funcField = String.format(functions.get(orderByObj.get("func").getAsString()),
              orderByObj.get("field").getAsString());
      order.append(String.format("%s %s, ", funcField, orderByObj.get("order").getAsString()));
    });
    String orderBy = order.toString();
    if(orderBy.length() > 0) {
      orderBy = orderBy.substring(0, orderBy.length() - 2);
      orderBy = String.format("order by %s", orderBy);
    }
    return orderBy;
  }
  
  private static String getGroup(JsonArray rulesGroupBy) {
    StringBuilder group = new StringBuilder();
    rulesGroupBy.forEach(groupBy -> {
      JsonObject groupByObj = groupBy.getAsJsonObject();
      group.append(String.format("%s, ", groupByObj.get("field").getAsString()));
    });
    String groupBy = group.toString();
    if(groupBy.length() > 0) {
      groupBy = groupBy.substring(0, groupBy.length() - 2);
      groupBy = String.format("group by %s", groupBy);
    }
    return groupBy;
  }
  
  private static String getFields(JsonArray rulesSelect) {
    StringBuilder fields = new StringBuilder();
    rulesSelect.forEach(rule -> {
      JsonObject ruleObj = rule.getAsJsonObject();
      String field = String.format("%s, ",
              String.format(functions.get(ruleObj.get("func").getAsString()), ruleObj.get("field").getAsString()));
      fields.append(field);
    });
    String allFields = fields.toString();
    allFields = allFields.substring(0, allFields.length() - 2);
    return allFields;
  }
  
  private static String getFieldsForUpdate(JsonArray rulesSelect) {
    StringBuilder fields = new StringBuilder();
    rulesSelect.forEach(rule -> {
      JsonObject ruleObj = rule.getAsJsonObject();
      String field = String.format("%s = %s, ", ruleObj.get("field").getAsString(),
              getParameter(ruleObj.get("value").getAsString(), ruleObj.get("operator").getAsString(),
                      ruleObj.get("type").getAsString()));
      fields.append(field);
    });
    String allFields = fields.toString();
    if(allFields.length() > 0)
      allFields = allFields.substring(0, allFields.length() - 2);
    return allFields;
  }
  
  private static String getEntities(JsonArray rulesEntity) {
    StringBuilder entities = new StringBuilder();
    rulesEntity.forEach(rule -> {
      JsonObject ruleObj = rule.getAsJsonObject();
      String entity = String.format("%s %s, ", ruleObj.get("entity").getAsString(), ruleObj.get("alias").getAsString());
      entities.append(entity);
    });
    String allEntities = entities.toString();
    allEntities = allEntities.substring(0, allEntities.length() - 2);
    return allEntities;
  }
  
  private static String getCondition(JsonObject cond) {
    return getCondition(cond, true);
  }
  
  private static String getCondition(JsonObject cond, boolean canAddWhere) {
    StringBuilder sbRules = new StringBuilder();
    String rules = "";
    if(cond.get("condition") != null) {
      String condition = cond.get("condition").getAsString();
      boolean not = cond.get("not").getAsBoolean();
      cond.get("rules").getAsJsonArray().forEach(rule -> {
        JsonObject ruleObj = rule.getAsJsonObject();
        sbRules.append(String.format("%s %s ", getRule(ruleObj), condition));
      });
      
      rules = sbRules.toString();
      if(rules.length() > 0) {
        rules = rules.substring(0, rules.length() - condition.length() - 1);
        if(not)
          rules = String.format("NOT (%s)", rules);
        
      }
    }
    if(rules.length() > 0 && canAddWhere)
      rules = String.format("where %s", rules);
    return rules;
  }
  
  private static String getRule(JsonObject ru) {
    if(ru.get("condition") != null && !ru.get("condition").isJsonNull())
      return String.format("(%s)", getCondition(ru, false));
    else {
      String rule = String.format("%s %s", ru.get("field").getAsString(),
              operators.get(ru.get("operator").getAsString()));
      if(rule.contains("%s")) {
        String type = ru.get("type").getAsString();
        if(ru.get("valueIsField") != null && ru.get("valueIsField").getAsBoolean())
          type = "field";
        rule = String.format(rule, getValue(ru.get("value"), ru.get("operator").getAsString(), type));
      }
      return rule;
    }
  }
  
  private static String getValue(JsonElement jsonValue, String operator, String type) {
    if(jsonValue instanceof JsonArray) {
      StringBuilder values = new StringBuilder();
      String join = "integer".equals(type) || "date".equals(type) || "datetime".equals(type) ? " AND " : ", ";
      jsonValue.getAsJsonArray().forEach(value -> {
        values.append(String.format("%s%s", getParameter(value.getAsString(), operator, type), join));
      });
      return values.substring(0, values.length() - join.length());
    }
    else {
      return getParameter(jsonValue.getAsString(), operator, type);
    }
  }
  
  private static String getParameter(String value, String operator, String type) {
    value = value.trim();
    
    if(type.equals("string")) {
      value = value.replaceAll("'", "''");
      if(!value.startsWith(":"))
        value = String.format("'%s'", value);
      if(operator.contains("begins_with"))
        return String.format("CONCAT(%s, '%%')", value);
      else if(operator.contains("ends_with"))
        return String.format("CONCAT('%%', %s)", value);
      else if(operator.contains("contains"))
        return String.format("CONCAT('%%', %s, '%%')", value);
      
      return value;
    }
    else if(value.startsWith(":"))
      return value;
    else
      return value;
  }
}