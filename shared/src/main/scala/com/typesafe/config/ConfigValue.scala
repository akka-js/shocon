package com.typesafe.config

import eu.unicredit.shocon

trait ConfigValue extends ConfigMergeable {

	val inner: shocon.Config.Value
	
}
