package TreeTest

/**
  * Created by Case on 02/07/2017.
  */
abstract class SMT(MAX_DEPTH: Int, MAX_PHI: Int, EXPAND_SEQUENCE_COUNT: Int)

  case class Node(val MAX_DEPTH: Int, val MAX_PHI: Int, val EXPAND_SEQUENCE_COUNT: Int, val key: Int,  var children: List[SMT]) extends SMT(MAX_DEPTH, MAX_PHI, EXPAND_SEQUENCE_COUNT){}
  //case class Wildcard(val MAX_DEPTH: Int, val MAX_PHI: Int, val EXPAND_SEQUENCE_COUNT: Int, val phi: Int) extends SMT(MAX_DEPTH, MAX_PHI, EXPAND_SEQUENCE_COUNT){}
  case class SequenceList(val MAX_DEPTH: Int, val MAX_PHI: Int, val EXPAND_SEQUENCE_COUNT: Int, list: List[Sequence]) extends SMT(MAX_DEPTH, MAX_PHI, EXPAND_SEQUENCE_COUNT){}




/*
abstract class Tree
case class Branch(left: Tree, right: Tree) extends Tree
case class Leaf(x: Int) extends Tree*/
