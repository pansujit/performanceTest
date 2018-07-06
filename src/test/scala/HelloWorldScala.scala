import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


object HelloWorldScala {
  def main(args: Array[String]) {
    println(LocalDateTime.now().plusMinutes(30).withSecond(0).truncatedTo(ChronoUnit.MICROS).toString())
  }
}