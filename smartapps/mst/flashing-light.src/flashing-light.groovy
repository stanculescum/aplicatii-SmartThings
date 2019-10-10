/**
 *  Flashing light
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
definition(
    name: "Flashing light",
    namespace: "mST",
    author: "Mihail Stanculescu",
    description: "Flashing light in response to motion, an open/close event, or a switch.",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/flashing-light-bulb.png",
    iconX2Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/flashing-light-bulb.png",
    iconX3Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/flashing-light-bulb.png"
)

preferences {
	section() {
        paragraph image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/flashing-light-bulb.png",
                  title: "Flashing light",
                  required: true,
                  "Flashing light in response to motion, an open/close event, or a switch."
    }
    section(hideWhenEmpty: true, "When any of the following devices trigger..."){
		input "myPresence", "capability.presenceSensor", title: "Select Presence Sensors", required: false, multiple: true
        input "motion", "capability.motionSensor", title: "Select Motion Sensors", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Select Contact Sensors", required: false, multiple: true
        input "mySwitch", "capability.switch", title: "SelectSwitchs", required: false, multiple: true
        input "valves", "capability.valve", title: "Select Valves", required: false, multiple: true
        input "smokeDetector", "capability.smokeDetector", title: "Select Smoke Detector", required: false, multiple: true
        input "waterSensor", "capability.waterSensor", title: "Select Water Sensor", required: false, multiple: true
        input "myLock", "capability.lock", title: "Select Lock", required: false, multiple: true
	}
	section("Then flashing..."){
		input "switches", "capability.switch", title: "These lights:", required: true, multiple: true	
	}
	section("Number of times & Time settings in milliseconds..."){
		input "numFlashes", "number", title: "This number of times (default 3)", required: false
        input "onFor", "number", title: "On for (default 1000ms)", required: false
		input "offFor", "number", title: "Off for (default 1000ms)", required: false
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
	if (myPresence) {
		subscribe(myPresence, "presence", presenceHandler)
	}
    if (motion) {
		subscribe(motion, "motion.active", motionActiveHandler)
	}
    if (contact) {
		subscribe(contact, "contact", contactHandler)
	}
    if (mySwitch) {
		subscribe(mySwitch, "switch", switchHandler)
	}
	if (valves) {
		subscribe(valves, "valve", valveHandler)
	}
    if (smokeDetector) {
		subscribe(smokeDetector, "smoke.detected", smokeDetectedHandler)
	}
    if (waterSensor) {
		subscribe(waterSensor, "water", waterHandler)
	}
    if (myLock) {
		subscribe(myLock, "lock", lockHandler)
	}
}

def presenceHandler(evt) {
	log.debug "presence $evt.value"
	if (evt.value == "present") {
		flashLights()
	} else if (evt.value == "not present") {
		flashLights()
	}
}

def motionActiveHandler(evt) {
	log.debug "motion.active $evt.value"
	flashLights()
}

def contactHandler(evt) {
	log.debug "contact $evt.value"
    if (evt.value == "open") {
		flashLights()
	} else if (evt.value == "closed") {
	flashLights()
    }
}

def switchHandler(evt) {
	log.debug "switch $evt.value"
	if (evt.value == "on") {
		flashLights()
	} else if (evt.value == "off") {
		flashLights()
	}
}

def valveHandler(evt) {
	log.debug "valve $evt.value"
	if (evt.value == "open") {
		flashLights()
	} else if (evt.value == "closed") {
		flashLights()
	}
}

def smokeDetectedHandler(evt) {
	log.debug "smoke.detected $evt.value"
	flashLights()
}

def waterHandler(evt) {
	log.debug "water $evt.value"
	if (evt.value == "wet") {
		flashLights()
	} else if (evt.value == "dry") {
		flashLights()
	}
}

def lockHandler(evt) {
	log.debug "lock $evt.value"
	if (evt.value == "locked") {
		flashLights()
	} else if (evt.value == "unlocked") {
		flashLights()
	}
}


private flashLights() {
	def doFlash = true
	def onFor = onFor ?: 1000
	def offFor = offFor ?: 1000
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