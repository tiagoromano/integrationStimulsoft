package cronapi.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DataType {
  private static Map<String, String> contentType = initializeDefaultFileMimeTypes();

  private static HashMap<String, String> initializeDefaultFileMimeTypes() {

    HashMap<String, String> map = new HashMap<>();

    // document
    map.put("pdf", "application/pdf");

    // images
    map.put("jpg", "image/jpeg");
    map.put("gif", "image/gif");
    map.put("png", "image/png");
    map.put("bmp", "image/bmp");
    map.put("ico", "image/x-icon");

    // xml
    map.put("htm", "text/html");
    map.put("html", "text/html");
    map.put("xml", "text/xml");
    map.put("xsd", "text/xml");

    // xml rest
    map.put("yaml", "text/x-yaml");
    map.put("raml", "text/x-raml");

    // script
    map.put("js", "text/javascript");
    map.put("bat", "text/bat");
    map.put("sh", "text/sh");

    // compressed files
    map.put("zip", "application/zip");
    map.put("bz2", "application/x-bzip2");
    map.put("gz", "application/gzip");

    // compressed java files
    map.put("ear", "application/java-archive");
    map.put("jar", "application/java-archive");
    map.put("war", "application/java-archive");

    // text
    map.put("txt", "text/plain");
    map.put("css", "text/css");
    map.put("md", "text/markdown");
    map.put("mf", "text/manifest");
    map.put("sql", "text/sql");
    map.put("jsp", "text/jsp");
    map.put("json", "text/json");
    map.put("db", "text/database");
    map.put("java", "text/java");
    map.put("properties", "text/properties");
    map.put("project", "text/project");
    map.put("classpath", "text/classpath");
    map.put("ftl", "text/ftl");
    map.put("gitignore", "text/plain");

    // diagram
    map.put("umlcd", "text/umlcd");
    map.put("umlsc", "text/umlsc");
    map.put("erd", "text/erd");
    map.put("bpmn", "text/bpmn");
    map.put("fsa", "text/fsa");
    map.put("org", "text/org");
    map.put("pn", "text/pn");
    map.put("devs", "text/devs");
    map.put("logic", "text/logic");
    map.put("graph", "text/graph");

    map.put("kroki", "text/kroki");


    // report
    map.put("jrxml", "text/xml");

    map.put("blockly", "text/blockly");

    return map;
  }

  /**
   * Obtém a extensão do arquivo a partir da referência File do mesmo.
   *
   * @param file
   *          Referência do arquivo que se deseja obter a extensão.
   * @return Extensão do arquivo.
   */
  public static String getFileExtension(File file) {
    return getFileExtension(file.getAbsolutePath());
  }

  /**
   * Obtém a extensão do arquivo a partir do caminho absoluto do mesmo.
   *
   * @param path
   *          Caminho absoluto do arquivo que se deseja obter a extensão.
   * @return Extensão
   */
  public static String getFileExtension(String path) {
    int fileType = path.lastIndexOf(".");
    return path.substring(fileType + 1).trim();
  }

  /**
   * Obtém o mime-type a partir da referência do arquivo na lista de mim-type
   * definida. Caso não seja encontrado, será retornado o mime-type do tipo
   * "application/binary".
   *
   * @param filename
   *          Referência do arquivo.
   * @return Retorna o mime-type do arquivo informado.
   */
  public static String getContentType(File filename) {
    return getContentType(filename.getAbsolutePath());
  }

  /**
   * Obtém o mime-type a partir do nome do arquivo na lista de mime-type
   * definida. Caso não seja encontrado, será retornado o mime-type do tipo
   * "application/binary".
   *
   * @param filename
   *          Nome do arquivo
   * @return Retorna o mime-type do arquivo informado.
   */
  public static String getContentType(String filename) {
    String ext = getFileExtension(filename);
    if(contentType.containsKey(ext.toLowerCase()))
      return contentType.get(ext.toLowerCase());

    return "application/binary";
  }
}
