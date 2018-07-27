/**
 * Copyright 2011-2018 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import com.typesafe.config._
import io.gatling.core.structure.PopulationBuilder

class GetMemberTest extends Simulation {
      var token="";
			val conf= ConfigFactory.load(System.getProperty("environment"))
			val authToken= Map("X-AUTH-TOKEN" -> "${token}") 
			val loginData=csv(conf.getString("members")).circular
			val searchAddressData=ssv(conf.getString("bookingSearchFilteredByAddress")).random
			val baseurl=conf.getString("baseURL");



	/**
	 * This request will send the get member complete request to the server
	 */
	object getMember{
		val getMemberRequest=
				feed(loginData)
				.exec(session => session.set("token",token))
				.exec(http("Get single member")
						.get("/members/${id}/complete")
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
			val getMemberDetails = scenario("getMemberTest").exec(getMember.getMemberRequest)

			//setUp(scn.inject(constantUsersPerSec(10) during (1 minutes)).protocols(httpConf))


			/*setUp(
			    scn.inject(
			        constantUsersPerSec(10) during (1 minutes)).throttle(
  reachRps(20) in (10 seconds),
  holdFor(1 minute),
  jumpToRps(30),
  holdFor(15 minutes)).protocols(httpConf))*/

			//setUp(scn.inject(    
			//nothingFor(4 seconds), // 1
			//atOnceUsers(1), // 2
			//rampUsers(30) over (10 seconds), // 3
			//constantUsersPerSec(1) during (2 seconds), // 4
			//constantUsersPerSec(20) during (15 seconds) randomized, // 5
			//rampUsersPerSec(1) to 10 during (10 minutes), // 6
			//rampUsersPerSec(3) to 10 during (10 minutes) randomized, // 7
			//splitUsers(10) into (rampUsers(10) over (200 seconds)) separatedBy (30 seconds), // 8
			// splitUsers(1000) into (rampUsers(10) over (10 seconds)) separatedBy atOnceUsers(30), // 9
			//heavisideUsers(20) over (10 seconds)
			//).protocols(httpConf))

			// This is the benchmark to test new release		
/*setUp(loginBackuser.inject(nothingFor(2 seconds),atOnceUsers(1)),
    searchVehicleForBooking.inject(nothingFor(10 seconds),constantUsersPerSec(5) during (1 seconds),nothingFor(2 seconds)
    ,constantUsersPerSec(3) during (1 seconds),nothingFor(2 seconds),
    splitUsers(20) into (rampUsers(2) over (20 seconds)) separatedBy (2 seconds),nothingFor(2 seconds),heavisideUsers(30) over (30 seconds)
    )).protocols(httpConf)*/

			// This is the benchmark to test new release		
/*setUp(loginBackuser.inject(nothingFor(2 seconds),atOnceUsers(1)),
    searchVehicleForBooking.inject(nothingFor(10 seconds),constantUsersPerSec(5) during (60 seconds),nothingFor(5 seconds)
    ,constantUsersPerSec(10) during (30 seconds),nothingFor(5 seconds),
   heavisideUsers(30) over (10 seconds),nothingFor(2 seconds)
    )).protocols(httpConf)*/

	/*setUp(loginBackuser.inject(nothingFor(2 seconds),atOnceUsers(1)),
    getMemberDetails.inject(nothingFor(5 seconds),atOnceUsers(1)
    )).protocols(httpConf)*/



			setUp(loginBackuser.inject(nothingFor(2 seconds),atOnceUsers(1)),
					getMemberDetails.inject(nothingFor(5 seconds),splitUsers(20) into atOnceUsers(1) separatedBy(10 seconds)
							)).protocols(httpConf)
			/*setUp(loginBackuser.inject(nothingFor(2 seconds),atOnceUsers(1)),
    searchVehicleForBooking.inject(nothingFor(10 seconds),rampUsersPerSec(0.09) to(1) during(10 minutes)
    )).protocols(httpConf)
	setUp(loginBackuser.inject(nothingFor(2 seconds),atOnceUsers(1)),
    searchVehicleForBooking.inject(nothingFor(10 seconds),rampUsersPerSec(0.08) to(1) during(5 minutes)
    )).protocols(httpConf)	*/	

			/*setUp(loginBackuser.inject(nothingFor(2 seconds),atOnceUsers(1)),
    searchVehicleForBooking.inject(nothingFor(10 seconds),constantUsersPerSec(5) during (5 seconds)
    )).protocols(httpConf)	*/
}
