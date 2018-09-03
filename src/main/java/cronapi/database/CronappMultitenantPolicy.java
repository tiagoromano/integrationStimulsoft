package cronapi.database;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.MultitenantPolicy;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.tools.schemaframework.TableDefinition;

public class CronappMultitenantPolicy implements MultitenantPolicy {
  
  private MultitenantPolicy multitenantPolicy;
  
  public CronappMultitenantPolicy(MultitenantPolicy multitenantPolicy) {
    this.multitenantPolicy = multitenantPolicy;
  }
  
  @Override
  public void addToTableDefinition(TableDefinition tableDefinition) {
    this.multitenantPolicy.addToTableDefinition(tableDefinition);
  }
  
  @Override
  public void addFieldsToRow(AbstractRecord abstractRecord, AbstractSession abstractSession) {
    if(!CronappDescriptorQueryManager.isDisabled())
      this.multitenantPolicy.addFieldsToRow(abstractRecord, abstractSession);
  }
  
  @Override
  public MultitenantPolicy clone(ClassDescriptor classDescriptor) {
    return new CronappMultitenantPolicy(this.multitenantPolicy.clone(classDescriptor));
  }
  
  @Override
  public boolean isSingleTableMultitenantPolicy() {
    return this.multitenantPolicy.isSingleTableMultitenantPolicy();
  }
  
  @Override
  public boolean isSchemaPerMultitenantPolicy() {
    return this.multitenantPolicy.isSchemaPerMultitenantPolicy();
  }
  
  @Override
  public boolean isTablePerMultitenantPolicy() {
    return this.multitenantPolicy.isTablePerMultitenantPolicy();
  }
  
  @Override
  public void postInitialize(AbstractSession abstractSession) {
    this.multitenantPolicy.postInitialize(abstractSession);
  }
  
  @Override
  public void initialize(AbstractSession abstractSession) throws DescriptorException {
    this.multitenantPolicy.initialize(abstractSession);
  }
  
  @Override
  public void preInitialize(AbstractSession abstractSession) throws DescriptorException {
    this.multitenantPolicy.preInitialize(abstractSession);
  }
}
