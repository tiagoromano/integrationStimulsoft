package cronapi.conversion;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.Utils;
import cronapi.Var;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-03-29
 *
 */
@CronapiMetaData(category = CategoryType.CONVERSION, categoryTags = { "Conversão", "Convert" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{TextToBase64Name}}", nameTags = {
			"TextToBase64" }, description = "{{TextToBase64Description}}", params = {
					"{{TextToConvert}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	public static final Var StringToBase64(Var text) throws Exception {
		String base64 = Base64.getEncoder().encodeToString(text.getObjectAsString().getBytes());
		return new Var(base64);
	}

	@CronapiMetaData(type = "function", name = "{{base64ToText}}", nameTags = {
			"base64ToString" }, description = "{{functionConvertBase64ToText}}", params = {
					"{{contentInBase64}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var base64ToString(Var base64Var) throws Exception {
		if (base64Var.getObject() instanceof String) {
			return new Var(new String(Base64.getDecoder().decode(base64Var.getObjectAsString())));
		} else {
			byte[] decodedBytes = Base64.getDecoder().decode((byte[]) base64Var.getObject());
			return new Var(new String(decodedBytes));
		}
	}

	@CronapiMetaData(type = "function", name = "{{textToTextBinary}}", nameTags = {
			"asciiToBinary" }, description = "{{functionToConvertTextInTextBinary}}", params = {
					"{{contentInAscii}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	public static final Var asciiToBinary(Var asciiVar) throws Exception {
		byte[] bytes = asciiVar.getObjectAsString().getBytes();
		StringBuilder binary = new StringBuilder();
		for (byte b : bytes) {
			int val = b;
			for (int i = 0; i < 8; i++) {
				binary.append((val & 128) == 0 ? 0 : 1);
				val <<= 1;
			}
		}
		return new Var(binary.toString());
	}

	@CronapiMetaData(type = "function", name = "{{textBinaryToText}}", nameTags = {
			"binaryToAscii" }, description = "{{functionToConvertTextBinaryToText}}", params = {
					"{{contentInTextBinary}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	public static final Var binaryToAscii(Var binaryVar) throws Exception {

		BigInteger bg = new BigInteger(binaryVar.getObjectAsString(), 2);
		byte[] bt = bg.toByteArray();
		return new Var(new String(bt));
	}

	@CronapiMetaData(type = "function", name = "{{convertToBytes}}", nameTags = {
			"toBytes" }, description = "{{convertToBytesDescription}}", params = {
					"{{convertToBytesParam0}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.OBJECT)
	public static final Var toBytes(Var var) throws Exception {
		return new Var(var.getObjectAsString().getBytes());
	}

	@CronapiMetaData(type = "function", name = "{{convertToAscii}}", nameTags = { "chrToAscii",
			"convertToAscii" }, description = "{{functionToConvertToAscii}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.DOUBLE)
	public static final Var chrToAscii(Var value) throws Exception {
		if (!value.equals(Var.VAR_NULL))
			return new Var(Long.valueOf(value.getObjectAsString().charAt(0)));
		else
			return Var.VAR_ZERO;
	}

	@CronapiMetaData(type = "function", name = "{{convertHexadecimalToInt}}", nameTags = { "hexToInt",
			"hexadecimalToInteger" }, description = "{{functionToConvertHexadecimalToInt}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.LONG)
	public static final Var hexToInt(Var value) {
		return new Var(Long.parseLong(value.getObjectAsString(), 16));
	}

	@CronapiMetaData(type = "function", name = "{{convertArrayToList}}", nameTags = {
			"arrayToList" }, description = "{{functionToConvertArrayToList}}", params = {
					"{{content}}" }, paramsType = { ObjectType.LIST }, returnType = ObjectType.LIST)
	public static final Var arrayToList(Var arrayVar) throws Exception {
		List<?> t = Arrays.asList(arrayVar.getObject());
		return new Var(t);
	}

	@CronapiMetaData(type = "function", name = "{{convertBase64ToBinary}}", nameTags = {
			"base64ToBinary" }, description = "{{functionToConvertBase64ToBinary}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.OBJECT)
	public static final Var base64ToBinary(Var base64) throws Exception {
		if (!base64.equals(Var.VAR_NULL)) {
			Var text = base64ToString(base64);
			return asciiToBinary(text);
		}
		return Var.VAR_NULL;
	}

	@CronapiMetaData(type = "function", name = "{{convertStringToJs}}", nameTags = {
			"stringToJs" }, description = "{{functionToConvertStringToJs}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	public static final Var stringToJs(Var val) throws Exception {
		return new Var(Utils.stringToJs(val.getObjectAsString()));
	}

	@CronapiMetaData(type = "function", name = "{{convertStringToDate}}", nameTags = {
			"stringToDate" }, description = "{{functionToConvertStringToDate}}", params = { "{{content}}",
					"{{mask}}" }, paramsType = { ObjectType.STRING,
							ObjectType.STRING }, returnType = ObjectType.DATETIME)
	public static final Var stringToDate(Var val, Var mask) throws Exception {
		return new Var(Utils.toCalendar(val.getObjectAsString(), mask.getObjectAsString()));
	}

	@CronapiMetaData(type = "function", name = "{{convertDecToHex}}", nameTags = {
			"decToHex" }, description = "{{functionToConvertDecToHex}}", params = { "{{content}}"}, paramsType = { ObjectType.LONG }, returnType = ObjectType.STRING)
	public static final Var decToHex(Var value) throws Exception {
		String hex = Long.toHexString(value.getObjectAsInt());
		return new Var(hex);
	}

	@CronapiMetaData(type = "function", name = "{{convertToLong}}", nameTags = {
			"toLong" }, description = "{{functionToConvertToLong}}", params = {
					"{{content}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.LONG)
	public static final Var toLong(Var value) throws Exception {
		return new Var(value.getObjectAsLong());
	}

	@CronapiMetaData(type = "function", name = "{{convertToString}}", nameTags = {
			"toString" }, description = "{{functionToConvertToString}}", params = {
					"{{content}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.STRING)
	public static final Var toString(Var value) throws Exception {
		return new Var(value.getObjectAsString());
	}

	@CronapiMetaData(type = "function", name = "{{convertToDouble}}", nameTags = {
			"toDouble" }, description = "{{functionToConvertToDouble}}", params = {
					"{{content}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.DOUBLE)
	public static final Var toDouble(Var value) throws Exception {
		return new Var(value.getObjectAsDouble());
	}

	@CronapiMetaData(type = "function", name = "{{toLogic}}", nameTags = {
			"toBoolean" }, description = "{{functionConvertToLogic}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var toBoolean(Var var) throws Exception {
		return new Var(Utils.stringToBoolean(var.getObjectAsString()));
	}
}
