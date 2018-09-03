package cronapi.xml;

import java.io.File;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import cronapi.CronapiMetaData;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

/**
 * Classe que representa ...
 * 
 * @author Rodrigo Reis
 * @version 1.0
 * @since 2017-03-29
 *
 */
@CronapiMetaData(category = CategoryType.XML, categoryTags = { "XML" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{newXMLEmptyName}}", nameTags = {
			"newXMLEmpty" }, description = "{{newXMLEmptyDescription}}", returnType = ObjectType.OBJECT)
	public static final Var newXMLEmpty() throws Exception {
		return new Var(new Document());
	}

	@CronapiMetaData(type = "function", name = "{{newXMLEmptyName}}", nameTags = {
			"newXMLEmpty" }, description = "{{newXMLEmptyDescription}}", params = {
					"{{newXMLEmptyParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var newXMLEmpty(Var rootElement) throws Exception {
		return new Var(new Document((Element) rootElement.getObject()));
	}

	@CronapiMetaData(type = "function", name = "{{XMLOpenFromFileName}}", nameTags = {
			"XMLOpenFromFile" }, description = "{{XMLOpenFromFileDescription}}", params = {
					"{{XMLOpenFromFileParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var XMLOpenFromFile(Var absPath) throws Exception {
		File fileCasted = new File(absPath.getObjectAsString());
		SAXBuilder builder = new SAXBuilder();
		return new Var(builder.build(fileCasted));
	}

	@CronapiMetaData(type = "function", name = "{{XMLOpenName}}", nameTags = {
			"XMLOpen" }, description = "{{XMLOpenDescription}}", params = {
					"{{XMLOpenParam0}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.OBJECT)
	public static final Var XMLOpen(Var name) throws Exception {
		if (name.getObjectAsString() != "" && !name.equals(Var.VAR_NULL)) {
			SAXBuilder builder = new SAXBuilder();
			return new Var(builder.build(name.getObjectAsString()));
		}
		return Var.VAR_NULL;
	}

	@CronapiMetaData(type = "function", name = "{{XMLcreateElementName}}", nameTags = {
			"XMLcreateElement" }, description = "{{XMLcreateElementDescription}}", params = {
					"{{XMLcreateElementParam0}}", "{{XMLcreateElementParam1}}" }, paramsType = { ObjectType.STRING,
							ObjectType.STRING }, returnType = ObjectType.OBJECT)
	public final static Var XMLcreateElement(Var name, Var value) {
		if (!name.equals(Var.VAR_NULL)) {
			Element newElement = new Element(name.getObjectAsString());
			if (!value.equals(Var.VAR_NULL) && !value.getObjectAsString().trim().isEmpty())
				newElement.setText(value.getObjectAsString());
			return new Var(newElement);
		} else
			return Var.VAR_NULL;
	}

	@CronapiMetaData(type = "function", name = "{{XMLaddElementName}}", nameTags = {
			"XMLaddElement" }, description = "{{XMLaddElementDescription}}", params = { "{{XMLaddElementParam0}}",
					"{{XMLaddElementParam1}}" }, paramsType = { ObjectType.STRING, ObjectType.STRING })
	public final static void XMLaddElement(Var parent, Var element) {

		if (parent.getObject() instanceof Element && element.getObject() instanceof Element) {
			Element parentCasted = (Element) parent.getObject();
			Element elementCasted = (Element) element.getObject();

			parentCasted.getChildren().add(elementCasted);

		} else if (parent.getObject() instanceof Document && element.getObject() instanceof Element) {
			Document parentCasted = (Document) parent.getObject();

			if (!parentCasted.hasRootElement()) {
				Element elementCasted = (Element) element.getObject();
				parentCasted.setRootElement(elementCasted);
			}
		}
	}

	@CronapiMetaData(type = "function", name = "{{XMLHasRootElementName}}", nameTags = {
			"XMLHasRootElement" }, description = "{{XMLHasRootElementDescription}}", params = {
					"{{XMLHasRootElementParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.BOOLEAN)
	public final static Var hasRootElement(Var document) {

		if (document.getObject() instanceof Document) {
			Document documentCasted = (Document) document.getObject();
			if (documentCasted.hasRootElement())
				return new Var(true);
			else
				return new Var(false);
		} else if (document.getObject() instanceof Element) {
			Element elementCasted = (Element) document.getObject();
			if (elementCasted.getDocument().hasRootElement())
				return new Var(true);
			else
				return new Var(false);
		}
		return new Var(false);
	}

	@CronapiMetaData(type = "function", name = "{{XMLGetRootElementName}}", nameTags = {
			"XMLGetRootElement" }, description = "{{XMLGetRootElementDescription}}", params = {
					"{{XMLGetRootElementParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public final static Var getRootElement(Var document) {
		if (document.getObject() instanceof Document) {
			if (hasRootElement(document).getObjectAsBoolean()) {
				Document documentCasted = (Document) document.getObject();
				return new Var(documentCasted.getRootElement());
			} else {
				return Var.VAR_NULL;
			}
		} else if (document.getObject() instanceof Element) {
			Element elementCasted = (Element) document.getObject();
			if (hasRootElement(new Var(elementCasted.getDocument())).getObjectAsBoolean()) {
				return new Var(elementCasted.getDocument().getRootElement());
			}
		}
		return Var.VAR_NULL;

	}

	@CronapiMetaData(type = "function", name = "{{XMLDocumentToStringName}}", nameTags = {
			"XMLDocumentToString" }, description = "{{XMLDocumentToStringDescription}}", params = {
					"{{XMLDocumentToStringParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.STRING)
	public final static Var XMLDocumentToString(Var document) {
		return Var.valueOf(document.getObjectAsString());
	}

	@CronapiMetaData(type = "function", name = "{{XMLElementToStringName}}", nameTags = {
			"XMLElementToString" }, description = "{{XMLElementToStringDescription}}", params = {
					"{{XMLElementToStringParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.STRING)
	public final static Var XMLElementToString(Var document) {
		if (!document.equals(Var.VAR_NULL)) {
			if (document.getObject() instanceof Element) {
				Element documentCasted = (Element) document.getObject();
				XMLOutputter xmlOut = new XMLOutputter();
				return new Var(xmlOut.outputString(documentCasted));
			}
		}
		return Var.VAR_NULL;
	}

	@CronapiMetaData(type = "function", name = "{{XMLGetChildElementName}}", nameTags = {
			"XMLGetChildElement" }, description = "{{XMLGetChildElementDescription}}", params = {
					"{{XMLGetChildElementParam0}}", "{{XMLGetChildElementParam1}}" }, paramsType = { ObjectType.OBJECT,
							ObjectType.STRING }, returnType = ObjectType.LIST)
	public static final Var XMLGetChildElement(Var element, Var child) throws Exception {
		if (!element.equals(Var.VAR_NULL)) {
			if (element.getObject() instanceof Element) {
				Element elementCasted = (Element) element.getObject();
				if (child.equals(Var.VAR_NULL))
					return new Var(elementCasted.getChildren());
				if (child.getObject() instanceof Element) {
					Element childCasted = (Element) child.getObject();
					return new Var(elementCasted.getChildren(childCasted.getName()));
				} else if (child.getObject() instanceof String) {
					return new Var(elementCasted.getChildren(child.getObjectAsString()));
				}
			} else if (element.getObject() instanceof Document) {
				if (child.getObject() instanceof Element) {

				} else if (child.getObject() instanceof String) {
					Document documentCasted = (Document) element.getObject();
					if (documentCasted.getRootElement().getName() == child.getObjectAsString()) {
						return new Var(documentCasted.getRootElement());
					} else {
						return new Var(documentCasted.getRootElement().getChildren(child.getObjectAsString()));
					}
				}
			}
			return Var.VAR_NULL;
		}
		return Var.VAR_NULL;
	}

	// Alterar o valor de um Atributo XML
	@CronapiMetaData(type = "function", name = "{{XMLSetElementAttributeValueName}}", nameTags = {
			"XMLSetElementAttributeValue" }, description = "{{XMLSetElementValueDescription}}", params = {
					"{{XMLSetElementAttributeValueParam0}}", "{{XMLSetElementAttributeValueParam1}}",
					"{{XMLSetElementAttributeValueParam2}}"

	}, paramsType = { ObjectType.OBJECT, ObjectType.STRING, ObjectType.STRING })
	public static final void XMLSetElementAttributeValue(Var element, Var attributeName, Var value) throws Exception {
		if (!element.equals(Var.VAR_NULL) && !attributeName.equals(Var.VAR_NULL) && !value.equals(Var.VAR_NULL)) {
			if (element.getObject() instanceof Element) {
				Element elementCasted = (Element) element.getObject();
				elementCasted.setAttribute(attributeName.getObjectAsString(), value.getObjectAsString());
			} else
				throw new Exception();
		} else
			throw new Exception();
	}

	@CronapiMetaData(type = "function", name = "{{XMLGetAttributeName}}", nameTags = {
			"XMLGetAttribute" }, description = "{{XMLGetAttributeDescription}}", params = { "{{XMLGetAttributeParam0}}",
					"{{XMLGetAttributeParam1}}" }, paramsType = { ObjectType.OBJECT,
							ObjectType.OBJECT }, returnType = ObjectType.STRING)
	public static final Var XMLGetAttributeValue(Var element, Var attribute) throws Exception {
		if (!element.equals(Var.VAR_NULL) && (element.getObject() instanceof Element) && !attribute.equals(Var.VAR_NULL)
				&& attribute.getObjectAsString() != "") {
			Element elementCasted = (Element) element.getObject();
			return new Var(elementCasted.getAttributeValue(attribute.getObjectAsString()));
		}
		return Var.VAR_NULL;
	}

	@CronapiMetaData(type = "function", name = "{{XMLGetParentElementName}}", nameTags = {
			"XMLGetParentElement" }, description = "{{XMLGetParentElementDescription}}", params = {
					"{{XMLGetParentElementParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var XMLGetParentElement(Var element) throws Exception {
		if (!element.equals(Var.VAR_NULL)) {
			if (element.getObject() instanceof Element) {
				Element elementCasted = (Element) element.getObject();
				return new Var(elementCasted.getParentElement());
			} else if (element.getType() == Var.Type.LIST) {
				if (element.getObjectAsList().getFirst().getObject() instanceof Element) {
					return new Var(((Element) element.getObjectAsList().getFirst().getObject()).getParent());
				}
			}
		}
		return Var.VAR_NULL;
	}

	@CronapiMetaData(type = "function", name = "{{XMLSetElementValueName}}", nameTags = {
			"XMLSetElementValue" }, description = "{{XMLSetElementValueDescription}}", params = {
					"{{XMLSetElementValueParam0}}",
					"{{XMLSetElementValueParam1}}" }, paramsType = { ObjectType.OBJECT, ObjectType.STRING })
	public static final void XMLSetElementValue(Var element, Var value) throws Exception {
		if (!element.equals(Var.VAR_NULL) && !value.equals(Var.VAR_NULL)) {
			if (element.getObject() instanceof Element) {
				Element elementCasted = (Element) element.getObject();
				if (value.getObject() instanceof Element) {
					Element valueCasted = (Element) value.getObject();
					elementCasted.setText(valueCasted.getText());
				} else if (value.getType() == Var.Type.STRING) {
					elementCasted.setText(value.getObjectAsString());
				}
			} else
				throw new Exception();
		}
	}

	@CronapiMetaData(type = "function", name = "{{XMLGetElementValueName}}", nameTags = {
			"XMLGetElementValue" }, description = "{{XMLGetElementValueDescription}}", params = {
					"{{XMLGetElementValueParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.STRING)
	public static final Var XMLGetElementValue(Var element) throws Exception {

		if (element.getObject() instanceof Element) {
			Element elementCasted = (Element) element.getObject();
			return new Var(elementCasted.getText());
		} else if (element.getType() == Var.Type.LIST) {
			String result = "";
			for (Var v : element.getObjectAsList()) {
				if (v.getObject() instanceof Element)
					result = result + (((Element) v.getObject()).getText());
				else {
					result = result + v.getObjectAsString();
				}
			}
			return new Var(result);
		}
		return new Var("");
	}

	@CronapiMetaData(type = "function", name = "{{XMLRemoveElementName}}", nameTags = {
			"XMLRemoveElement" }, description = "{{XMLRemoveElementDescription}}", params = {
					"{{XMLRemoveElementParam0}}", "{{XMLRemoveElementParam1}}" }, paramsType = { ObjectType.OBJECT,
							ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var XMLRemoveElement(Var parent, Var element) throws Exception {

		if (!parent.equals(Var.VAR_NULL)) {
			if (element.equals(Var.VAR_NULL)) {
				Element parentCasted = (Element) parent.getObject();
				parentCasted.removeContent();
				return Var.VAR_TRUE;
			}
			Element parentCasted = (Element) parent.getObject();
			if (element.getObject() instanceof Element) {
				parentCasted.removeChildren(((Element) element.getObject()).getName());
				return Var.VAR_TRUE;
			}
			parentCasted.removeChildren(element.getObjectAsString());
			return Var.VAR_TRUE;
		} else
			return Var.VAR_FALSE;
	}

	@CronapiMetaData(type = "function", name = "{{XMLGetElementTagNameName}}", nameTags = {
			"XMLGetElementTagName" }, description = "{{XMLGetElementTagNameDescription}}", params = {
					"{{XMLGetElementTagNameParam0}}" }, paramsType = {
							ObjectType.OBJECT }, returnType = ObjectType.STRING)
	public static final Var XMLGetElementTagName(Var element) throws Exception {
		if (!element.equals(Var.VAR_NULL)) {
			if (element.getObject() instanceof Element) {
				Element elementCasted = (Element) element.getObject();
				return new Var(elementCasted.getName());
			}
		}
		return Var.VAR_NULL;
	}

	@CronapiMetaData(type = "function", name = "{{XMLChangeNodeNameName}}", nameTags = {
			"XMLChangeNodeName" }, description = "{{XMLChangeNodeNameDescription}}", params = {
					"{{XMLChangeNodeNameParam0}}", "{{XMLChangeNodeNameParam1}}" }, paramsType = { ObjectType.OBJECT,
							ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var XMLChangeNodeName(Var node, Var name) throws Exception {

		if (!node.equals(Var.VAR_NULL) && !name.equals(Var.VAR_NULL)) {
			if (node.getObject() instanceof Element) {
				Element elementCasted = (Element) node.getObject();
				elementCasted.setName(name.getObjectAsString());
				return Var.VAR_TRUE;
			} else
				return Var.VAR_FALSE;
		}
		return Var.VAR_FALSE;
	}

}
