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
    section("to default color:") {
        input "mycolor", "enum", title: " ", required: true, multiple:false, options: ["Cold White","Warm White","Red","Orange","Yellow","Green","Blue","Purple","Pink"]
    }
    section("and default level:") {
        input "mylevel", "number", title: " ", required: true, defaultValue:50
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
        //auto off in 5sec.
        runIn(5, turnOffBulb)
        log.debug "Bulbs reset!"
	}
    /**else if (evt.value == "off") {
		bulbs*.off()
	}*/
}

def turnOffBulb(){
bulbs*.off()
myswitch.off()
}

private takeAction() {
	//Cold White
	def hueColor = 15
    def saturationColor = 0
    
	if(mycolor == "Warm White"){
		hueColor = 20
        saturationColor = 80}
	else if(mycolor == "Red"){
		hueColor = 0
        saturationColor = 100}
	else if(mycolor == "Orange"){
		hueColor = 15
        saturationColor = 100}
	else if(mycolor == "Yellow"){
		hueColor = 20
        saturationColor = 100}
	else if(mycolor == "Green"){
		hueColor = 35
        saturationColor = 100}
	else if(mycolor == "Blue"){
		hueColor = 70
        saturationColor = 100}
    else if(mycolor == "Purple"){
		hueColor = 80
        saturationColor = 100}
    else if(mycolor == "Pink"){
		hueColor = 90
        saturationColor = 100}

	log.debug "current values = $state.previous"

	def newValue = [hue: hueColor, saturation: saturationColor]
	log.debug "new value = $newValue"

	bulbs*.setColor(newValue)
    
    def level = mylevel
    log.debug "new level = $level"
    bulbs*.setLevel(level)
}