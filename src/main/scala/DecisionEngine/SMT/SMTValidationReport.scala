package DecisionEngine.SMT

/**
  * Created by apinter on 24/08/2017.
  */
class SMTValidationReport extends SMTReport{

  def classificationError = anomalyPercentage
  def sensitivity = normalCount.toDouble / (normalCount + anomalyCount)
  def specificity = 0.0
  def precision = 1.0
  def recall = sensitivity
}
