package AG

import breeze.linalg.DenseMatrix
import AG.Main.Patient

object DistanceFunctions {

  def euclideanDistance(patient1: Patient, patient2: Patient): Double = {
    assert(patient1.data.length == patient2.data.length, "Data size is not equal")
    val n = patient1.data.length
    var dist = 0.0
    for(i <- 0 until n) {
        dist += Math.pow((patient1.data(i) - patient2.data(i)), 2)
    }
    Math.sqrt(dist)
  }

  def mahalanobisDistance(patient1: Patient, patient2: Patient, inverseCovariance: DenseMatrix[Double]): Double = {
    assert(patient1.data.length == patient2.data.length, "Data size is not equal")
    val n = patient1.data.length
    var dist = 0.0
    for(i <- 0 until n) {
      for(j <- 0 until n) {
        dist += (patient1.data(i) - patient2.data(i)) * inverseCovariance(0,i) * (patient1.data(i) - patient2.data(i))
      }
    }
    return Math.sqrt(dist)
  }

}
