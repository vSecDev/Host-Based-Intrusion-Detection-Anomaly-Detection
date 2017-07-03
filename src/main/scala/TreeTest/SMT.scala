package TreeTest

/**
  * Created by Case on 02/07/2017.
  */
abstract class SMT[A](MAX_DEPTH: Int, MAX_PHI: Int, EXPAND_SEQUENCE_COUNT: Int)

  /*case class Node(val MAX_DEPTH: Int, val MAX_PHI: Int, val EXPAND_SEQUENCE_COUNT: Int, val key: Int, val input: List[Int], var prediction: Int, var children: List[SMT]) extends SMT(MAX_DEPTH, MAX_PHI, EXPAND_SEQUENCE_COUNT){


    /*val key: Int
    var children: List[SMT]

    def this()

    def getProbability(input: Int)*/
    override def toString: String = "\n00000000\nNODE\n-MAX_DEPTH: " + MAX_DEPTH + "\n-MAX_PHI: " + MAX_PHI + "\n-SPLIT: " + EXPAND_SEQUENCE_COUNT + "\n-key: " + key + "\n-children: \n      " + children.toString() + "\n--\n00000000"


  }*/
  //case class Wildcard(val MAX_DEPTH: Int, val MAX_PHI: Int, val EXPAND_SEQUENCE_COUNT: Int, val phi: Int) extends SMT(MAX_DEPTH, MAX_PHI, EXPAND_SEQUENCE_COUNT){}
  case class SequenceList[A](val MAX_DEPTH: Int, val MAX_PHI: Int, val EXPAND_SEQUENCE_COUNT: Int, list: List[Sequence[A]]) extends SMT(MAX_DEPTH, MAX_PHI, EXPAND_SEQUENCE_COUNT){}




/*
abstract class Tree
case class Branch(left: Tree, right: Tree) extends Tree
case class Leaf(x: Int) extends Tree*/
