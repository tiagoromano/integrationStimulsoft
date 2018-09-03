package cronapi.util;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-10-05
 *
 */

public class StorageServiceFileObject {
	public String name;
	public String extension;
	public String contentType;
	public byte[] bytes;

	public StorageServiceFileObject(String name, String extension, String contentType, byte[] bytes) {
		this.name = name;
		this.extension = extension;
		this.bytes = bytes;
		this.contentType = contentType;
	}

}
