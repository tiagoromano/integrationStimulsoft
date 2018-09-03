package cronapi.object;

import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.ParamMetaData;
import cronapi.Var;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Classe que representa ...
 *
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-07-06
 */
@CronapiMetaData(category = CategoryType.OBJECT, categoryTags = {"Object", "Objeto"})
public class Operations {

  @CronapiMetaData(type = "function", name = "{{getObjectFieldName}}", nameTags = {
      "getObjectFieldName"}, description = "{{getObjectFieldDescription}}", returnType = ObjectType.OBJECT, wizard = "procedures_get_field")
  public static final Var getObjectField(
      @ParamMetaData(blockType = "variables_get", type = ObjectType.OBJECT, description = "{{getObjectFieldParam0}}") Var objVar,
      @ParamMetaData(blockType = "procedures_get_field_object", type = ObjectType.STRING, description = "{{getObjectFieldParam1}}") Var keyVar)
      throws Exception {
    return cronapi.json.Operations.getJsonOrMapField(objVar, keyVar);
  }

  @CronapiMetaData(type = "function", name = "{{setObjectFieldName}}", nameTags = {
      "seObjectField"}, description = "{{setObjectFieldDescription}}", returnType = ObjectType.VOID, wizard = "procedures_set_field")
  public static final void setObjectField(
      @ParamMetaData(blockType = "variables_get", type = ObjectType.OBJECT, description = "{{setObjectFieldParam0}}") Var objVar,
      @ParamMetaData(blockType = "procedures_get_field_object", type = ObjectType.STRING, description = "{{setObjectFieldParam1}}") Var keyVar,
      @ParamMetaData(type = ObjectType.OBJECT, description = "{{setObjectFieldParam2}}") Var value)
      throws Exception {
    cronapi.json.Operations.setJsonOrMapField(objVar, keyVar, value);
  }

  @CronapiMetaData(type = "function", name = "{{newObject}}", nameTags = {"newObject",
      "NovoObjeto"}, description = "{{newObjectDescription}}", params = {"{{object}}",
      "{{params}}"}, paramsType = {
      ObjectType.STRING,
      ObjectType.MAP}, returnType = ObjectType.OBJECT, arbitraryParams = true, wizard = "procedures_createnewobject_callreturn")
  public static final Var newObject(Var object, Var... params) throws Exception {
    if (!object.equals(Var.VAR_NULL)) {
      String className = object.getObjectAsString();
      Class<?> c = Class.forName(className);

      Class<?> builderClass = Arrays.stream(c.getDeclaredClasses())
          .filter(e -> e.getSimpleName().equals("Builder"))
          .findFirst()
          .orElse(null);

      boolean hasDefaultConstructor = Arrays.stream(c.getConstructors())
          .anyMatch(e -> e.isAccessible() && e.getParameterCount() == 0);

      Var returnObject;

      if (!hasDefaultConstructor && builderClass != null) {
        Object builderObject = builderClass.newInstance();
        for (Var param : params) {
          if (param.isNull() || param.getObject() == null) {
            continue;
          }
          Method method = Arrays.stream(builderClass.getMethods())
              .filter(m -> m.getName().equals(param.getId()) && m.getParameterCount() == 1)
              .findFirst()
              .orElse(null);
          if (method != null) {
            method.invoke(builderObject, param.getTypedObject(method.getParameterTypes()[0]));
          }
        }

        returnObject = Var.valueOf(builderClass.getMethod("build").invoke(builderObject));
      } else {
        returnObject = new Var((Object) c.newInstance());
        for (Var param : params) {
          returnObject.setField(param.getId(), param.getObject());
        }
      }
      return returnObject;
    }
    return Var.VAR_NULL;
  }

}
