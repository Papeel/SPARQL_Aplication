
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.lang.SPARQLParser;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *http://aemet.linkeddata.es/sparql
 * @author nelso
 */
public class Consultor {
    
    public static ResultSet current;
    
    public static String  consultar(String path, String query){
        Model model = RDFDataMgr.loadModel(path);
        return ejecutarConsultaFormateada(model, query);
        
    }
    
    public static String consultaRemota(String url ,String query){
        return ejecutarConsultaRemota(url, query);
    }
    
    
    public static boolean saveResult(String path){
        if(current == null)return false;
        boolean state = true;
        FileWriter fichero = null;
        PrintWriter pw = null;
        
        try{
            
            fichero = new FileWriter(path, false);
            pw = new PrintWriter(fichero);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            ResultSetFormatter.outputAsJSON(outputStream, current);


            String json = new String(outputStream.toByteArray());

            
            pw.write(json);
           
           
            
        }catch(Exception e){
            state=false;
        }finally{
            try {
           // Nuevamente aprovechamos el finally para 
           // asegurarnos que se cierra el fichero.
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
        return state;
    }
    
    private static String ejecutarConsultaRemota(String url, String query) {
        try{
            String str_consulta = "Select distinct ?Concepto"
                     + " where { ?s a ?Concepto .} "
                     + "limit 10";
            
             QueryExecution ejecucion = QueryExecutionFactory.sparqlService(url, query);
             ((QueryEngineHTTP) ejecucion).addParam("timeout", "1000");
             ResultSet resultados = ejecucion.execSelect();
             ResultSet resultados2 = ResultSetFactory.copyResults(resultados);
             current = resultados2;
             //ResultSetFormatter.out(System.out,resultados);
             ejecucion.close();
             return ResultSetFormatter.asText(resultados2);
             
        }catch(QueryParseException e){
            current = null;
            return "Error consulta\n" + e.getMessage() + "\n Linea: " + e.getLine() + " Columna: " + e.getColumn();
            
        }catch(QueryExceptionHTTP r){
            return "Utilice una url adecuada.";
        }
        
    }
    
    private static String ejecutarConsultaFormateada(Model datos, String consulta) {
        try{
            QueryExecution ejecucion =  QueryExecutionFactory.create(consulta,datos);
            ResultSet resultados =  ejecucion.execSelect();
            ResultSet resultados2 =  ResultSetFactory.copyResults(resultados);
            ejecucion.close();
            
            current = resultados2;
            //ResultSetFormatter.out(System.out,resultados2);
            String re  =ResultSetFormatter.asText(resultados2);
            
            
            
            return re;
        }catch(QueryParseException e){
            current = null;
            return "Error consulta\n" + e.getMessage() + "\n Linea: " + e.getLine() + " Columna: " + e.getColumn();
            
        }
        
        
    }
    
    
}
