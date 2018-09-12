package cronapi.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cronapi.rest.DownloadREST;
import cronapp.reports.PrintDesign;
import cronapp.reports.ReportExport;
import cronapp.reports.ReportManager;
import cronapp.reports.commons.Functions;
import cronapp.reports.commons.Parameter;
import cronapp.reports.commons.ParameterType;
import cronapp.reports.commons.ReportFront;
import cronapp.reports.j4c.dataset.J4CDataset;
import cronapp.reports.j4c.dataset.J4CEntity;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@Service
public class ReportService {

	private static final Logger log = LoggerFactory.getLogger(ReportService.class);

	private final ClassLoader loader;

	public ReportService() {
		this.loader = Thread.currentThread().getContextClassLoader();
	}

	private InputStream getInputStream(String reportName) {
		InputStream inputStream = loader.getResourceAsStream(reportName);
		if (inputStream == null)
			throw new RuntimeException("File [" + reportName + "] not found.");
		return inputStream;
	}
	
	public ReportFront getReport(String reportName) {
		ReportFront reportResult = new ReportFront(reportName);
		try {
			if (reportName.contains("jrxml")) {
				log.info("Report in design mode, build the parameters...");
				InputStream inputStream = this.getInputStream(reportName);
				JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
				Stream.of(jasperDesign.getParameters()).filter(jrParameter -> !jrParameter.isSystemDefined())
						.filter(jrParameter -> !jrParameter.getName().contains("image_"))
						.filter(jrParameter -> !jrParameter.getName().contains("sub_")).forEach(jrParameter -> {
							Parameter parameter = new Parameter();
							parameter.setName(jrParameter.getName());
							parameter.setType(ParameterType.toType(jrParameter.getValueClass()));
							parameter.setDescription(jrParameter.getDescription());
							JRExpression expression = jrParameter.getDefaultValueExpression();
							if (expression != null)
								parameter.setValue(expression.getText());
							reportResult.addParameter(parameter);
						});
			}
		// 	else if (reportName.endsWith(".report")) {
		// 	  String content = getContentReport(reportName);
		// 	  JsonParser parser = new JsonParser();
  //       JsonObject json = parser.parse(content).getAsJsonObject();
		// 	}
		} catch (JRException e) {
			log.error("Problems to make JasperDesign object.");
			throw new RuntimeException(e);
		}
		return reportResult;
	}
	
	public String getContentReport(String reportName) {
	  try (InputStream inputStream = this.getInputStream(reportName)) {
  	  try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
          return buffer.lines().collect(Collectors.joining("\n"));
      }
	  }
	  catch (Exception e) {
	    throw new RuntimeException(e);    
	  }
	}

	public String getPDFAsFile(ReportFront reportFront) {
		ReportExport result = this.getReportExport(reportFront);
		result.exportReportToPdfFile();
		if (result == null)
			return "";
		return DownloadREST.getDownloadUrl(new File(result.getTargetFile()));
	}

	public byte[] getPDF(ReportFront reportFront) {
		ReportExport result = this.getReportExport(reportFront);
		if (result == null)
			return new byte[0];
		return result.toPDF();
	}

	public byte[] getXLS(ReportFront reportFront) {
		ReportExport result = this.getReportExport(reportFront);
		if (result == null)
			return new byte[0];
		return result.toXLS();
	}

	public ReportExport getReportExport(ReportFront reportFront) {
		return this.getReportExport(reportFront, null);
	}

	public ReportExport getReportExport(ReportFront reportFront, File file) {
		ReportExport result = null;
		File pdf = null;
		try {
			try {
				if (file == null) {
					pdf = DownloadREST.getTempFile(UUID.randomUUID().toString() + ".pdf");
					pdf.createNewFile();
				} else {
				  pdf = file;
				}
			} catch (IOException e) {
				log.error("Problems to make the temporary report file.");
				throw new RuntimeException(e);
			}

			InputStream inputStream = this.getInputStream(reportFront.getReportName());
			ReportManager reportManager = ReportManager.newPrint(inputStream, pdf.getAbsolutePath());

			String reportName = reportFront.getReportName();
			if (reportName.contains("jrxml")) {
				PrintDesign printDesign = reportManager.byDesign(reportFront.getParameters()).updateParameters()
						.updateImages().updateSubreports();

				J4CDataset dataset = printDesign.getCollectionDataset();
				if (dataset == null) {
					String datasource = printDesign.getDatasource();
					try (Connection connection = this.getConnection(datasource)) {
						result = printDesign.print(connection);
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				} else {
					J4CEntity entity = dataset.getEntity();
					String jpql = entity.getJpql();
					if (Functions.isExists(jpql)) {
						String persistenceUnit = dataset.getPersistenceUnitName();
						EntityManager entityManager = this.getEntityManager(persistenceUnit);

						Map<String, Object> printParameters = printDesign.getPrintParameters();

						Query queryObject = entityManager.createQuery(jpql);

						Set<javax.persistence.Parameter<?>> objectParameters = queryObject.getParameters();
						Set<String> parameterNames = objectParameters.stream().map(javax.persistence.Parameter::getName)
								.collect(Collectors.toSet());

						Set<Map.Entry<String, Object>> entrySet = printParameters.entrySet();
						for (Map.Entry<String, Object> item : entrySet) {
							String name = item.getKey();
							if (parameterNames.contains(name)) {
								Object value = item.getValue();
								queryObject.setParameter(name, value);
							}
						}

						List resultList = Collections.emptyList();
						try {
							resultList = queryObject.getResultList();
						} catch (IllegalArgumentException e) {
							log.error(e.getMessage());
						}
						result = printDesign.print(resultList);
					}
				}
			}
		} finally {
			// A remoção do arquivo vai ser gerenciado pela classe DownloadREST
			//      if(pdf != null && pdf.exists())
			//        pdf.delete();
		}
		return result;
	}

	private EntityManager getEntityManager(String persistenceUnit) {
		HashMap<String, Object> properties = new HashMap<>();
		properties.put(PersistenceUnitProperties.JTA_DATASOURCE, persistenceUnit);
		EntityManagerFactory managerFactory = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		return managerFactory.createEntityManager();
	}

	private Connection getConnection(String datasource) {
		if (datasource != null && !datasource.isEmpty() && !"null".equals(datasource)) {
			javax.naming.Context context = null;
			DataSource dataSource = null;
			try {
				context = (javax.naming.Context) new InitialContext().lookup("java:/comp/env");
				dataSource = (DataSource) context.lookup(datasource);
			} catch (NamingException e) {
				try {
					if (context != null)
						dataSource = (DataSource) context.lookup(datasource.toLowerCase());
				} catch (NamingException e1) {
					throw new RuntimeException(
							new Exception("Connection context not found.\nError: " + e.getMessage()));
				}
			}
			try {
				if (dataSource != null)
					return dataSource.getConnection();
			} catch (SQLException e) {
				throw new RuntimeException(
						new Exception("Trouble getting a connection from the context.\nError: " + e.getMessage()));
			}
		}
		return null;
	}

}
