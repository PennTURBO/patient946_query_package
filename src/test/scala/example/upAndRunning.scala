package example

//import org.eclipse.rdf4j.model.impl._
//import org.eclipse.rdf4j.model.vocabulary.FOAF
//import org.eclipse.rdf4j.model.vocabulary.RDF
//import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
//import org.eclipse.rdf4j.repository.sparql._
import com.typesafe.config.ConfigFactory
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.scalatest.FunSuite

class upAndRunning extends FunSuite {

  var parsedConfig = ConfigFactory.parseResources("defaults.conf")
  var endpointValue = parsedConfig.getString("conf.sparqlEndpoint")
  //  println(endpointValue)

  //  var vf = SimpleValueFactory.getInstance()
  //  var ex = "http://example.org/"
  //  var picasso = vf.createIRI(ex, "Picasso")
  //  var artist = vf.createIRI(ex, "Artist")
  //  var model = new TreeModel()
  //  model.add(picasso, RDF.TYPE, artist)
  //  model.add(picasso, FOAF.FIRST_NAME, vf.createLiteral("Pablo"))
  //
  //  var firstStmt = model.first()
  //  println(firstStmt)

  //    for (statement <- model) {
  //      println(statement(s))
  //    }

  //  val sparqlEndpoint = "http://pennturbo.org:7200/repositories/Mark_production"

  val SparqlRepo = new SPARQLRepository(endpointValue)
  SparqlRepo.initialize()

  val con = SparqlRepo.getConnection()

  //  var queryString = "SELECT ?x ?p ?y WHERE { ?x ?p ?y } limit 30 "
  //  var tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString)
  //  var tuplesResult = tupleQuery.evaluate()
  //
  //  while (tuplesResult.hasNext()) { // iterate over the result
  //    val bindingSet = tuplesResult.next()
  //    val valueOfX = bindingSet.getValue("x")
  //    val valueOfP = bindingSet.getValue("p")
  //    val valueOfY = bindingSet.getValue("y")
  //    val toPrint = valueOfX + " " + valueOfP + " " + valueOfY
  //    println(toPrint)
  //  }

  var expectedGraph = "http://www.itmat.upenn.edu/biobank/expanded"
  var queryString = " ask where { graph <" + expectedGraph + "> { ?s ?p ?o } } "
  var booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString)
  var booleanResult = booleanQuery.evaluate()
  println(booleanResult)

  test("named graph " + expectedGraph + " is present") {
    assert(booleanResult)
  }

  //  tuplesResult.close
  //  booleanResult.close
  con.close
  SparqlRepo.shutDown

}