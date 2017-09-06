package Data

import java.io.{File, FileInputStream, FileNotFoundException, FileOutputStream, IOException, InvalidClassException, NotSerializableException, ObjectInputStream, ObjectOutputStream, Serializable, StreamCorruptedException}
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
        val fos = try{ new FileOutputStream(_target) } catch{
          case t: Throwable => throw new DataException(t.getClass.getName + " thrown during model serialisation", t)
        /*  case fnfe: FileNotFoundException => throw new DataException("FileNotFoundException thrown during model serialisation.", fnfe)
          case se: SecurityException => throw new DataException("SecurityException thrown during model serialisation.", se)
          case npe: NullPointerException => throw new DataException("NullPointerException thrown during model serialisation.", npe)*/
        }
        val oos = try{ new ObjectOutputStream(fos) } catch{
          case t: Throwable => throw new DataException(t.getClass.getName + " thrown during model serialisation", t)
          /*case ioe: IOException => throw new DataException("IOException thrown during model serialisation.", ioe)
          case se: SecurityException => throw new DataException("SecurityException thrown during model serialisation.", se)
          case npe: NullPointerException => throw new DataException("NullPointerException thrown during model serialisation.", npe)*/
        }
        try {
          oos.writeObject(x)
          oos.close
          true
        } catch {
          case t: Throwable => throw new DataException(t.getClass.getName + " thrown during model serialisation", t)
          /*case ice: InvalidClassException => throw new DataException("InvalidClassException thrown during model serialisation.", ice)
          case nse: NotSerializableException => throw new DataException("NotSerializableException thrown during model serialisation.", nse)
          case ioe: IOException => throw new DataException("IOException thrown during model serialisation.", ioe)*/
        } finally {
          fos.close
          oos.close
        }
      }
    }
  }

  @throws(classOf[DataException])
  def deserialise(_source: File): Option[DataModel] = {
    if(!_source.exists || !_source.isFile) return None
    val fis = try{ new FileInputStream(_source) }catch{
      case t: Throwable => throw new DataException(t.getClass.getName + " thrown during model deserialisation", t)
      /*case fnfe: FileNotFoundException => throw new DataException("FileNotFoundException thrown during model deserialisation",  fnfe)
      case se: SecurityException => throw new DataException("SecurityException thrown during model deserialisation", se)*/
    }
    val ois = try{ new ObjectInputStreamWithCustomClassLoader(fis) }catch{
      case t: Throwable => throw new DataException(t.getClass.getName + " thrown during model deserialisation", t)
      /*case sce: StreamCorruptedException =>  throw new DataException("StreamCorruptedException thrown during model deserialisation.", sce)
      case ioe: IOException => throw new DataException("IOException thrown during model deserialisation.", ioe)
      case se: SecurityException => throw new DataException("SecurityException thrown during model deserialisation", se)
      case npe: NullPointerException => throw new DataException("NullPointerException thrown during model deserialisation", npe)*/
    }
    try {
      val newModel = ois.readObject.asInstanceOf[Serializable]
      ois.close
      store(newModel)
      Some(this)
    } catch {
      case t: Throwable => throw new DataException(t.getClass.getName + " thrown during model deserialisation", t)
      /*case cnfe: ClassNotFoundException => throw new DataException("ClassNotFoundException thrown during model deserialisation.", cnfe)
      case ice: InvalidClassException => throw new DataException("InvalidClassException thrown during model deserialisation.", ice)
      case sce: StreamCorruptedException => throw new DataException("StreamCorruptedException thrown during model deserialisation.", sce)
      case ioe: IOException => throw new DataException("IOException thrown during model deserialisation.", ioe)*/
    } finally {
      fis.close
      ois.close
    }
  }

  private class ObjectInputStreamWithCustomClassLoader(
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