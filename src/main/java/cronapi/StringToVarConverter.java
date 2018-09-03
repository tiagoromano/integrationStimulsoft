package cronapi;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToVarConverter implements Converter<String, Var> {


  @Override
  public Var convert(String source) {
    return Var.valueOf(source);
  }
}
