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
class MultipleScenarioTest extends Simulation {

      var CreateId = ""
      val conf= ConfigFactory.load(System.getProperty("environment"))
      	val authToken= Map("X-AUTH-TOKEN" -> "${CreateId}") 
			val loginData=csv(conf.getString("members")).circular
			val searchAddressData=ssv(conf.getString("bookingSearchFilteredByAddress")).random
	    //val conf1= java.util.ResourceBundle.getBundle("src/test/resources/valid2.properties")
	    val baseurl=conf.getString("baseURL");
      //	val admin=conf.getString("superAdminUsername");
	    //val password=conf.getString("password")


	object SetTime{
		// this is the anonymous funtion which is going to return the startdate now+ given startMinute and end date now + given endMinute
		val startTime = (startMinute: Int) => LocalDateTime.now().plusMinutes(startMinute).withSecond(0).truncatedTo(ChronoUnit.MICROS).toString()
				val endDate = (endMinute: Int) => LocalDateTime.now().plusMinutes(endMinute).withSecond(0).truncatedTo(ChronoUnit.MICROS).toString()
				//val startTime=LocalDateTime.now().plusMinutes(30).withSecond(0).truncatedTo(ChronoUnit.MICROS).toString()
				//val endDate=LocalDateTime.now().plusMinutes(120).withSecond(0).truncatedTo(ChronoUnit.MICROS).toString()
				
	}

	/**
	 * This is search filtered request which will search for the vehicle for the given users
	 */
	object searchFilterted{
		val searchFilteredRequest=feed(searchAddressData)
		.feed(loginData)
				// This session will call the SetTime object and set the start and end time. The startDate and endDate further utilise in request
				.exec(session => session.set("startDate", SetTime.startTime(Integer.parseInt(session("startTime").as[String]))))
				.exec(session => session.set("endDate", SetTime.startTime(Integer.parseInt(session("endTime").as[String]))))
				.exec(session => session.set("CreateId",CreateId))
				//This message will sent to the server for search vehicle
				.exec(http("searchFiltering with address")
						.post("/bookings/search-filtered")
						.headers(authToken)  
						.header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
						.header(HttpHeaderNames.Accept, HttpHeaderValues.ApplicationJson)		
						.body(StringBody("""
								{
                 "memberLogin":"${username}",
								"passengers": "${passengers}",
								"start": {
								"address": {
								"formattedAddress": "${formattedAddress}",
								"coordinates": {
								"latitude": ${latitude},
								"longitude": ${longitude}
								}
								},
								"date": "${startDate}"
								},
								"end": {
								"address": {
								"formattedAddress": "${formattedAddress}",
								"coordinates": {
								"latitude": ${latitude},
								"longitude": ${longitude}
								}
								},
								"date": "${endDate}"
								}
								}

								""")).asJSON
						)
				pause(10)
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
            CreateId = session("auth_token").as[String].trim
            println("%%%%%%%%%%% ID =====>>>>>>>>>> " + CreateId)     
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
			.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")
      .disableCaching
      
			val loginBackuser= scenario("login").exec(authetication.login)
			val searchVehicleForBooking = scenario("search filetered by user").exec(searchFilterted.searchFilteredRequest)
			
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
			
			
setUp(loginBackuser.inject(nothingFor(2 seconds),atOnceUsers(1)),searchVehicleForBooking.inject(nothingFor(10 seconds),atOnceUsers(1)))
    .protocols(httpConf)




}
