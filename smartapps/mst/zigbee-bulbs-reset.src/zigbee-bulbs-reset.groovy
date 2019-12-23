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
 */
definition(
    name: "ZigBee Bulbs Reset",
    namespace: "mST",
    author: "Mihail Stănculescu",
    description: "ZigBee Bulbs wiil be reset to custom default value (color and level)",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/reset-bulb.png",
    iconX2Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/reset-bulb@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/reset-bulb@2x.png")


preferences {
	section("ZigBee Bulbs:") {
        input "hues", "capability.colorControl", required: true, title: " "
    }
    section("Reset bulbs with this switch") {
        input "myswitch", "capability.switch", required: true, title: " "
    }
    section("to default color:") {
        input "color", "enum", title: " ", required: true, multiple:false, options: ["Cold White","Warm White","Red","Orange","Yellow","Green","Blue","Purple","Pink"]
    }
    section("and default level:") {
        input "blevel", "number", title: " ", required: true
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
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
        runIn(5, turnOffBulb)
        log.debug "Bulbs reset!"
	}
    else if (evt.value == "off") {
		hues*.off()
	}
}

def turnOffBulb(){
hues*.off()
myswitch.off()
}

private takeAction() {
	//Cold White
	def hueColor = 15
    def saturation = 0
    def level = blevel
    
	if(color == "Warm White"){
		hueColor = 20
        saturation = 80}
	else if(color == "Red"){
		hueColor = 0
        saturation = 100}
	else if(color == "Orange"){
		hueColor = 15
        saturation = 100}
	else if(color == "Yellow"){
		hueColor = 20
        saturation = 100}
	else if(color == "Green"){
		hueColor = 35
        saturation = 100}
	else if(color == "Blue"){
		hueColor = 70
        saturation = 100}
    else if(color == "Purple"){
		hueColor = 80
        saturation = 100}
    else if(color == "Pink"){
		hueColor = 90
        saturation = 100}

	log.debug "current values = $state.previous"

	def newValue = [hue: hueColor, saturation: saturation]
	log.debug "new value = $newValue"

	hues*.setColor(newValue)
    hues*.setLevel(level)
}