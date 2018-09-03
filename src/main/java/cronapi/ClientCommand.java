package cronapi;

import org.apache.olingo.odata2.api.ClientCallback;

import java.util.LinkedList;
import java.util.List;

public class ClientCommand {
  private Var function;
  private List<Var> params = new LinkedList<Var>();

  public ClientCommand(String function) {
    this.function = Var.valueOf(function);
  }

  public void addParam(Object... values) {
    for (Object o : values) {
      params.add(Var.valueOf(o));
    }
  }

  public Var getFunction() {
    return function;
  }

  public void setFunction(Var function) {
    this.function = function;
  }

  public List<Var> getParams() {
    return params;
  }

  public void setParams(List<Var> params) {
    this.params = params;
  }

  public ClientCallback toClientCallback() {
    ClientCallback clientCallback = new ClientCallback(function.toString());
    for (Var v: params) {
      clientCallback.addParam(v);
    }

    return clientCallback;
  }
}
