package example

import com.typesafe.config.ConfigFactory
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.scalatest.FunSuite
import oracle.jrockit.jfr.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.scalatest.BeforeAndAfter
import com.typesafe.config.Config

// write some tests that fail when unexpected relations are present

// what do we want to keep as raw/supporting evidence for transformed values (like M -> MGID)?

// instantiate unknown datums (like a OMRSE_00000098 non-specific race identity datum about a person?)

// check shortcuts or Hyaden's Drivetrain graph transformation processes?  Like PCORowl data transformations.

// check for datasets?

// check SYNTAX of URIs, base on template + identifying value pattern devised by Amanda and Mark ?

// height, weight, bmi (assay?  Quality?  ?datum? value specification?)

// qualities inhere in or are quality of, etc.

// handcrafted biobank encounter/LOF allele information?
// Tumor ID?

// haven't tested for the patient's CRID registry

// HCEs
// 17616 / 71ed95de-8d50-4cfe-8aaa-a4d26d859215
// 15512 / d346249f-c84a-442e-9c73-6315838d543c

// need to try this out in some higher levels of GraphDB reasoning for inverse properties and subproperties etc.

// make a prefix block?

// make a prefix alias hack block?

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
    assert(fxnRes)
  }

  // TODO: TURBO ontology (and all ontologies) should be loaded into a graph named after the subject of their '?a a owl:Ontology' statement
  // additional statements could saw where the ontology was loaded from, like a web URL

  test("Check for eponymous TURBO graph") {
    val fxnRes = graphInRepo("https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl")
    assert(fxnRes)
  }

  test("Check for Homo sapiens in expanded graph") {
    val fxnRes = classInGraph("http://purl.obolibrary.org/obo/NCBITaxon_9606", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(fxnRes)
  }

  test("Check for CRID subclass in expanded graph") {
    val fxnRes = superClassInGraph("http://purl.obolibrary.org/obo/IAO_0000578", "http://www.itmat.upenn.edu/biobank/expanded")
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
    assert(fxnRes)
  }

  test("Check for start of neonate stage PROCESS BOUNDARY in expanded graph") {
    val fxnRes = classInGraph("http://purl.obolibrary.org/obo/UBERON_0035946", "http://www.itmat.upenn.edu/biobank/expanded")
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

  test("Check for a date of birth, with date value 1916-02-02, about some start of neonate stage on which the '00002c66-a365-4e88-8e80-d52bcad4869e' person was born (in the expanded graph.)") {
    val myAsk = """
  ask where {
      graph <http://www.itmat.upenn.edu/biobank/ontology> {
          ?cridType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000578> .
          ?symbolType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000028> .
          ?p <http://www.w3.org/2000/01/rdf-schema#subPropertyOf>* <http://transformunify.org/ontologies/TURBO_0010094> .
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
                    <http://purl.obolibrary.org/obo/IAO_0000136> ?neonateStart ;
                    ?p "1916-02-02"^^xsd:date .
          ?neonateStart a <http://purl.obolibrary.org/obo/UBERON_0035946> .
          ?personInst a <http://purl.obolibrary.org/obo/NCBITaxon_9606> ;
              <http://transformunify.org/ontologies/TURBO_0000303> ?neonateStart .
      }
  }"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  // change this to the new male datum (union of biosex and GID)

  test("Check for gender identitiy datum subclass in expanded graph") {
    val fxnRes = superClassInGraph("http://purl.obolibrary.org/obo/OMRSE_00000133", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(fxnRes)
  }

  test("Check for a gender identitiy datum subclass, about the '00002c66-a365-4e88-8e80-d52bcad4869e' person (in the expanded graph.)") {
    val myAsk = """
  ask where {
      graph <http://www.itmat.upenn.edu/biobank/ontology> {
          ?cridType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000578> .
          ?symbolType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000028> .
          ?gendSexDataType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/OMRSE_00000133> .
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
          ?personInst a <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
          ?gendSexDataInst a  ?gendSexDataType ;
              <http://purl.obolibrary.org/obo/IAO_0000136> ?personInst .
      }
  }"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  test("Check for racial identitiy datum subclass in expanded graph") {
    val fxnRes = superClassInGraph("http://purl.obolibrary.org/obo/OMRSE_00000098", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(fxnRes)
  }

  test("Check for black racial identitiy datum in expanded graph") {
    val fxnRes = classInGraph("http://purl.obolibrary.org/obo/OMRSE_00000182", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(fxnRes)
  }

  test("Check for a black racial identitiy datum about the '00002c66-a365-4e88-8e80-d52bcad4869e' person (in the expanded graph.)") {
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
          ?personInst a <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
          ?ridInstance a  <http://purl.obolibrary.org/obo/OMRSE_00000182> ;
              <http://purl.obolibrary.org/obo/IAO_0000136> ?personInst .
      }
  }"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  test("Check for patient role in expanded graph") {
    val fxnRes = classInGraph("http://purl.obolibrary.org/obo/OBI_0000093", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(fxnRes)
  }

  test("patient role inheres in, is role of, or is borne by person '00002c66-a365-4e88-8e80-d52bcad4869e' (in the expanded graph.)") {
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
        ?personInst a <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
        ?roleInst a <http://purl.obolibrary.org/obo/OBI_0000093> .
        {
            {
                ?personInst <http://purl.obolibrary.org/obo/RO_0000087> ?roleInst .
            }
            union {
                ?roleInst <http://purl.obolibrary.org/obo/RO_0000052> ?personInst .
            }
            union {
                ?roleInst <http://purl.obolibrary.org/obo/RO_0000080>  ?personInst .
            }
        }
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  test("Check for health care encounter in expanded graph") {
    val fxnRes = classInGraph("http://purl.obolibrary.org/obo/OGMS_0000097", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(fxnRes)
  }

  // realized in?  realizes?

  test("patient role associated with person '00002c66-a365-4e88-8e80-d52bcad4869e' is realized in a health care encounter(in the expanded graph.)") {
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
        ?personInst a <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
        ?roleInst a <http://purl.obolibrary.org/obo/OBI_0000093> ;
            <http://purl.obolibrary.org/obo/BFO_0000054> ?hce .
        ?hce a <http://purl.obolibrary.org/obo/OGMS_0000097> .    
        {
            {
                ?personInst <http://purl.obolibrary.org/obo/RO_0000087> ?roleInst .
            }
            union {
                ?roleInst <http://purl.obolibrary.org/obo/RO_0000052> ?personInst .
            }
            union {
                ?roleInst <http://purl.obolibrary.org/obo/RO_0000080>  ?personInst .
            }
        }
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  // is participation necessary on top of role realization?

  test("person '00002c66-a365-4e88-8e80-d52bcad4869e' participates in the health care encounter that realizes the patient's role (in the expanded graph.)") {
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
        ?personInst a <http://purl.obolibrary.org/obo/NCBITaxon_9606> ;
            <http://purl.obolibrary.org/obo/RO_0000056> ?hce .
        ?roleInst a <http://purl.obolibrary.org/obo/OBI_0000093> ;
            <http://purl.obolibrary.org/obo/BFO_0000054> ?hce .
        ?hce a <http://purl.obolibrary.org/obo/OGMS_0000097> .    
        {
            {
                ?personInst <http://purl.obolibrary.org/obo/RO_0000087> ?roleInst .
            }
            union {
                ?roleInst <http://purl.obolibrary.org/obo/RO_0000052> ?personInst .
            }
            union {
                ?roleInst <http://purl.obolibrary.org/obo/RO_0000080>  ?personInst .
            }
        }
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  // identify encounter with keys or CRIDs?

  test("here, encounters are denoted by CRIDs (in the expanded graph.)") {
    val myAsk = """
ask where {
    graph <http://www.itmat.upenn.edu/biobank/ontology> {
        ?EcridType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000578> .
        # ?EsymbolType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000028> .
    }
    graph <http://www.itmat.upenn.edu/biobank/expanded> {
        ?EcridInst a ?EcridType ;
                  <http://purl.obolibrary.org/obo/IAO_0000219> ?encInst .
        ?encInst a <http://purl.obolibrary.org/obo/OGMS_0000097> . } } 
 """
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  //    test("person '00002c66-a365-4e88-8e80-d52bcad4869e' participates in a CRID-denoted health care encounter  (in the expanded graph.)") {
  //    val myAsk = """
  //ask where {
  //    graph <http://www.itmat.upenn.edu/biobank/ontology> {
  //        ?pcridType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000578> .
  //        ?psymbolType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000028> .
  //        ?ecridType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000578> .
  //        ?esymbolType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://purl.obolibrary.org/obo/IAO_0000028> .
  //    }
  //    graph <http://www.itmat.upenn.edu/biobank/expanded> {
  //        ?pcridInst a ?pcridType ;
  //                  <http://purl.obolibrary.org/obo/IAO_0000219> ?personInst .
  //        ?psymbolInst a ?psymbolType ;
  //                    <http://transformunify.org/ontologies/TURBO_0010094> "00002c66-a365-4e88-8e80-d52bcad4869e"
  //        {
  //            {
  //                ?psymbolInst <http://purl.obolibrary.org/obo/BFO_0000050> ?pcridInst .
  //            }
  //            union {
  //                ?pcridInst <http://purl.obolibrary.org/obo/BFO_0000051> ?psymbolInst .
  //            }
  //        }
  //                ?ecridInst a ?ecridType ;
  //                  <http://purl.obolibrary.org/obo/IAO_0000219> ?hce .
  //        ?esymbolInst a ?esymbolType ;
  //                    <http://transformunify.org/ontologies/TURBO_0010094> "00002c66-a365-4e88-8e80-d52bcad4869e"
  //        {
  //            {
  //                ?psymbolInst <http://purl.obolibrary.org/obo/BFO_0000050> ?pcridInst .
  //            }
  //            union {
  //                ?pcridInst <http://purl.obolibrary.org/obo/BFO_0000051> ?psymbolInst .
  //            }
  //        }
  //        ?personInst a <http://purl.obolibrary.org/obo/NCBITaxon_9606> ;
  //            <http://purl.obolibrary.org/obo/RO_0000056> ?hce .
  //        ?roleInst a <http://purl.obolibrary.org/obo/OBI_0000093> ;
  //            <http://purl.obolibrary.org/obo/BFO_0000054> ?hce .
  //        ?hce a <http://purl.obolibrary.org/obo/OGMS_0000097> .
  //        {
  //            {
  //                ?personInst <http://purl.obolibrary.org/obo/RO_0000087> ?roleInst .
  //            }
  //            union {
  //                ?roleInst <http://purl.obolibrary.org/obo/RO_0000052> ?personInst .
  //            }
  //            union {
  //                ?roleInst <http://purl.obolibrary.org/obo/RO_0000080>  ?personInst .
  //            }
  //        }
  //    }
  //}"""
  //    val fxnRes = arbitraryAsk(myAsk)
  //    assert(fxnRes)
  //  }

  //  tuplesResult.close

}
