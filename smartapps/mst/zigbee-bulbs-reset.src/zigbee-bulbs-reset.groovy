/**
 *  ZigBee Bulbs Reset
 *
 *  Copyright 2019 Mihail Stănculescu
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  v1.0 - ZigBee Bulbs will be reset to custom value (color and level)
 */
definition(
    name: "ZigBee Bulbs Reset",
    namespace: "mST",
    author: "Mihail Stănculescu",
    description: "ZigBee Bulbs will be reset to custom value (color and level)",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/reset-bulb.png",
    iconX2Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/reset-bulb@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/reset-bulb@2x.png")


preferences {
	section("ZigBee Bulbs:") {
        input "bulbs", "capability.colorControl", required: true, title: " ", multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/bulb.png"
    }
    section("Reset bulbs with this switch") {
        input "myswitch", "capability.switch", required: true, title: " ", multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/light-switch.png"
    }
    section("to color:") {
        input "mycolor", "enum", title: " ", required: true, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple"],
                    defaultValue: "Soft White"
    }
    section("and level:") {
        input "mylevel", "number", title: " ", required: true, description: "1...100", range: "1..100", defaultValue: "50"
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
    unschedule()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
		subscribe(myswitch, "switch", switchHandler) 
}

// TODO: implement event handlers

def switchHandler(evt) {
	log.debug "switch $evt.value"
    
	if (evt.value == "on") {
		takeAction()
        //auto off in 5sec.
        runIn(5, turnOffBulb)
        log.debug "Bulbs reset!"
	}
}

def turnOffBulb(){
bulbs*.off()
myswitch.off()
}

private takeAction() {

	def hueColor = 15
	def saturation = 0
    
    switch(mycolor) {
		case "White":
			hueColor = 15
			saturation = 0
			break;
		case "Daylight":
			hueColor = 50
			saturation = 85
			break;
		case "Soft White":
			hueColor = 20
			saturation = 30
			break;
		case "Warm White":
			hueColor = 20
			saturation = 80
			break;
		case "Blue":
			hueColor = 66
            saturation = 100
			break;
		case "Green":
			hueColor = 33
            saturation = 100
			break;
		case "Yellow":
			hueColor = 17
            saturation = 100
			break;
		case "Orange":
			hueColor = 6
            saturation = 100
			break;
		case "Purple":
			hueColor = 83
            saturation = 100
			break;
		case "Red":
			hueColor = 1
            saturation = 100
			break;
	}

	state.previous = [:]

	bulbs*.each {
		state.previous[it.id] = [
			"switch": it.currentValue("switch"),
			"level" : it.currentValue("level"),
			"hue": it.currentValue("hue"),
			"saturation": it.currentValue("saturation")
		]
	}

	log.debug "current values = $state.previous"

	def newValue = [hue: hueColor, saturation: saturation]
	log.debug "new value = $newValue"

	bulbs*.setColor(newValue)
    
    def level = mylevel
    log.debug "new level = $level"
    bulbs*.setLevel(level)
}