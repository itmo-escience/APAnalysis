package AG

import AG.Main.Patient

object DistanceFunctions {

  def euclideanDistance(patient1: Patient, patient2: Patient): Double = {
    
    val x_h = patient1.highs
    val x_l = patient1.lows
    val y_h = patient2.highs
    val y_l = patient2.lows

    val n = x_h.length
    var dist = 0.0

    (1 to n).foreach { i =>
      dist += Math.pow((x_h(i) - y_h(i)), 2) + Math.pow((x_l(i) - y_l(i)), 2)
    }

    Math.sqrt(dist)

  }

}

