import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

object Engine extends App {

	val props = new GatlingPropertiesBuilder()
		.dataDirectory(IDEPathHelper.dataDirectory.toString)
		.resultsDirectory(IDEPathHelper.resultsDirectory.toString)
		.bodiesDirectory(IDEPathHelper.bodiesDirectory.toString)
		.binariesDirectory(IDEPathHelper.mavenBinariesDirectory.toString)
	Gatling.fromMap(props.build)
	props.runDescription("n/A")
	
	//props.runDescription("N/A") // do not ask for a descr. upon run
  props.simulationClass("computerdatabase/GetCompanies") // do not ask for a simulation to run upon run
}