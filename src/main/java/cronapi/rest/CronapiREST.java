package cronapi.rest;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;

import cronapi.ErrorResponse;
import cronapi.QueryManager;
import cronapi.RestBody;
import cronapi.RestClient;
import cronapi.RestResult;
import cronapi.Utils;
import cronapi.Var;
import cronapi.database.DataSource;
import cronapi.database.DataSourceFilter;
import cronapi.database.EntityMetadata;
import cronapi.database.TenantService;
import cronapi.database.TransactionManager;
import cronapi.database.DataSourceFilter.DataSourceFilterItem;
import cronapi.rest.CronapiREST.TranslationPath;
import cronapi.util.SecurityUtil;
import cronapi.util.StorageService;
import cronapi.util.StorageServiceFileObject;

@RestController
@RequestMapping(value = "/api/cronapi")
public class CronapiREST {

  private static Pattern RELATION_PARAM = Pattern.compile("relation:(.*?):(.*?)$");

  @Autowired
  private HttpServletRequest request;
  
  @Autowired
	private HttpServletResponse response;

  @Autowired
  private TenantService tenantService;
  
  private int paramBlockly;

  public class TranslationPath {
    public Var[] params;
    public String relationClass;
    public String relationAssossiative;
    public String field;
    public String refId;
    public Var[] relationParams;
    public DataSourceFilter filter;
  }

  private TranslationPath translatePathVars(String clazz) {
    return translatePathVars(clazz, 0, -1);
  }

  private TranslationPath translatePathVars(String clazz, int offSet, int max) {
    String paths = request.getServletPath().substring(request.getServletPath().indexOf(clazz) + clazz.length()).trim();
    if(paths.startsWith("/")) {
      paths = paths.substring(1);
    }
    if(paths.endsWith("/")) {
      paths = paths.substring(0, paths.length() - 1);
    }

    String[] strParams = paths.split("/");

    strParams = Arrays.copyOfRange(strParams, offSet, max!=-1?max:strParams.length);

    List<Var> params = new LinkedList<>();
    List<Var> relationParams = new LinkedList<>();

    TranslationPath translationPath = new TranslationPath();

    boolean isParam = true;
    boolean isRelationParam = false;
    if(!paths.isEmpty()) {
      for(int i = 0; i < strParams.length; i++) {

        if (strParams[i] != null) {
          Matcher matcher = RELATION_PARAM.matcher(strParams[i]);
          if(matcher.matches()) {
            translationPath.relationClass = matcher.group(2);
            translationPath.field = matcher.group(1);
            translationPath.refId = strParams[i];
            isParam = false;
            isRelationParam = true;
            continue;
          }
        }

        if(isParam) {
          params.add(Var.valueOf(strParams[i]));
        }

        if(isRelationParam) {
          relationParams.add(Var.valueOf(strParams[i]));
        }
      }
    }
    translationPath.params = params.toArray(new Var[params.size()]);
    translationPath.relationParams = relationParams.toArray(new Var[relationParams.size()]);

    boolean caseInsensitive = false;
    if (request.getParameter("filterCaseInsensitive") != null && !request.getParameter("filterCaseInsensitive").isEmpty()) {
      caseInsensitive = "true".equalsIgnoreCase(request.getParameter("filterCaseInsensitive"));
    }
    translationPath.filter = DataSourceFilter.getInstance(request.getParameter("filter"), request.getParameter("order"),
            request.getParameter("filterType"), caseInsensitive);

    return translationPath;
  }

  private Var[] toVarArray(LinkedList list) {
    Var[] vars = new Var[list.size()];
    for(int i = 0; i < list.size(); i++) {
      vars[i] = Var.valueOf(list.get(i));
    }

    return vars;
  }

