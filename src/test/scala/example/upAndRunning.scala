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

// check shortcuts?

// check SYNTAX of URIs, based on template + identifying value pattern devised by Amanda and Mark ?
// drivetrain is making UUID based URIs

// what do we want to keep as raw/supporting evidence for transformed values (like M -> MGID)?

// instantiate unknown datums (like a OMRSE_00000098 non-specific race identity datum about a person?)

// height, weight, bmi (assay?  Quality?  ?datum? value specification?)

// qualities inhere in or are quality of, etc.

// handcrafted biobank encounter/LOF allele information?
// Tumor ID?

// haven't tested for the patient's CRID registry

// HCEs
// 17616 / 71ed95de-8d50-4cfe-8aaa-a4d26d859215
// 15512 / d346249f-c84a-442e-9c73-6315838d543c

// assume RDFS+ reasoning in expanded graph?

// factor out hard-coded ontology graph name?

// import rxnorm and cvx

class upAndRunning extends FunSuite with BeforeAndAfter {

  var parsedConfig: Config = null
  var endpointValue: String = null
  var SparqlRepo: SPARQLRepository = null
  var con: RepositoryConnection = null

  val prefixesAliases = """
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX dc11: <http://purl.org/dc/elements/1.1/>
PREFIX efo: <http://www.ebi.ac.uk/efo/>
PREFIX faldo: <http://biohackathon.org/resource/faldo#>
PREFIX fn: <http://www.w3.org/2005/xpath-functions#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX go: <http://purl.obolibrary.org/obo/go#>
PREFIX mydata: <http://example.com/resource/>
PREFIX ncbitaxon: <http://purl.obolibrary.org/obo/ncbitaxon#>
PREFIX nci: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX oboInOwl: <http://www.geneontology.org/formats/oboInOwl#>
PREFIX ontoneo: <http://purl.obolibrary.org/obo/ontoneo/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX pmbb: <http://www.itmat.upenn.edu/biobank/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ro: <http://www.obofoundry.org/ro/ro.owl#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX snomed: <http://purl.bioontology.org/ontology/SNOMEDCT/>
PREFIX terms: <http://purl.org/dc/terms/>
PREFIX Thesaurus: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>
PREFIX turbo: <http://transformunify.org/ontologies/>
PREFIX umls: <http://bioportal.bioontology.org/ontologies/umls/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX rxnorm: <http://purl.bioontology.org/ontology/RXNORM/>
PREFIX cvx: <http://purl.bioontology.org/ontology/CVX/>
PREFIX turboOntGraph: <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl>
PREFIX expandedGraph: <http://www.itmat.upenn.edu/biobank/expanded>
PREFIX human: <http://purl.obolibrary.org/obo/NCBITaxon_9606> 
PREFIX crid: <http://purl.obolibrary.org/obo/IAO_0000578>   
PREFIX symbol: <http://purl.obolibrary.org/obo/IAO_0000028>   
PREFIX denotes: <http://purl.obolibrary.org/obo/IAO_0000219>   
PREFIX hasRepresentation: <http://transformunify.org/ontologies/TURBO_0010094>   
PREFIX partOf: <http://purl.obolibrary.org/obo/BFO_0000050>    
PREFIX hasPart: <http://purl.obolibrary.org/obo/BFO_0000051>   
PREFIX hce: <http://purl.obolibrary.org/obo/OGMS_0000097>
PREFIX participatesIn: <http://purl.obolibrary.org/obo/RO_0000056>
PREFIX processBoundary: <http://purl.obolibrary.org/obo/BFO_0000035>
PREFIX starts: <http://purl.obolibrary.org/obo/RO_0002223>
PREFIX mentions: <http://purl.obolibrary.org/obo/IAO_0000142>
PREFIX patientRole: <http://purl.obolibrary.org/obo/OBI_0000093>
PREFIX neonateStart: <http://purl.obolibrary.org/obo/UBERON_0035946>
PREFIX dobTmd: <http://www.ebi.ac.uk/efo/EFO_0004950>
PREFIX dataSet: <http://purl.obolibrary.org/obo/IAO_0000100>
PREFIX procBound: <http://purl.obolibrary.org/obo/BFO_0000035>
        """

