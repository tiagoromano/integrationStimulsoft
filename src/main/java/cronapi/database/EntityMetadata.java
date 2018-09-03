package cronapi.database;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EntityMetadata {
  private String name;
  private String simpleName;
  private String link;
  private String idLink;
  private String findLink;
  private List<FieldMetadata> fields = new ArrayList<>();
  private EntityManager em;
  
  private Map<String, RelationMetadata> relations = new LinkedHashMap<>();
  
  public EntityMetadata(String domainClass) {
    try {
      construct(Class.forName(domainClass), true);
    }
    catch(ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  
  public EntityMetadata(Class domainClass) {
    construct(domainClass, true);
  }
  
  public EntityMetadata(ManagedType type, boolean detectRelations) {
    try {
      construct(Class.forName(type.getJavaType().getCanonicalName()), detectRelations);
    }
    catch(ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  
  private void construct(Class domainClass, boolean detectRelations) {
    em = TransactionManager.getEntityManager(domainClass);
    EntityType type = em.getMetamodel().entity(domainClass);
    
    this.name = type.getJavaType().getCanonicalName();
    this.simpleName = type.getName();
    if(detectRelations)
      this.link = "/cronapi/data/" + this.name + "/crud/";
    this.findLink = this.link;
    this.idLink = "";
    
    for(Object o : type.getAttributes()) {
      Attribute attr = (Attribute)o;
      FieldMetadata field = new FieldMetadata(attr);
      fields.add(field);
      if(field.isId()) {
        this.findLink += "{" + this.simpleName + ":" + field.getName() + "}/";
        this.idLink += "{" + this.simpleName + ":" + field.getName() + "}/";
      }
    }
    
    if(detectRelations) {
      for(ManagedType managedType : em.getMetamodel().getManagedTypes()) {
        checkReverseField(domainClass, managedType);
      }
    }
    
  }
  
  @JsonIgnore
  public String getIdLink() {
    return this.idLink;
  }
  
  private ManagedType findManagedType(Class javaType) {
    for(ManagedType managedType : em.getMetamodel().getManagedTypes()) {
      if(managedType.getJavaType() == javaType) {
        return managedType;
      }
    }
    
    return null;
  }
  
  private void checkReverseField(Class domainClass, ManagedType managedType) {
    for(Object obj : managedType.getAttributes()) {
      Attribute attr = (Attribute)obj;
      
      if(attr.getJavaType() == domainClass) {
        RelationMetadata relation = new RelationMetadata(this, attr, managedType, null, null);
        relations.put(relation.getId(), relation);
        
        for(Object obj2 : managedType.getAttributes()) {
          Attribute attr2 = (Attribute)obj2;
          if(attr2.getJavaType() != domainClass && attr2.isAssociation()) {

            RelationMetadata relation2 = new RelationMetadata(this, attr2, findManagedType(attr2.getJavaType()), attr, managedType);
            relations.put(relation2.getId(), relation2);
          }
        }
      }
    }
    
  }
  
  public String getName() {
    return name;
  }
  
  public String getSimpleName() {
    return simpleName;
  }
  
  public String getLink() {
    return link;
  }
  
  public List<FieldMetadata> getFields() {
    return fields;
  }
  
  public Map<String, RelationMetadata> getRelations() {
    return relations;
  }
  
  public String getFindLink() {
    return findLink;
  }
}
