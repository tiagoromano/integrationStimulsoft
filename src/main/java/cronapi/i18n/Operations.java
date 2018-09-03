package cronapi.i18n;

import java.lang.reflect.Method;
import java.text.MessageFormat;

import cronapi.Utils;
import cronapi.Var;

public class Operations {

  public static final Var translate(Var keyI18n, Var... params) throws Exception {
    String text = keyI18n.getObjectAsString();
    text = MessageFormat.format(AppMessages.getString(text), Var.asObjectArray(params));
    return Var.valueOf(text);
  }
}
