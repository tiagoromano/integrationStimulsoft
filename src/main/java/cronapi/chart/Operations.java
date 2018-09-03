package cronapi.chart;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
 * @since 2018-01-25
 *
 */
@CronapiMetaData(category = CategoryType.CHART, categoryTags = { "Gráfico", "Chart" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{createChartName}}", nameTags = { "chart", "series", "serie",
			"gráfico" }, description = "{{createChartDescription}}", arbitraryParams = true)
	public static final void createChart(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{createChartId}}", blockType = "ids_from_screen") Var chartId,
			@ParamMetaData(type = ObjectType.STRING, description = "{{createChartType}}", blockType = "util_dropdown", keys = {
					"line", "bar", "doughnut",
					"pie","polarArea" }, values = { "{{line}}", "{{bar}}", "{{doughnut}}", "{{pie}}","{{polarArea}}" }) Var type,
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{createChartLegends}}") Var chartLegends,
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{createChartOptions}}") Var options,
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{createChartSeries}}") Var... series

	) throws Exception {

		List<Var> list = new ArrayList<>();
		list.add(chartId);
		list.add(type);
		list.add(chartLegends);
		list.add(options);
		for (Var s : series) {
			list.add(s);
		}

		Var[] a = new Var[list.size()];
		list.toArray(a);
		cronapi.util.Operations.callClientFunction(Var.valueOf("cronapi.chart.createChart"), a);
	}

	@CronapiMetaData(type = "function", name = "{{createSerieName}}", nameTags = { "chart", "series", "serie",
			"gráfico" }, description = "{{createSerieDescription}}", returnType = ObjectType.OBJECT)
	public static final Var createChartSerie(
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{createSerieParamName}}") Var serieLegends,
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{createSerieParamData}}") Var serieData,
			@ParamMetaData(type = ObjectType.OBJECT, description = "{{createSerieParamOptions}}") Var serieOption

	) throws Exception {
		Var dataset = new Var(new LinkedHashMap<>());
		dataset.setField("label", serieLegends);
		dataset.setField("data", serieData);
		dataset.setField("options", serieOption);
		return dataset;
	}

}
