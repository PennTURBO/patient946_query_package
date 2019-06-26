package example

import com.typesafe.config.ConfigFactory
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.scalatest.FunSuite
import oracle.jrockit.jfr.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.scalatest.BeforeAndAfter
import com.typesafe.config.Config

class upAndRunning extends FunSuite with BeforeAndAfter {

  var parsedConfig: Config = null
  var endpointValue: String = null
  var SparqlRepo: SPARQLRepository = null
  var con: RepositoryConnection = null

  before {
    parsedConfig = ConfigFactory.parseResources("defaults.conf")
    endpointValue = parsedConfig.getString("conf.sparqlEndpoint")
    SparqlRepo = new SPARQLRepository(endpointValue)
    SparqlRepo.initialize()
    con = SparqlRepo.getConnection()
  }

  after {
    con.close
    SparqlRepo.shutDown
  }

  def graphInRepo(myGraph: String): Boolean = {
    //    println(myGraph)
    val queryString = " ask where { graph <" + myGraph + "> { ?s ?p ?o } } "
    //    println(queryString)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString)
    val booleanResult = booleanQuery.evaluate()
    //    println(booleanResult)
    booleanResult
  }

  def classInGraph(myClass: String, myGraph: String): Boolean = {
    //    println(myGraph)
    val queryString = " ask where { graph <" + myGraph + "> { ?s a <" + myClass + "> } } "
    //    println(queryString)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString)
    val booleanResult = booleanQuery.evaluate()
    //    println(booleanResult)
    booleanResult
  }

  // fix this to use the TURBO autonym/epinym
  def superClassInGraph(myClass: String, myGraph: String): Boolean = {
    //    println(myGraph)
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
    //    println(queryString)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString)
    val booleanResult = booleanQuery.evaluate()
    //    println(booleanResult)
    booleanResult
  }

  def arbitraryAsk(myAsk: String): Boolean = {
    //    println(myGraph)
    //    println(myAsk)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, myAsk)
    val booleanResult = booleanQuery.evaluate()
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

  test("Check for eponymous TURBO graph") {
      val fxnRes = graphInRepo("https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl")
      //    println(fxnRes)
      assert(fxnRes)
    }

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

  // why?  we're talking about using much more generic classes now
  // drivetrain wouldn't necessarily be able to handle the generic approach yet

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

  // check whether it denotes ANYTHING first?

  // PCORowl hasn't committed itself to patient CRIDs?

  test("Check for CRID subclass, with symbol subclass part, denoting a person in the expanded graph") {
    val myAsk = """
  ask where {
      graph <http://www.itmat.upenn.edu/biobank/ontology> {
          ?cridType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000578> .
          ?symbolType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000028> .
      }
      graph <http://www.itmat.upenn.edu/biobank/expanded> {
          ?cridInst a ?cridType ;
                    <http://purl.obolibrary.org/obo/IAO_0000219> ?personInst .
          ?personInst a <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
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

  // a consensus on the datatype property for tying identifying values to symbols hasn't been pinned down in recent OBI spinoff calls
  // has value?  representation?  literal?
  // identifying value from  OMOP:  946
  // from synthea:  00002c66-a365-4e88-8e80-d52bcad4869e

  test("Check for CRID subclass, with symbol subclass part, denoting a person in the expanded graph. Symbol should have a value of 00002c66-a365-4e88-8e80-d52bcad4869e") {
    val myAsk = """
  ask where {
      graph <http://www.itmat.upenn.edu/biobank/ontology> {
          ?cridType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000578> .
          ?symbolType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000028> .
      }
      graph <http://www.itmat.upenn.edu/biobank/expanded> {
          ?cridInst a ?cridType ;
                    <http://purl.obolibrary.org/obo/IAO_0000219> ?personInst .
          ?personInst a <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
          ?symbolInst a ?symbolType ;
          <http://transformunify.org/ontologies/TURBO_0010094> "00002c66-a365-4e88-8e80-d52bcad4869e"
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

  test("Check for date of birth DATUM in expanded graph") {
    val fxnRes = classInGraph("http://www.ebi.ac.uk/efo/EFO_0004950", "http://www.itmat.upenn.edu/biobank/expanded")
    //    println(fxnRes)
    assert(fxnRes)
  }

  test("Check for start of neonate stage PROCESS BOUNDARY in expanded graph") {
    val fxnRes = classInGraph("http://purl.obolibrary.org/obo/UBERON_0035946", "http://www.itmat.upenn.edu/biobank/expanded")
    //    println(fxnRes)
    assert(fxnRes)
  }

  test("Check for date of birth about some start of neonate stage in the expanded graph.") {
    val myAsk = """
  ask where {
      graph <http://www.itmat.upenn.edu/biobank/expanded> {
          ?dob a <http://www.ebi.ac.uk/efo/EFO_0004950>;
                    <http://purl.obolibrary.org/obo/IAO_0000136> ?neonateStart .
          ?neonateStart a <http://purl.obolibrary.org/obo/UBERON_0035946>
      }
  }"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  // Currently working on property chain that shows equivalence between this TURBO pattern and the longer PCORowl pattern
  test("Check for date of birth about some start of neonate stage on which a person was born (in the expanded graph.)") {
    val myAsk = """
  ask where {
      graph <http://www.itmat.upenn.edu/biobank/expanded> {
          ?dob a <http://www.ebi.ac.uk/efo/EFO_0004950>;
                    <http://purl.obolibrary.org/obo/IAO_0000136> ?neonateStart .
          ?neonateStart a <http://purl.obolibrary.org/obo/UBERON_0035946> .
          ?person a <http://purl.obolibrary.org/obo/NCBITaxon_9606> ;
              <http://transformunify.org/ontologies/TURBO_0000303> ?neonateStart .
      }
  }"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  test("Check for a date of birth about some start of neonate stage on which the '00002c66-a365-4e88-8e80-d52bcad4869e' person was born (in the expanded graph.)") {
    val myAsk = """
  ask where {
      graph <http://www.itmat.upenn.edu/biobank/ontology> {
          ?cridType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000578> .
          ?symbolType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000028> .
      }
      graph <http://www.itmat.upenn.edu/biobank/expanded> {
          ?cridInst a ?cridType ;
                    <http://purl.obolibrary.org/obo/IAO_0000219> ?personInst .
          ?symbolInst a ?symbolType ;
          <http://transformunify.org/ontologies/TURBO_0010094> "00002c66-a365-4e88-8e80-d52bcad4869e"
          {
              {
                  ?symbolInst <http://purl.obolibrary.org/obo/BFO_0000050> ?cridInst .
              }
              union {
                  ?cridInst <http://purl.obolibrary.org/obo/BFO_0000051> ?symbolInst .
              }
          }
          ?dob a <http://www.ebi.ac.uk/efo/EFO_0004950>;
                    <http://purl.obolibrary.org/obo/IAO_0000136> ?neonateStart .
          ?neonateStart a <http://purl.obolibrary.org/obo/UBERON_0035946> .
          ?personInst a <http://purl.obolibrary.org/obo/NCBITaxon_9606> ;
              <http://transformunify.org/ontologies/TURBO_0000303> ?neonateStart .
      }
  }"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  //  tuplesResult.close

}
