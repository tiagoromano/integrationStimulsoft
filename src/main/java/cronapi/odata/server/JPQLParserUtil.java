package cronapi.odata.server;

import org.eclipse.persistence.jpa.jpql.parser.*;

public class JPQLParserUtil {

  public static IdentificationVariableDeclaration getIdentificationVariableDeclaration(JPQLExpression jpqlExpression ) {
    SelectStatement selectStatement = ((SelectStatement) jpqlExpression.getQueryStatement());


    Expression declaration = ((FromClause) selectStatement.getFromClause()).getDeclaration();
    IdentificationVariableDeclaration identificationVariableDeclaration = null;
    if (declaration instanceof IdentificationVariableDeclaration) {
      identificationVariableDeclaration = ((IdentificationVariableDeclaration) ((FromClause) selectStatement.getFromClause()).getDeclaration());
    }

    if (declaration instanceof CollectionExpression) {
      CollectionExpression collectionExpression = ((CollectionExpression) ((FromClause) selectStatement.getFromClause()).getDeclaration());
      identificationVariableDeclaration = (IdentificationVariableDeclaration) collectionExpression.getChild(0);
    }

    return identificationVariableDeclaration;
  }

  public static String getMainEntity(JPQLExpression jpqlExpression ) {

    String mainEntity = null;

    IdentificationVariableDeclaration identificationVariableDeclaration = getIdentificationVariableDeclaration(jpqlExpression);

    if (!identificationVariableDeclaration.hasJoins()) {
      RangeVariableDeclaration rangeVariableDeclaration = (RangeVariableDeclaration) identificationVariableDeclaration.getRangeVariableDeclaration();
      mainEntity = rangeVariableDeclaration.getRootObject().toString();
    }

    return mainEntity;
  }

  public static String getMainAlias(JPQLExpression jpqlExpression ) {

    String alias = null;

    IdentificationVariableDeclaration identificationVariableDeclaration = getIdentificationVariableDeclaration(jpqlExpression);

    if (!identificationVariableDeclaration.hasJoins()) {
      RangeVariableDeclaration rangeVariableDeclaration = (RangeVariableDeclaration) identificationVariableDeclaration.getRangeVariableDeclaration();
      alias = rangeVariableDeclaration.getIdentificationVariable().toString();
    }

    return alias;
  }
}