  @ExceptionHandler(Throwable.class)
  @ResponseBody
  ResponseEntity<ErrorResponse> handleControllerException(HttpServletRequest req, Throwable ex) {
    ex.printStackTrace();;
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex, req.getMethod());
    return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  //Api de Crud
  @RequestMapping(method = RequestMethod.GET, value = "/metadata/{entity}/**")
  public HttpEntity<EntityMetadata> dataOptions(@PathVariable("entity") String entity) throws Exception {
    DataSource ds = new DataSource(entity);
    EntityMetadata data = ds.getMetadata();
    return new ResponseEntity<EntityMetadata>(data, HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/crud/{entity}/**")
  public HttpEntity<Object> crudGet(@PathVariable("entity") String entity, Pageable pageable) throws Exception {
    RestResult data = runIntoTransaction(() -> {

      TranslationPath translationPath = translatePathVars(entity);

      PageRequest page = new PageRequest(pageable.getPageNumber(), pageable.getPageSize());

      DataSource ds = new DataSource(entity);
      if(translationPath.relationClass == null) {
        ds.checkRESTSecurity("GET");
        if (translationPath.params.length > 0 && translationPath.params[0].equals("__new__")) {
          ds.insert();
          return Var.valueOf(ds.getObject());
        } else {
          ds.setDataSourceFilter(translationPath.filter);
          ds.filter(null, page, translationPath.params);
        }
      }
      else {
        ds.checkRESTSecurity(translationPath.refId, "GET");
        if (translationPath.relationParams.length > 0 && translationPath.relationParams[0].equals("__new__")) {
          ds.resolveRelation(translationPath.refId);
          ds.insert();
          return Var.valueOf(ds.getObject());
        } else {
          ds.setDataSourceFilter(translationPath.filter);
          ds.filterByRelation(translationPath.refId, page, translationPath.params);
        }
      }

      return Var.valueOf(ds.getPage().getContent());
    });

    return new ResponseEntity<Object>(data.getValue().getObject(), HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.PUT, value = "/crud/{entity}/**")
  public HttpEntity<Object> crudPut(@PathVariable("entity") String entity, @RequestBody final Var data)
      throws Exception {
    RestResult result = runIntoTransaction(() -> {
      DataSource ds = new DataSource(entity);
      TranslationPath translationPath = translatePathVars(entity);
      
      if(translationPath.relationClass == null) {
        ds.checkRESTSecurity("PUT");
        ds.filter(data, null);
        ds.update(data);
        return Var.valueOf(ds.save());
      }
      else {
        ds.checkRESTSecurity(translationPath.refId, "PUT");
        String entityRelation = ds.getRelationEntity(translationPath.refId);
        ds = new DataSource(entityRelation);
        ds.filter(data, null);
        ds.update(data);
        return Var.valueOf(ds.save());
      }
    });

    return new ResponseEntity<Object>(result.getValue().getObject(), HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/crud/{entity}/**")
  public HttpEntity<Object> crudPost(@PathVariable("entity") String entity, @RequestBody final Var data)
      throws Exception {
    RestResult result = runIntoTransaction(() -> {
      DataSource ds = new DataSource(entity);
      TranslationPath translationPath = translatePathVars(entity);

      Object inserted = null;
      if(translationPath.relationClass == null) {
        ds.checkRESTSecurity("POST");
        ds.insert((Map<?, ?>)data.getObject());
        inserted = ds.save();
      }
      else {
        ds.checkRESTSecurity(translationPath.refId, "POST");
        inserted = ds.insertRelation(translationPath.refId, (Map<?, ?>)data.getObject(), translationPath.params);
      }
      return Var.valueOf(inserted);
    });

    return new ResponseEntity<Object>(result.getValue().getObject(), HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.DELETE, value = "/crud/{entity}/**")
  public void crudDelete(@PathVariable("entity") String entity) throws Exception {
    runIntoTransaction(() -> {
      TranslationPath translationPath = translatePathVars(entity);
      DataSource ds = new DataSource(entity);
      if(translationPath.relationClass == null) {
        ds.checkRESTSecurity("DELETE");
        ds.delete(translationPath.params);
      }
      else {
        ds.checkRESTSecurity(translationPath.refId, "DELETE");
        ds.deleteRelation(translationPath.refId, translationPath.params, translationPath.relationParams);
      }
      return null;
    });
  }
  //Fim Api de Crud

  //Api de Fonte de Dados
  @RequestMapping(method = RequestMethod.GET, value = "/query/{id}/__new__")
  public HttpEntity<Var> queryGetNew(@PathVariable("id") String id) throws Exception {
    RestResult data = runIntoTransaction(() -> {
      JsonObject query = QueryManager.getQuery(id);
      QueryManager.checkSecurity(query, "GET");

      if (!QueryManager.getType(query).equals("blockly")) {
        DataSource ds = new DataSource(query);

        ds.insert();

        QueryManager.addDefaultValues(query, Var.valueOf(ds), false);

        QueryManager.executeNavigateEvent(query, ds);
        QueryManager.checkFieldSecurity(query, ds, "GET");

        return Var.valueOf(ds.getObject());
      } else {
        Var empty = Var.newMap();
        QueryManager.addDefaultValues(query, empty, false);
        return empty;
      }
    });

    return new ResponseEntity<Var>(data.getValue(), HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/query/{id}/**")
  public HttpEntity<?> queryGet(@PathVariable("id") String id, Pageable pageable) throws Exception {
    RestResult data = runIntoTransaction(() -> {
      PageRequest page = new PageRequest(pageable.getPageNumber(), pageable.getPageSize());
      
      JsonObject query = QueryManager.getQuery(id);
      QueryManager.checkSecurity(query, "GET");
      if (QueryManager.getType(query).equals("blockly")) {
        TranslationPath translationPath = translatePathVars(id);
        if (translationPath.filter != null && translationPath.filter.getItems().size() > 0) {
          Var[] filterParams = toVarArray(translationPath.filter.getItems());
          Var[] params = (Var[])ArrayUtils.addAll(translationPath.params, filterParams);
          return QueryManager.executeBlockly(query, "FILTER", params).getObjectAsPOJOList();
        } else {
          return QueryManager.executeBlockly(query, "GET", translationPath.params).getObjectAsPOJOList();
        }
      } else {
        //TranslationPath translationPath = translatePathVars(id, 0, query.getAsJsonArray("queryParamsValues").size());
        TranslationPath translationPath = translatePathVars(id, 0, -1);

        QueryManager.checkFilterSecurity(query, translationPath.filter);

        DataSource ds = new DataSource(query);

        String jpql = QueryManager.getJPQL(query);

        List<Var> params = Utils.getParamsAndExecuteBlockParams(query, translationPath);
        ds.setDataSourceFilter(translationPath.filter);
        ds.filter(jpql, page, params.toArray(new Var[0]));

        QueryManager.addCalcFields(query, ds);
        QueryManager.executeNavigateEvent(query, ds);
        QueryManager.checkFieldSecurity(query, ds, "GET");

        return Var.valueOf(ds.getPage());
      }
    });

    if (data.getValue().getObject() instanceof Page) {
      Page page = (Page) data.getValue().getObject();
      return new ResponseEntity<List>(page.getContent(), HttpStatus.OK);
    } else {
      return new ResponseEntity<Var>(data.getValue(), HttpStatus.OK);
    }

  }

  @RequestMapping(method = RequestMethod.POST, value = "/query/{id}/**")
  public Object queryPost(@PathVariable("id") String id, @RequestBody final Map<?,?> rawData, @RequestHeader(value="X-From-DataSource", defaultValue = "false") boolean isFromDataSource) throws Exception {
    RestResult result = runIntoTransaction(() -> {

      JsonObject query = QueryManager.getQuery(id);
      QueryManager.checkSecurity(query, "POST");

      RestBody data = RestBody.parseBody(rawData, isFromDataSource);

      QueryManager.checkFieldSecurity(query, data, "POST");

      Var entity =  Var.valueOf(data.getEntityData());

      RestClient.getRestClient().setRawBody(entity);
      RestClient.getRestClient().setBody(data);

      if (QueryManager.getType(query).equals("blockly")) {
        TranslationPath translationPath = translatePathVars(id);

        Var[] params = (Var[])ArrayUtils.addAll(new Var[] {entity}, translationPath.params);
        QueryManager.executeEvent(query, entity, "beforeInsert");
        Var inserted = QueryManager.executeBlockly(query, "POST", params);
        QueryManager.executeEvent(query, entity, "afterInsert");

        return inserted.getPOJO();
      } else {
        DataSource ds = new DataSource(query);
        
        ds.insert(entity.getObject());

        QueryManager.addDefaultValues(query, Var.valueOf(ds), true);

        QueryManager.executeEvent(query, ds.getObject(), "beforeInsert");
        Object inserted = ds.save(false);
        QueryManager.executeEvent(query, ds.getObject(), "afterInsert");
        QueryManager.checkFieldSecurity(query, ds, "GET");
        QueryManager.addCalcFields(query, ds);

        return Var.valueOf(inserted);
      }
    });

    if (isFromDataSource) {
      return result;
    } else {
      return result.getValue();
    }
  }

  @RequestMapping(method = RequestMethod.PUT, value = "/query/{id}/**")
  public Object queryPut(@PathVariable("id") String id, @RequestBody final Map<?, ?> rawData, @RequestHeader(value="X-From-DataSource", defaultValue = "false") boolean isFromDataSource) throws Exception {
    RestResult result = runIntoTransaction(() -> {

      JsonObject query = QueryManager.getQuery(id);
      QueryManager.checkSecurity(query, "PUT");

      RestBody data = RestBody.parseBody(rawData, isFromDataSource);

      QueryManager.checkFieldSecurity(query, data, "PUT");

      Var entity =  Var.valueOf(data.getEntityData());
      RestClient.getRestClient().setRawBody(entity);
      RestClient.getRestClient().setBody(data);

      if (QueryManager.getType(query).equals("blockly")) {
        TranslationPath translationPath = translatePathVars(id);

        Var[] params = (Var[])ArrayUtils.addAll(new Var[] {entity}, translationPath.params);
        QueryManager.executeEvent(query, entity, "beforeUpdate");
        Var modified = QueryManager.executeBlockly(query, "PUT", params);
        QueryManager.executeEvent(query, entity, "beforeUpdate");

        return modified.getPOJO();
      } else {
        DataSource ds = new DataSource(query);

        ds.filter(entity, null);
        QueryManager.executeEvent(query, ds.getObject(), "beforeUpdate");
        ds.update(entity);
        Var saved = Var.valueOf(ds.save());
        QueryManager.executeEvent(query, ds.getObject(), "afterUpdate");
        QueryManager.checkFieldSecurity(query, ds, "GET");
        QueryManager.addCalcFields(query, ds);
        return saved;
      }
    });

    if (isFromDataSource) {
      return result;
    } else {
      return result.getValue();
    }
  }

  @RequestMapping(method = RequestMethod.DELETE, value = "/query/{id}/**")
  public Object queryDelete(@PathVariable("id") String id, @RequestHeader(value="X-From-DataSource", defaultValue = "false") boolean isFromDataSource) throws Exception {
    RestResult result = runIntoTransaction(() -> {

      JsonObject query = QueryManager.getQuery(id);
      QueryManager.checkSecurity(query, "DELETE");

      if (QueryManager.getType(query).equals("blockly")) {
        TranslationPath translationPath = translatePathVars(id);

        QueryManager.executeEvent(query, "beforeDelete", translationPath.params);
        QueryManager.executeBlockly(query, "DELETE", translationPath.params);
        QueryManager.executeEvent(query, "afterDelete", translationPath.params);
      } else {
        TranslationPath translationPath = translatePathVars(id, query.getAsJsonArray("queryParamsValues").size(), -1);

        DataSource ds = new DataSource(query);
        ds.filter(null, new PageRequest(1, 1), translationPath.params);
        QueryManager.executeEvent(query, ds.getObject(), "beforeDelete");
        ds.delete();
        QueryManager.executeEvent(query, ds.getObject(), "afterDelete");
      }
      return Var.VAR_NULL;
    });

    if (isFromDataSource) {
      return result;
    } else {
      return result.getValue();
    }
  }
  //Fim de api de Fonte de Dados

  //Api via bloco
  @RequestMapping(method = RequestMethod.POST, value = "/call/body/{class}/**")
  public RestResult postBody(@RequestBody RestBody body, @PathVariable("class") String clazz) throws Exception {
    return runIntoTransaction(() -> {
      RestClient.getRestClient().setBody(body);
      return cronapi.util.Operations.callBlockly(new Var(clazz), true, "execute", body.getInputs());
    });
  }

  @RequestMapping(method = RequestMethod.GET, value = "/call/{class}/**")
  public RestResult getParam(@PathVariable("class") String clazz) throws Exception {
    return runIntoTransaction(() -> {
      TranslationPath translationPath = translatePathVars(clazz);
      return cronapi.util.Operations.callBlockly(new Var(clazz), true, "execute", translationPath.params);
    });
  }

  @RequestMapping(method = RequestMethod.POST, value = "/call/{class}/**")
  public RestResult postParams(@RequestBody Var[] vars, @PathVariable("class") String clazz) throws Exception {
    return runIntoTransaction(() -> {
      return cronapi.util.Operations.callBlockly(new Var(clazz), true, RestClient.getRestClient().getMethod(), vars);
    });
  }
  //Fim Api via bloco

  //Api REST
  @RequestMapping(method = RequestMethod.GET, value = "/rest/{class}/**")
  public Var getRest(@PathVariable("class") String clazz) throws Exception {
    return runIntoTransactionVar(() -> {
      TranslationPath translationPath = translatePathVars(clazz);
      return cronapi.util.Operations.callBlockly(new Var(clazz), true, RestClient.getRestClient().getMethod(), translationPath.params);
    });
  }

  @RequestMapping(method = RequestMethod.POST, value = "/rest/{class}/**")
  public Var postRest(@RequestBody(required = false) Var[] vars, @PathVariable("class") String clazz) throws Exception {
    return runIntoTransactionVar(() -> {
      return cronapi.util.Operations.callBlockly(new Var(clazz), true, RestClient.getRestClient().getMethod(), vars);
    });
  }

  @RequestMapping(method = RequestMethod.PUT, value = "/rest/{class}/**")
  public Var putRest(@RequestBody(required = false) Var[] vars, @PathVariable("class") String clazz) throws Exception {
    return runIntoTransactionVar(() -> {
      return cronapi.util.Operations.callBlockly(new Var(clazz), true, RestClient.getRestClient().getMethod(), vars);
    });
  }

  @RequestMapping(method = RequestMethod.DELETE, value = "/rest/{class}/**")
  public Var deleteRest(@PathVariable("class") String clazz) throws Exception {
    return runIntoTransactionVar(() -> {
      TranslationPath translationPath = translatePathVars(clazz);
      return cronapi.util.Operations.callBlockly(new Var(clazz), true, RestClient.getRestClient().getMethod(), translationPath.params);
    });
  }
  //Fim api REST

  //Api de segurança

  @RequestMapping(method = RequestMethod.GET, value = "/security/roles")
  public List<SecurityUtil.SecurityGroup> securityRoles() throws Exception {
    return SecurityUtil.getRoles();
  }
  //Fim Api de Segurança

  //Api upload e visualização de arquivo
	@RequestMapping(method = RequestMethod.GET, value = "/filePreview/{fileName}/**")
	public void filePreview(@PathVariable("fileName") String fileName) throws Exception {
		StorageServiceFileObject fileObject = StorageService.getFileObjectFromTempDirectory(fileName);
		response.setContentType(fileObject.contentType);
		response.setHeader("Content-disposition", "attachment; filename="+ fileObject.name + fileObject.extension);

		ServletOutputStream responseOutputStream = response.getOutputStream();
		responseOutputStream.write(fileObject.bytes);
		responseOutputStream.flush();
		responseOutputStream.close();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/downloadFile/{entity}/{field}/**")
	public void downloadFile(@PathVariable("entity") String entity, @PathVariable("field") String field,
			@RequestBody final Var data) throws Exception {
		DataSource ds = new DataSource(entity);
		ds.checkRESTSecurity("GET");
		ds.filter(data, null);
		Object obj = ds.getObject();
		byte[] bytes = (byte[]) Utils.getFieldValue(obj, field);
		StorageServiceFileObject fileObject = StorageService.getFileObjectFromBytes(bytes);
		response.setContentType(fileObject.contentType);
		response.addHeader("x-filename", fileObject.name + fileObject.extension);

		ServletOutputStream responseOutputStream = response.getOutputStream();
		responseOutputStream.write(fileObject.bytes);
		responseOutputStream.flush();
		responseOutputStream.close();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/downloadFile/{entity}/{field}/{ids}/**")
	public void downloadFileGet(@PathVariable("entity") String entity, @PathVariable("field") String field,
			@PathVariable("ids") String ids) throws Exception {
		
		DataSource ds = new DataSource(entity);
		
		List<Var> varIds = new LinkedList<Var>();
		String[] idsSplited = ids.split(Pattern.quote(":"));
		for (String id: idsSplited) {
		  varIds.add(Var.valueOf(id));
		}
	  Object domainInstance = ds.getObjectWithId(varIds.toArray(new Var[0]));
		ds.filter(Var.valueOf(domainInstance) , null);
		Object obj = ds.getObject();
		
		byte[] bytes = (byte[]) Utils.getFieldValue(obj, field);
		StorageServiceFileObject fileObject = StorageService.getFileObjectFromBytes(bytes);
		response.setContentType(fileObject.contentType);
		response.addHeader("x-filename", fileObject.name + fileObject.extension);
		response.setHeader("Content-Disposition", "attachment;filename=" + fileObject.name + fileObject.extension);

		ServletOutputStream responseOutputStream = response.getOutputStream();
		responseOutputStream.write(fileObject.bytes);
		responseOutputStream.flush();
		responseOutputStream.close();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/uploadFile")
	public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile[] uploadfiles) throws Exception {
		return new ResponseEntity<Object>(StorageService.saveUploadFiles(uploadfiles), HttpStatus.OK);
	}
	//Fim Api upload e visualizaão de arquivo

  private RestResult runIntoTransaction(Callable<Var> callable) throws Exception {
    RestClient.getRestClient().setFilteredEnabled(true);
    RestClient.getRestClient().setTenantService(tenantService);
    Var var = Var.VAR_NULL;
    try {
      var = callable.call();
      TransactionManager.commit();
    }
    catch(Exception e) {
      TransactionManager.rollback();
      throw e;
    }
    finally {
      TransactionManager.close();
      TransactionManager.clear();
    }
    return new RestResult(var, RestClient.getRestClient().getCommands());
  }

  private Var runIntoTransactionVar(Callable<Var> callable) throws Exception {
    RestClient.getRestClient().setFilteredEnabled(true);
    RestClient.getRestClient().setTenantService(tenantService);
    Var var = Var.VAR_NULL;
    try {
      var = callable.call();
      TransactionManager.commit();
    }
    catch(Exception e) {
      TransactionManager.rollback();
      throw e;
    }
    finally {
      TransactionManager.close();
      TransactionManager.clear();
    }
    return var;
  }
}