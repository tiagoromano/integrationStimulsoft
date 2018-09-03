package cronapi.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import cronapi.CronapiMetaData;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.i18n.Messages;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-04-17
 *
 */

// @CronapiMetaData(category = CategoryType.FTP, categoryTags = { "FTP" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{openFTPConnection}}", nameTags = { "openFTPConnection", "ftpConnect",
			"openFtpConn" }, description = "{{functionToOpenFTPConnection}}", params = { "{{hostAddress}}",
					"{{hostPort}}", "{{login}}", "{{password}}" }, paramsType = { ObjectType.STRING, ObjectType.LONG,
							ObjectType.STRING, ObjectType.STRING }, returnType = ObjectType.OBJECT)
	public static final Var openFTPConnection(Var hostAddress, Var hostPort, Var login, Var password) throws Exception {
		//Doc:: https://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/ftp/FTPClient.html
		FTPClient ftp = new FTPClient();
		ftp.connect(hostAddress.getObjectAsString(), hostPort.getObjectAsInt());
		int reply = ftp.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			throw new Exception(Messages.getString("ftpServerRefusedConnection"));
		}
		if (!ftp.login(login.getObjectAsString().trim(), password.getObjectAsString())) {
			ftp.disconnect();
			throw new Exception(Messages.getString("loginPasswordInvalid"));
		}
		return new Var(ftp);
	}

	@CronapiMetaData(type = "function", name = "{{closeFTPConnection}}", nameTags = { "closeFTPConnection",
			"closeFtpConn" }, description = "{{functionToCloseFTPConnection}}", params = {
					"{{ftpConnectionObj}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.VOID)
	public static final void closeFTPConnection(Var ftpConnectionVar) throws Exception {
		FTPClient ftp = (FTPClient) ftpConnectionVar.getObject();
		ftp.logout();
		ftp.disconnect();
	}

	@CronapiMetaData(type = "function", name = "{{setPassiveModeFTP}}", nameTags = { "passiveModeFTP",
			"setPassiveModeFtp" }, description = "{{functionToSetPassiveModeFTP}}", params = {
					"{{ftpConnectionObj}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.VOID)
	public static final void setPassiveModeFTP(Var ftpConnectionVar) throws Exception {
		FTPClient ftp = (FTPClient) ftpConnectionVar.getObject();
		ftp.enterRemotePassiveMode();
	}

	@CronapiMetaData(type = "function", name = "{{sendFTPCommand}}", nameTags = {
			"sendFTPCommand" }, description = "{{functionToSendFTPCommand}}", params = { "{{ftpConnectionObj}}",
					"{{commandFtp}}" }, paramsType = { ObjectType.OBJECT,
							ObjectType.STRING }, returnType = ObjectType.VOID)
	public static final void sendFTPCommand(Var ftpConnectionVar, Var commandFtp) throws Exception {
		FTPClient ftp = (FTPClient) ftpConnectionVar.getObject();
		ftp.sendCommand(commandFtp.getObjectAsString().trim());
	}

	@CronapiMetaData(type = "function", name = "{{getCurrentWorkingDirectoryFTP}}", nameTags = {
			"getCurrentWorkingDirectoryFTP" }, description = "{{functionToGetCurrentWorkingDirectoryFTP}}", params = {
					"{{ftpConnectionObj}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.STRING)
	public static final Var getCurrentWorkingDirectoryFTP(Var ftpConnectionVar) throws Exception {
		FTPClient ftp = (FTPClient) ftpConnectionVar.getObject();
		return new Var(ftp.printWorkingDirectory());
	}

	@CronapiMetaData(type = "function", name = "{{listFTPFiles}}", nameTags = {
			"listFTPFiles" }, description = "{{functionToListFTPFiles}}", params = { "{{ftpConnectionObj}}",
					"{{withDetail}}" }, paramsType = { ObjectType.OBJECT,
							ObjectType.BOOLEAN }, returnType = ObjectType.STRING)
	public static final Var listFTPFiles(Var ftpConnectionVar, Var withDetail) throws Exception {
		FTPClient ftp = (FTPClient) ftpConnectionVar.getObject();
		StringBuilder sb = new StringBuilder();
		if (withDetail.getObjectAsBoolean()) {
			for (Object object : ftp.listFiles())
				sb.append(object).append("\n");
		} else {
			for (Object object : ftp.listNames())
				sb.append(object).append("\n");
		}
		return new Var(sb.toString());
	}

	@CronapiMetaData(type = "function", name = "{{listFTPFilesFromFolder}}", nameTags = {
			"listFTPFilesFromFolder" }, description = "{{functionToListFTPFilesFromFolder}}", params = {
					"{{ftpConnectionObj}}", "{{folder}}", "{{withDetail}}" }, paramsType = { ObjectType.OBJECT,
							ObjectType.STRING, ObjectType.BOOLEAN }, returnType = ObjectType.STRING)
	public static final Var listFTPFilesFromFolder(Var ftpConnectionVar, Var folder, Var withDetail) throws Exception {
		FTPClient ftp = (FTPClient) ftpConnectionVar.getObject();
		StringBuilder sb = new StringBuilder();
		String currentDir = ftp.printWorkingDirectory();
		ftp.changeWorkingDirectory(folder.getObjectAsString().trim());
		if (withDetail.getObjectAsBoolean()) {
			for (Object object : ftp.listFiles())
				sb.append(object).append("\n");
		} else {
			for (Object object : ftp.listNames())
				sb.append(object).append("\n");
		}
		ftp.changeWorkingDirectory(currentDir);
		return new Var(sb.toString());
	}

	@CronapiMetaData(type = "function", name = "{{changeFTPFolder}}", nameTags = {
			"changeFTPFolder" }, description = "{{functionToChangeFTPFolder}}", params = { "{{ftpConnectionObj}}",
					"{{folder}}" }, paramsType = { ObjectType.OBJECT, ObjectType.STRING }, returnType = ObjectType.VOID)
	public static final void changeFTPFolder(Var ftpConnectionVar, Var folder) throws Exception {
		FTPClient ftp = (FTPClient) ftpConnectionVar.getObject();
		ftp.changeWorkingDirectory(folder.getObjectAsString().trim());
	}

	@CronapiMetaData(type = "function", name = "{{changeToParentFTPFolder}}", nameTags = {
			"changeToParentFTPFolder" }, description = "{{functionToChangeToParentFTPFolder}}", params = {
					"{{ftpConnectionObj}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.VOID)
	public static final void changeToParentFTPFolder(Var ftpConnectionVar) throws Exception {
		FTPClient ftp = (FTPClient) ftpConnectionVar.getObject();
		ftp.changeToParentDirectory();
	}

	@CronapiMetaData(type = "function", name = "{{deleteFTPFolder}}", nameTags = {
			"deleteFTPFolder" }, description = "{{functionToDeleteFTPFolder}}", params = { "{{ftpConnectionObj}}",
					"{{folder}}" }, paramsType = { ObjectType.OBJECT, ObjectType.STRING }, returnType = ObjectType.VOID)
	public static final void deleteFTPFolder(Var ftpConnectionVar, Var folder) throws Exception {
		FTPClient ftp = (FTPClient) ftpConnectionVar.getObject();
		ftp.removeDirectory(folder.getObjectAsString().trim());
	}

	@CronapiMetaData(type = "function", name = "{{deleteFTPFile}}", nameTags = {
			"deleteFTPFile" }, description = "{{functionToDeleteFTPFile}}", params = { "{{ftpConnectionObj}}",
					"{{file}}" }, paramsType = { ObjectType.OBJECT, ObjectType.STRING }, returnType = ObjectType.VOID)
	public static final void deleteFTPFile(Var ftpConnectionVar, Var file) throws Exception {
		FTPClient ftp = (FTPClient) ftpConnectionVar.getObject();
		ftp.deleteFile(file.getObjectAsString().trim());
	}

	@CronapiMetaData(type = "function", name = "{{createNewFTPFolder}}", nameTags = {
			"createNewFTPFolder" }, description = "{{functionToCreateNewFTPFolder}}", params = { "{{ftpConnectionObj}}",
					"{{folder}}" }, paramsType = { ObjectType.OBJECT, ObjectType.STRING }, returnType = ObjectType.VOID)
	public static final void createNewFTPFolder(Var ftpConnectionVar, Var folder) throws Exception {
		FTPClient ftp = (FTPClient) ftpConnectionVar.getObject();
		ftp.makeDirectory(folder.getObjectAsString().trim());
	}

	@CronapiMetaData(type = "function", name = "{{uploadFileToFTP}}", nameTags = {
			"uploadFileToFTP" }, description = "{{functionToUploadFileToFTP}}", params = { "{{ftpConnectionObj}}",
					"{{pathOfFile}}", "{{pathOfFolderFTP}}", "{{isBinaryFile}}" }, paramsType = { ObjectType.OBJECT, ObjectType.STRING, ObjectType.STRING, ObjectType.BOOLEAN }, returnType = ObjectType.VOID)
	public static final void uploadFileToFTP(Var ftpConnectionVar, Var pathFile, Var ftpFolder, Var isBinaryFile)
			throws Exception {
		FTPClient ftp = (FTPClient) ftpConnectionVar.getObject();
		InputStream in = new FileInputStream(pathFile.getObjectAsString().trim());
		ftp.setFileType(isBinaryFile.getObjectAsBoolean() ? FTPClient.BINARY_FILE_TYPE : FTPClient.ASCII_FILE_TYPE);
		ftp.storeFile(ftpFolder.getObjectAsString().trim(), in);
		in.close();
	}
	
	@CronapiMetaData(type = "function", name = "{{downloadFileFromFTP}}", nameTags = {
			"downloadFileFromFTP" }, description = "{{functionToDownloadFileFromFTP}}", params = { "{{ftpConnectionObj}}",
					"{{saveFileInPath}}", "{{fileToDownloadFTPPath}}", "{{isBinaryFile}}" }, paramsType = { ObjectType.OBJECT, ObjectType.STRING, ObjectType.STRING, ObjectType.BOOLEAN }, returnType = ObjectType.VOID)
	public static final void downloadFileFromFTP(Var ftpConnectionVar, Var saveFileInPath, Var fileToDownloadFTPPath, Var isBinaryFile) throws Exception {
    FTPClient ftp = (FTPClient)ftpConnectionVar.getObject();
    ftp.setFileType(isBinaryFile.getObjectAsBoolean() ? FTPClient.BINARY_FILE_TYPE : FTPClient.ASCII_FILE_TYPE);
    OutputStream fileDownloaded = new FileOutputStream(saveFileInPath.getObjectAsString().trim());
    if(ftp.retrieveFile(fileToDownloadFTPPath.getObjectAsString(), fileDownloaded)) {
      fileDownloaded.close();
    } else {
      fileDownloaded.close(); 
      new File(saveFileInPath.getObjectAsString().trim()).delete(); 
      throw new Exception(Messages.getString("errorDownloadFile"));
    }
  }	
}
