package Data

import java.io.{File, FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream, Serializable}
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
        val fos = try {
          new FileOutputStream(_target)
        } catch {
          case t: Throwable => throw new DataException(t.getClass.getName + " thrown during model serialisation", t)
        }
        val oos = try {
          new ObjectOutputStream(fos)
        } catch {
          case t: Throwable => throw new DataException(t.getClass.getName + " thrown during model serialisation", t)
        }
        try {
          oos.writeObject(x)
          oos.close
          true
        } catch {
          case t: Throwable => throw new DataException(t.getClass.getName + " thrown during model serialisation", t)
        } finally {
          fos.close
          oos.close
        }
      }
    }
  }

  @throws(classOf[DataException])
  def deserialise(_source: File): Option[DataModel] = {
    if (!_source.exists || !_source.isFile) return None
    val fis = try {
      new FileInputStream(_source)
    } catch {
      case t: Throwable => throw new DataException(t.getClass.getName + " thrown during model deserialisation", t)
    }
    val ois = try {
      new ObjectInputStreamWithCustomClassLoader(fis)
    } catch {
      case t: Throwable => throw new DataException(t.getClass.getName + " thrown during model deserialisation", t)
    }
    try {
      val newModel = ois.readObject.asInstanceOf[Serializable]
      ois.close
      store(newModel)
      Some(this)
    } catch {
      case t: Throwable => throw new DataException(t.getClass.getName + " thrown during model deserialisation", t)
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