package cronapi.database;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;

import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.ParamMetaData;
import cronapi.RestClient;
import cronapi.Var;

/**
 * Classe que representa operações de acesso ao banco
 * 
 * @author Robson Ataíde
 * @version 1.0
 * @since 2017-05-05
 *
 */
@CronapiMetaData(category = CategoryType.DATABASE, categoryTags = { "Database", "Banco", "Dados", "Storage" })
public class Operations {
  
  @CronapiMetaData(type = "function", name = "{{datasourceQuery}}", nameTags = { "datasourceQuery", "openConnection",
      "abrirConsulta" }, description = "{{functionToQueryInDatasource}}", params = { "{{entity}}", "{{query}}",
          "{{paramsQueryTuples}}" }, paramsType = { ObjectType.STRING, ObjectType.STRING,
              ObjectType.LIST }, returnType = ObjectType.DATASET, arbitraryParams = true, wizard = "procedures_sql_callreturn")
  public static Var query(Var entity, Var query, Var ... params) {
    DataSource ds = new DataSource(entity.getObjectAsString());
    if(query == Var.VAR_NULL)
      ds.fetch();
    else {
      ds.filter(query.getObjectAsString(), params);
    }
    Var varDs = new Var(ds);
    return varDs;
  }
  
  public static Var queryPaged(Var entity, Var query, Var useRestPagination, Var ... params) {
    DataSource ds = new DataSource(entity.getObjectAsString());
    
    List<Var> finalParams = new LinkedList<>();
    for (Var p : params) 
      finalParams.add(p);
    
    PageRequest page = null;
    if (useRestPagination.getObjectAsBoolean()) {
      if(query != Var.VAR_NULL) {
        String queryString = RestClient.getRestClient().getRequest().getServletPath();
        if (queryString.indexOf("/api/cronapi/query/") > -1) {
          queryString = queryString.replace("/api/cronapi/query/", "");
          String[] splitedQueryString = queryString.split("/");
          if (splitedQueryString.length > 1) {
            for (int ix = 1; ix < splitedQueryString.length; ix++) {
              Var param = Var.valueOf("id"+(ix-1), splitedQueryString[ix]);
              finalParams.add(param);
            }
          }
        }
        
      }
      String pageFromRequest = RestClient.getRestClient().getRequest().getParameter("page");
      if (pageFromRequest == null || pageFromRequest.isEmpty())
        pageFromRequest = "0";
      String pageSizeFromRequest = RestClient.getRestClient().getRequest().getParameter("size");
      if (pageSizeFromRequest == null || pageSizeFromRequest.isEmpty())
        pageSizeFromRequest = "100";
      int pageNumber = Integer.parseInt(pageFromRequest);
      int pageSize = Integer.parseInt(pageSizeFromRequest);
      page = new PageRequest(pageNumber, pageSize);
    }
    
    if(query == Var.VAR_NULL)
      ds.fetch();
    else {
      if (page != null)
        ds.filter(query.getObjectAsString(), page, finalParams.toArray(new Var[0]));
      else
        ds.filter(query.getObjectAsString(), finalParams.toArray(new Var[0]));
    }
    Var varDs = new Var(ds);
    return varDs;
  }

  @CronapiMetaData(type = "function", name = "{{datasourceNext}}", nameTags = { "next", "avançar",
      "proximo" }, description = "{{functionToMoveCursorToNextPosition}}", params = {
          "{{datasource}}" }, paramsType = { ObjectType.DATASET }, returnType = ObjectType.VOID, displayInline = true)
  public static void next(Var ds) {
    ((DataSource)ds.getObject()).next();
  }
  
  @CronapiMetaData(type = "function", name = "{{datasourceHasData}}", nameTags = { "hasElement", "existeRegistro",
      "temRegistro" }, description = "{{functionToVerifyDataInCurrentPosition}}", params = {
          "{{datasource}}" }, paramsType = {
              ObjectType.DATASET }, returnType = ObjectType.BOOLEAN, displayInline = true)
  public static Var hasElement(Var ds) {
    if (ds.getObject() != null) {
      return Var.valueOf(((DataSource) ds.getObject()).getObject() != null);
    }

    return Var.VAR_FALSE;
  }
  
