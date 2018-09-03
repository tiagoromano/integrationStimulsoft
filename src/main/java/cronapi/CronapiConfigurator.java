package cronapi;

import java.io.IOException;
import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;

import cronapi.serialization.CronappModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.CalendarSerializer;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.gson.JsonElement;

@Configuration
public class CronapiConfigurator {
  
  public static String ENCODING = "UTF-8";

  @Bean
  public FilterRegistrationBean userInsertingMdcFilterRegistrationBean() {
    FilterRegistrationBean registrationBean = new FilterRegistrationBean();
    registrationBean.setFilter(new CronapiFilter());
    registrationBean.setOrder(-1 * Integer.MAX_VALUE);
    return registrationBean;
  }

  @Bean
  public Jackson2ObjectMapperBuilder objectMapperBuilder() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    builder.serializationInclusion(JsonInclude.Include.NON_NULL);
    builder.deserializerByType(Var.class, new VarDeserializer());
    builder.serializerByType(JsonElement.class, new ToStringSerializer());
    
    builder.serializerByType(Calendar.class, new CalendarSerializer() {
      
      @Override
      public void serialize(Calendar value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        
        if(RestClient.getRestClient() != null && RestClient.getRestClient().getRequest() != null &&
                ("true".equals(RestClient.getRestClient().getRequest().getHeader("toJS")) ||
                        "true".equals(RestClient.getRestClient().getRequest().getParameter("toJS"))))
          gen.writeRawValue("new Date(\"" + ISO8601Utils.format(value.getTime(), true) + "\")");
        else
          gen.writeString(ISO8601Utils.format(value.getTime(), true));
      }
    });
    builder.dateFormat(new ISO8601DateFormat() {
      @Override
      public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        String value = ISO8601Utils.format(date, true);
        toAppendTo.append(value);
        return toAppendTo;
      }
    });
    
    builder.modulesToInstall(new CronappModule());
    return builder;
  }

//  @Bean(name = "conversionService")
//  public ConversionService getConversionService() {
//    ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
//    Set<Converter> converters = new HashSet<Converter>();
//
//    //converters.add(new StringToVarConverter());
//
//    bean.setConverters(converters);
//    return bean.getObject();
//  }

  @Bean
  public MultipartConfigElement multipartConfigElement() {
    MultipartConfigFactory factory = new MultipartConfigFactory();
    factory.setMaxFileSize("102400MB");
    factory.setMaxRequestSize("102400MB");
    return factory.createMultipartConfig();
  }
}
