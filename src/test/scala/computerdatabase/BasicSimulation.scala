package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.response.Response
import io.gatling.http.response.ResponseBody
import io.gatling.recorder.scenario.ResponseBody

class BasicSimulation extends Simulation {

	val httpConf = http
			.baseURL("http://computer-database.gatling.io") // Here is the root for all relative URLs
			.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
			.acceptEncodingHeader("gzip, deflate")
			.acceptLanguageHeader("en-US,en;q=0.5")
			.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")


	object Browse{
		val browse= repeat(5,"i"){
			exec(http("browse Steps ${i}")
					.get("/computers?p=${i}"))
			.pause(670 milliseconds)
		}
	}
	object Search{

	  
	  		val feeder=csv("search.csv").random
					val search=exec(http("request_1")
							.get("/"))
					.pause(5)
					.feed(feeder)
					.exec(http("request_${searchCriterion}")
							.get("/computers?f=${searchCriterion}")
					.check(css("a:contains('${searchComputerName}')","href").saveAs("computerURL"))	
					)
					.pause(5)
					
					.exec(http("request_${computerURL}")
					.get("${computerURL}")
					.check(status.is(200))
					
					  
					)
					.pause(1)
	}
		
	object Edit{
		val edit= exec(http("edit") // Here's an example of a POST request
				.post("/computers")
				.formParam("""name""", """Beautiful Computer""") // Note the triple double quotes: used in Scala for protecting a whole chain of characters (no need for backslash)
				.formParam("""introduced""", """2012-05-30""")
				.formParam("""discontinued""", """""")
				.formParam("""company""", """37"""))
	} 

	val users= scenario("users").exec(Browse.browse,Search.search)
	val admins= scenario("admins").exec(Browse.browse,Search.search,Edit.edit)

			setUp(
					users.inject(rampUsers(10) over (10 seconds)),
					admins.inject(rampUsers(10) over (10 seconds))
					).protocols(httpConf)
					.assertions(
					global.responseTime.max.lt(50),
					global.successfulRequests.percent.gt(95)
					)
}