  @CronapiMetaData(type = "function", name = "{{datasourceClose}}", nameTags = { "close", "fechar", "limpar",
      "clear" }, description = "{{functionToCloseAndCleanDatasource}}", params = {
          "{{datasource}}" }, paramsType = { ObjectType.DATASET }, returnType = ObjectType.VOID, displayInline = true)
  public static void close(Var ds) {
    ((DataSource)ds.getObject()).clear();
  }
  
  @CronapiMetaData(type = "function", name = "{{datasourceUpdateField}}", nameTags = { "updateField", "atualizarCampo",
      "setField", "modificarCampo" }, description = "{{functionToUpdateFieldInDatasource}}", params = {
          "{{datasource}}", "{{fieldName}}", "{{fieldValue}}" }, paramsType = { ObjectType.DATASET, ObjectType.STRING,
              ObjectType.STRING }, returnType = ObjectType.VOID)
  public static void updateField(Var ds, Var fieldName, Var fieldValue) {
    ds.setField(fieldName.getObjectAsString(), fieldValue.getObjectAsString());
  }
  
  @CronapiMetaData(type = "function", name = "{{datasourceGetActiveData}}", nameTags = { "getElement",
      "obterElemento" }, description = "{{functionToDatasourceGetActiveData}}", params = {
          "{{datasource}}" }, paramsType = { ObjectType.DATASET }, returnType = ObjectType.OBJECT)
  public static Var getActiveData(Var ds) {
    return new Var(((DataSource)ds.getObject()).getObject());
  }
  
  @CronapiMetaData(type = "function", name = "{{datasourceInsert}}", nameTags = { "insert", "create", "novo", "inserir",
      "criar" }, description = "{{functionToInsertObjectInDatasource}}", params = { "{{datasource}}",
          "{{params}}" }, paramsType = { ObjectType.DATASET,
              ObjectType.LIST }, returnType = ObjectType.VOID, arbitraryParams = true, wizard = "procedures_sql_insert_callnoreturn")
  public static void insert(Var entity, Var ... params) {
    DataSource ds = new DataSource(entity.getObjectAsString());
    ds.insert();
    ds.updateFields(params);
    ds.save();
  }
  
  public static void insert(Var entity, Var object) {
    if(!object.equals(Var.VAR_NULL)) {
      DataSource ds = new DataSource(entity.getObjectAsString());
      ds.insert(object.getObjectAsMap());
      Object saved = ds.save();
      object.updateWith(saved);
    }
  }
  
  @CronapiMetaData(type = "function", name = "{{update}}", nameTags = { "update", "edit", "editar",
      "alterar" }, description = "{{functionToUpdateObjectInDatasource}}", params = { "{{datasource}}",
          "{{entity}}" }, paramsType = { ObjectType.DATASET,
              ObjectType.OBJECT }, returnType = ObjectType.VOID, arbitraryParams = true, wizard = "procedures_sql_update_callnoreturn")
  public static void update(Var entity, Var object) {
    if(!object.equals(Var.VAR_NULL)) {
      DataSource ds = new DataSource(entity.getObjectAsString());
      ds.filter(object, null);
      ds.update(new Var(object.getObjectAsMap()));
      Object saved = ds.save();
      object.updateWith(saved);
    }
  }
  
  @CronapiMetaData(type = "function", name = "{{datasourceRemove}}", nameTags = { "remove", "delete", "remover",
      "deletar", "excluir" }, description = "{{functionToRemoveObject}}", params = { "{{datasource}}",
          "{{entity}}" }, paramsType = { ObjectType.DATASET,
              ObjectType.OBJECT }, returnType = ObjectType.VOID, arbitraryParams = true, wizard = "procedures_sql_delete_callnoreturn")
  public static void remove(Var entity, Var object) {
    if(!object.equals(Var.VAR_NULL)) {
      DataSource ds = new DataSource(entity.getObjectAsString());
      ds.filter(object, null);
      ds.delete();
    }
  }
  
  @CronapiMetaData(type = "function", name = "{{datasourceGetField}}", nameTags = { "getField",
      "obterCampo" }, description = "{{functionToGetFieldOfCurrentCursorInDatasource}}", params = { "{{datasource}}",
          "{{fieldName}}" }, paramsType = { ObjectType.DATASET,
              ObjectType.STRING }, returnType = ObjectType.OBJECT, wizard = "procedures_get_field")
  public static Var getField(@ParamMetaData(blockType = "variables_get", type = ObjectType.OBJECT, description = "{{datasource}}") Var ds,
                             @ParamMetaData(blockType = "procedures_get_field_datasource", type = ObjectType.STRING, description = "{{fieldName}}") Var fieldName) {
    return ds.getField(fieldName.getObjectAsString());
  }
  
