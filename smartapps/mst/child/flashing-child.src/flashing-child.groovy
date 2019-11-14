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
 */

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

preferences {
	page(name: "pageOne", title: "", nextPage: "pageTwo", uninstall: true){
		section("These devices flashing"){
			input "switches", "capability.switch", title: "The select switchs are", required: true, multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/light-switch.png"
		}
        section("Only when") {
      		input "conditions", "enum", title: "When?", options: ["always":"Always", "sunrise":"Sunrise and Sunset", "sunset":"Sunset and Sunrise", "custom":"Specify time (view last page)"], defaultValue: "always", image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/24_hours.png", submitOnChange: true

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
        section([title:"Name of child app", mobileOnly:true]) {
			label title:"Assign a name for child app", required:true
		}
	}
	page(name: "pageTwo", title: "", nextPage: "pageThree") {
		section(hideWhenEmpty: true, "When any of the following devices trigger..."){
			input "presence", "capability.presenceSensor", title: "Select Presence Sensors", required: false, multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/presence-sensor.png"
			input "motion", "capability.motionSensor", title: "Select Motion Sensors", required: false, multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/motion-sensor.png"
			input "contact", "capability.contactSensor", title: "Select Contact Sensors", required: false, multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/contact-sensor.png"
			input "myswitch", "capability.switch", title: "SelectSwitchs", required: false, multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/light-switch.png"
			input "valves", "capability.valve", title: "Select Valves", required: false, multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/valve.png"
			input "smokeDetector", "capability.smokeDetector", title: "Select Smoke Detector", required: false, multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/smoke-detector.png"
            input "carbonMonoxideDetector", "capability.carbonMonoxideDetector", title: "Select Carbon Monoxide Detector", required: false, multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/carbon-monoxide-sensor.png"
			input "waterSensor", "capability.waterSensor", title: "Select Water Sensor", required: false, multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/water-sensor.png"
			input "lock", "capability.lock", title: "Select Lock", required: false, multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/door-lock.png"
            input "button", "capability.button", title: "Select Button", required: false, multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/button.png"
		}
	}
	page(name: "pageThree", title: "", nextPage: "custom") {
		section("START event"){
			input "numStartFlashes", "number", title: "This number of times (default 1)", required: false
			input "onStart", "number", title: "On for (default 1000ms)", required: false
			input "offStart", "number", title: "Off for (default 1000ms)", required: false
		}
		section("STOP event"){
			input "numStopFlashes", "number", title: "This number of times (default 3)", required: false
			input "onStop", "number", title: "On for (default 1000ms)", required: false
			input "offStop", "number", title: "Off for (default 1000ms)", required: false
		}
	}
    page(name: "custom",title: "", install: true, uninstall: true) {
    	section("If you have chosen >> Specify the time << enter the time below"){
    		input "from", "time", title: "From", required: false
			input "until", "time", title: "Until", required: false
    	}
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe()
}

def subscribe() {
	if (presence) {
		subscribe(presence, "presence", presenceHandler)
	}
	if (motion) {
		subscribe(motion, "motion.active", motionActiveHandler)
	}
	if (contact) {
		subscribe(contact, "contact", contactHandler)
	}
	if (myswitch) {
		subscribe(myswitch, "switch", switchHandler)
	}
	if (valves) {
		subscribe(valves, "valve", valveHandler)
	}
	if (smokeDetector) {
		subscribe(smokeDetector, "smoke.detected", smokeDetectedHandler)
	}
    if (carbonMonoxideDetector) {
		subscribe(carbonMonoxideDetector, "carbonMonoxide.detected", carbonMonoxideDetectedHandler)
	}
	if (waterSensor) {
		subscribe(waterSensor, "water", waterHandler)
	}
	if (lock) {
		subscribe(lock, "lock", lockHandler)
	}
    if (button) {
		subscribe(button, "button", buttonHandler)
	}
}

def presenceHandler(evt) {
	log.debug "presence $evt.value"
	if (evt.value == "present") {
		startflashLights()
	} else if (evt.value == "not present") {
		stopflashLights()
	}
}

def motionActiveHandler(evt) {
	log.debug "motion.active $evt.value"
	startflashLights()
}

def contactHandler(evt) {
	log.debug "contact $evt.value"
	if (evt.value == "open") {
		startflashLights()
	} else if (evt.value == "closed") {
	stopflashLights()
    }
}

def switchHandler(evt) {
	log.debug "switch $evt.value"
    
    if (!checkConditions()) {
    	log.debug("Conditions not met, skipping")
    	return
  	}
	if (evt.value == "on") {
		startflashLights()
	}
    else if (evt.value == "off") {
		stopflashLights()
	}
}

def valveHandler(evt) {
	log.debug "valve $evt.value"
	if (evt.value == "open") {
		startflashLights()
	} else if (evt.value == "closed") {
		stopflashLights()
	}
}

def smokeDetectedHandler(evt) {
	log.debug "smoke.detected $evt.value"
	statflashLights()
}

def carbonMonoxideDetectedHandler(evt) {
	log.debug "carbonMonoxide.detected $evt.value"
	statflashLights()
}

def waterHandler(evt) {
	log.debug "water $evt.value"
	if (evt.value == "wet") {
		startflashLights()
	} else if (evt.value == "dry") {
		stopflashLights()
	}
}

def lockHandler(evt) {
	log.debug "lock $evt.value"
	if (evt.value == "locked") {
		startflashLights()
	} else if (evt.value == "unlocked") {
		stopflashLights()
	}
}

def buttonHandler(evt) {
	log.debug "button $evt.value"
	if (evt.value == "pushed") {
		startflashLights()
	} else if (evt.value == "held") {
		stopflashLights()
	}
}

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

private startflashLights() {
	def doStartFlash = true
	def onStart = onStart ?: 1000
	def offStart = offStart ?: 1000
	def numStartFlashes = numStartFlashes ?: 1

	log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
	if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numStartFlashes + 1) * (onStart + offStart)
		doStartFlash = elapsed > sequenceTime
		log.debug "DO FLASH: $doStartFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if (doStartFlash) {
		log.debug "FLASHING $numStartFlashes times"
		state.lastActivated = now()
		log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switches.collect{it.currentSwitch != "on"}
		def delay = 0L
		numStartFlashes.times {
			log.trace "Switch on after  $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.on(delay: delay)
				}
				else {
					s.off(delay:delay)
				}
			}
			delay += onStart
			log.trace "Switch off after $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.off(delay: delay)
				}
				else {
					s.on(delay:delay)
				}
			}
			delay += offStart
		}
	}
}

private stopflashLights() {
	def doStopFlash = true
	def onStop = onStop ?: 1000
	def offStop = offStop ?: 1000
	def numStopFlashes = numStopFlashes ?: 3

	log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
	if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numStopFlashes + 1) * (onStop + offStop)
		doStopFlash = elapsed > sequenceTime
		log.debug "DO FLASH: $doStopFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if (doStopFlash) {
		log.debug "FLASHING $numStopFlashes times"
		state.lastActivated = now()
		log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switches.collect{it.currentSwitch != "on"}
		def delay = 0L
		numStopFlashes.times {
			log.trace "Switch on after  $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.on(delay: delay)
				}
				else {
					s.off(delay:delay)
				}
			}
			delay += onStop
			log.trace "Switch off after $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.off(delay: delay)
				}
				else {
					s.on(delay:delay)
				}
			}
			delay += offStop
		}
	}
}