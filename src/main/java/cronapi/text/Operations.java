package cronapi.text;

import cronapi.Var;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-04-03
 *
 */
public class Operations {

	public static final Var newText(Var text) throws Exception {
		return new Var(text.getObjectAsString());
	}

	public static final Var newText(Var... text) throws Exception {
		Var result = new Var("");
		for (Var t : text) {
			result.append(t.getObjectAsString());
		}
		return result;
	}

	public static final Var concat(Var item, Var... itens) throws Exception {
		for (Var t : itens) {
			item.append(t.getObjectAsString());
		}
		return item;
	}

	public static final Var titleCase(Var text) {
		StringBuilder titleCase = new StringBuilder();
		boolean nextTitleCase = true;
		String input = text.getObjectAsString();

		for (char c : input.toCharArray()) {
			if (Character.isSpaceChar(c)) {
				nextTitleCase = true;
			} else if (nextTitleCase) {
				c = Character.toTitleCase(c);
				nextTitleCase = false;
			}
			titleCase.append(c);
		}
		return Var.valueOf(titleCase.toString());
	}

	public static final Var getLetter(Var text, Var index) throws Exception {
		if (text.getType().equals(Var.Type.NULL))
			return Var.VAR_NULL;
		return (text.getObjectAsString().length() >= index.getObjectAsInt())
				? (index.getObjectAsInt() < 1 ? new Var(text.getObjectAsString().charAt(0))
						: new Var(text.getObjectAsString().charAt(index.getObjectAsInt() - 1)))
				: new Var(text.getObjectAsString().charAt(text.getObjectAsString().length() - 1));
	}

	public static final Var getLetterFromEnd(Var text, Var index) throws Exception {
		if (text.getType().equals(Var.Type.NULL))
			return Var.VAR_NULL;
		if (index.getObjectAsInt() <= 0)
			return new Var(text.getObjectAsString().charAt(text.getObjectAsString().length() - 1));
		return (text.getObjectAsString().length() - index.getObjectAsInt() > 0)
				? new Var(text.getObjectAsString().charAt(text.getObjectAsString().length() - index.getObjectAsInt()))
				: new Var(text.getObjectAsString().charAt(0));
	}

	public static final Var getFirstLetter(Var text) throws Exception {

		return getLetter(text, Var.valueOf(1));
	}

	public static final Var getLastLetter(Var text) throws Exception {

		return getLetter(text, Var.valueOf(text.getObjectAsString().length()));
	}

	public static final Var getRandomLetter(Var text) throws Exception {
		int i = new java.util.Random().nextInt(text.getObjectAsString().length());
		if (i == 0)
			i++;
		return getLetter(text, Var.valueOf(i));
	}

	public static final Var getLettersFromStartToFromStart(Var text, Var index1, Var index2) throws Exception {
		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);
		if (index2.getObjectAsInt() < 1)
			index2 = new Var(1);
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			index1 = new Var(text.getObjectAsString().length());
		if (index2.getObjectAsInt() > text.getObjectAsString().length())
			index2 = new Var(text.getObjectAsString().length());

		if (index1.getObjectAsInt() <= index2.getObjectAsInt()) {
			return new Var(text.getObjectAsString().substring(index1.getObjectAsInt() - 1, index2.getObjectAsInt()));
		} else {
			return new Var(text.getObjectAsString().substring(index2.getObjectAsInt() - 1, index1.getObjectAsInt()));
		}
	}

	public static final Var getLettersFromStartToFromEnd(Var text, Var index1, Var index2) throws Exception {
		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);
		if (index2.getObjectAsInt() <= 1)
			index2 = new Var(1);
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			index1 = new Var(text.getObjectAsString().length());
		if (index2.getObjectAsInt() > text.getObjectAsString().length())
			index2 = new Var(text.getObjectAsString().length());

		if (index1.getObjectAsInt() <= (text.getObjectAsString().length() - index2.getObjectAsInt())) {
			return new Var(text.getObjectAsString().substring(index1.getObjectAsInt() - 1,
					text.getObjectAsString().length() - (index2.getObjectAsInt() - 1)));
		}
		if (index1.getObjectAsInt() == 1 && index2.getObjectAsInt() == text.getObjectAsString().length())
			return new Var(text.getObjectAsString().substring(0, 1));

		return Var.VAR_NULL;
	}

	public static final Var getLettersFromStartToLast(Var text, Var index1) throws Exception {
		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			index1 = new Var(text.getObjectAsString().length() + 1);
		return new Var(text.getObjectAsString().substring(index1.getObjectAsInt() - 1));
	}

	public static final Var getLettersFromEndToFromStart(Var text, Var index1, Var index2) throws Exception {
		if (index2.getObjectAsInt() < 1)
			index2 = new Var(1);
		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);

		if (index2.getObjectAsInt() >= text.getObjectAsString().length())
			index2 = new Var(text.getObjectAsString().length());
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			index1 = new Var(text.getObjectAsString().length());

		if (index2.getObjectAsInt() == 1 && (text.length() - index1.getObjectAsInt()) == 0) {
			return new Var(text.getObjectAsString().substring(0, 1));
		} else if ((text.length() - index1.getObjectAsInt()) < index2.getObjectAsInt()) {
			return new Var(text.getObjectAsString().substring((text.length() - (index1.getObjectAsInt())),
					index2.getObjectAsInt()));
		}
		return Var.VAR_NULL;
	}

	public static final Var getLettersFromEndToFromEnd(Var text, Var index1, Var index2) throws Exception {
		if (index2.getObjectAsInt() < 1)
			index2 = new Var(1);
		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);

		if (index2.getObjectAsInt() > text.getObjectAsString().length())
			index2 = new Var(text.getObjectAsString().length());
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			index1 = new Var(text.getObjectAsString().length());

		if (index1.getObjectAsInt() > index2.getObjectAsInt()) {
			return new Var(text.getObjectAsString().substring((text.length() - index1.getObjectAsInt()),
					(text.length() - index2.getObjectAsInt() + 1)));
		} else if (index1.getObjectAsInt() == index2.getObjectAsInt()) {
			return new Var(text.getObjectAsString().substring((text.length() - (index1.getObjectAsInt())),
					(text.length() - (index1.getObjectAsInt() - 1))));
		}
		return Var.VAR_NULL;

	}

	public static final Var getLettersFromEndToFromLast(Var text, Var index1) throws Exception {
		if (text.getType().equals(Var.Type.NULL) || text.getObjectAsString().length() < 1) {
			return Var.VAR_NULL;
		}
		if (index1.getObjectAsInt() < 1) {
			return new Var(text.getObjectAsString().charAt(text.getObjectAsString().length() - 1));
		} else if (index1.getObjectAsInt() > text.getObjectAsString().length()) {
			index1 = new Var(text.getObjectAsString().length() + 1);
			return text;
		} else {
			return new Var(text.getObjectAsString().substring((text.length() - (index1.getObjectAsInt()))));
		}
	}

	public static final Var getLettersFromFirstToFromStart(Var text, Var index1) throws Exception {

		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			index1 = new Var(text.getObjectAsString().length());
		return new Var(text.getObjectAsString().substring(0, index1.getObjectAsInt()));
	}

	public static final Var getLettersFromFirstToFromEnd(Var text, Var index1) throws Exception {

		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			return new Var(text.getObjectAsString().substring(0, 1));
		return new Var(text.getObjectAsString().substring(0, text.length() - (index1.getObjectAsInt() - 1)));
	}

	public static final Var getLettersFromFirstToEnd(Var text) throws Exception {
		return new Var(text.getObjectAsString());

	}
}
