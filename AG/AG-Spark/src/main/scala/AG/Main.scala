package AG

import AG.DistanceFunctions.mahalanobisDistance
import AG.Utils.{findCovariance, parseData}
import breeze.linalg.inv
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Main {

  case class Patient(sex: String, age: Int, setting: String, data: Array[Double])

  def main(args: Array[String]) {

    val delimiterKeyword = "PATIENT"
    val filePath = "/Users/antonradice/Desktop/APAnalysis/AG/AG-Java/data.out.ch0"
    val conf = new Configuration
    conf.set("textinputformat.record.delimiter", delimiterKeyword)
    val sc = new SparkContext("local", "main", new SparkConf())

    val rawData = sc.newAPIHadoopFile(filePath, classOf[TextInputFormat], classOf[LongWritable], classOf[Text], conf).map(x => x._2.toString)
    val firstLine = rawData.first // first line is empty
    val parsedData = rawData.filter(x => x != firstLine).map(parseData).zipWithIndex().map{case(v,i)=>(i,v)}.cache()
    val measurementData: RDD[Vector] = parsedData.map(x => Vectors.dense(x._2.data))

    val n = parsedData.count().toInt // number of patients

    //calculate entire distance matrix
    /*
    var euclDistMatr = Array.empty[Vector[Double]]
    (0 to n-1).foreach { row =>
      var distVect = Vector.empty[Double]
      (row + 1 to n).foreach { column =>
        distVect :+= euclideanDistance(parsedDataIndexed.lookup(row).head, parsedDataIndexed.lookup(column).head)
      }
      euclDistMatr :+= distVect
    }
    euclDistMatr.foreach(println)
    */

    //calculate euclidean distance vector for one patient
//    var euclDistVect = Vector.empty[Double]
//    (1 to n-1).foreach { column =>
//      euclDistVect :+= euclideanDistance(parsedData.lookup(0).head, parsedData.lookup(column).head)
//    }
//    println(euclDistVect)

    //calculate mahalanobis distance vector for one patient
    val covariance = findCovariance(measurementData)
    val inverseCovariance = inv(covariance)

    covariance.toArray.foreach(println)
    println(covariance.rows, covariance.cols)

    var mahalDistVect = Vector.empty[Double]
    (1 to n-1).foreach { column =>
      mahalDistVect :+= mahalanobisDistance(parsedData.lookup(0).head, parsedData.lookup(column).head, inverseCovariance)
    }
    println(mahalDistVect)
  }

}
