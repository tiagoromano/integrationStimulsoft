package cronapi.database;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

public class RelationMetadata {
  
  private EntityMetadata from;
  private EntityMetadata toMetadata;
  private Attribute associationAttr;
  private ManagedType association;
  private Attribute attr;
  private ManagedType to;
  private String id;
  
  public RelationMetadata(EntityMetadata from, Attribute attr, ManagedType to, Attribute associationAttr, ManagedType association) {
    this.from = from;
    this.associationAttr = associationAttr;
    this.association = association;
    this.attr = attr;
    this.to = to;
    this.toMetadata = new EntityMetadata(to, false);
    this.id = "relation:" + attr.getName() + ":" + to.getJavaType().getSimpleName();
  }
  
  @JsonIgnore
  public String getId() {
    return this.id;
  }
  
  @JsonIgnore
  public Attribute getAttribute() {
    return this.attr;
  }

  @JsonIgnore
  public Attribute getAssociationAttribute() {
    return this.associationAttr;
  }
  
  public String getLink() {
    return from.getFindLink() + this.id + "/";
  }
  
  public String getFindLink() {
    return getLink() + toMetadata.getIdLink();
  }
  
  public List<FieldMetadata> getFields() {
    return toMetadata.getFields();
  }
  
  public String getName() {
    return to.getJavaType().getCanonicalName();
  }
  
  public String getSimpleName() {
    return to.getJavaType().getSimpleName();
  }
  
  public String getAssossiationName() {
    return association != null ? association.getJavaType().getCanonicalName() : null;
  }
  
  public String gettAssossiationSimpleName() {
    return association != null ? association.getJavaType().getSimpleName() : null;
  }
}
