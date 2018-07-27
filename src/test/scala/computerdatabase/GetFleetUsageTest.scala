package computerdatabase

import io.gatling.core.Predef._
import com.typesafe.config._
import io.gatling.http.Predef._
import scala.concurrent.duration._
class GetFleetUsageTest extends Simulation {
  
  var token="";
	    val conf= ConfigFactory.load(System.getProperty("environment"))
			val authToken= Map("X-AUTH-TOKEN" -> "${token}") 
			val loginData=csv(conf.getString("members")).circular
			val searchAddressData=ssv(conf.getString("bookingSearchFilteredByAddress")).random
			val baseurl=conf.getString("baseURL");



	/**
	 * This request will send the get member complete request to the server
	 */
	object getFleetUsage{
		val getFleetUsageRequest=
				feed(loginData)
				.exec(session => session.set("token",token))
				.exec(http("Get the vehicle usage for a super company")
						.get("/companies/${superCompanyId}/fleetUsages")
						.headers(authToken)  
						.header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
						.header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)		

						)
				.pause(10)
	}

	/**
	 * This method authenticate user using the username and password that are coming from csv files
	 */
	object authetication{
		val login=
				exec(session => session.set("admin",conf.getString("superAdminUsername")))
				.exec(session => session.set("password",conf.getString("password")))
				.exec(http("login_request")
						.post("/users/authenticate")
						.body(StringBody("""{"login":"${admin}","password":"${password}" }""")).asJSON
						.check(header("x-auth-token").saveAs("auth_token"))  
						.check(jsonPath("$.id").saveAs("id")))
				.pause(2)

				.exec(session => {
					token = session("auth_token").as[String].trim    
							session}       
						)
	}

	/**
	 * This is the basic configuration for the header and base urls 
	 */
	val httpConf =http
			.baseURL(baseurl) // Here is the root for all relative URLs
			.acceptHeader("text/html,application/xhtml+xml,application/xml,application/json;q=0.9,*/*;q=0.8") // Here are the common headers
			.doNotTrackHeader("1")
			.acceptLanguageHeader("en-US,en;q=0.5")
			.acceptEncodingHeader("gzip, deflate")
			.disableCaching



			val loginBackuser= scenario("login").exec(authetication.login)
			val getAllFleetUsage= scenario("Fleet Usage of a super company").exec(getFleetUsage.getFleetUsageRequest)




			setUp(loginBackuser.inject(nothingFor(2 seconds),atOnceUsers(1)),
					getAllFleetUsage.inject(nothingFor(10 seconds),splitUsers(20) into atOnceUsers(1) separatedBy(10 seconds)
							)).protocols(httpConf)
  
}