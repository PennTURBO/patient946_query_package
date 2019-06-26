package example

import org.scalatest.FunSuite
import com.typesafe.config.ConfigFactory
import org.eclipse.rdf4j.model.impl._
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.FOAF
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.sparql._
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository

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

  var con = SparqlRepo.getConnection()

  var queryString = "SELECT ?x ?p ?y WHERE { ?x ?p ?y } limit 30 "
  var tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString)
  var result = tupleQuery.evaluate()

  while (result.hasNext()) { // iterate over the result
    val bindingSet = result.next()
    val valueOfX = bindingSet.getValue("x")
    val valueOfP = bindingSet.getValue("p")
    val valueOfY = bindingSet.getValue("y")
    val toPrint = valueOfX + " " + valueOfP + " " + valueOfY
    println(toPrint)
  }

  queryString = " ask where { graph <http://www.itmat.upenn.edu/biobank/expanded> { ?s ?p ?o } } "
  tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString)
  result = tupleQuery.evaluate()
  print(result)

  result.close
  con.close
  SparqlRepo.shutDown

  test("1 plus 2 equals 3") {
    val x = 1 + 2
    assert(x == 3)
  }

}