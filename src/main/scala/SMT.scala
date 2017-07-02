package TreeTest

/**
  * Created by Case on 02/07/2017.
  */
abstract class SMT(val MAX_DEPTH: Int, val MAX_PHI: Int, val EXPAND_SEQUENCE_COUNT: Int){

  case class Node()
  case class Wildcard()
  case class SequenceList(list: List[Sequence])
}



/*
abstract class Tree
case class Branch(left: Tree, right: Tree) extends Tree
case class Leaf(x: Int) extends Tree*/
