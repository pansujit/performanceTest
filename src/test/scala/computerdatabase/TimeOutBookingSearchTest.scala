package computerdatabase

import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import io.gatling.core.Predef._
import com.typesafe.config._
import scala.concurrent.duration._
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
class TimeOutBookingSearchTest extends Simulation {

	val conf= ConfigFactory.load(System.getProperty("environment"))
	var baseURI=conf.getString("baseURL"); 
	
	val searchAddressData=ssv(conf.getString("timeOutSearchBooking")).circular
	var xAUTHTOKEN=""
	//var token="";
	val authToken= Map("X-AUTH-TOKEN" -> "${xAUTHTOKEN}") 

	object authentication{
		val login=
				exec(session => session.set("admin",conf.getString("superAdminUsername")))
				.exec(session => session.set("password",conf.getString("password")))
				.exec(http("login_request")
						.post("/users/authenticate")
						.body(StringBody("""{"login":"${admin}","password":"${password}" }""")).asJSON
						.check(header("x-auth-token").saveAs("auth_token"))  
						.check(jsonPath("$.id").saveAs("id"))
						)
				.pause(2)

				.exec(session => {
					xAUTHTOKEN = session("auth_token").as[String].trim
							println("%%%%%%%%%%% ID =====>>>>>>>>>> " + xAUTHTOKEN)     
							session}       
						)
}
	object SetTime{
		// this is the anonymous funtion which is going to return the startdate now+ given startMinute and end date now + given endMinute
		val startTime = (startdate:Int,startMinute:Int ) => LocalDateTime.now().plusDays(startdate).plusMinutes(startMinute).withSecond(0).truncatedTo(ChronoUnit.MICROS).toString()
				val endDate = (enddate:Int,endMinute: Int) => LocalDateTime.now().plusDays(enddate).plusMinutes(endMinute).withSecond(0).truncatedTo(ChronoUnit.MICROS).toString()
				//val startTime=LocalDateTime.now().plusMinutes(30).withSecond(0).truncatedTo(ChronoUnit.MICROS).toString()
				//val endDate=LocalDateTime.now().plusMinutes(120).withSecond(0).truncatedTo(ChronoUnit.MICROS).toString()
				
	}

/**
	 * This is search filtered request which will search for the vehicle for the given users
	 */
	object searchFilterted{
		val searchFilteredRequest=feed(searchAddressData)
				// This session will call the SetTime object and set the start and end time. The startDate and endDate further utilise in request
				.exec(session => session.set("startDate", SetTime.startTime(Integer.parseInt(session("startDate").as[String]),Integer.parseInt(session("startTime").as[String]))))
				.exec(session => session.set("endDate", SetTime.startTime(Integer.parseInt(session("endDate").as[String]),Integer.parseInt(session("endTime").as[String]))))
				.exec(session => session.set("xAUTHTOKEN",xAUTHTOKEN))
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
				.pause(10)
	}
	
		object searchFilterted1{
		val searchFilteredRequest=feed(searchAddressData)
				// This session will call the SetTime object and set the start and end time. The startDate and endDate further utilise in request
				.exec(session => session.set("startDate", SetTime.startTime(Integer.parseInt(session("startDate").as[String]),Integer.parseInt(session("startTime").as[String]))))
				.exec(session => session.set("endDate", SetTime.startTime(Integer.parseInt(session("endDate").as[String]),Integer.parseInt(session("endTime").as[String]))))
				.exec(session => session.set("xAUTHTOKEN",xAUTHTOKEN))
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
				.pause(10)
	}
	

	val httpConf= http
			.baseURL(baseURI)
			.acceptHeader("application/json")
			.contentTypeHeader("application/json")
			.doNotTrackHeader("1")
			.disableCaching

			val loginBackuser= scenario("login").exec(authentication.login)
			val searchVehicleForBooking = scenario("search filetered by user").exec(searchFilterted.searchFilteredRequest)
			

			setUp(loginBackuser.inject(nothingFor(2 seconds),atOnceUsers(1)),
					searchVehicleForBooking.inject(
							nothingFor(10 seconds),splitUsers(32) into atOnceUsers(1) separatedBy(35 seconds)
							)).protocols(httpConf)

}