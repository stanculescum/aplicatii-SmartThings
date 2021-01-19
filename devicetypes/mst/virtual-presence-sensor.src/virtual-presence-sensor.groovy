/**
 *  Virtual Presence Sensor
 *
 *  Copyright 2020
 *
 *  This handler was designed to have the ability to set presence on a virtual presence sensor in the new app
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
 *  v1.0 (19/01/2021) - Initial Release
 *
 */

metadata {
    // Automatically generated. Make future change here.
    definition (name: "Virtual Presence Sensor", namespace: "mST", author: "Mihail Stanculescu") {
        capability "Presence Sensor"
        capability "Sensor"
        capability "Health Check"
        capability "Switch"

        command "arrived"
        command "departed"
    }

    simulator {
        status "present": "presence: present"
        status "not present": "presence: not present"
    }

}

def parse(String description) {
    def pair = description.split(":")
    createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def installed() {
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
    sendEvent(name: "healthStatus", value: "online")
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
}

// handle commands
def arrived() {
    
    on()
    
}


def departed() {
    
    off()
    
}

def on() {

log.debug "${device.displayName} - Turning Switch On and Setting Presence to Present"

sendEvent(name: "presence", value: "present", isStateChange: true, display: true, displayed: true)

sendEvent(name: "switch", value: "on", isStateChange: true, display: true, displayed: true)

}

def off() {

log.debug "${device.displayName} - Turning Switch Off and Setting Presence to Not Present"

sendEvent(name: "presence", value: "not present", isStateChange: true, display: true, displayed: true)

sendEvent(name: "switch", value: "off", isStateChange: true, display: true, displayed: true)

}