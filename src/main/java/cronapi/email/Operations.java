package cronapi.email;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import cronapi.CronapiMetaData;
import cronapi.ParamMetaData;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-04-10
 *
 */

@CronapiMetaData(category = CategoryType.EMAIL, categoryTags = { "Email" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{sendEmailName}}", nameTags = {
			"sendEmail" }, description = "{{sendEmailDescription}}", params = { "{{sendEmailParam0}}",
					"{{sendEmailParam1}}", "{{sendEmailParam2}}", "{{sendEmailParam3}}", "{{sendEmailParam4}}",
					"{{sendEmailParam5}}", "{{sendEmailParam6}}", "{{sendEmailParam7}}", "{{sendEmailParam8}}",
					"{{sendEmailParam9}}", "{{sendEmailParam10}}", "{{sendEmailParam11}}" }, paramsType = {
							ObjectType.STRING, ObjectType.STRING, ObjectType.LIST, ObjectType.LIST, ObjectType.STRING,
							ObjectType.STRING, ObjectType.STRING, ObjectType.LIST, ObjectType.STRING, ObjectType.STRING,
							ObjectType.STRING, ObjectType.STRING })
	public static final void sendEmail(
			@ParamMetaData(defaultValue = "email@techne.com.br", type = ObjectType.STRING, description = "{{sendEmailParam0}}") Var from,
			@ParamMetaData(type = ObjectType.STRING, description = "{{sendEmailParam1}}") Var to,
			@ParamMetaData(type = ObjectType.STRING, description = "{{sendEmailParam2}}") Var Cc,
			@ParamMetaData(type = ObjectType.STRING, description = "{{sendEmailParam3}}") Var Bcc,
			@ParamMetaData(type = ObjectType.STRING, description = "{{sendEmailParam4}}") Var subject,
			@ParamMetaData(type = ObjectType.STRING, description = "{{sendEmailParam5}}") Var msg,
			@ParamMetaData(type = ObjectType.STRING, description = "{{sendEmailParam6}}") Var html,
			@ParamMetaData(type = ObjectType.STRING, description = "{{sendEmailParam7}}") Var attachments,
			@ParamMetaData(defaultValue = "smtp.office365.com", type = ObjectType.STRING, description = "{{sendEmailParam8}}") Var smtpHost,
			@ParamMetaData(defaultValue = "587", type = ObjectType.STRING, description = "{{sendEmailParam9}}") Var smtpPort,
			@ParamMetaData(defaultValue = "email@techne.com.br", type = ObjectType.STRING, description = "{{sendEmailParam10}}") Var login,
			@ParamMetaData(type = ObjectType.STRING, description = "{{sendEmailParam11}}") Var password,
			@ParamMetaData(type = ObjectType.BOOLEAN, description = "{{sendEmailParam12}}", defaultValue = "false") Var ssl)
			throws Exception {
		try {
			HtmlEmail email = new HtmlEmail();
			email.setCharset(cronapi.CronapiConfigurator.ENCODING);
			if (ssl.getObjectAsBoolean() == true) {
				email.setSSLOnConnect(true);
				email.setSslSmtpPort(smtpPort.getObjectAsString());
			} else {
				email.setTLS(true);
				email.setSSLOnConnect(false);
				email.setSmtpPort(smtpPort.getObjectAsInt());
			}

			email.setHostName(smtpHost.getObjectAsString());
			email.setAuthenticator(new DefaultAuthenticator(login.getObjectAsString(), password.getObjectAsString()));
			email.setFrom(from.getObjectAsString());
			email.setDebug(false);
			email.setSubject(subject.getObjectAsString());
			email.setMsg(msg.getObjectAsString());

			if (Cc.getType() == Var.Type.LIST) {
				for (Var v : Cc.getObjectAsList()) {
					email.addCc(v.getObjectAsString());
				}
			} else if (!Cc.equals(Var.VAR_NULL)) {
				email.addCc(Cc.getObjectAsString());
			}

			if (Bcc.getType() == Var.Type.LIST) {
				for (Var v : Bcc.getObjectAsList()) {
					email.addBcc(v.getObjectAsString());
				}
			} else if (!Bcc.equals(Var.VAR_NULL)) {
				email.addBcc(Bcc.getObjectAsString());
			}

			if (!html.equals(Var.VAR_NULL)) {
				email.setHtmlMsg(html.getObjectAsString());
			}

			if (!attachments.equals(Var.VAR_NULL)) {
				if (attachments.getType() == Var.Type.LIST) {
					for (Var v : attachments.getObjectAsList()) {
						EmailAttachment anexo = new EmailAttachment();
						anexo.setPath(v.getObjectAsString());
						anexo.setDisposition(EmailAttachment.ATTACHMENT);
						anexo.setName(v.getObjectAsString());
						email.attach(anexo);
					}
				} else if (attachments.getType() == Var.Type.STRING) {
					EmailAttachment anexo = new EmailAttachment();
					anexo.setPath(attachments.getObjectAsString());
					anexo.setDisposition(EmailAttachment.ATTACHMENT);
					anexo.setName(attachments.getObjectAsString());
					email.attach(anexo);
				}
			}

			if (to.getType() == Var.Type.LIST) {
				for (Var v : to.getObjectAsList()) {
					email.addTo(v.getObjectAsString());
				}
			} else if (to.getType() == Var.Type.STRING) {
				email.addTo(to.getObjectAsString());
			}
			email.send();
		} catch (EmailException e) {
			throw new RuntimeException(e);
		}
	}
}
