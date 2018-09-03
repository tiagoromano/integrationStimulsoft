package cronapi.odata.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cronapi.AppConfig;
import cronapi.QueryManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.*;
import org.apache.olingo.odata2.core.CloneUtils;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.access.data.VirtualClass;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPATypeConverter;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.internal.jpa.jpql.HermesParser;
import org.eclipse.persistence.internal.queries.ReportItem;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.jpql.parser.*;
import org.eclipse.persistence.jpa.jpql.utility.iterable.ListIterable;
import org.eclipse.persistence.queries.ReportQuery;

import java.io.InputStream;
import java.util.*;

public class DatasourceExtension implements JPAEdmExtension {

  private final ODataJPAContext context;

  public DatasourceExtension(ODataJPAContext context) {
    this.context = context;
  }

  @Override
  public void extendWithOperation(JPAEdmSchemaView jpaEdmSchemaView) {
    jpaEdmSchemaView.registerOperations(DatasourceOperations.class, null);
  }

  @Override
  public void extendJPAEdmSchema(JPAEdmSchemaView view) {
    Schema edmSchema = view.getEdmSchema();

    for (EntitySet set : edmSchema.getEntityContainers().get(0).getEntitySets()) {
      set.setShowMetadata(AppConfig.exposeLocalEntities());
    }

    List<EntityType> localEntities = new LinkedList<>();

    for (EntityType type : edmSchema.getEntityTypes()) {
      localEntities.add(type);
    }

    List<EntitySet> queryDatasource = new LinkedList<>();

    JsonObject queries = QueryManager.getJSON();
    for (Map.Entry<String, JsonElement> entry : queries.entrySet()) {
      JsonObject customObj = entry.getValue().getAsJsonObject();
      if (!QueryManager.isNull(customObj.get("entityFullName"))) {
        String clazz = customObj.get("entityFullName").getAsString();
        if (clazz.startsWith(edmSchema.getNamespace())) {
          try {
            LinkedList<String> calcFields = new LinkedList<>();
            if (!QueryManager.isNull(customObj.get("calcFields"))) {
              JsonObject calcObj = customObj.get("calcFields").getAsJsonObject();
              for (Map.Entry<String, JsonElement> entryObj : calcObj.entrySet()) {
                calcFields.add(entryObj.getKey());
              }
            }
            EntitySet set = createDataSource(edmSchema, customObj.get("customId").getAsString(), customObj.get("entitySimpleName").getAsString(), calcFields);
            if (set != null) {
              queryDatasource.add(set);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }

    if (!AppConfig.exposeLocalEntities()) {
      edmSchema.getEntityContainers().get(0).getAssociationSets().clear();

      for (EntityType type : edmSchema.getEntityTypes()) {
        if (type.getNavigationProperties() != null) {
          type.getNavigationProperties().clear();
        }
      }
    }

    for (EntityType type : localEntities) {
      JPAEdmMappingImpl mapping = (JPAEdmMappingImpl) type.getMapping();
      if (mapping != null && !mapping.isVirtualAccess()) {
        addDisplayFields(edmSchema, type);
      }
    }

    for (EntityType type : edmSchema.getEntityTypes()) {
      type.setShowMetadata(AppConfig.exposeMetadada());
    }

    for (EntityContainer container : edmSchema.getEntityContainers()) {
      for (EntitySet set : container.getEntitySets()) {
        set.setShowMetadata(AppConfig.exposeMetadada());
      }
    }
  }

  private static boolean isEdmSimpleTypeKind(Class fieldClass) {
    try {
      JPATypeConverter.convertToEdmSimpleType(fieldClass, null);
      return false;
    } catch (ODataJPAModelException e) {
      return true;
    }
  }

  private static EdmSimpleTypeKind toEdmSimpleTypeKind(Class fieldClass) {
    try {
      return JPATypeConverter.convertToEdmSimpleType(fieldClass, null);
    } catch (ODataJPAModelException e) {
      return EdmSimpleTypeKind.String;
    }
  }

  private static EdmSimpleTypeKind toEdmSimpleTypeKind(String fieldClass) {
    return Enum.valueOf(EdmSimpleTypeKind.class, fieldClass);
  }

  private EntityType findEntityType(Schema edmSchema, String entity) {
    for (EntityType type : edmSchema.getEntityTypes()) {
      if (type.getName().equals(entity)) {
        return type;
      }
    }

    return null;
  }

  private PropertyRef findKey(EntityType entityType, String name) {
    for (PropertyRef item : entityType.getKey().getKeys()) {
      if (item.getName().equals(name)) {
        return item;
      }
    }

    return null;
  }

  private Property findProperty(EntityType entityType, String name) {
    for (Property item : entityType.getProperties()) {
      if (item.getName().equals(name)) {
        return item;
      }
    }

    return null;
  }

  private void addCalcFields(EntityType newType, List<String> addFields) {
    if (addFields != null) {
      for (String field : addFields) {
        SimpleProperty property = new SimpleProperty();
        property.setType(EdmSimpleTypeKind.Auto);
        property.setName(field);

        JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
        mapping.setInternalName(field);
        mapping.setJPAType(Object.class);
        mapping.setVirtualAccess(true);
        mapping.setCalculated(true);

        property.setMapping(mapping);

        newType.getProperties().add(property);
      }
    }
  }


  private Property findBestDisplayField(EntityType complexType) {
    Property best = null;
    for (Property p : complexType.getProperties()) {
      JPAEdmMappingImpl mapping = (JPAEdmMappingImpl) p.getMapping();
      PropertyRef key = findKey(complexType, p.getName());
      if (key == null && mapping.getJPAType() == String.class) {
        best = p;
        break;
      }
    }

    if (best == null) {
      for (Property p : complexType.getProperties()) {
        PropertyRef key = findKey(complexType, p.getName());
        if (key == null) {
          best = p;
          break;
        }
      }
    }

    if (best == null) {
      best = complexType.getProperties().get(0);
    }

    return best;
  }

  private void addDisplayFields(Schema edmSchema, EntityType type) {
    List<Property> properties = new LinkedList<>();
    properties.addAll(type.getProperties());

    for (Property p : properties) {

      JPAEdmMappingImpl mapping = (JPAEdmMappingImpl) p.getMapping();
      EntityType complexType = findEntityType(edmSchema, mapping.getJPAType().getSimpleName());
      if (complexType != null) {
        SimpleProperty best = (SimpleProperty) findBestDisplayField(complexType);
        SimpleProperty newProp = (SimpleProperty) CloneUtils.getClone(p);
        newProp.setName(newProp.getName() + "_" + best.getName());
        newProp.setType(best.getType());

        JPAEdmMappingImpl newMapping = (JPAEdmMappingImpl) CloneUtils.getClone(p.getMapping());
        newMapping.setInternalName(newMapping.getInternalName().substring(0, newMapping.getInternalName().lastIndexOf(".")) + "." + best.getName());
        newProp.setMapping(newMapping);

        int total = 0;
        String name = newProp.getName();
        for (Property prop : type.getProperties()) {
          if (prop.getName().equals(name) || prop.getName().startsWith(name + "_")) {
            total++;
          }
        }

        if (total > 0) {
          newProp.setName(name + "_" + total);
        }


        type.getProperties().add(newProp);

      }
    }
  }

  private SimpleProperty addProperty(String alias, EntityType mainType, Schema edmSchema, Class type, String orgName, String internalName, String expression, List<Property> properties, List<PropertyRef> propertyRefList) {

    boolean isComplex = isEdmSimpleTypeKind(type);

    if (isComplex) {
      EntityType complexType = findEntityType(edmSchema, type.getSimpleName());
      if (complexType != null) {

        String internalExpression = expression.substring(expression.indexOf(".") + 1);
        List<PropertyRef> keys = complexType.getKey().getKeys();
        SimpleProperty first = null;
        for (PropertyRef key : keys) {
          Property prop = findProperty(complexType, key.getName());
          first = addProperty(orgName, mainType, edmSchema, ((JPAEdmMappingImpl) prop.getMapping()).getJPAType(), alias != null ? alias : internalExpression.replace(".", "_") + "_" + key.getName(), "[name]." + key.getName(), expression + "." + key.getName(), properties, propertyRefList);
        }

        if (first != null) {
          SimpleProperty best = (SimpleProperty) findBestDisplayField(complexType);
          addProperty(orgName, mainType, edmSchema, ((JPAEdmMappingImpl) best.getMapping()).getJPAType(), alias != null ? alias + "_" + best.getName() : internalExpression.replace(".", "_") + "_" + best.getName(), first.getName() + "." + best.getName(), expression + "." + best.getName(), properties, propertyRefList);
        }

      }

    } else {

      int count = StringUtils.countMatches(expression, ".");
      boolean useExpression = false;

      if (alias == null && count > 1) {
        useExpression = true;
      }

      SimpleProperty property = new SimpleProperty();

      property.setType(toEdmSimpleTypeKind(type));

      if (useExpression) {
        orgName = expression.substring(expression.indexOf(".") + 1).replace(".", "_");
        internalName = orgName;
      }

      property.setName(orgName);
      int total = 0;
      String name = orgName;
      for (Property prop : properties) {
        if (prop.getName().equals(name)) {
          total++;
          name = property.getName() + "_" + total;
        }
      }

      if (total > 0) {
        property.setName(name);
      }

      JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
      mapping.setInternalExpression(expression);
      mapping.setInternalName(internalName.replace("[name]", property.getName()));
      mapping.setJPAType(type);
      mapping.setVirtualAccess(true);

      property.setMapping(mapping);

      properties.add(property);

      return property;
    }

    return null;
  }

  private EntitySet createDataSource(Schema edmSchema, String id, String entity, List<String> addFields) {

    String edmNamespace = edmSchema.getNamespace();
    EntityManagerImpl em = (EntityManagerImpl) context.getEntityManager();
    JsonObject queryJson = QueryManager.getQuery(id);
    String jpql = QueryManager.getJPQL(queryJson);
    AbstractSession session = em.getActiveSessionIfExists();

    HermesParser parser = new HermesParser();
    Object query = parser.buildQuery(jpql, session);

    if (query instanceof ReportQuery) {

      ReportQuery reportQuery = (ReportQuery) parser.buildQuery(jpql, session);
      reportQuery.prepareInternal(session);

      if (reportQuery.getItems().size() == 1 && reportQuery.getItems().get(0).getDescriptor() != null) {
        entity = reportQuery.getItems().get(0).getDescriptor().getJavaClass().getSimpleName();
        return createEntityDataSource(edmSchema, id, entity, addFields);
      } else {

        JPQLExpression jpqlExpression = new JPQLExpression(
            jpql,
            DefaultEclipseLinkJPQLGrammar.instance(),
            true
        );

        String mainEntity = JPQLParserUtil.getMainEntity(jpqlExpression);

        EntityType mainType = findEntityType(edmSchema, mainEntity);

        EntitySet set = new EntitySet();
        set.setName(id);
        set.setEntityType(new FullQualifiedName(edmNamespace, id));

        edmSchema.getEntityContainers().get(0).getEntitySets().add(set);

        ListIterable<Expression> children = ((SelectClause) ((SelectStatement) jpqlExpression.getQueryStatement()).getSelectClause()).getSelectExpression().children();
        ListIterator<Expression> expressions = children.iterator();
        Key key = new Key();
        List<PropertyRef> propertyRefList = new ArrayList<>();
        key.setKeys(propertyRefList);

        List<Property> properties = new ArrayList<>();
        boolean keysSet = false;
        for (ReportItem item : reportQuery.getItems()) {
          String alias = null;
          Expression expression = expressions.next();
          if (expression instanceof IdentificationVariable) {
            expression = expression.getParent();
          }

          if (expression instanceof ResultVariable) {
            if (((ResultVariable) expression).getResultVariable() != null) {
              alias = ((ResultVariable) expression).getResultVariable().toActualText();
            }
            expression = ((ResultVariable) expression).getSelectExpression();
          }

          Class type = Object.class;
          if (item.getMapping() != null) {
            type = item.getMapping().getField().getType();
          } else if (item.getDescriptor() != null) {
            type = item.getDescriptor().getJavaClass();
          } else if (item.getResultType() != null) {
            type = item.getResultType();
          }

          String name = item.getName();

          if (name == null || name.isEmpty()) {
            name = "expression";
          }

          SimpleProperty added = addProperty(alias, mainType, edmSchema, type, name, name, expression.toString(), properties, propertyRefList);

          if (added != null) {
            if (findKey(mainType, added.getName()) != null) {
              PropertyRef propertyRef = new PropertyRef();
              propertyRef.setName(name);
              propertyRefList.add(propertyRef);
              keysSet = true;
            }
          }

        }

        boolean canEdit = true;
        if (!keysSet || propertyRefList.size() != mainType.getKey().getKeys().size()) {
          propertyRefList.clear();
          canEdit = false;
          for (Property item : properties) {
            PropertyRef propertyRef = new PropertyRef();
            propertyRef.setName(item.getName());
            propertyRefList.add(propertyRef);
          }
        }

        EntityType type = new EntityType();

        type.setProperties(properties);
        type.setKey(key);
        type.setName(id);
        JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
        mapping.setCanEdit(canEdit);
        mapping.setJPAType(((JPAEdmMappingImpl) mainType.getMapping()).getJPAType());
        mapping.setVirtualAccess(true);
        if (canEdit) {
          mapping.setInternalName(mainEntity);
        }
        type.setMapping(mapping);

        addCalcFields(type, addFields);

        edmSchema.getEntityTypes().add(type);

        return set;
      }
    } else {
      return createEntityDataSource(edmSchema, id, entity, addFields);
    }
  }

  private EntitySet createEntityDataSource(Schema edmSchema, String id, String entity, List<String> addFields) {

    String edmNamespace = edmSchema.getNamespace();

    for (EntityContainer container : edmSchema.getEntityContainers()) {

      EntitySet foundES = null;

      for (EntitySet entitySet : container.getEntitySets()) {
        if (entitySet.getEntityType().getName().equals(entity)) {
          foundES = entitySet;
          break;
        }
      }

      EntityType oldType = edmSchema.getEntityType(entity);

      EntityType newType = (EntityType) CloneUtils.getClone(oldType);
      newType.setShowMetadata(true);
      newType.setName(id);

      addDisplayFields(edmSchema, newType);

      addCalcFields(newType, addFields);

      edmSchema.getEntityTypes().add(newType);

      EntitySet set = new EntitySet();
      set.setName(id);
      set.setEntityType(new FullQualifiedName(edmNamespace, id));
      set.setMapping(foundES.getMapping());
      set.setAnnotationAttributes(foundES.getAnnotationAttributes());
      set.setAnnotationElements(foundES.getAnnotationElements());

      container.getEntitySets().add(set);

      List<AssociationSet> addAssociationSet = new LinkedList<>();

      for (AssociationSet association : container.getAssociationSets()) {
        if (association.getEnd1().getRole().equals(entity)) {

          AssociationSet newAssociation = new AssociationSet();
          newAssociation.setName(association.getName() + "_" + id);
          newAssociation.setAnnotationAttributes(association.getAnnotationAttributes());
          newAssociation.setAnnotationElements(association.getAnnotationElements());
          newAssociation.setAssociation(association.getAssociation());

          AssociationSetEnd end = new AssociationSetEnd();
          end.setEntitySet(id);
          end.setRole(association.getEnd1().getRole());

          newAssociation.setEnd1(end);
          newAssociation.setEnd2(association.getEnd2());

          addAssociationSet.add(newAssociation);
        }
      }

      for (AssociationSet association : addAssociationSet) {
        container.getAssociationSets().add(association);
      }

      return set;
    }

    return null;
  }

  private EntitySet createBlocklyDataSource(Schema edmSchema, JsonObject query) {
    JsonObject defaultValuesProperties = null;
    String id = query.get("customId").getAsString();
    if (query.get("defaultValuesProperties") != null) {
      defaultValuesProperties = query.get("defaultValuesProperties").getAsJsonObject();
    }

    if (defaultValuesProperties != null) {
      boolean hasDefined = false;
      for (Map.Entry<String, JsonElement> entry : defaultValuesProperties.entrySet()) {
        if (entry.getValue() != null && !entry.getValue().isJsonNull()) {
          hasDefined = true;
          break;
        }
      }

      if (hasDefined) {
        EntityType type = new EntityType();

        Key key = new Key();
        List<PropertyRef> propertyRefList = new ArrayList<>();
        key.setKeys(propertyRefList);

        List<Property> properties = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : defaultValuesProperties.entrySet()) {
          JsonObject obj = entry.getValue().getAsJsonObject();

          SimpleProperty property = new SimpleProperty();

          property.setType(toEdmSimpleTypeKind(obj.get("type").getAsString()));
          property.setName(entry.getKey());

          JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
          mapping.setInternalExpression(entry.getKey());
          mapping.setInternalName(entry.getKey());
          mapping.setJPAType(String.class);
          mapping.setVirtualAccess(true);

          property.setMapping(mapping);

          if (obj.get("key").getAsBoolean()) {
            PropertyRef propertyRef = new PropertyRef();
            propertyRef.setName(entry.getKey());
            propertyRefList.add(propertyRef);
          }
        }

        type.setProperties(properties);
        type.setKey(key);
        type.setName(id);

        JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
        mapping.setCanEdit(true);
        mapping.setJPAType(VirtualClass.class);
        mapping.setVirtualAccess(true);
        type.setMapping(mapping);

        edmSchema.getEntityTypes().add(type);
      }

    }

    return null;
  }


  @Override
  public InputStream getJPAEdmMappingModelStream() {
    return null;
  }
}
