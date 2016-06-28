package AG

import java.time.format.DateTimeFormatter

import AG.Main.Patient
import breeze.linalg.DenseMatrix
import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.rdd.RDD

object Utils {

  def timer[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + " nanoseconds, " + (t1 - t0)/ 1000000000.0 + " seconds.")
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
      data :+= row(1).toDouble
      data :+= row(2).toDouble
    })
    Patient(sex, age, setting, data)
  }

  def findCovariance(input: RDD[(Long, Patient)]): DenseMatrix[Double] = {
    val matrix: RDD[Vector] = input.map(x => Vectors.dense(x._2.data))
    val mean = Statistics.colStats(matrix).mean
    val n = matrix.first().size
    var result: DenseMatrix[Double] = DenseMatrix.zeros[Double](n,n)
    matrix.foreach{ row =>
      for(i <- 0 until row.size) {
        for(j <- 0 until row.size) {
          result(i,j) :+= ((row(i) - mean(i))*(row(j) - mean(j)))
        }
      }
    }
    assert(result.rows == result.cols, "Data size not equal.")
    for(i <- 0 until result.rows) {
      for(j <- 0 until result.cols) {
        result(i,j) = result(i,j) / result.cols
      }
    }
    result
  }
}