  before {
    parsedConfig = ConfigFactory.parseResources("defaults.conf")
    endpointValue = parsedConfig.getString("conf.sparqlEndpoint")
    //    println(endpointValue)
    SparqlRepo = new SPARQLRepository(endpointValue)
    SparqlRepo.initialize()
    con = SparqlRepo.getConnection()
  }

  after {
    con.close
    SparqlRepo.shutDown
  }

  ///   ///   ///

  def graphInRepo(myGraph: String): Boolean = {
    val queryString = " ask where { graph <" + myGraph + "> { ?s ?p ?o } } "
    println(queryString)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString)
    val booleanResult = booleanQuery.evaluate()
    booleanResult
  }

  def classInGraph(myClass: String, myGraph: String): Boolean = {
    val queryString = " ask where { graph <" + myGraph + "> { ?s a <" + myClass + "> } } "
    println(queryString)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString)
    val booleanResult = booleanQuery.evaluate()
    booleanResult
  }

  def propertyInGraph(myProp: String, myGraph: String): Boolean = {
    val queryString = " ask where { graph <" + myGraph + "> { ?s <" + myProp + ">  ?o } } "
    println(queryString)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString)
    val booleanResult = booleanQuery.evaluate()
    booleanResult
  }

  def subPropertyInGraph(myProp: String, myGraph: String): Boolean = {
    val queryString = """
  ask where {
      graph turboOntGraph: {
          ?p rdfs:subPropertyOf* <""" + myProp + """>
      }
      graph <""" + myGraph + """> {
          ?s ?p ?t .
      }
  }
  """
    val withPrefixes = prefixesAliases + queryString
    println(withPrefixes)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, withPrefixes)
    val booleanResult = booleanQuery.evaluate()
    booleanResult
  }

  def superClassInGraph(myClass: String, myGraph: String): Boolean = {
    val queryString = """
  ask where {
      graph turboOntGraph: {
          ?t rdfs:subClassOf* <""" + myClass + """>
      }
      graph <""" + myGraph + """> {
          ?s a ?t .
      }
  }
  """
    val withPrefixes = prefixesAliases + queryString
    println(withPrefixes)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, withPrefixes)
    val booleanResult = booleanQuery.evaluate()
    booleanResult
  }

  def arbitraryAsk(myAsk: String): Boolean = {
    val withPrefixes = prefixesAliases + myAsk
    println(withPrefixes)
    val booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, withPrefixes)
    val booleanResult = booleanQuery.evaluate()
    booleanResult
  }

  ///   ///   ///

  test("Check for expanded graph") {
    val fxnRes = graphInRepo("http://www.itmat.upenn.edu/biobank/expanded")
    assert(fxnRes)
  }

  // has literal value is still used in the ontology to define the loss of function values!
  
  test("there shouldn't be any usage of 'has literal value' properties in the expanded graph") {
    val fxnRes = subPropertyInGraph("http://transformunify.org/ontologies/TURBO_0006510", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(!fxnRes)
  }

  test("there shouldn't be any usage of 'instantiation hybrid shortcut' properties in the expanded graph") {
    val fxnRes = subPropertyInGraph("http://transformunify.org/ontologies/TURBO_0000670", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(!fxnRes)
  }

  test("there shouldn't be any usage of 'instantiation object shortcut' properties in the expanded graph") {
    val fxnRes = subPropertyInGraph("http://transformunify.org/ontologies/TURBO_0010080", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(!fxnRes)
  }

  test("there shouldn't be any data model instances in the expanded graph") {
    val fxnRes = superClassInGraph("http://transformunify.org/ontologies/TURBO_0010143", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(!fxnRes)
  }

  test("No need for namespace textual values... requires dicussion") {
    val fxnRes = propertyInGraph("http://transformunify.org/ontologies/TURBO_0006515", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(!fxnRes)
  }

  test("No need for namespace object relations... requires dicussion") {
    val fxnRes = propertyInGraph("http://transformunify.org/ontologies/TURBO_0000703", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(!fxnRes)
  }

  test("Don't bother time stamping BMIs or anything elsea... requires dicussion") {
    val fxnRes = propertyInGraph("http://purl.obolibrary.org/obo/IAO_0000581", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(!fxnRes)
  }

  test("untyped subjects") {
    val myAsk = """
      ask where {
    graph expandedGraph: {
        ?s ?p ?o .
    }
    minus {
        ?s a ?t
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(!fxnRes)
  }

  // http://purl.bioontology.org/ontology/CVX/140... instantiated properly; mark needs to re-extract from UMLS
  // http://purl.bioontology.org/ontology/RXNORM/1006478, not http://purl.bioontology.org/ontology/RxNorm/316672
  // turbo defines snmoed as http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C53489
  // this test would also fail if an upstream source used a RxNorm, SNOMED, etc. term that wasn't defined in the relevant ontology
  test("untyped statement objects... pass requires that SNOMED, RxNorm and CVX have been loaded ON CODE") {
    val myAsk = """
ask
where {
    graph expandedGraph: {
        ?s ?p ?o .
    }
    filter(isuri(?o))
    filter(?p != mentions: )
    minus {
        ?o a ?t
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(!fxnRes)
  }

  test("untyped objects... unfiltered mentions") {
    val myAsk = """
ask 
where {
    graph expandedGraph: {
        ?s ?p ?o .
    }
    filter(isuri(?o))
#    filter(?p != mentions: )
    minus {
        ?o a ?t
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(!fxnRes)
  }

  // we haven't agreed on whether name spaces should be asserted for diagnoses (or anything else)
  // could break this into at least two tests for each of the predicates
  // and or be more specific about the subject and predicate types
  test("namespace predicates... requires decision https://github.com/pennbiobank/turbo/issues/267") {
    val myAsk = """
ask where {
    values ?p {
        turbo:TURBO_0006515 
        turbo:TURBO_0000703
    }
    graph expandedGraph: {
        ?s ?p ?o
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  test("Check for CRID subclass in expanded graph") {
    val fxnRes = superClassInGraph("http://purl.obolibrary.org/obo/IAO_0000578", "http://www.itmat.upenn.edu/biobank/expanded")
    assert(fxnRes)
  }

  test("CRIDs with no parts") {
    val myAsk = """
ask where {
    graph <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
        ?crid rdfs:subClassOf* <http://purl.obolibrary.org/obo/IAO_0000578> .
    }
    graph <http://www.itmat.upenn.edu/biobank/expanded> {
        ?x a ?crid .
        minus {
            ?x <http://purl.obolibrary.org/obo/BFO_0000051> ?y .
        }
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(!fxnRes)
  }

  // slow
  test("CRIDs with no symbol parts") {
    val myAsk = """
ask where {
    graph <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
        ?crid rdfs:subClassOf* <http://purl.obolibrary.org/obo/IAO_0000578> .
    }
    graph <http://www.itmat.upenn.edu/biobank/expanded> {
        ?x a ?crid .
    }
    filter (not exists {
            graph <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
                ?symb rdfs:subClassOf* <http://purl.obolibrary.org/obo/IAO_0000028> .
            }
            ?x <http://purl.obolibrary.org/obo/BFO_0000051> ?y .
            ?y a ?symb .
        })
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(!fxnRes)
  }

  // slow
  test("CRIDs with no registry denoter parts") {
    val myAsk = """
ask where {
    graph <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
        ?crid rdfs:subClassOf* <http://purl.obolibrary.org/obo/IAO_0000578> .
    }
    graph <http://www.itmat.upenn.edu/biobank/expanded> {
        ?x a ?crid .
    }
    filter (not exists {
            graph <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
                ?regden rdfs:subClassOf* <http://transformunify.org/ontologies/TURBO_00001501> .
            }
            ?x <http://purl.obolibrary.org/obo/BFO_0000051> ?y .
            ?y a ?regden .
        })
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(!fxnRes)
  }

  // similar but not identical to the above... would tolerate any other ICE that denotes a registry
  test("CRIDs with no parts that denote a registry") {
    val myAsk = """
ask where {
    graph <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
        ?crid rdfs:subClassOf* <http://purl.obolibrary.org/obo/IAO_0000578> .
    }
    graph <http://www.itmat.upenn.edu/biobank/expanded> {
        ?cridInst a ?crid .
    }
    filter (not exists {
            graph <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
                ?registry rdfs:subClassOf* <http://purl.obolibrary.org/obo/IAO_0000579> .
            }
            ?cridInst <http://purl.obolibrary.org/obo/BFO_0000051> ?cridPart .
            ?cridPart <http://purl.obolibrary.org/obo/IAO_0000219> ?denoted .
            ?denoted a ?registry .
        })
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(!fxnRes)
  }

  // Don't make one registry denoter for each CRID
  test("dentoers of regsitries should be singletons defined in the ontology") {
    val myAsk = """
select * where {
    graph <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
        ?registry rdfs:subClassOf* <http://purl.obolibrary.org/obo/IAO_0000579> .
        ?denoted a ?registry .
    }
    graph <http://www.itmat.upenn.edu/biobank/expanded> {
        ?regden <http://purl.obolibrary.org/obo/IAO_0000219> ?denoted .
    }
    filter (not exists {
            graph <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
                ?regden ?p ?o .
            }
        })
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(!fxnRes)
  }

  // people should be denoted by person crids, not biobank consenter crids
  // could do the same kind of tests for regdens and symbols
  // also... check definitions and usage of biobank retired classes and "*biobank*" shortcut predicates

  // are the registry denoting instances defined singletons in the ontology?

  test("there should be patient crid instances") {
    val myAsk = """
ask where {
    graph <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
        ?crid rdfs:subClassOf* <http://transformunify.org/ontologies/TURBO_0010092>   
    }
    graph <http://www.itmat.upenn.edu/biobank/expanded> {
        ?s a ?crid
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  test("there shouldn't be any biobank consenter crid instances") {
    val myAsk = """
ask where {
    graph <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
        ?crid rdfs:subClassOf* <http://transformunify.org/ontologies/TURBO_0000503>   
    }
    graph <http://www.itmat.upenn.edu/biobank/expanded> {
        ?s a ?crid
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(!fxnRes)
  }

  // we haven't agreed on whether prescription CRIDs should be used
  test("presciption CRIDs... requires decision") {
    val myAsk = """
ask where {
    graph expandedGraph: {
        ?s a turbo:TURBO_0000561 .
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  // we haven't agreed on how the usage of datasets should be tested
  test("datasets... requires decision") {
    val myAsk = """
ask where {
    graph expandedGraph: {
    ?s partOf: ?d .
        ?d a dataSet: .
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  // we haven't agreed on whether there should be any tests over the graph transformation recipe processes
  // still need to define a parent class for the expansions and the "entity linking"
  // Like PCORowl data transformations?
  test("INSTANTIATED graph transformation processes... requires decision") {
    val myAsk = """
ask
where {
    graph 
    <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl>  
    {
        ?proc rdfs:subClassOf* turbo:TURBO_0010176
    }
#    graph
#    ?g 
#    #<http://www.itmat.upenn.edu/biobank/expanded> 
    {
        ?s a ?proc .
    }
}
"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  // all graphs should have some basic annotations.
  // see, for example, <http://purl.bioontology.org/ontology/RXNORM/> ?p ?o in graph <http://purl.bioontology.org/ontology/RXNORM/>
  test("unannotated graphs") {
    val myAsk = """
ask
#select distinct ?g
where {
    graph ?g {
        ?s ?sp ?so .
    }
    minus {
        ?g ?gp ?go
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(!fxnRes)
  }

  test("default unnamed graph should be empty") {
    val myAsk = """
ask
from <http://www.openrdf.org/schema/sesame#nil>
where {
    ?s ?p ?o .
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(!fxnRes)
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

  // why?  we're considering using much more generic classes now
  // drivetrain wouldn't necessarily be able to handle the generic approach yet

  test("Check for CRID subclass denoting a person in the expanded graph") {
    val myAsk = """
              ask where {
      graph turboOntGraph: {
          ?cridType rdfs:subClassOf* crid:
      }
      graph expandedGraph: {
          ?cridInst a ?cridType ;
                    denotes: ?personInst .
          ?personInst a human: .
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
      graph turboOntGraph: {
          ?cridType rdfs:subClassOf* crid: .
          ?symbolType rdfs:subClassOf* symbol: .
      }
      graph expandedGraph: {
          ?cridInst a ?cridType .
          ?symbolInst a ?symbolType .
          {
              {
                  ?symbolInst partOf: ?cridInst .
              }
              union {
                  ?cridInst hasPart: ?symbolInst .
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
      graph turboOntGraph: {
          ?cridType rdfs:subClassOf* crid: .
          ?symbolType rdfs:subClassOf* symbol: .
      }
      graph expandedGraph: {
          ?cridInst a ?cridType ;
                    denotes: ?personInst .
          ?personInst a human: .
          ?symbolInst a ?symbolType .
          {
              {
                  ?symbolInst partOf: ?cridInst .
              }
              union {
                  ?cridInst hasPart: ?symbolInst .
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
      graph turboOntGraph: {
          ?cridType rdfs:subClassOf* crid: .
          ?symbolType rdfs:subClassOf* symbol: .
      }
      graph expandedGraph: {
          ?cridInst a ?cridType ;
                    denotes: ?personInst .
          ?personInst a human: .
          ?symbolInst a ?symbolType ;
          hasRepresentation: "00002c66-a365-4e88-8e80-d52bcad4869e"
          {
              {
                  ?symbolInst partOf: ?cridInst .
              }
              union {
                  ?cridInst hasPart: ?symbolInst .
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
      graph expandedGraph: {
          ?dob a dobTmd:;
                    <http://purl.obolibrary.org/obo/IAO_0000136> ?neonateStart .
          ?neonateStart a neonateStart:
      }
  }"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  // Currently working on property chain that shows equivalence between this TURBO pattern and the longer PCORowl pattern
  test("Check for date of birth about some start of neonate stage on which a person was born (in the expanded graph.)") {
    val myAsk = """
  ask where {
      graph expandedGraph: {
          ?dob a dobTmd:;
                    <http://purl.obolibrary.org/obo/IAO_0000136> ?neonateStart .
          ?neonateStart a neonateStart: .
          ?person a human: ;
              <http://transformunify.org/ontologies/TURBO_0000303> ?neonateStart .
      }
  }"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  test("Check for a date of birth about some start of neonate stage on which the '00002c66-a365-4e88-8e80-d52bcad4869e' person was born (in the expanded graph.)") {
    val myAsk = """
  ask where {
      graph turboOntGraph: {
          ?cridType rdfs:subClassOf* crid: .
          ?symbolType rdfs:subClassOf* symbol: .
      }
      graph expandedGraph: {
          ?cridInst a ?cridType ;
                    denotes: ?personInst .
          ?symbolInst a ?symbolType ;
          hasRepresentation: "00002c66-a365-4e88-8e80-d52bcad4869e"
          {
              {
                  ?symbolInst partOf: ?cridInst .
              }
              union {
                  ?cridInst hasPart: ?symbolInst .
              }
          }
          ?dob a dobTmd:;
                    <http://purl.obolibrary.org/obo/IAO_0000136> ?neonateStart .
          ?neonateStart a neonateStart: .
          ?personInst a human: ;
              <http://transformunify.org/ontologies/TURBO_0000303> ?neonateStart .
      }
  }"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  test("Check for a date of birth, with date value 1916-02-02, about some start of neonate stage on which the '00002c66-a365-4e88-8e80-d52bcad4869e' person was born (in the expanded graph.)") {
    val myAsk = """
  ask where {
      graph turboOntGraph: {
          ?cridType rdfs:subClassOf* crid: .
          ?symbolType rdfs:subClassOf* symbol: .
          ?p <http://www.w3.org/2000/01/rdf-schema#subPropertyOf>* hasRepresentation: .
      }
      graph expandedGraph: {
          ?cridInst a ?cridType ;
                    denotes: ?personInst .
          ?symbolInst a ?symbolType ;
          hasRepresentation: "00002c66-a365-4e88-8e80-d52bcad4869e"
          {
              {
                  ?symbolInst partOf: ?cridInst .
              }
              union {
                  ?cridInst hasPart: ?symbolInst .
              }
          }
          ?dob a dobTmd:;
                    <http://purl.obolibrary.org/obo/IAO_0000136> ?neonateStart ;
                    ?p "1916-02-02"^^xsd:date .
          ?neonateStart a neonateStart: .
          ?personInst a human: ;
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
      graph turboOntGraph: {
          ?cridType rdfs:subClassOf* crid: .
          ?symbolType rdfs:subClassOf* symbol: .
          ?gendSexDataType rdfs:subClassOf* <http://purl.obolibrary.org/obo/OMRSE_00000133> .
      }
      graph expandedGraph: {
          ?cridInst a ?cridType ;
                    denotes: ?personInst .
          ?symbolInst a ?symbolType ;
          hasRepresentation: "00002c66-a365-4e88-8e80-d52bcad4869e"
          {
              {
                  ?symbolInst partOf: ?cridInst .
              }
              union {
                  ?cridInst hasPart: ?symbolInst .
              }
          }
          ?personInst a human: .
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
      graph turboOntGraph: {
          ?cridType rdfs:subClassOf* crid: .
          ?symbolType rdfs:subClassOf* symbol: .
      }
      graph expandedGraph: {
          ?cridInst a ?cridType ;
                    denotes: ?personInst .
          ?symbolInst a ?symbolType ;
          hasRepresentation: "00002c66-a365-4e88-8e80-d52bcad4869e"
          {
              {
                  ?symbolInst partOf: ?cridInst .
              }
              union {
                  ?cridInst hasPart: ?symbolInst .
              }
          }
          ?personInst a human: .
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
    graph turboOntGraph: {
        ?cridType rdfs:subClassOf* crid: .
        ?symbolType rdfs:subClassOf* symbol: .
    }
    graph expandedGraph: {
        ?cridInst a ?cridType ;
                  denotes: ?personInst .
        ?symbolInst a ?symbolType ;
                    hasRepresentation: "00002c66-a365-4e88-8e80-d52bcad4869e"
        {
            {
                ?symbolInst partOf: ?cridInst .
            }
            union {
                ?cridInst hasPart: ?symbolInst .
            }
        }
        ?personInst a human: .
        ?roleInst a patientRole: .
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
    graph turboOntGraph: {
        ?cridType rdfs:subClassOf* crid: .
        ?symbolType rdfs:subClassOf* symbol: .
    }
    graph expandedGraph: {
        ?cridInst a ?cridType ;
                  denotes: ?personInst .
        ?symbolInst a ?symbolType ;
                    hasRepresentation: "00002c66-a365-4e88-8e80-d52bcad4869e"
        {
            {
                ?symbolInst partOf: ?cridInst .
            }
            union {
                ?cridInst hasPart: ?symbolInst .
            }
        }
        ?personInst a human: .
        ?roleInst a patientRole: ;
            <http://purl.obolibrary.org/obo/BFO_0000054> ?hce .
        ?hce a hce: .    
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
    graph turboOntGraph: {
        ?cridType rdfs:subClassOf* crid: .
        ?symbolType rdfs:subClassOf* symbol: .
    }
    graph expandedGraph: {
        ?cridInst a ?cridType ;
                  denotes: ?personInst .
        ?symbolInst a ?symbolType ;
                    hasRepresentation: "00002c66-a365-4e88-8e80-d52bcad4869e"
        {
            {
                ?symbolInst partOf: ?cridInst .
            }
            union {
                ?cridInst hasPart: ?symbolInst .
            }
        }
        ?personInst a human: ;
            <http://purl.obolibrary.org/obo/RO_0000056> ?hce .
        ?roleInst a patientRole: ;
            <http://purl.obolibrary.org/obo/BFO_0000054> ?hce .
        ?hce a hce: .    
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
    graph turboOntGraph: {
        ?EcridType rdfs:subClassOf* crid: .
        # ?EsymbolType rdfs:subClassOf* symbol: .
    }
    graph expandedGraph: {
        ?EcridInst a ?EcridType ;
                  denotes: ?encInst .
        ?encInst a hce: . } } 
 """
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  // start writing the tests with the assumption that RDFS+ reasoning has been completed
  // start using prefix and alias block
  // differentiate patient and encounter crids and parts

  //whoa, takes ~ 30 seconds!

  test("WITH PREFIXES and aliases, symb part of crid") {
    val myAsk = """
ask where {

    graph expandedGraph: {
        ?pCridInst a ?pCridType ;
                   denotes: ?personInst .
        ?personInst a human: ;
                    participatesIn: ?encInst .
        ?pSymbolInst a ?pSymbolType ;
                     partOf: ?pCridInst ;
                     hasRepresentation: "00002c66-a365-4e88-8e80-d52bcad4869e" .
        ?encInst a hce: .
        ?eCridInst a ?eCridType ;
                   denotes: ?encInst .
    }
        graph turboOntGraph: {
        ?pCridType rdfs:subClassOf* crid: .
        ?pSymbolType rdfs:subClassOf* symbol: .
        ?eCridType rdfs:subClassOf* crid: .
#        ?eSymbolType rdfs:subClassOf* symbol: .
    }
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  test("WITH PREFIXES and aliases, crid has symbol part with two expected representative values") {
    val myAsk = """
ask  where {
    graph expandedGraph: {
        values ?eSv {
            "71ed95de-8d50-4cfe-8aaa-a4d26d859215" 
            "d346249f-c84a-442e-9c73-6315838d543c" 
        }
        ?pSymbolInst 
            hasRepresentation: 
                "00002c66-a365-4e88-8e80-d52bcad4869e" ;
                                                       a ?pSymbolType ;
                                                       partOf: ?pCridInst .
        ?eSymbolInst hasRepresentation: ?eSv  ;
                     a ?eSymbolType ;
                     partOf: ?eCridInst .
        ?pCridInst a ?pCridType ;
                   denotes: ?personInst ;
                   hasPart: ?pSymbolInst .
        ?personInst a human: ;
                    participatesIn: ?encInst .
        ?encInst a hce: .
        ?eCridInst a ?eCridType ;
                   denotes: ?encInst .
    } 
    graph turboOntGraph: {
        ?pCridType rdfs:subClassOf* crid: .
        ?pSymbolType rdfs:subClassOf* symbol: .
        ?eCridType rdfs:subClassOf* crid: .
        ?eSymbolType rdfs:subClassOf* symbol: .
    } 
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  test("WITH PREFIXES and aliases, encounter started by some kind of process boundary") {
    val myAsk = """
ask  
where {
    ?pSymbolInst 
        hasRepresentation: 
            "00002c66-a365-4e88-8e80-d52bcad4869e" ;
                                                   a ?pSymbolType ;
                                                   partOf: ?pCridInst .
    ?pCridInst a ?pCridType ;
               denotes: ?personInst ;
               hasPart: ?pSymbolInst .
    ?personInst a human: ;
                participatesIn: ?encInst .
    ?encInst a hce: .
    ?hceStart starts: ?encInst ;
              a ?hceStartType .
    graph turboOntGraph: {
        ?pCridType rdfs:subClassOf* crid: .
        ?pSymbolType rdfs:subClassOf* symbol: .
        ?hceStartType rdfs:subClassOf* procBound: .  
    } 
}"""
    val fxnRes = arbitraryAsk(myAsk)
    assert(fxnRes)
  }

  //  tuplesResult.close

}

