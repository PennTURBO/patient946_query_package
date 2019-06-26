package example

import com.typesafe.config.ConfigFactory
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.scalatest.FunSuite
import oracle.jrockit.jfr.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.scalatest.BeforeAndAfter

class upAndRunning extends FunSuite with BeforeAndAfter {
  
  // TODO don't send debug log statements to console

  // TODO factor out the connection opening and closing into before and after blocks

  def graphInRepo(myGraph: String): Boolean = {
    //    println(myGraph)
    val parsedConfig = ConfigFactory.parseResources("defaults.conf")
    val endpointValue = parsedConfig.getString("conf.sparqlEndpoint")
    val SparqlRepo = new SPARQLRepository(endpointValue)
    SparqlRepo.initialize()
    val con = SparqlRepo.getConnection()
    val queryString = " ask where { graph <" + myGraph + "> { ?s ?p ?o } } "
    //    println(queryString)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString)
    val booleanResult = booleanQuery.evaluate()
    con.close
    SparqlRepo.shutDown
    //    println(booleanResult)
    booleanResult
  }

  def classInGraph(myClass: String, myGraph: String): Boolean = {
    //    println(myGraph)
    val parsedConfig = ConfigFactory.parseResources("defaults.conf")
    val endpointValue = parsedConfig.getString("conf.sparqlEndpoint")
    val SparqlRepo = new SPARQLRepository(endpointValue)
    SparqlRepo.initialize()
    val con = SparqlRepo.getConnection()
    val queryString = " ask where { graph <" + myGraph + "> { ?s a <" + myClass + "> } } "
    //    println(queryString)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString)
    val booleanResult = booleanQuery.evaluate()
    con.close
    SparqlRepo.shutDown
    //    println(booleanResult)
    booleanResult
  }

  // fix this to use the TURBO autonym/epinym
  def superClassInGraph(myClass: String, myGraph: String): Boolean = {
    //    println(myGraph)
    val parsedConfig = ConfigFactory.parseResources("defaults.conf")
    val endpointValue = parsedConfig.getString("conf.sparqlEndpoint")
    val SparqlRepo = new SPARQLRepository(endpointValue)
    SparqlRepo.initialize()
    val con = SparqlRepo.getConnection()
    val queryString = """
ask where {
    graph <http://www.itmat.upenn.edu/biobank/ontology> {
        ?t <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <""" + myClass + """> 
    }
    graph <""" + myGraph + """> {
        ?s a ?t .
    }
}
"""
    println(queryString)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString)
    val booleanResult = booleanQuery.evaluate()
    con.close
    SparqlRepo.shutDown
    //    println(booleanResult)
    booleanResult
  }

  def arbitraryAsk(myAsk: String): Boolean = {
    //    println(myGraph)
    val parsedConfig = ConfigFactory.parseResources("defaults.conf")
    val endpointValue = parsedConfig.getString("conf.sparqlEndpoint")
    val SparqlRepo = new SPARQLRepository(endpointValue)
    SparqlRepo.initialize()
    val con = SparqlRepo.getConnection()
    //    println(myAsk)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, myAsk)
    val booleanResult = booleanQuery.evaluate()
    con.close
    SparqlRepo.shutDown
    //    println(booleanResult)
    booleanResult
  }

  test("Check for expanded graph") {
    val fxnRes = graphInRepo("http://www.itmat.upenn.edu/biobank/expanded")
    //    println(fxnRes)
    assert(fxnRes)
  }

  // TODO: TURBO ontology (and all ontologies) should be loaded into a graph named after the subject of their '?a a owl:Ontology' statement
  // additional statements could saw where the ontology was loaded from, like a web URL

  //  test("Check for eponymous TURBO graph") {
  //    val fxnRes = graphInRepo("https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl")
  //    //    println(fxnRes)
  //    assert(fxnRes)
  //  }

  test("Check for Homo sapiens in expanded graph") {
    val fxnRes = classInGraph("http://purl.obolibrary.org/obo/NCBITaxon_9606", "http://www.itmat.upenn.edu/biobank/expanded")
    //    println(fxnRes)
    assert(fxnRes)
  }

  test("Check for CRID subclass in expanded graph") {
    val fxnRes = superClassInGraph("http://purl.obolibrary.org/obo/IAO_0000578", "http://www.itmat.upenn.edu/biobank/expanded")
    //    println(fxnRes)
    assert(fxnRes)
  }

  test("Check for CRID subclass denoting a person in the expanded graph") {
    val myAsk = """
            ask where {
    graph <http://www.itmat.upenn.edu/biobank/ontology> {
        ?cridType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000578> 
    }
    graph <http://www.itmat.upenn.edu/biobank/expanded> {
        ?cridInst a ?cridType ;
                  <http://purl.obolibrary.org/obo/IAO_0000219> ?personInst .
        ?personInst a <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  test("Check for symbol subclass in expanded graph") {
    val fxnRes = superClassInGraph("http://purl.obolibrary.org/obo/IAO_0000028", "http://www.itmat.upenn.edu/biobank/expanded")
    //    println(fxnRes)
    assert(fxnRes)
  }

  // part of or has part?

  test("Check for symbol subclass part of CRID subclass in the expanded graph") {
    val myAsk = """
ask where {
    graph <http://www.itmat.upenn.edu/biobank/ontology> {
        ?cridType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000578> .
        ?symbolType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000028> .
    }
    graph <http://www.itmat.upenn.edu/biobank/expanded> {
        ?cridInst a ?cridType .
        ?symbolInst a ?symbolType .
        {
            {
                ?symbolInst <http://purl.obolibrary.org/obo/BFO_0000050> ?cridInst .
            }
            union {
                ?cridInst <http://purl.obolibrary.org/obo/BFO_0000051> ?symbolInst .
            }
        }
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  //  tuplesResult.close

}