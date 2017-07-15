addSbtPlugin("eu.unicredit" % "sbt-shocon" % sys.props.getOrElse("plugin.version", sys.error("'plugin.version' environment variable is not set")))
