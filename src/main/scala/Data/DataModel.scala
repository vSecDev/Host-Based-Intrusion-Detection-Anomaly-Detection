package Data

import java.io.{File, FileInputStream, FileOutputStream, IOException, InvalidClassException, NotSerializableException, ObjectInputStream, ObjectOutputStream, Serializable, StreamCorruptedException}
/**
  * Created by apinter on 08/08/2017.
  */
class DataModel {

  var model: Option[Serializable] = None

  def store(_model: Serializable): Unit = model = Some(_model)

  def retrieve(): Option[Serializable] = model

  @throws(classOf[DataException])
  def serialise(_target: File): Boolean = {
    retrieve() match {
      case None => false
      case Some(x) => {
        val fos = new FileOutputStream(_target)
        val oos = new ObjectOutputStream(fos)
        try {
          oos.writeObject(x)
          oos.close
          true
        } catch {
          case ice: InvalidClassException => throw new DataException("InvalidClassException thrown during model serialisation.", ice)
          case nse: NotSerializableException => throw new DataException("NotSerializableException thrown during model serialisation.", nse)
          case ioe: IOException => throw new DataException("IOException thrown during model serialisation.", ioe)
        } finally {
          fos.close
          oos.close
        }
      }
    }
  }

  def deserialise(_source: File): Option[DataModel] = {
    if(!_source.exists || !_source.isFile) return None
    val fis = new FileInputStream(_source)
    val ois = new ObjectInputStreamWithCustomClassLoader(fis)
    try {
      val newModel = ois.readObject.asInstanceOf[Serializable]
      ois.close
      //val res = new DataModel
      //res.store(model)
      store(newModel)
      //Some(res)
      Some(this)
      //Some(smt.asInstanceOf[SMT[Int, Int]])
    } catch {
      case cnfe: ClassNotFoundException => throw new DataException("ClassNotFoundException thrown during model deserialisation.", cnfe)
      case ice: InvalidClassException => throw new DataException("InvalidClassException thrown during model deserialisation.", ice)
      case sce: StreamCorruptedException => throw new DataException("StreamCorruptedException thrown during model deserialisation.", sce)
      case ioe: IOException => throw new DataException("IOException thrown during model deserialisation.", ioe)
    } finally {
      fis.close
      ois.close
    }
  }


  class ObjectInputStreamWithCustomClassLoader(
                                                fileInputStream: FileInputStream
                                              ) extends ObjectInputStream(fileInputStream) {
    override def resolveClass(desc: java.io.ObjectStreamClass): Class[_] = {
      try {
        Class.forName(desc.getName, false, getClass.getClassLoader)
      }
      catch {
        case ex: ClassNotFoundException => super.resolveClass(desc)
      }
    }
  }
}
/*
case class SMTDataModel extends DataModel {
  override type T = String
  override var data: Option[String] = _
  override def store(_data: String): Unit = {data = Some(_data)}
  override def retrieve(): Option[String] = data
}*/
