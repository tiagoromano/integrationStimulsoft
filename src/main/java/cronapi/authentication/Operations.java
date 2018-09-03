package cronapi.authentication;

import java.util.LinkedList;

import cronapi.CronapiMetaData;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;

/**
 * Classe que representa ...
 * 
 * @author Rodrigo Santos Reis
 * @email rodrigo.reis@cronapp.io
 * @version 1.0
 * @since 2017-03-29
 *
 */
@CronapiMetaData(category = CategoryType.AUTHENTICATION, categoryTags = { "Autenticação", "Authentication" })
public class Operations {


  @CronapiMetaData(type = "function", name = "{{getUserRoles}}", nameTags = { "listar", "list","regras","roles","papeis","grupos" }, description = "{{getUserRolesDescription}}")
  public static final Var getUserRoles() throws Exception{ 
    LinkedList<String> groups = new LinkedList<String>();
    for( cronapi.util.SecurityUtil.SecurityGroup obj : cronapi.util.SecurityUtil.getRoles()){
	   groups.add(obj.id); 
	}
		return Var.valueOf(groups);
  }

}
