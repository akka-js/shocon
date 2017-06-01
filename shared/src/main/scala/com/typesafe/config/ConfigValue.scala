/* Copyright 2016 UniCredit S.p.A.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.typesafe.config

import eu.unicredit.shocon

trait ConfigValue extends ConfigMergeable {
  val inner: shocon.Config.Value
  def render(): String = inner.toString
  def valueType(): ConfigValueType = inner match {
    case _: shocon.Config.Object => ConfigValueType.OBJECT
    case _: shocon.Config.Array => ConfigValueType.LIST 
    case _: shocon.Config.NumberLiteral => ConfigValueType.NUMBER 
    case _: shocon.Config.StringLiteral => ConfigValueType.STRING 
    case _: shocon.Config.BooleanLiteral => ConfigValueType.BOOLEAN 
    case _: shocon.Config.NullLiteral.type => ConfigValueType.NULL 
  }
}
