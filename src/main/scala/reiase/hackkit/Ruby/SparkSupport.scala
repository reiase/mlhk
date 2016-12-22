package reiase.hackkit.Ruby

import org.apache.spark.sql.SparkSession
import reiase.hackkit.Ruby.Convertions._
/**
  * Created by reiase on 10/26/16.
  */
trait SparkSupport {
  val spark = SparkSession.builder().getOrCreate

  private def callRubyFunction(name: String, argv: Seq[Any]) = {
    val retval: String = RubyContext.applyDynamic(name)(argv: _*)
    retval
  }

  def registerUDF(name: String): Any = {
    val argc: Int = RubyContext.lookupMethod(name)
    argc match {
      case 0 => spark.udf.register(name, {()=>{
        val argv: Seq[Any] = Nil
        callRubyFunction(name, argv)
      }})
      case 1 => spark.udf.register(name, {(a: Any)=>{
        val argv: Seq[Any] = a::Nil
        callRubyFunction(name, argv)
      }})
      case 2 => spark.udf.register(name, {(a: Any, b: Any)=>{
        val argv: Seq[Any] = a::b::Nil
        callRubyFunction(name, argv)
      }})
      case 3 => spark.udf.register(name, {(a: Any, b: Any, c: Any)=>{
        val argv: Seq[Any] = a::b::c::Nil
        callRubyFunction(name, argv)
      }})
      case 4 => spark.udf.register(name, {(a: Any, b: Any, c: Any, d: Any)=>{
        val argv: Seq[Any] = a::b::c::d::Nil
        callRubyFunction(name, argv)
      }})
      case 5 => spark.udf.register(name, {(a: Any, b: Any, c: Any, d: Any, e: Any)=>{
        val argv: Seq[Any] = a::b::c::d::e::Nil
        callRubyFunction(name, argv)
      }})
      case 6 => spark.udf.register(name, {(a: Any, b: Any, c: Any, d: Any, e: Any, f: Any)=>{
        val argv: Seq[Any] = a::b::c::d::e::f::Nil
        callRubyFunction(name, argv)
      }})
      case -1 => throw new NoSuchMethodException("method %s not found!".format(name))
      case _ => throw new IllegalArgumentException("too many argument for %s".format(name))
    }
  }

}
