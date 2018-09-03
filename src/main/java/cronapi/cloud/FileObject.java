/*
 * Copyright (c) 2017, Techne Engenharia e Sistemas S/C Ltda. All rights reserved.
 * TECHNE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cronapi.cloud;

import java.io.InputStream;

public final class FileObject {

	private final String fileName;
	private final String fieldReference;
	private String fileDirectUrl;
	private final InputStream fileContent;

	FileObject(String fileName, String fieldReference, InputStream fileContent) {
		this.fileName = fileName;
		this.fileContent = fileContent;
		this.fieldReference = fieldReference;
	}
	
	String getFileName() {
		return fileName;
	}

	InputStream getFileContent() {
		return fileContent;
	}
	
	public void setFileDirectUrl(String url) {
	  this.fileDirectUrl = url; 
	}

  public String getFileDirectUrl() {
	  return this.fileDirectUrl; 
	}
	
	public String getFieldReference() {
	  return this.fieldReference; 
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		FileObject that = (FileObject) o;
		return fileName != null ? fileName.equals(that.fileName) : that.fileName == null;
	}

	@Override
	public int hashCode() {
		return fileName != null ? fileName.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "FileObject{" + "fileName='" + fileName + '\'' + '}';
	}
}
