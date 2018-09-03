package cronapi.io;

import cronapi.i18n.Messages;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cronapi.*;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.rest.DownloadREST;
import cronapi.util.Callback;
import org.apache.commons.io.IOUtils;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-03-28
 *
 */

@CronapiMetaData(category = CategoryType.IO, categoryTags = { "Arquivo", "File" }, helpTemplate = "{{addressOfTheServerBlockDocumentation}}")
public class Operations {

	private static String APP_FOLDER;

	static {
		if (System.getProperty("cronos.bin") != null && !System.getProperty("cronos.bin").isEmpty()) {
			APP_FOLDER = new File(System.getProperty("cronos.bin")).getParent();
		} else {
			try {
				URL location = Operations.class.getProtectionDomain().getCodeSource().getLocation();
				String file = new File(location.getFile()).getAbsolutePath();
				APP_FOLDER = file.substring(0, file.indexOf("WEB-INF") - 1);
			} catch (Exception e) {
				//Abafa
			}
		}
	}

	/**
	 * Criar nova pasta 
	 */
	@CronapiMetaData(type = "function", name = "{{createFolder}}", nameTags = {
			"createFolder" }, description = "{{functionToCreateNewFolder}}", params = {
					"{{pathMustBeCreatedForFolder}}", "{{folderName}}" }, paramsType = { ObjectType.STRING,
							ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var folderCreate(Var path, Var folderName) throws Exception {
		boolean success = true;
		String folder = path.getObjectAsString().trim() + File.separator + folderName.getObjectAsString().trim();
		File dir = new File(folder);
		if (!dir.exists()) {
			success = dir.mkdirs();
			return new Var(success);
		}
		return Var.VAR_FALSE;
	}

	/**
	 * MD5 do Arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{MD5OfFile}}", nameTags = {
			"fileMD5" }, description = "{{functionToReturnMD5OfFile}}", params = {
					"{{pathOfFile}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	public static final Var fileMD5(Var path) throws Exception {
		return new Var(Utils.encodeMD5(new File(path.getObjectAsString().trim())));
	}

	/**
	 * Remover Pasta de Arquivos
	 */
	@CronapiMetaData(type = "function", name = "{{removeFolderFiles}}", nameTags = { "removeFolder",
			"deleteFolder" }, description = "{{functionToRemoveFolderFiles}}", params = {
					"{{pathOfFolder}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var fileRemoveAllFolder(Var path) throws Exception {
		File dir = new File(path.getObjectAsString().trim());
		return new Var(Utils.deleteFolder(dir));
	}

	/**
	 * Pode Ler?
	 */
	@CronapiMetaData(type = "function", name = "{{canReadyFile}}", nameTags = {
			"fileCanRead" }, description = "{{functionToCheckIfCanReadFile}}", params = {
					"{{pathOfFile}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var fileCanRead(Var path) throws Exception {
		File file = new File(path.getObjectAsString().trim());
		return new Var(file.canRead());
	}

	/**
	 * Pode Escrever?
	 */
	@CronapiMetaData(type = "function", name = "{{canWriteFile}}", nameTags = {
			"fileCanWrite" }, description = "{{functionToCheckIfCanWriteFile}}", params = {
					"{{pathOfFile}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var fileCanWrite(Var path) throws Exception {
		File file = new File(path.getObjectAsString().trim());
		return new Var(file.canWrite());
	}

	/**
	 * Criar Novo Arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{createNewFile}}", nameTags = {
			"fileCreate" }, description = "{{functionToCreateFile}}", params = {
					"{{pathOfFile}}" }, paramsType = { ObjectType.STRING })
	public static final void fileCreate(Var path) throws Exception {
		if (!Files.exists(Paths.get(path.getObjectAsString().trim()), LinkOption.NOFOLLOW_LINKS))
			Files.createFile(Paths.get(path.getObjectAsString().trim()));
	}

	/**
	 * Remover Arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{removeFile}}", nameTags = {
			"fileRemove" }, description = "{{functionToRemoveFile}}", params = {
					"{{pathOfFile}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var fileRemove(Var path) throws Exception {
		Path p = Paths.get(path.getObjectAsString().trim());
		return new Var(Files.deleteIfExists(p));
	}

	/**
	 * Existe o Arquivo?
	 */
	@CronapiMetaData(type = "function", name = "{{fileExists}}", nameTags = {
			"fileExists" }, description = "{{functionToCheckIfExistFile}}", params = {
					"{{pathOfFile}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var fileExists(Var path) throws Exception {
		Path p = Paths.get(path.getObjectAsString().trim());
		return new Var(Files.exists(p, LinkOption.NOFOLLOW_LINKS));
	}

	/**
	 * Copiar Arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{copyFile}}", nameTags = {
			"fileCopy" }, description = "{{functionToCopyFile}}", params = { "{{sourcePath}}",
					"{{destinationPath}}" }, paramsType = { ObjectType.STRING, ObjectType.STRING })
	public static final void fileCopy(Var pathFrom, Var pathTo) throws Exception {
		File from = new File(pathFrom.getObjectAsString().trim());
		File to = new File(pathTo.getObjectAsString().trim());
		Utils.copyFileTo(from, to);
	}

	/**
	 * Obter Pai do Arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{getParentOfFile}}", nameTags = {
			"fileGetParent" }, description = "{{functionToGetParentOfFile}}", params = {
					"{{pathOfFile}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	public static final Var fileGetParent(Var path) throws Exception {
		File file = new File(path.getObjectAsString().trim());
		if (file.exists()) {
			return new Var(file.getParent());
		} else {
			return new Var(null);
		}
	}

	/**
	 * Renomear Arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{renameFile}}", nameTags = {
			"fileRename" }, description = "{{functionToRenameFile}}", params = { "{{pathOfFile}}",
					"{{newNameOfFile}}" }, paramsType = { ObjectType.STRING,
							ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var fileRename(Var path, Var name) throws Exception {
		File from = new File(path.getObjectAsString().trim());
		File to = new File(from.getParentFile(), name.getObjectAsString().trim());
		return new Var(from.renameTo(to));
	}

  /**
   * Mover Arquivo
   */
  @CronapiMetaData(type = "function", name = "{{moveFile}}", nameTags = {
      "fileMove" }, description = "{{functionToMoveFile}}", params = { "{{pathOfSourceFile}}",
      "{{pathOfDestinationFile}}" }, paramsType = { ObjectType.STRING,
      ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
  public static final Var fileMove(Var pathFrom, Var pathTo) throws Exception {
    File from = new File(pathFrom.getObjectAsString().trim());
    File toFolder = new File(pathTo.getObjectAsString().trim());
    File to = toFolder;
    if (toFolder.isDirectory()) {
      to = new File(toFolder, from.getName());
    }

    Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);

    return Var.VAR_TRUE;
  }

	/**
	 * Abrir arquivo para escrita
	 */
	@CronapiMetaData(type = "function", name = "{{openFileToWrite}}", nameTags = {
			"fileOpenToWrite" }, description = "{{functionToOpenFileToWrite}}", params = { "{{pathOfFile}}",
					"{{addText}}" }, paramsType = { ObjectType.STRING,
							ObjectType.STRING }, returnType = ObjectType.OBJECT)
	public static final Var fileOpenToWrite(Var url, Var append) throws Exception {
		if (!append.equals(Var.VAR_NULL)) {
			FileOutputStream out = new FileOutputStream(new File(url.getObjectAsString()));
			out.write(append.getObjectAsString().getBytes());
			return new Var(out);
		} else {
			FileOutputStream out = new FileOutputStream(new File(url.getObjectAsString()));
			return new Var(out);
		}
	}

	/**
	 * Abrir arquivo para leitura
	 */
	@CronapiMetaData(type = "function", name = "{{openFileToRead}}", nameTags = {
			"fileOpenToRead" }, description = "{{functionToOpenFileToRead}}", params = {
					"{{pathOfFile}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.OBJECT)
	public static final Var fileOpenToRead(Var url) throws Exception {
		FileInputStream in = new FileInputStream(new File(url.getObjectAsString()));
		return new Var(in);
	}

	/**
	 * Adicionar conteúdo a arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{addContentToFile}}", nameTags = {
			"fileAppend" }, description = "{{functionToAddContentToFile}}", params = { "{{streamOfFileToWrite}}",
					"{{contentOfFile}}" }, paramsType = { ObjectType.OBJECT, ObjectType.OBJECT })
	public static final void fileAppend(Var outPut, Var content) throws Exception {
		FileOutputStream out = (FileOutputStream) outPut.getObject();
		if (content.getObject() instanceof byte[])
			out.write((byte[]) content.getObject());
		else
			out.write(content.getObjectAsString().getBytes());
	}

	/**
	 * Ler conteúdo do arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{readContentOfFile}}", nameTags = {
			"fileRead" }, description = "{{functionToReadContentOfFile}}", params = { "{{streamOfFileToRead}}",
					"{{sizeInBytes}}" }, paramsType = { ObjectType.OBJECT, ObjectType.LONG }, returnType = ObjectType.STRING)
	public static final Var fileRead(Var input, Var size) throws Exception {
		byte[] byteSizeToRead = new byte[size.getObjectAsInt()];
		FileInputStream in = (FileInputStream) input.getObject();
		int bytes = in.read(byteSizeToRead);
		return new Var(new String(byteSizeToRead, 0, bytes));
	}

	/**
	 * Ler todo contéudo do arquivos
	 */
	@CronapiMetaData(type = "function", name = "{{readAllContentOfFile}}", nameTags = {
			"fileReadAll" }, description = "{{functionToReadAllContentOfFile}}", params = {
					"{{streamOfFileToRead}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.STRING)
	public static final Var fileReadAll(Var input) throws Exception {
		FileInputStream in = (FileInputStream) input.getObject();
		return new Var(Utils.getFileContent(in).toString());
	}

	/**
	 * Ler uma linha do arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{readLineOfFile}}", nameTags = {
			"fileReadLine" }, description = "{{functionToReadLineOfFile}}", params = { "{{streamOfFileToRead}}",
					"{{callBackStatements}}" }, paramsType = { ObjectType.OBJECT, ObjectType.STATEMENTSENDER })
	public static final void readLine(Var input, cronapi.util.Callback callback) throws Exception {
		cronapi.util.Operations.readLinesFromStream(input, callback);
	}

	@CronapiMetaData(type = "function", name = "{{readBytesFromStreamName}}", nameTags = {
			"readBytesFromStream" }, description = "{{readBytesFromStreamDescription}}", params = {
					"{{readBytesFromStreamParam0}}", "{{readBytesFromStreamParam1}}",
					"{{callBackStatements}}" }, paramsType = { ObjectType.OBJECT, ObjectType.LONG,
							ObjectType.STATEMENTSENDER })
	public static void readBytesFromStream(Var input, Var size, Callback callback) throws Exception {
		cronapi.util.Operations.readBytesFromStream(input, size, callback);
	}

	/**
	 * Limpar o arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{clearFile}}", nameTags = {
			"fileFlush" }, description = "{{functionToClearFile}}", params = {
					"{{streamOfFileToWrite}}" }, paramsType = { ObjectType.OBJECT })
	public static final void fileFlush(Var input) throws Exception {
		FileOutputStream fos = (FileOutputStream) input.getObject();
		fos.flush();
	}

	/**
	 * Fechar o arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{closeFile}}", nameTags = {
			"fileClose" }, description = "{{functionToCloseFile}}", params = {
					"{{streamOfFile}}" }, paramsType = { ObjectType.OBJECT })
	public static final void fileClose(Var input) throws Exception {
		if (input.getObject() instanceof FileOutputStream) {
			FileOutputStream fos = (FileOutputStream) input.getObject();
			fos.flush();
			fos.close();
		} else {
			FileInputStream fis = (FileInputStream) input.getObject();
			fis.close();
		}
	}

	/**
	 * Diretorio temporário da aplicação
	 */
	@CronapiMetaData(type = "function", name = "{{applicationTemporaryFolder}}", nameTags = {
			"fileTempDir" }, description = "{{functionToReturnApplicationTemporaryFolder}}", params = {}, returnType = ObjectType.STRING)
	public static final Var fileTempDir() throws Exception {
		return new Var(System.getProperty("java.io.tmpdir"));
	}

	/**
	 * Diretorio temporário da aplicação
	 */
	@CronapiMetaData(type = "function", name = "{{applicationFolder}}", nameTags = {
			"fileTempDir" }, description = "{{functionToReturnApplicationFolder}}", params = {}, returnType = ObjectType.STRING)
	public static final Var fileAppDir() throws Exception {
		return new Var(APP_FOLDER);
	}

  /**
   * Diretorio temporário da aplicação reciclável
   */
  @CronapiMetaData(type = "function", name = "{{applicationRecycleFolder}}", nameTags = {
      "fileTempDir" }, description = "{{functionToReturnApplicationRecycleFolder}}", params = {}, returnType = ObjectType.STRING)
  public static final Var fileAppReclycleDir() throws Exception {
    return new Var(DownloadREST.TEMP_FOLDER.getAbsolutePath());
  }

  /**
   * Download de arquivo
   */
  @CronapiMetaData(type = "function", name = "{{fileDownloadName}}", nameTags = {
      "fileTempDir" }, description = "{{fileDownloadDescription}}", params = {"{{fileDownloadParam0}}"}, paramsType = { ObjectType.OBJECT })
  public static final void fileDownload(Var varFile) throws Exception {
    RestClient.getRestClient().downloadURL(DownloadREST.getDownloadUrl(varFile.getObjectAsFile()));
  }

	/**
	 * Ler todo conteudo do arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{readAllContentFileInBytes}}", nameTags = {
			"fileReadAllToBytes" }, description = "{{functionToReadAllContentFileInBytes}}", params = {
					"{{streamOfFileToRead}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var fileReadAllToBytes(Var input) throws Exception {
		FileInputStream fin = (FileInputStream) input.getObject();
		long length = fin.getChannel().size();
		byte fileContent[] = new byte[(int) length];
		fin.read(fileContent);
		fin.close();
		return new Var(fileContent);
	}

	/**
	 * Checar se é final do arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{isEndOfFile}}", nameTags = {
			"isFileEoF" }, description = "{{functionToCheckIsEndOfFile}}", params = {
					"{{streamOfFileToRead}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.BOOLEAN)
	public static final Var isFileEoF(Var input) throws Exception {
		FileInputStream fis = (FileInputStream) input.getObject();
		return new Var(fis.getChannel().position() == fis.getChannel().size());
	}

	/**
	 * Obter o tamanho do arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{sizeOfFile}}", nameTags = {
			"fileGetSize" }, description = "{{functionToGetSizeOfFile}}", params = {
					"{{streamOfFileToRead}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.LONG)
	public static final Var fileGetSize(Var input) throws Exception {
		FileInputStream fis = (FileInputStream) input.getObject();
		return new Var(fis.getChannel().size());
	}

	/**
	 * É diretorio?
	 */
	@CronapiMetaData(type = "function", name = "{{isFolder}}", nameTags = { "isDirectory",
			"isFolder" }, description = "{{functionToCheckIsFolder}}", params = {
					"{{pathOfFolder}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var isDirectory(Var path) {
		File dir = new File(path.getObjectAsString());
		return new Var(dir.isDirectory());
	}

	/**
	 * Obter Total de Linhas do Arquivo
	 */
	@CronapiMetaData(type = "function", name = "{{totalLinesFile}}", nameTags = {
			"fileGetNumberOfLines" }, description = "{{functionToGetTotalLinesFile}}", params = {
					"{{pathOfFile}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.LONG)
	public static final Var fileGetNumberOfLines(Var path) throws Exception {
		Path p = Paths.get(path.getObjectAsString());
		long lineCount = Files.lines(p).count();
		return new Var(lineCount);
	}

	/**
	 *  Download Arquivo a partir de URL
	 */
	@CronapiMetaData(type = "function", name = "{{downloadFileFromUrl}}", nameTags = {
			"downloadFileFromUrl" }, description = "{{functionToDownloadFileFromUrl}}", params = { "{{URLAddress}}",
					"{{folderPathToSaveFile}}", "{{nameOfFileFromURL}}", "{{fileExtensionWithDot}}" }, paramsType = {
							ObjectType.STRING, ObjectType.STRING, ObjectType.STRING,
							ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var downloadFileFromUrl(Var urlAddress, Var path, Var name, Var extension) {
		try {
			if (path.isNull() && !name.isNull()) {
        return downloadUrltoFile(urlAddress, Var.valueOf(name.getObjectAsString() + extension.getObjectAsString()));
			} else if (!name.isNull()) {

				String pathLocal = path.getObjectAsString();
				java.net.URL url = new java.net.URL(urlAddress.getObjectAsString());
				if (!pathLocal.endsWith(File.separator))
					pathLocal += File.separator;

        return downloadUrltoFile(urlAddress, Var.valueOf(pathLocal + name.getObjectAsString() + extension.getObjectAsString()));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return new Var(false);
	}

	public static void resolveAuthentication(String urlAddress, URLConnection urlConnection) {
    Pattern pattern = Pattern.compile("https?:\\/\\/(.*?):(.*?)@.*");
    Matcher matcher = pattern.matcher(urlAddress);

    if (matcher.find()) {
      try {
        String user = URLDecoder.decode(matcher.group(1), "UTF8");
        String pass = URLDecoder.decode(matcher.group(2), "UTF8");
        String header = "Basic " + new String(Base64.getEncoder().encode((user+":"+pass).getBytes()));
        urlConnection.addRequestProperty("Authorization", header);
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }

  }

  @CronapiMetaData(type = "function", name = "{{downloadUrltoFile}}", nameTags = {
      "download", "url" }, description = "{{functionDownloadUrltoFile}}", returnType = ObjectType.BOOLEAN)
  public static final Var downloadUrltoFile(
      @ParamMetaData(type = ObjectType.STRING, description = "{{URLAddress}}") Var urlAddress,
      @ParamMetaData(type = ObjectType.STRING, description = "{{filePathToSaveURL}}") Var file) throws Exception {
    java.net.URL url = new java.net.URL(urlAddress.getObjectAsString());

    URLConnection urlConnection = url.openConnection();
    resolveAuthentication(urlAddress.getObjectAsString(), urlConnection);

    try (java.io.InputStream is = urlConnection.getInputStream()) {
      File outFile = new File(file.getObjectAsString());
      if (outFile.exists()) {
        outFile.delete();
      }

      try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile)) {
        IOUtils.copy(is, fos);
      }
    }

    return new Var(true);
  }

  /**
   *  Ler Todo Arquivo Definindo Charset
   */
  @CronapiMetaData(type = "function", name = "{{readAllFileWithCharset}}", nameTags = {
      "fileReadContentWithCharset" }, description = "{{functionToReadAllFileWithCharset}}", params = {
          "{{streamOfFileToRead}}", "{{charset}}" }, paramsType = { ObjectType.OBJECT,
              ObjectType.STRING }, returnType = ObjectType.STRING)
  public static final Var fileReadContentWithCharset(
      @ParamMetaData(type = ObjectType.STRING, description = "{{streamOfFileToRead}}") Var finp,
      @ParamMetaData(type = ObjectType.STRING, description = "{{charset}}", blockType = "util_dropdown", keys = {
          "UTF-8", "UTF-16", "US-ASCII", "ISO-8859-1", "ISO-8859-2" }) Var charsetSelected)
      throws Exception {
    String result = org.apache.commons.io.IOUtils.toString((java.io.InputStream) finp.getObject(),
        charsetSelected.getObjectAsString());
    return new Var(result);
  }

	/**
	 *  Descompactar arquivo zip	
	 */
	@CronapiMetaData(type = "function", name = "{{unZipFile}}", nameTags = {
			"unZip" }, description = "{{functionToUnZipFile}}", params = { "{{streamOfFileToRead}}",
					"{{destinationFolder}}" }, paramsType = { ObjectType.OBJECT, ObjectType.STRING })
	public static void unZip(Var zippedFile, Var destFolder) throws Exception {
		FileInputStream zipFile = (FileInputStream) zippedFile.getObject();
		String outputFolder = destFolder.getObjectAsString();
		if (!outputFolder.endsWith("/")) {
			outputFolder += "/";
		}
		byte[] buffer = new byte[1024];
		org.apache.commons.compress.archivers.zip.ZipArchiveInputStream zis = new org.apache.commons.compress.archivers.zip.ZipArchiveInputStream(
				zipFile, "UTF-8", true);
		org.apache.commons.compress.archivers.zip.ZipArchiveEntry ze = zis.getNextZipEntry();
		java.nio.charset.Charset utf8charset = java.nio.charset.Charset.forName("UTF-8");
		java.nio.charset.Charset iso88591charset = java.nio.charset.Charset.forName("ISO-8859-1");
		while (ze != null) {
			String fileName = ze.getName();
			java.io.File newFile = new java.io.File(outputFolder + fileName);
			if (ze.isDirectory()) {
				new java.io.File(outputFolder + fileName).mkdirs();
			} else {
				new java.io.File(newFile.getParent()).mkdirs();
				java.io.FileOutputStream fos = new java.io.FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					if (newFile.getAbsolutePath().endsWith(".js") || newFile.getAbsolutePath().endsWith(".html")
							|| newFile.getAbsolutePath().endsWith(".htm")) {
						java.nio.ByteBuffer inputBuffer = java.nio.ByteBuffer.wrap(buffer, 0, len);
						java.nio.CharBuffer data = utf8charset.decode(inputBuffer);
						java.nio.ByteBuffer outputBuffer = iso88591charset.encode(data);
						fos.write(outputBuffer.array(), 0, outputBuffer.array().length);
					} else {
						fos.write(buffer, 0, len);
					}
				}
				fos.close();
			}
			ze = zis.getNextZipEntry();
		}
		zis.close();
	}

	@CronapiMetaData(type = "function", name = "{{listFilesName}}", nameTags = {
			"listFiles" }, description = "{{listFilesDescription}}", returnType = ObjectType.STRING)
	public static final Var listFiles(
			@ParamMetaData(type = ObjectType.STRING, description = "{{listFilesParam0}}") Var path,
			@ParamMetaData(type = ObjectType.STRING, description = "{{listFilesParam1}}", blockType = "util_dropdown", keys = {
					"all", "directories", "files" }, values = { "{{all}}", "{{directories}}", "{{files}}" }) Var type)
			throws Exception {
		try {
			if (path.equals(Var.VAR_NULL))
				return Var.newList();

			if (type.getObjectAsString().equals("directories")) {

				FileFilter filter = new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname.isDirectory();
					}
				};

				File[] files = new File(path.getObjectAsString()).listFiles(filter);
				LinkedList<String> result = new LinkedList<String>();
				for (File f : files) {
					result.add(f.getName());
				}
				return new Var(result);

			} else if (type.getObjectAsString().equals("files")) {

				FileFilter filter = new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname.isFile();
					}
				};

				File[] files = new File(path.getObjectAsString()).listFiles(filter);
				LinkedList<String> result = new LinkedList<String>();
				for (File f : files) {
					result.add(f.getName());
				}
				return new Var(result);

			} else {
				String[] files = new File(path.getObjectAsString()).list();
				Var list = new Var(new LinkedList<String>(Arrays.asList(files)));
				return list;
			}

		} catch (Exception e) {
			return Var.newList();
		}
	}

	@CronapiMetaData(type = "function", name = "{{fileSeparatorName}}", nameTags = {
			"fileSeparator" }, description = "{{fileSeparatorDescription}}", returnType = ObjectType.STRING)
	public static final Var fileSeparator() {
		return Var.valueOf(File.separator);
	}
}