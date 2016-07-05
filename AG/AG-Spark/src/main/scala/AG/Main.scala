package AG

import AG.DistanceFunctions.{euclideanDistanceMap, mahalanobisDistanceMap}
import AG.Utils.{findCovariance, parseData, timer}
import breeze.linalg.{DenseMatrix, inv}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Main {

  case class Patient(sex: String, age: Int, setting: String, data: Array[Double])

  def main(args: Array[String]) {

    val delimiterKeyword = "PATIENT"
    val filePath = "/Users/antonradice/Desktop/APAnalysis/AG/Data/original_data.csv"
    val conf = new Configuration
    conf.set("textinputformat.record.delimiter", delimiterKeyword)
    val spark_conf = new SparkConf().set("spark.driver.maxResultSize", "4g").set("spark.driver.memory", "6g") // set in spark-conf for local mode
    val sc = new SparkContext("local", "main", spark_conf)

    val rawData = sc.newAPIHadoopFile(filePath, classOf[TextInputFormat], classOf[LongWritable], classOf[Text], conf).map(x => x._2.toString)
    val firstLine = rawData.first // first line is empty
    val parsedData = rawData.filter(x => x != firstLine).map(parseData).zipWithIndex().map { case (v, i) => (i, v) }.cache()

    val n = parsedData.count().toInt // number of patients
    println("Patients count: " + n)

    val firstPatient = sc.broadcast(parsedData.first()._2) // broadcast first patient

    //calculate entire distance matrix
    /*
    var euclDistMatr = Array.empty[Vector[Double]]
    (0 until n).foreach { row =>
      var distVect = Vector.empty[Double]
      (row + 1 until n).foreach { column =>
        distVect :+= euclideanDistanceSeq(parsedData.lookup(row).head, parsedData.lookup(column).head)
      }
      euclDistMatr :+= distVect
    }
    euclDistMatr.foreach(println)
    */

    //calculate euclidean distance vector for one patient
    var euclidDistVect: RDD[(Double, Int)] = null
    timer({euclidDistVect = parsedData.map(patient => euclideanDistanceMap(firstPatient.value, patient))}, "Euclidean distance execution")
    euclidDistVect.collect().sortBy(_._1).foreach{ x =>
      print(x._2 + " - " + x._1 + " ; ")
    }

    //calculate mahalanobis distance vector for one patient
    var mahalDist: RDD[(Double, Int)] = null
    timer({
      var covariance: DenseMatrix[Double] = null
      timer({covariance = findCovariance(parsedData)}, "Covariance execution")
      var inverseCovariance: DenseMatrix[Double] = null
      timer({inverseCovariance = inv(covariance)}, "Inverse covariance execution")
      val inverseCovarianceBroadcast = sc.broadcast(inverseCovariance)
      timer({mahalDist = parsedData.map(patient => {
        mahalanobisDistanceMap(firstPatient.value, patient, inverseCovarianceBroadcast.value)
      })}, "Mahalanobis distance execution")
    }, "Total execution")
    mahalDist.collect().sortBy(_._1).foreach{ x =>
      print(x._2 + " - " + x._1 + " ; ")
    }

  }
}
