package cronapi.pushnotification;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.http.HttpEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import cronapi.CronapiMetaData;
import cronapi.ParamMetaData;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import org.springframework.web.client.RestTemplate;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2018-01-24
 *
 */
@CronapiMetaData(category = CategoryType.UTIL, categoryTags = { "UTIL", "Util" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{firebaseSendNotification}}", nameTags = {
			"SendNotification" }, description = "{{firebaseSendNotificationDescription}}" )
	public static final void sendNotification(
			@ParamMetaData(type = ObjectType.STRING, description = "{{FirebaseServerKey}}") Var serverKey,
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{FirebaseTo}}") Var paramTo,
			@ParamMetaData(type = ObjectType.STRING, description = "{{FirebaseTitle}}") Var paramTitle,
			@ParamMetaData(type = ObjectType.STRING, description = "{{FirebaseBody}}") Var paramBody,
			@ParamMetaData(type = ObjectType.JSON, description = "{{FirebaseData}}") Var paramData)
			throws Exception {

		JsonObject body = new JsonObject();
		body.addProperty("to", paramTo.getObjectAsString());
		body.addProperty("priority", "high");
		
		JsonObject notification = new JsonObject();
		notification.addProperty("title", paramTitle.getObjectAsString());
		notification.addProperty("body", paramBody.getObjectAsString());

		body.add("notification", notification);
		body.add("data", (JsonObject) paramData.getObject()); 
		
		HttpEntity<String> request = new HttpEntity<>(body.toString());
		FirebasePushNotificationService firebaseService = new FirebasePushNotificationService(serverKey.getObjectAsString());
		CompletableFuture<String> pushNotification = firebaseService.send(request);
		CompletableFuture.allOf(pushNotification).join();
		
		try {
			String firebaseResponse = pushNotification.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	
	
	@CronapiMetaData(type = "function", name = "{{firebaseRegister}}", nameTags = {
			"Firebase","Topic","Register","Registrar" } , description = "{{firebaseRegisterDescription}}")
	public static void firebaseRegister(
			@ParamMetaData(type = ObjectType.STRING, description = "{{firebaseServerKey}}") Var serverKey,
			@ParamMetaData(type = ObjectType.STRING, description = "{{firebaseTopicName}}") Var topicName,
			@ParamMetaData(type = ObjectType.STRING, description = "{{firebaseToken}}") Var token)
			throws Exception {
		String baseUrl = "https://iid.googleapis.com/iid/v1/";
		String topicUrl = "/rel/topics/";
		RestTemplate restTemplate = new RestTemplate();
		ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new HeaderRequestInterceptor("Authorization", "key=" + serverKey.toString()));
		interceptors.add(new HeaderRequestInterceptor("Content-Type", "application/json"));
		restTemplate.setInterceptors(interceptors);
		HttpEntity<String> request = new HttpEntity<>("");

		if (token.getType().equals(Var.Type.LIST)) {
			for (Var tokenItem : token.getObjectAsList()) {
				String url = baseUrl + tokenItem.getObjectAsString() + topicUrl + topicName.getObjectAsString();
				restTemplate.postForObject(url, request, String.class);
			}
		} else {

			String url = baseUrl + token.getObjectAsString() + topicUrl + topicName.getObjectAsString();
			restTemplate.postForObject(url, request, String.class);

		}

	}

}