  @CronapiMetaData(type = "function", name = "{{datasourceGetField}}", nameTags = { "getField",
      "obterCampo" }, description = "{{functionToGetFieldOfCurrentCursorInDatasource}}", returnType = ObjectType.STRING, wizard = "procedures_get_field_datasource")
  public static Var getFieldFromDatasource() {
    return Var.VAR_NULL;
  }
  
  @CronapiMetaData(type = "function", name = "{{datasourceRemove}}", nameTags = { "remove", "delete", "apagar",
      "remover" }, description = "{{functionToRemoveObjectInDatasource}}", params = {
          "{{datasource}}" }, paramsType = { ObjectType.DATASET }, returnType = ObjectType.VOID, displayInline = true)
  public static void remove(Var ds) {
    ((DataSource)ds.getObject()).delete();
  }
  
  @CronapiMetaData(type = "function", name = "{{datasourceExecuteQuery}}", nameTags = { "datasourceExecuteQuery",
      "executeCommand", "executarComando" }, description = "{{functionToExecuteQuery}}", params = { "{{entity}}",
          "{{query}}", "{{paramsQueryTuples}}" }, paramsType = { ObjectType.STRING, ObjectType.STRING,
              ObjectType.LIST }, returnType = ObjectType.DATASET, arbitraryParams = true, wizard = "procedures_sql_command_callnoreturn")
  public static void execute(Var entity, Var query, Var ... params) {
    DataSource ds = new DataSource(entity.getObjectAsString());
    ds.execute(query.getObjectAsString(), params);
  }
  
  @CronapiMetaData(type = "function", name = "{{newEntity}}", nameTags = { "newEntity",
      "NovaEntidade" }, description = "{{newEntityDescription}}", params = { "{{entity}}",
          "{{params}}" }, paramsType = { ObjectType.STRING,
              ObjectType.MAP }, returnType = ObjectType.OBJECT, arbitraryParams = true, wizard = "procedures_createnewobject_callreturn")
  public static final Var newEntity(Var object, Var ... params) throws Exception {
    return cronapi.object.Operations.newObject(object, params);
  }
  
  @CronapiMetaData(type = "function", name = "{{datasourceExecuteJQPLQuery}}", nameTags = { "datasourceQuery",
      "openConnection", "abrirConsulta" }, description = "{{functionToQueryInDatasource}}", params = {
          "{{entity}}", "{{query}}", "{{paramsQueryTuples}}" }, paramsType = { ObjectType.STRING,
              ObjectType.STRING,
              ObjectType.MAP }, returnType = ObjectType.DATASET)
  public static Var executeQuery(Var entity, Var query, Var params) {
    Var[] vars = new Var[params.length()];
    LinkedHashMap<String, Var> map = (LinkedHashMap<String, Var>) params.getObject();  
    int i = 0;
    for (Map.Entry<String, Var> entry : map.entrySet()) {
      vars[i] = new Var(entry.getKey(), entry.getValue());
      i++;
    }
    return query(entity, query, vars);
  }
  
  	@CronapiMetaData(type = "function", name = "{{datasourceGetColumnName}}", nameTags = { "GetColumn",
			"obterColuna","datasource","dados" }, description = "{{datasourceGetColumnDescription}}", params = {
					"{{datasource}}", "{{fieldName}}" }, paramsType = { ObjectType.DATASET,
							ObjectType.STRING }, returnType = ObjectType.OBJECT, wizard = "procedures_get_field")
	public static Var getColumn(
			@ParamMetaData(blockType = "variables_get", type = ObjectType.OBJECT, description = "{{datasource}}") Var ds,
			@ParamMetaData(blockType = "procedures_get_field_datasource", type = ObjectType.STRING, description = "{{fieldName}}") Var fieldName) {
		Object obj = ds.getObject();

		List<Object> dst = new LinkedList<Object>();
		if (obj instanceof DataSource) {
			DataSource datasource = (DataSource) obj;

			while (datasource.hasNext()) {
				dst.add(ds.getField(fieldName.getObjectAsString()));
				datasource.next();
			}
			dst.add(ds.getField(fieldName.getObjectAsString()));
			datasource.setCurrent(0);
			return Var.valueOf(dst);
		}
		return Var.valueOf(dst);
	}
  
}
