package cronapi.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cronapi.rest.DownloadREST;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-10-04
 *
 */

public class StorageService {

  private static String UPLOADED_FOLDER_FULLPATH;

  private ServletContext servletContext;

	static {
    UPLOADED_FOLDER_FULLPATH = DownloadREST.TEMP_FOLDER.getAbsolutePath();
	}

	public static StorageServiceResult saveUploadFiles(MultipartFile[] files) {
		String savedFiles = "";
		String name = "";
		String fileExtension = "";
		String contentType = "";
		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				continue;
			}

			try {
				UUID uuid = UUID.randomUUID();
				String randomUUIDString = uuid.toString().replace("-", "");

				Path moveTo = Paths.get(UPLOADED_FOLDER_FULLPATH + File.separator + randomUUIDString + ".bin");
				file.transferTo(moveTo.toFile());

				Path metadata = Paths.get(UPLOADED_FOLDER_FULLPATH + File.separator + randomUUIDString + ".md");
				Files.write(metadata, generateMetadata(file));

				fileExtension = "";
				if (file.getOriginalFilename().indexOf(".") > -1)
					fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().indexOf("."))
							.trim();
				name = file.getOriginalFilename().replace(fileExtension, "");
				contentType = file.getContentType();
				savedFiles = String.format("%s", Paths.get(randomUUIDString + ".bin").toString());

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		String json = String.format(
				"{\"type\": \"tempFile\", \"path\": \"%s\", \"name\": \"%s\", \"fileExtension\": \"%s\", \"contentType\": \"%s\"}",
				savedFiles, name, fileExtension, contentType);
		return new StorageServiceResult(json);
	}

  private static String getFileName(Object file) {
    if (file instanceof File)
      return ((File)file).getName();
    else
      return ((MultipartFile)file).getOriginalFilename();
  }
  
  private static String getFileContentType(Object file) {
    if (file instanceof File) {
      try {
        String path = ((File)file).getPath(); 
        return java.nio.file.Files.probeContentType(java.nio.file.Paths.get(path));
      }
      catch (Exception e) {
        return "";
      }
    }
    else {
      return ((MultipartFile)file).getContentType();
    }
  }

	public static byte[] generateMetadata(Object file) {
		String fileExtension = "";
		String fileName = getFileName(file);
		if (fileName.indexOf(".") > -1)
			fileExtension = fileName.substring(fileName.indexOf(".")).trim();
		
		String name = fileName.replace(fileExtension, "");
		String contentType = getFileContentType(file);
		if (name.length() > 250)
			name = name.substring(0, 250);

		String result = String.format("{\"name\":\"%s\",\"fileExtension\":\"%s\",\"contentType\":\"%s\"}", name,
				fileExtension, contentType);
		while (result.length() < 256)
			result += " ";
		return result.getBytes();
	}
	
	public static boolean isFileImage(Object file) {
	  String contentFile = getFileContentType(file);
	  return contentFile.indexOf("image/") > -1 ;
	}

  private static JsonObject getTempFileJson(String content) {
    JsonObject tempFileJson = new JsonParser().parse(getJsonAdjusted(content)).getAsJsonObject();
    return tempFileJson;
  }

	public static boolean isTempFileJson(String content) {
		boolean result = false;
		try {
			JsonObject tempFileJson = new JsonParser().parse(getJsonAdjusted(content)).getAsJsonObject();
			if ("tempFile".equals(tempFileJson.get("type").getAsString()))
				result = true;
		} catch (Exception e) {
			//Abafa, não é tempfileJson, irá retornar false
		}
		return result;
	}

	public boolean deleteFile(String path) {
		try {
			return new File(path).delete();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] getFileBytesWithMetadata(String name) {
		try {
			name = getTempFileJson(name).get("path").getAsString();
			
			String pathBinary = UPLOADED_FOLDER_FULLPATH + File.separator + name;
			String pathMetadata = UPLOADED_FOLDER_FULLPATH + File.separator + name.replace(".bin", ".md");

			byte[] fileBynary = Files.readAllBytes(Paths.get(pathBinary));
			byte[] fileMetadata = Files.readAllBytes(Paths.get(pathMetadata));

			byte[] bytes = new byte[fileMetadata.length + fileBynary.length];
			System.arraycopy(fileMetadata, 0, bytes, 0, fileMetadata.length);
			System.arraycopy(fileBynary, 0, bytes, fileMetadata.length, fileBynary.length);
			return bytes;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static byte[] getFileBytesWithMetadata(File file) {
		try {
			byte[] fileBynary = FileUtils.readFileToByteArray(file);
			byte[] fileMetadata = generateMetadata(file);

			byte[] bytes = new byte[fileMetadata.length + fileBynary.length];
			System.arraycopy(fileMetadata, 0, bytes, 0, fileMetadata.length);
			System.arraycopy(fileBynary, 0, bytes, fileMetadata.length, fileBynary.length);
			return bytes;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] getFileBytesWithoutMetadata(String name) {
		try {
			name = getTempFileJson(name).get("path").getAsString();
			String pathBinary = UPLOADED_FOLDER_FULLPATH + File.separator + name;
			byte[] fileBynary = Files.readAllBytes(Paths.get(pathBinary));
			return fileBynary;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static StorageServiceFileObject getFileObjectFromTempDirectory(String name) {
		try {
			try {
			  name = getTempFileJson(name).get("path").getAsString();
			}
			catch (Exception e) {
			  //Abafa, Vai tentar pegar diretamente do nome.
			}
			String pathBinary = UPLOADED_FOLDER_FULLPATH + File.separator + name;
			String pathMetadata = UPLOADED_FOLDER_FULLPATH + File.separator + name.replace(".bin", ".md");

			byte[] fileBynary = Files.readAllBytes(Paths.get(pathBinary));
			byte[] fileMetadata = Files.readAllBytes(Paths.get(pathMetadata));

			return generateStorageServiceFileObject(fileBynary, fileMetadata);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static StorageServiceFileObject generateStorageServiceFileObject(byte[] fileBinary, byte[] fileMetadata) {
		if (fileMetadata != null) {
  		JsonObject metadata = new JsonParser().parse(getJsonAdjusted(new String(fileMetadata))).getAsJsonObject();
  		return new StorageServiceFileObject(metadata.get("name").getAsString(), metadata.get("fileExtension").getAsString(),
  				metadata.get("contentType").getAsString(), fileBinary);
		}
		else {
		  InputStream is = new BufferedInputStream(new ByteArrayInputStream(fileBinary));
      String mimeType = "";
      try {      
        mimeType = URLConnection.guessContentTypeFromStream(is);
      }
      catch (Exception e) {
      }
      String extension = "";
      UUID uuid = UUID.randomUUID();
      String name = uuid.toString().substring(0, 6);
      if (mimeType != null && mimeType.length() > 0) {
        extension = "." + mimeType.split("/")[1];
      }
		  return new StorageServiceFileObject(name, extension, mimeType, fileBinary);
		}
	}

  private static String getJsonAdjusted(String rawJson) {
    return rawJson.substring(0, rawJson.indexOf("}")+1);
  }

  public static boolean isStorageServiceFileObject(byte[] result) {
    byte[] fileMetadata = new byte[256];
		System.arraycopy(result, 0, fileMetadata, 0, 256);
		try {
		  JsonObject metadata = new JsonParser().parse(getJsonAdjusted(new String(result))).getAsJsonObject();
		  return true;
		}
		catch (Exception e) {
		  return false;
		}
  }

	public static StorageServiceFileObject getFileObjectFromBytes(byte[] result) {
		try {
		  if (isStorageServiceFileObject(result)) {
  			byte[] fileBytes = new byte[result.length - 256];
  			System.arraycopy(result, 256, fileBytes, 0, result.length - 256);
  			byte[] fileMetadata = new byte[256];
  			System.arraycopy(result, 0, fileMetadata, 0, 256);
  			return generateStorageServiceFileObject(fileBytes, fileMetadata);
		  }
		  else {
		    return generateStorageServiceFileObject(result, null);
		  }
		  
		  
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Só retorna conteudo se tiver um metadata valido
	 */
	public static byte[] getFileBytesMetadata(byte[] result) {
		try {
			byte[] fileMetadata = new byte[256];
			System.arraycopy(result, 0, fileMetadata, 0, 256);
			JsonObject metadata = new JsonParser().parse(getJsonAdjusted(new String(fileMetadata))).getAsJsonObject();
			return fileMetadata;
		} catch (Exception e) {
			//É abafado propositalmente, pois pode ser um array sem metadata
		}
		return null;
	}

}