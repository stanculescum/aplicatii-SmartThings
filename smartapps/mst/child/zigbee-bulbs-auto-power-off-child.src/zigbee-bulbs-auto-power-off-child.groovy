/**
 *  ZigBee Bulbs Auto Power Off!-child
 *  Copyright 2019
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *  
 *  About: ZigBee bulbs are automatically off when the main power supply voltage is restored and bulbs will be on, but no one is home.
 *
 *  Version: v1.0 / 2019-12-12 - Initial Release
 *  Author: Mihail Stanculescu
 */
definition(
    name: "ZigBee Bulbs Auto Power Off!-child",
    namespace: "mST/child",
    author: "Mihail Stanculescu",
    description: "ZigBee bulbs are auto power off when the main power supply voltage is restored and bulbs will on, but no one is home.",
    category: "My Apps",
    parent: "mST/parent:ZigBee Bulbs Auto Power Off!",
    iconUrl: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/bulb-zigbee-icon.png",
    iconX2Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/bulb-zigbee-icon@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/bulb-zigbee-icon@2x.png"
)

preferences {
    section("The following ZigBee bulbs...") {
		input "bulb1", "capability.colorControl", title: " ", multiple: true, required: true
	}
/**    section("When they are not home..."){
		input "presence1", "capability.presenceSensor", title: " ", multiple: false, required: false
    }*/
    section("Turn off after...minute when the main power supply voltage is restored") {
		input "lockTime", "number", title: "Auto off time (minutes)", description: "Number of minutes", required: true, defaultValue: "1"
	}
}

def installed() {
	subscribe(bulb1, "switch", bulbHandler)
}

def updated() {
	unsubscribe()
	subscribe(bulb1, "switch", bulbHandler)
}

def bulbHandler(evt) {
	log.debug "$evt.value"
    //def presenceValue = presence1.find{it.currentPresence == "not present"}
    //log.debug presenceValue
	if (evt.value == "on" /**&& presenceValue*/) {
    	def MinuteDelay = (60 * lockTime)
    	runIn(MinuteDelay, turnOffSwitch)
    	log.debug "The lights will be off!"
    }
}

def turnOffSwitch() {
	bulb1.off()
}
