/**
 *  WindowOrDoorOpen!-Parent SmartApp for SmartThings
 *
 *  Copyright 2019
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
 *  v1.0 / 2019-11-05 - Initial Release
 */
definition(
    name: "WindowOrDoorOpen!",
    namespace: "mST/parent",
    author: "Mihail Stanculescu",
    description: "Choose some contact sensors and get a notification (with voice as an option) when they are left open for too long.  Optionally, turn off the HVAC and set it back to cool/heat when window/door is closed",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/open-door-icon.png",
    iconX2Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/open-door-icon@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/open-door-icon@2x.png"
)

preferences {
    // The parent app preferences are pretty simple: just use the app input for the child app.
    page(name: "mainPage", title: "Content", install: true, uninstall: true, submitOnChange: true) {
        section {
            app(name: "WindowOrDoorOpen!", appName: "WindowOrDoorOpen!-child", namespace: "mST/child", title: "Create a new automation", multiple: true, image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/start-button.png")
		}
        
        section([title: "Other Options", mobileOnly: true]) {
            label title: "Assign a name for the app (optional)", required: false
        }
        section(title: "About") { 
			headerSECTION()
		}
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
    // nothing needed here, since the child apps will handle preferences/subscriptions
    // this just logs some messages for demo/information purposes
    log.debug "there are ${childApps.size()} child smartapps"
    childApps.each {child ->
        log.debug "child app: ${child.label}"
    }
}

def headerSECTION() {
	return paragraph (image: "https://raw.githubusercontent.com/stanculescum/aplicatii-smarthome/master/pictures/open-door-icon.png", "${textVersion()}")
}

private def textVersion() {
    def text = "This app allows you to choose some contact sensors and get a notification (with voice as an option) when left open for too long. Optionally turn off the HVAC and set it to cooling / heating when the window / door is closed.\nThe smartapp can track up to 30 contacts and can keep track of 6 open contacts at the same time due to ST scheduling limitations\nVersion: v1.0 / 2019-11-05"
}