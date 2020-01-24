/**
 *  Flashing Child SmartApp for SmartThings
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
 *  v1.1 / 2019-11-05 - Adding time conditions (always, day, night and custom)
 *  v1.0 / 2019-10-15 - Initial Release
 */

//Definition - The defintion section of the SmartApp specifies the name of the app along with other information that identifies and describes it.

definition(
    name: "Flashing-child",
    namespace: "mST/child",
    author: "Mihail Stanculescu",
    description: "Flashing light in response to presence/motion, an open/close event, an on/off switch, or lock/unlock door",
    category: "My Apps",
    parent: "mST/parent:Flashing Lights",
    iconUrl: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/flashing-light-bulb.png",
    iconX2Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/flashing-light-bulb.png",
    iconX3Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/flashing-light-bulb.png"
)

//Preferences - The preferences section is responsible for defining the screens that appear in the mobile app when a SmartApp is installed or updated.

preferences {
	page(name: "triggerpage")
	page(name: "actuatorpage")
	page(name: "settingspage")
	page(name: "timepage")
}

def triggerpage() {
	dynamicPage(name: "triggerpage", title: "Page 1", nextPage: "actuatorpage", uninstall: true){
    	section([title:"Name of child app", mobileOnly:true]) {
			label title:"Assign a name for child app", required:true
		}
        section(hideWhenEmpty: true, "When any of the following devices trigger..."){
			input "accelerationTrigger", "capability.accelerationSensor", title: "Acceleration Sensor?", required: false, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/attention-icon.png"
        	input "contactTrigger", "capability.contactSensor", title: "Contact Sensor?", required: false, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/contact-sensor.png"
        	input "motionTrigger", "capability.motionSensor", title: "Motion Sensor?", required: false, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/motion-sensor.png"
			input "presenceTrigger", "capability.presenceSensor", title: "Presence Sensor?", required: false, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/presence-sensor.png"
			input "switchTrigger", "capability.switch", title: "Switch?", required: false, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/light-switch.png"
		}
    }
}

def actuatorpage() {
	dynamicPage(name: "actuatorpage", title: "Page 2", nextPage: "settingspage"){
    	section(hideWhenEmpty: true, "These device flashing..."){
			input "switches", "capability.switchLevel", title: " ", required: false, multiple: true, submitOnChange: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/color-bulb.png"
		}
        if (switches) {
            // Do something here like update a message on the screen, or introduce more inputs. "submitOnChange" will refresh the page and allow the user to see the changes immediately.

            section("") {
            	input "mycolor", "enum", title: "Choose color", required: false, multiple:false, options: ["None","Cold White","Warm White","Red","Orange","Yellow","Green","Blue","Purple","Pink"], defaultValue: "None", description: "None"
            	input "mylevel", "number", title: "Choose level (1%...100%)", required: false, multiple:false, range: "1..100", defaultValue: "50"
            }
        }
    }
}

def settingspage() {
	dynamicPage(name: "settingspage", title: "Page 2", nextPage: "timepage"){
    	section("Settings..."){
			input "numFlashes", "number", title: "This number of times (default 3)", required: false
        	input "onFor", "number", title: "On for (default 1s)", required: false
			input "offFor", "number", title: "Off for (default 1s)", required: false
		}
    }
}

def timepage() {
	dynamicPage(name: "timepage", title: "Page 4", install: true, uninstall: true){
    	section("Only") {
      		input "conditions", "enum", title: "When?", options: ["always":"Always", "sunrise":"Sunrise to Sunset", "sunset":"Sunset to Sunrise", "custom":"Custom time"], defaultValue: "always", image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/24_hours.png", submitOnChange: true

      		switch(conditions) {
        		case "always":
          		break
        		case "sunrise":
          		break
        		case "sunset":
                break
                case "custom":
				break
      		}
    	}
        if (conditions) {
            switch(conditions) {
        	case "custom":
            	section("OPTION: only for Custom time"){
    				input "from", "time", title: "From", required: false
					input "until", "time", title: "Until", required: false
        		}
            }
    	}
    }
}

//Pre-defined callbacks - The following methods, if present, are automatically called at various times during the lifecycle of a SmartApp:
//Called when a SmartApp is first installed.
def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe()
}
//Called when the preferences of an installed smart app are updated.
def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe()
}

def subscribe() {
	if (accelerationTrigger) {
		subscribe(accelerationTrigger, "acceleration.active", accelerationActiveHandler)
	}
	if (contactTrigger) {
		subscribe(contactTrigger, "contact.open", contactOpenHandler)
	}
	if (motionTrigger) {
		subscribe(motionTrigger, "motion.active", motionActiveHandler)
	}
	if (presenceTrigger) {
		subscribe(presenceTrigger, "presence", presenceHandler)
	}
    if (switchTrigger) {
		subscribe(switchTrigger, "switch", switchHandler)
	}
	
}

//Event Handlers

def accelerationActiveHandler(evt) {
	log.debug "acceleration $evt.value"
	flashLights()
}

def contactOpenHandler(evt) {
	log.debug "contact $evt.value"
	flashLights()
}

def motionActiveHandler(evt) {
	log.debug "motion $evt.value"
	flashLights()
}

def presenceHandler(evt) {
	log.debug "presence $evt.value"
	if (evt.value == "present") {
		flashLights()
	} else if (evt.value == "not present") {
		flashLights()
	}
}

def switchHandler(evt) {
	log.debug "switch $evt.value"
    if (!checkConditions()) {
    	log.debug("Conditions not met, skipping")
    	return
  	}
	if (evt.value == "on") {
		flashLights()
	}
    else if (evt.value == "off") {
		flashLights()
	}
}

//==========

private def checkConditions() {
  switch(conditions) {
    case "always":
      return true
    case "sunset":
      def day = getSunriseAndSunset()
      return timeOfDayIsBetween(day.sunset, day.sunrise, new Date(), location.timeZone)
    case "sunrise":
      def night = getSunriseAndSunset()
      return timeOfDayIsBetween(night.sunrise, night.sunset, new Date(), location.timeZone)
    case "custom":
      return timeOfDayIsBetween(from, until, new Date(), location.timeZone)
  }
}

private flashLights() {
	
    def hueColor = ""
    def saturationColor = ""
    
    if(mycolor == "Cold White"){
		hueColor = 15
        saturationColor = 0}
	else if(mycolor == "Warm White"){
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
		hueColor = 65
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

	switches*.setColor(newValue)
    
    def level = mylevel
    log.debug "new level = $level"
    switches*.setLevel(level)
    
    def doFlash = true
	def onFor = onFor * 1000 ?: 1000
	def offFor = offFor * 1000 ?: 1000
	def numFlashes = numFlashes ?: 3

	log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
	if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (onFor + offFor)
		doFlash = elapsed > sequenceTime
		log.debug "DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if (doFlash) {
		log.debug "FLASHING $numFlashes times"
		state.lastActivated = now()
		log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switches.collect{it.currentSwitch != "on"}
		def delay = 0L
		numFlashes.times {
			log.trace "Switch on after  $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.on(delay: delay)
				}
				else {
					s.off(delay:delay)
				}
			}
			delay += onFor
			log.trace "Switch off after $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.off(delay: delay)
				}
				else {
					s.on(delay:delay)
				}
			}
			delay += offFor
		}
	}
}
