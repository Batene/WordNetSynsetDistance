package acoli.uni.frankfurt.restful.wordnet.topk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.*;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

import at.ac.wu.arqext.path.PathPropertyFunctionFactory;
import at.ac.wu.arqext.path.topk;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

@Path("/")
public class WordNetTopK {
	

	private static Map<String, String> loadProp() {

		Map<String, String> map = new HashMap<String, String>();

		String configFile = "C:\\Users\\Kathrin\\FINAL_JENA_TEST\\config.properties";

		InputStream input;
		try {
			input = new FileInputStream(configFile);
			Properties prop = new Properties();
			prop.load(input);
			String virtuosoURLSQLport = prop.getProperty("virtuosoURLSQLport");
			String uid = prop.getProperty("virtuosoUID");
			String pwd = prop.getProperty("virtuosoPWD");
			String wnOntoURI = prop.getProperty("wordnetontologyURI");
			String wnGraphURI = prop.getProperty("wordnetGraphURI");
			String wnSynsetPrefix = prop.getProperty("wordnetSynsetPrefix");

			map.put("virtuosoURLSQLport",  virtuosoURLSQLport);
			map.put("uid",  uid);
			map.put("pwd",  pwd);
			map.put("wnOntoURI",  wnOntoURI);
			map.put("wnGraphURI",  wnGraphURI);
			map.put("wnSynsetPrefix", wnSynsetPrefix);


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return map;
	}


	private static Map<String,String> map = WordNetTopK.loadProp();
	private static String virtuosoURLSQLport = (String) map.get("virtuosoURLSQLport");
	private static String uid = map.get("uid");
	private static String pwd = map.get("pwd");
	private static String wnGraphURI = map.get("wnGraphURI");
	private static String wnOntoURI = map.get("wnOntoURI");
	private static String wordnetSynsetPrefix = map.get("wnSynsetPrefix");

	public static Model initializeModel() {
		PropertyFunctionRegistry reg = PropertyFunctionRegistry.chooseRegistry(ARQ.getContext());
		reg.put(topk.URI, new PathPropertyFunctionFactory());

		String virtuosojdbc = "jdbc:virtuoso://" + virtuosoURLSQLport; /**+ "/charset=UTF-8/log_enable=2";**/
		VirtGraph setConstruct = new VirtGraph (virtuosojdbc,uid,pwd);

		Query constructQuery = QueryFactory.create("PREFIX wordnet-ontology: "
				+ wnOntoURI /**"<http://wordnet-rdf.princeton.edu/ontology#>"**/
				+ "PREFIX lemon: <http://lemon-model.net/lemon#>"
				+ "CONSTRUCT { ?x wordnet-ontology:hyponym ?y } "
				+ "FROM "
				+ wnGraphURI /**"<http://wordnet-rdf.princeton.edu> "**/
				+ "WHERE { Graph ?graph {"
				+ "?x wordnet-ontology:hyponym ?y }}");    

		VirtuosoQueryExecution vqeConstruct = VirtuosoQueryExecutionFactory.create (constructQuery, setConstruct);
		System.out.println("Retrieving wordnet subgraph...");
		Model resultModel = vqeConstruct.execConstruct();
		vqeConstruct.close();
		return resultModel;

	}




	private static Model m = WordNetTopK.initializeModel();

	@GET
	@Path("/modelStatus")
	@Produces("text/plain")
	public String getModelStatus() {
		if(m!=null) {
			return "OK - Subgraph successfully loaded!";
		}else {
			return "Error - Subgraph could not be loaded :( ";
		}
	}


	// localhost:8080/restful-java-topk/query?s=100017402-n&t=111692851-n&k=3
	@GET
	@Path("/query")
	@Produces("application/json")	
	public String queryModel(@QueryParam("s")String s1, @QueryParam("t")String s2, @QueryParam("k")int k) {
		String synset1 = wordnetSynsetPrefix +s1; //"http://wordnet-rdf.princeton.edu/wn31/"+s1;
		String synset2 = wordnetSynsetPrefix + s2; //"http://wordnet-rdf.princeton.edu/wn31/"+s2;
		Query queryOnResult = QueryFactory.create(""
				+ "PREFIX wordnet-ontology: "
				+ wnOntoURI /**"<http://wordnet-rdf.princeton.edu/ontology#>"**/
				+ "PREFIX ppf:<java:at.ac.wu.arqext.path.>"
				+ "Select ?path ?length "
				+ "where  {"
				+ "BIND(<"
				+ synset1 /**"<http://wordnet-rdf.princeton.edu/wn31/100017402-n>"**/
				+ "> as ?a)"
				+ "BIND(<"
				+ synset2 /**"<http://wordnet-rdf.princeton.edu/wn31/111692851-n>"**/
				+ "> as ?b)"
				+ "?a wordnet-ontology:hyponym+ ?b ."
				+ "?path ppf:topk (?a ?b "
				+ k /**"1"**/
				+ ")"
				+ "bind("
				+ "strlen("
				+ "replace("
				+ "replace(?path, \"hyponym\", \"Å\")"
				+ ", \"[^Å]\", \"\""
				+ ")) as ?length)"
				+ "} ");

		try ( QueryExecution qExec = QueryExecutionFactory.create(queryOnResult, m) ) {
			ResultSet rsConstruct = qExec.execSelect() ;
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			ResultSetFormatter.outputAsJSON(outputStream, rsConstruct);
			String json = new String(outputStream.toByteArray());
			return json;

			/**ResultSetFormatter.out(rsConstruct) ;**/

		}

	}


}
