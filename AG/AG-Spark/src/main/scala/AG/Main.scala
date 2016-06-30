package AG

import AG.DistanceFunctions.{euclideanDistance, mahalanobisDistance}
import AG.Utils.{findCovariance, parseData}
import breeze.linalg.{DenseMatrix, inv}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.{SparkConf, SparkContext}

object Main {

  case class Patient(sex: String, age: Int, setting: String, data: Array[Double])

  def main(args: Array[String]) {

    val delimiterKeyword = "PATIENT"
    //val filePath = "/Users/antonradice/Desktop/APAnalysis/AG/AG-Java/data.out.ch0" // original data
    val filePath = "/Users/antonradice/Desktop/APAnalysis/AG/data/sample.csv" // 5 patient sample
    val conf = new Configuration
    conf.set("textinputformat.record.delimiter", delimiterKeyword)
    val sc = new SparkContext("local", "main", new SparkConf())

    val rawData = sc.newAPIHadoopFile(filePath, classOf[TextInputFormat], classOf[LongWritable], classOf[Text], conf).map(x => x._2.toString)
    val firstLine = rawData.first // first line is empty
    val parsedData = rawData.filter(x => x != firstLine).map(parseData).zipWithIndex().map{case(v,i)=>(i,v)}.cache()

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
    var euclDistVect = Vector.empty[(Double,Int)]
    (0 until n).foreach { column =>
      euclDistVect :+= (euclideanDistance(parsedData.lookup(0).head, parsedData.lookup(column).head), parsedData.lookup(column).head.age)
    }
    euclDistVect.sorted.foreach{ x =>
      print(x._2 + " - " + x._1 + " ; ")
    }

    //testing inverse (results agree with numpy inverse function)
    val m = DenseMatrix((1.0,2.0,3.0), (4.0,5.0,6.0), (7.0,8.0,9.0))
    val mi = inv(m)
    println("rows: " + m.rows + ", cols: " + m.cols)
    println(m)
    println(mi)

    //calculate mahalanobis distance vector for one patient
    var mahalDistVect = Vector.empty[(Double,Int)]
    val covariance = findCovariance(parsedData)
    val inverseCovariance = inv(covariance)
    (0 until n).foreach { column =>
      mahalDistVect :+= (mahalanobisDistance(parsedData.lookup(0).head, parsedData.lookup(column).head, inverseCovariance), parsedData.lookup(column).head.age)
    }
    mahalDistVect.sorted.foreach{ x =>
      print(x._2 + " - " + x._1 + " ; ")
    }

    //calculate mahalanobis distance vector for one patient using broadcast variable
    val firstPatient = sc.broadcast(parsedData.first()._2)
    def calculateMahalanobis(patient: (Long, Patient)): (Double, Int) = {
      val n = patient._2.data.length
      var dist = 0.0
      for(i <- 0 until n) {
        dist += (firstPatient.value.data(i) - patient._2.data(i)) * inverseCovariance(0,i) * (firstPatient.value.data(i) - patient._2.data(i))
      }
      return (Math.sqrt(Math.abs(dist)), patient._2.age)
    }
    val mahalDist = parsedData.map(x => calculateMahalanobis(x))
    mahalDist.sortBy(_._1).foreach{ x =>
      print(x._2 + " - " + x._1 + " ; ")
    }
  }
}
