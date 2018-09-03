package cronapi.util;

import cronapi.Var;

@FunctionalInterface
public interface Callback {
  public void call(Var sender) throws Exception;
}
