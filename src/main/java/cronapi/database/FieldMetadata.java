package cronapi.database;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

public class FieldMetadata {
  private String name;
  private String type;
  private boolean isId;
  private String metadata;
  private boolean nullable = true;
  private boolean unique = false;
  private boolean insertable = true;
  private boolean updatable = true;
  
  public FieldMetadata(Attribute attr) {
    this.name = attr.getName();
    this.type = attr.getJavaType().getCanonicalName();
    if(attr instanceof SingularAttribute) {
      this.isId = ((SingularAttribute)attr).isId();
      nullable = ((SingularAttribute)attr).isOptional();
      if(attr.isAssociation()) {
        this.metadata = "/api/cronapi/metadata/" + ((SingularAttribute)attr).getJavaType().getCanonicalName() + "/";
      }
    }
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public boolean isId() {
    return isId;
  }
  
  public void setId(boolean id) {
    isId = id;
  }
  
  public String getMetadata() {
    return metadata;
  }
  
  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public boolean isUnique() {
    return unique;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public boolean isInsertable() {
    return insertable;
  }

  public void setInsertable(boolean insertable) {
    this.insertable = insertable;
  }

  public boolean isUpdatable() {
    return updatable;
  }

  public void setUpdatable(boolean updatable) {
    this.updatable = updatable;
  }
}
