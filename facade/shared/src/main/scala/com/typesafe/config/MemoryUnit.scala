package com.typesafe.config

sealed abstract class MemoryUnit(val prefix:String, val powerOf:Int, val power:Int) {
  lazy val bytes = BigInt(powerOf).pow(power)
}

object MemoryUnit {

  case object BYTES extends MemoryUnit("", 1024, 0)
  case object KILOBYTES extends MemoryUnit("kilo", 1000, 1)
  case object MEGABYTES extends MemoryUnit("mega", 1000, 2)
  case object GIGABYTES extends MemoryUnit("giga", 1000, 3)
  case object TERABYTES extends MemoryUnit("tera", 1000, 4)
  case object PETABYTES extends MemoryUnit("peta", 1000, 5)
  case object EXABYTES extends MemoryUnit("exa", 1000, 6)
  case object ZETTABYTES extends MemoryUnit("zetta", 1000, 7)
  case object YOTTABYTES extends MemoryUnit("yotta", 1000, 8)
  case object KIBIBYTES extends MemoryUnit("kibi", 1024, 1)
  case object MEBIBYTES extends MemoryUnit("mebi", 1024, 2)
  case object GIBIBYTES extends MemoryUnit("gibi", 1024, 3)
  case object TEBIBYTES extends MemoryUnit("tebi", 1024, 4)
  case object PEBIBYTES extends MemoryUnit("pebi", 1024, 5)
  case object EXBIBYTES extends MemoryUnit("exbi", 1024, 6)
  case object ZEBIBYTES extends MemoryUnit("zebi", 1024, 7)
  case object YOBIBYTES extends MemoryUnit("yobi", 1024, 8)

  val values: Vector[MemoryUnit] = Vector(BYTES, KILOBYTES, MEGABYTES, GIGABYTES, TERABYTES, PETABYTES,
    EXABYTES, ZETTABYTES, YOTTABYTES, KIBIBYTES, MEBIBYTES, GIBIBYTES, TEBIBYTES,
    PEBIBYTES, EXBIBYTES, ZEBIBYTES, YOBIBYTES)


  lazy val unitsMap: Map[String, MemoryUnit] = {
    val map = Map.newBuilder[String, MemoryUnit]

    MemoryUnit.values.foreach { unit =>
      map += unit.prefix + "byte" -> unit
      map += unit.prefix + "bytes" -> unit
      if (unit.prefix.length() == 0) {
        map += "b" -> unit
        map += "B" -> unit
        map += "" -> unit // no unit specified means bytes
      } else {
        val first = unit.prefix.substring(0, 1)
        val firstUpper = first.toUpperCase()
        if (unit.powerOf == 1024) {
          map += first -> unit // 512m
          map += firstUpper -> unit // 512M
          map += firstUpper + "i" -> unit // 512Mi
          map += firstUpper + "iB" -> unit // 512MiB
        } else if (unit.powerOf == 1000) {
          if (unit.power == 1) {
            map += first + "B" -> unit // 512kB
          } else {
            map += firstUpper + "B" -> unit // 512MB
          }
        }
      }
    }
      map.result()
  }


  def parseUnit(unit: String):Option[MemoryUnit] = {
    unitsMap.get(unit)
  }
}
