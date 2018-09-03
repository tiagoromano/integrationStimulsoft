package cronapi.serialization;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class CronappModule extends SimpleModule {

  private boolean enableFilter = true;

  public CronappModule() {
  }

  public CronappModule(boolean enableFilter) {
    this.enableFilter = enableFilter;
  }

  @Override
  public void setupModule(SetupContext context) {
    super.setupModule(context);
    context.addBeanSerializerModifier(new CronappBeanSerializerModifier(enableFilter));
  }
}
