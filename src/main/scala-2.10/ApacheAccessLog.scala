/**
  * Created by hs4204 on 2/18/2017.
  */

case class ApacheAccessLog(ipAddress: String,
                           clientIdentd: String,
                           userId: String,
                           dateTime: String,
                           method: String,
                           endpoint: String,
                           protocol: String,
                           responseCode: Int,
                           contentSize: Long,
                           referrer: String,
                           userAgent: String) {
}

object ApacheAccessLog {
  val PATTERN = """^(\S+) (\S+) (\S+) \[([\w:/]+\s[+\-]\d{4})\] "(\S+) (\S+) (\S+)" (\d{3}) (\d+) (\S+) (.*)""".r

  def parseLogLine(log: String): ApacheAccessLog = {
    log match {
      case PATTERN(ipAddress, clientIdentd, userId, dateTime, method, endpoint, protocol, responseCode, contentSize, referrer, userAgent)
      => ApacheAccessLog(ipAddress, clientIdentd, userId, dateTime, method, endpoint, protocol, responseCode.toInt,
        contentSize.toLong, referrer, userAgent)
      case _ => throw new RuntimeException(s"""Cannot parse log line: $log""")
    }
  }
}
