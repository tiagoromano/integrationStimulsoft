/*
 * Copyright (c) 2017, Techne Engenharia e Sistemas S/C Ltda. All rights reserved.
 * TECHNE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cronapi.cloud;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.GetSharedLinksResult;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

public final class DropboxService implements CloudService {

	private static final Logger log = LoggerFactory.getLogger(DropboxService.class);

	private final DbxClientV2 client;

	private final List<FileObject> files;

	DropboxService(DbxClientV2 client, List<FileObject> files) {
		this.client = client;
		this.files = files;
	}

	@Override
	public void upload() {
		if (files == null || files.isEmpty()) {
			log.warn("File content not found to Dropbox upload");
			return;
		}
		files.forEach(fileObject -> {
			try {
				FileMetadata filesX = client.files().uploadBuilder(fileObject.getFileName())
						.withMode(WriteMode.OVERWRITE).uploadAndFinish(fileObject.getFileContent());

				GetSharedLinksResult sharedResult = client.sharing().getSharedLinks(filesX.getPathLower());
				if (sharedResult != null && sharedResult.getLinks() != null && sharedResult.getLinks().size() > 0)
					fileObject.setFileDirectUrl(convertToDirectLink(sharedResult.getLinks().get(0).getUrl()));
				else {
					SharedLinkMetadata shared = client.sharing().createSharedLinkWithSettings(filesX.getPathLower());
					fileObject.setFileDirectUrl(convertToDirectLink(shared.getUrl()));
				}

			} catch (DbxException | IOException e) {
				log.error(e.getMessage());
			}
		});
	}

	private String convertToDirectLink(String url) {
		return url.replace("https://www.dropbox.com/", "https://dl.dropboxusercontent.com/");
	}

	@Override
	public void popule(Object target) {
		this.files.forEach(fileObject -> {
			try {
				String fileName = fileObject.getFileName();
				String[] strings = fileName.split("/");
				if (strings.length > 0) {
					String fieldTarget = strings[2];
					Field declaredField = target.getClass().getDeclaredField(fieldTarget);
					declaredField.setAccessible(true);

					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					client.files().downloadBuilder(fileName).download(outputStream);

					declaredField.set(target, outputStream.toByteArray());
				}
			} catch (DbxException | IOException | NoSuchFieldException | IllegalAccessException e) {
				log.error(e.getMessage());
			}
		});
	}

	@Override
	public void delete() {
		this.files.forEach(fileObject -> {
			try {
				client.files().delete(fileObject.getFileName());
			} catch (DbxException e) {
				log.error(e.getMessage());
			}
		});
	}

}
