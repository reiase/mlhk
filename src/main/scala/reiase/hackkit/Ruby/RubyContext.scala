package reiase.hackkit.Ruby

import better.files.Cmds._
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.SparkContext
import org.jruby.RubyInstanceConfig.CompileMode
import org.jruby.embed.{LocalContextScope, PathType, ScriptingContainer}
import org.jruby.{RubyArray, RubyMethod}

import scala.collection.JavaConversions._
import scala.collection.immutable
import scala.language.dynamics
import scala.reflect.ClassTag

/**
  * Created by reiase on 10/14/16.
  */

object RubyConfig extends LazyLogging{
  var source_ : String = null
  var arguments_ : Array[String] = null

  def source = if (source_ != null) source_ else (jar/"INIT").lines.head
  def arguments = if (arguments_ != null) arguments_ else (jar/"INIT").lines.tail.toArray

  def jar = (pwd / "tmpcode.jar").unzip()

  def setInit(src: String, args: Array[String]) = {
    source_ = src
    arguments_ = args
  }

  def setSparkInit(src: String, args: Array[String]) = {
    setInit(src, args)
    logger.info("create ruby config with "
      + src
      + ", "
      + args.toString)
    val code_dirs = pwd.glob("**/RCODE").filter(_.isDirectory)
    val tmpdir = mkdirs(pwd / "tmp_code")
    for (d <- pwd/"RCODE"::code_dirs.toList){
      if (d.exists && d.isDirectory) {
        logger.info("add RCODE from " + d.pathAsString)
        for (f <- d.children)
          cp(f, tmpdir / f.name)
      }
    }
    cp(pwd / src, tmpdir)
    (tmpdir / "INIT").write((source::args.toList) mkString "\n")
    val tmpjar = tmpdir.zipTo(pwd / "tmpcode.jar")

    tmpdir.delete()
    SparkContext.getOrCreate.addJar(tmpjar.pathAsString)
  }
}

object RubyContext extends Dynamic {
  lazy val container = createContainer
  var receiver: Any = null

  def createContainer: ScriptingContainer = {
    System.setProperty("jruby.compile.invokedynamic", "true")
    val container = new ScriptingContainer(LocalContextScope.SINGLETON)
    init(RubyConfig.source, RubyConfig.arguments, container)
    container
  }

  def init(source: String, arguments: Array[String], container: ScriptingContainer = null) = {
  val ctx = if (container != null) container else this.container
    ctx.setCompileMode(CompileMode.FORCE)
    ctx.setArgv(arguments)

    val load_path = ctx.getLoadPaths
    for (d <- pwd/"RCODE"::pwd.glob("**/RCODE").filter(_.isDirectory).toList){
      if (d.exists && d.isDirectory)
        load_path.add(d.pathAsString)
    }
    ctx.setLoadPaths(load_path)
    receiver = ctx.runScriptlet(PathType.RELATIVE, source)
  }

  def applyDynamic(name: String)(args: Any*): RubyVal = {
    val real_arguments: Seq[Any] = args.map { x => x match {
      case i: Seq[_] => seqAsJavaList(i)
      case m: Map[_,_] => mapAsJavaMap(m)
      case s: Set[_] => setAsJavaSet(s)
      case null => ""
      case other: Any => other
    }
    }
    val value = container.callMethod(receiver, name,
      real_arguments.map(_.asInstanceOf[java.lang.Object]):_*).asInstanceOf[Any]
    new RubyVal(value)
  }

  def selectDynamic(name: String): RubyVal = {
    new RubyVal(container.get(receiver, "@"+name).asInstanceOf[Any])
  }

  def lookupMethod(code: String): Int = {
    try {
      val method = container
        .callMethod(receiver, "method", code.asInstanceOf[java.lang.Object])
        .asInstanceOf[RubyMethod]
      val parameters = method.parameters(method.getRuntime.getCurrentContext).asInstanceOf[RubyArray]

      parameters.size()
    } catch {
      case _: Throwable => -1
    }
  }
}

class RubyVal(val value: Any) {
  override def equals(obj: scala.Any): Boolean = value == obj
}

object Convertions {
  implicit def rv2Long(x: RubyVal): Long = x.value.asInstanceOf[Long]
  implicit def rv2Double(x: RubyVal): Double = x.value.asInstanceOf[Double]
  implicit def rv2String(x: RubyVal): String = x.value.toString

  implicit def rv2Array[T:ClassTag](x: RubyVal): Array[T] = {
    if (x.value == null) return Array[T]()
    x.value match {
      case vec: immutable.Iterable[T] => vec.toArray
      case _ => {
        val ra = x.value.asInstanceOf[RubyArray]
        for (i <- (0 until ra.size))
          yield ra.get(i).asInstanceOf[T]
      }.toArray
    }
  }
}