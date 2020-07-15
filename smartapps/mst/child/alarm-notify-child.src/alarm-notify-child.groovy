/**
 *  Alarm notify!-child
 *  Copyright 2020
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
 *  About: Alarm notification on the mobile phone using the Telegram App or another application via IFTTT. A simulated switch is required designed in Smartthings App.
 *
 *  Version: v1.0 / 2020-07-14 - Initial Release
 *  Author: Mihail Stanculescu
 */
definition(
    name: "Alarm notify!-child",
    namespace: "mST/child",
    author: "Mihail Stanculescu",
    description: "Alarm notification on the mobile phone using the Telegram App or another application via IFTTT. A simulated switch is required designed in Smartthings App.",
    category: "My Apps",
    parent: "mST/parent:Alarm notify!",
    iconUrl: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/bellnotification-icon.png",
    iconX2Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/bellnotification-icon@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/bellnotification-icon@3x.png"
)

preferences {
	page(name: "triggerpage")
    page(name: "timepage")
}

def triggerpage() {
	dynamicPage(name: "triggerpage", title: " ", nextPage: "timepage", uninstall: true){
        section([title:"Name of child app", mobileOnly:true]) {
			label title:"Assign a name for child app", required:true
		}
        section("Monitor access") {
			input "contactdevices", "capability.contactSensor", title: "Open/close contact sensors", multiple: true, required: false
		}
//==========================================        
        section("Switch connected to IFTTT") {
			input "switchIFTTT", "capability.switch", title: " ", description: " ", required: true
		}
	}
}

def timepage() {
	dynamicPage(name: "timepage", title: " ", install: true, uninstall: true){
    	section("Only") {
      		input "conditions", "enum", title: "When?", options: ["always":"Always", "sunrise":"Sunrise to Sunset", "sunset":"Sunset to Sunrise", "custom":"Custom time", "presence": "Presence"], defaultValue: "always", image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/24_hours.png", submitOnChange: true
      		switch(conditions) {
        		case "always":
          		break
        		case "sunrise":
          		break
        		case "sunset":
                break
                case "custom":
				break
                case "presence":
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
        if (conditions) {
            switch(conditions) {
        	case "presence":
            	section("Presence"){
    				input "userpresence", "capability.presenceSensor", title: "Presence Sensor", required: false, multiple: true, submitOnChange: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/presence-sensor.png"
				}
        		if (userpresence) {
            		section("") {
            			input "userpresenceValue", "enum", title: " ", required: true, multiple:false, options: ["present":"Home","not present":"Away"], defaultValue: "present"
            		}
        		}
            }
    	}
    }
}

//=============

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    unschedule()
	subscribe()
}

def subscribe() {
	subscribe(contactdevices, "contact", contactHandler)
}

//=====================

def contactHandler(evt) {
	log.debug "contact $evt.value"
    if (!checkConditions()) {
    	log.debug("Conditions met")
    	return
  	}
	if (evt.value == "open") {
    	switchIFTTT.on()
    	log.debug "alarm sensor!"
    }
}

//===========================

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
    case "presence":
    	if (userpresence.find{it.currentPresence == userpresenceValue}){
    	return true
        }
  }
}
