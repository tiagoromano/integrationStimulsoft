package cronapi;

import java.util.List;

public class RestResult {
  private Var value;
  
  private List<ClientCommand> commands;

  public RestResult(Var value, List<ClientCommand> commands) {
    this.value = value;
    this.commands = commands;
  }

  public Var getValue() {
    return value;
  }
  
  public void setValue(Var value) {
    this.value = value;
  }
  
  public List<ClientCommand> getCommands() {
    return commands;
  }
  
  public void setCommands(List<ClientCommand> commands) {
    this.commands = commands;
  }
}
