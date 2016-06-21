package AG

import java.time.LocalDateTime

import AG.Utils.parseData
import AG.DistanceFunctions.euclideanDistance
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.{SparkConf, SparkContext}

object Main {

  case class Record(date: LocalDateTime, high: Double, low: Double)
  case class Patient(sex: String, age: Int, setting: String, records: Array[Record], highs: Vector[Double], lows: Vector[Double])

  def main(args: Array[String]) {

    val delimiterKeyword = "PATIENT"
    val conf = new Configuration
    conf.set("textinputformat.record.delimiter", delimiterKeyword)
    val sc = new SparkContext("local", "main", new SparkConf())

    val rawData = sc.newAPIHadoopFile("/Users/antonradice/Desktop/AG/data.out.ch0", classOf[TextInputFormat], classOf[LongWritable], classOf[Text], conf).map(x => x._2.toString)
    val firstLine = rawData.first //first line is empty
    val parsedData = rawData.filter(x => x != firstLine).map(parseData)
    val parsedDataIndexed = parsedData.zipWithIndex().map{case (k,v) => (v,k)}.cache()

    val n = parsedData.count().toInt
    var euclDistMatr = Array.empty[Vector[Double]]

    (1 to n-1).foreach { row =>
      var distVect = Vector.empty[Double]
      (row + 1 to n).foreach { column =>
        println(parsedDataIndexed.lookup(row).toArray, parsedDataIndexed.lookup(column).toArray)
        distVect :+= euclideanDistance(parsedDataIndexed.lookup(row), parsedDataIndexed.lookup(column).toArray)
      }
      euclDistMatr :+= distVect
    }
  }

}
