package AG

import AG.Main.{Patient, Record}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Utils {

  def parseData(line: String) = {
    val pieces = line.split('\n')
    val header = pieces(0).trim().split(' ')
    val sex = header(0)
    val age = header(1).toInt
    val setting = header(2)
    var records = Array.empty[Record]
    var highs = Vector.empty[Double]
    var lows = Vector.empty[Double]
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    pieces.drop(1).foreach(x => {
      val row = x.split(';')
      records :+= Record(LocalDateTime.parse(row(0), formatter), row(1).toDouble, row(2).toDouble)
      highs :+= row(1).toDouble
      lows :+= row(2).toDouble
    })
    Patient(sex, age, setting, records, highs, lows)
  }

}
