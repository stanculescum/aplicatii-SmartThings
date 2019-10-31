/**
 *  Temperatures Dashboard SmartApp for SmartThings
 *
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
 *  Overview
 *  ----------------
 *  This SmartApp helps you see all your temperatures in a single view for the devices you selected capable of reporting temperature readings.
 *
 */

definition(
    name: "Temperatures Dashboard",
    namespace: "mST",
    author: "Mihail Stanculescu",
    description: "SmartApp to report temperature readings in a single view",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/hometemp-icon.png",
    iconX2Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/hometemp-icon@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/hometemp-icon@2x.png")


preferences {
    page name:"pageStatus"
    page name:"pageConfigure"
}

//***************************
//Show Status page
//***************************
def pageStatus() {
	def pageProperties = [
		name:       "pageStatus",
		title:      "",
		nextPage:   null,
		install:    true,
		uninstall:  true
	]

	if (settings.tempdevices == null) {
			return pageConfigure()
	}
    
	def goodlist = ""
	def badlist = ""
	def errorlist = ""
	    
	return dynamicPage(pageProperties) {
        
        def rightNow = new Date()
		settings.tempdevices.each() {
			def lastTemp = it.currentValue('temperature')
			try {
				if (lastTemp) {
					goodlist += "$lastTemp °C : $it.displayName\n"
				} else {
					badlist += "$it.displayName\n"	
				}

			} catch (e) {
					log.trace "Caught error checking a device."
					log.trace e
					errorlist += "$it.displayName\n"
			}
		}

		if (goodlist) {
			section("Current Temperatures and Devices") {
				paragraph goodlist.trim()
			}
		}

		if (badlist) {
			section("Devices NOT Reporting Temps") {
				paragraph badlist.trim()
			}
		}

		if (errorlist) {
			section("Devices with Errors") {
				paragraph errorlist.trim()
			}
		}

		section("Menu") {
			href "pageConfigure", title:"Configure", description:"Tap to manage your list of devices", required: false, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/configuration.png"
            //href "pageStatus", title:"Refresh", description:"Tap to refresh", required: false, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/reset-icon.png"
		}
        section("About") { 
			headerSECTION()
		}
	}
}

//***************************
//Show Configure Page
//***************************
def pageConfigure() {
	def pageProperties = [name:"pageConfigure",
		title:          "Dashboard Temperatures Configurator",
		nextPage:       "pageStatus",
		uninstall:      true
	]

	def inputTempDevices = [name:"tempdevices",type:"capability.temperatureMeasurement",title:"What devices does the temperature show?",multiple:true,required:true]
    
	return dynamicPage(pageProperties) {
		section("Devices To Check") {
			input inputTempDevices
		}

		section([title:"Available Options", mobileOnly:true]) {
			label title:"Assign a name for your app", required:false
		}
	}
}

def installed() {
	initialize()
}

def updated() {
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
	//log.trace "Launching Temperatures Dashboard"
    log.trace "Initializing Temperatures Dashboard"
}

def headerSECTION() {
	return paragraph (image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/hometemp-icon.png", "${textVersion()}")
}

private def textVersion() {
    def text = "* This SmartApp helps you see all your temperatures in a single view for the devices you selected capable of reporting temperature readings\n\nVersion: 1.0\nDate: 15/10/2019"
}