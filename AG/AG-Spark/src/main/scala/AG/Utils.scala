package AG

import AG.Main.Patient
import breeze.linalg.DenseMatrix
import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.mllib.stat.Statistics
import java.time.format.DateTimeFormatter
import org.apache.spark.rdd.RDD

import scala.reflect.io.File

object Utils {

  def timer[R](block: => R, message: String): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    val elapsedTimeNanoSeconds = (t1 - t0)
    val elapsedTimeSeconds = (t1 - t0)/ 1000000000.0
    val output = message + " elapsed time: " + elapsedTimeNanoSeconds + " nanoseconds, " + elapsedTimeSeconds + " seconds."
    println(output)
    File("D:/wspace/ExperimentalResults/result.txt").appendAll(output + "\n")
    result
  }

  def parseData(line: String) = {
    val pieces = line.split('\n')
    val header = pieces(0).trim().split(' ')
    val sex = header(0)
    val age = header(1).toInt
    val setting = header(2)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    var data = Array.empty[Double]
    pieces.drop(1).foreach(x => {
      val row = x.split(';')
      data :+= row(1).toDouble // high bp measurement
      data :+= row(2).toDouble // low bp measurement
    })
    Patient(sex, age, setting, data)
  }

  def findCovariance(input: RDD[(Long, Patient)]): DenseMatrix[Double] = {
    val patientMatrix: RDD[Vector] = input.map(x => Vectors.dense(x._2.data))
    val mean = Statistics.colStats(patientMatrix).mean
    val n = patientMatrix.first().size //dimension of our measurement data

//    def toIndividualCovariance(patient: Vector): DenseMatrix[Double] = {
//      var tmpResult: DenseMatrix[Double] = DenseMatrix.zeros[Double](n,n)
//      for(i <- 0 until n) {
//        for(j <- 0 until n) {
//          tmpResult(i,j) += ((patient(i) - mean(i))*(patient(j) - mean(j)))
//        }
//      }
//      tmpResult
//    }

    def toPartitionCovariance(patients: Iterator[Vector]):Iterator[DenseMatrix[Double]] = {
      val tmpResult: DenseMatrix[Double] = DenseMatrix.zeros[Double](n,n)
      for( patient <- patients) {
        for(i <- 0 until n) {
          for(j <- 0 until n) {
            tmpResult(i,j) += ((patient(i) - mean(i))*(patient(j) - mean(j)))
          }
        }
      }
      Iterator(tmpResult)
    }

    def toSummedCovariance(individualMatrix1: DenseMatrix[Double], individualMatrix2: DenseMatrix[Double]): DenseMatrix[Double] = {
      for(i <- 0 until n) {
        for(j <- 0 until n) {
          individualMatrix1(i,j) +=  individualMatrix2(i,j)
        }
      }
      individualMatrix1
    }
//    val finalResult = patientMatrix.map(toIndividualCovariance).reduce(toSummedCovariance).mapPairs({ case ((row, col), value) => { value / n }})
    val finalResult = patientMatrix.mapPartitions(toPartitionCovariance).reduce(toSummedCovariance).mapPairs({ case ((row, col), value) => { value / n }})
    finalResult
  }

}
