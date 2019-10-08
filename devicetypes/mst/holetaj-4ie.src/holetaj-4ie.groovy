/**
 *  HolEtaj 4IE
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
 *
 *	VERSION HISTORY
 *	08.10.2018 	v1.0 BETA Release 5 - Compatibility for New Smartthings App.
 *	10.12.2017 	v1.0 BETA Release 4 - Fix to boost functionality when thermostat is off.
 *	10.12.2017 	v1.0 BETA Release 3 - Add boost functionality.
 *	05.01.2017	v1.0 BETA Release 2 - Minor fix that prevented 'Manual' mode being selected and activated.
 *  14.12.2016	v1.0 BETA - Initial Release
 */
import groovy.time.TimeCategory 

preferences 
{
	input( "boostInterval", "number", title: "Boost Interval (minutes)", description: "Boost interval amount in minutes", required: false, defaultValue: 10 )
    input( "boostTemp", "decimal", title: "Boost Temperature (°C)", description: "Boost interval amount in Centigrade", required: false, defaultValue: 22, range: "5..32" )
    input( "disableDevice", "bool", title: "Disable Warmup Heating Device?", required: false, defaultValue: false )
}

metadata {
	definition (name: "HolEtaj 4IE", namespace: "mST", author: "Mihail stanculescu", ocfDeviceType: "oic.d.thermostat", mnmn: "SmartThings", vid: "SmartThings-smartthings-Z-Wave_Thermostat") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Temperature Measurement"
        capability "Thermostat"
        capability "Thermostat Mode"
		capability "Thermostat Heating Setpoint"
		capability "Switch"
        capability "Health Check"
        
        command "heatingSetpointUp"
		command "heatingSetpointDown"
        command "setThermostatMode"
        command "setHeatingSetpoint"
        command "setTemperatureForSlider"
        command "setTemperature"
        command "setBoostLength"
        command "boostButton"
        command "boostTimeUp"
		command "boostTimeDown"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {

		multiAttributeTile(name: "thermostat", width: 6, height: 4, type:"thermostat") {
			tileAttribute("device.temperature", key:"PRIMARY_CONTROL", canChangeBackground: true){
				attributeState "default", label: '${currentValue}°', unit:"C", 
                backgroundColors:[
					[value: 0, color: "#153591"],
					[value: 10, color: "#1e9cbb"],
					[value: 13, color: "#90d2a7"],
					[value: 17, color: "#44b621"],
					[value: 20, color: "#f1d801"],
					[value: 25, color: "#d04e00"],
					[value: 29, color: "#bc2323"]
				]
			}
            tileAttribute ("statusMsg", key: "SECONDARY_CONTROL") {
				attributeState "statusMsg", label:'${currentValue}'
			}
            tileAttribute("device.temperature", key: "VALUE_CONTROL") {
    				attributeState("default", action: "setTemperature")
  			}
  			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
    				attributeState("off", label:'Off')
    				attributeState("heat", label:'Manual')
    				attributeState("cool", label:'Manual')
    				attributeState("auto", label:'Schedule')
                    attributeState("emergency heat", label:'Boost')
  			}
  			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
                    attributeState "default", label: '${currentValue}', backgroundColors: [
				// Celsius Color Range
				[value: 0, color: "#153591"],
					[value: 10, color: "#1e9cbb"],
					[value: 13, color: "#90d2a7"],
					[value: 17, color: "#44b621"],
					[value: 20, color: "#f1d801"],
					[value: 25, color: "#d04e00"],
					[value: 29, color: "#bc2323"]
			]}
		}
        
        valueTile("temperature", "device.temperature", width: 2, height: 2){
			state "default", label: '${currentValue}°', unit:"C", 
            backgroundColors:[
				[value: 0, color: "#153591"],
				[value: 10, color: "#1e9cbb"],
				[value: 13, color: "#90d2a7"],
				[value: 17, color: "#44b621"],
				[value: 20, color: "#f1d801"],
				[value: 25, color: "#d04e00"],
				[value: 29, color: "#bc2323"]
			]
		}
        
        valueTile("heatingSetpoint", "device.desiredHeatSetpoint", width: 2, height: 2) {
			state "default", label:'${currentValue}°', unit:"C",
            backgroundColors:[
                [value: 0, color: "#153591"],
					[value: 10, color: "#1e9cbb"],
					[value: 13, color: "#90d2a7"],
					[value: 17, color: "#44b621"],
					[value: 20, color: "#f1d801"],
					[value: 25, color: "#d04e00"],
					[value: 29, color: "#bc2323"]
            ]
		}
        
        valueTile("averageAir", "device.averageAir", width: 2, height: 2) {
			state "default", label:'${currentValue}°', unit:"C",
            backgroundColors:[
                [value: 0, color: "#153591"],
					[value: 10, color: "#1e9cbb"],
					[value: 13, color: "#90d2a7"],
					[value: 17, color: "#44b621"],
					[value: 20, color: "#f1d801"],
					[value: 25, color: "#d04e00"],
					[value: 29, color: "#bc2323"]
            ]
		}
        standardTile("heatingSetpointUp", "device.desiredHeatSetpoint", width: 1, height: 1, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointUp", label:'  ', action:"heatingSetpointUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#ffffff"
		}

		standardTile("heatingSetpointDown", "device.desiredHeatSetpoint", width: 1, height: 1, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointDown", label:'  ', action:"heatingSetpointDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#ffffff"
		}
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("default", label:'refresh', action:"polling.poll", icon:"st.secondary.refresh-icon")
		}        
        
        standardTile("switch", "device.switch", decoration: "flat", height: 2, width: 2, inactiveLabel: false) {
			state "on", label:'${name}', action:"switch.off", icon:"st.Home.home1", backgroundColor:"#f1d801"
			state "off", label:'${name}', action:"switch.on", icon:"st.Home.home1", backgroundColor:"#ffffff"
		}
        
        standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: true, decoration: "flat", width: 2, height: 2) {
			state("auto", label: "SCHEDULED", icon:"st.Office.office7")
            state("heat", label: "FIXED TEMPERATURE", icon:"st.Weather.weather2")
            state("emergency heat", label: "OVERRIDE", icon:"st.Health & Wellness.health7")
			state("off", icon:"st.thermostat.heating-cooling-off")
		}
        
        standardTile("mode_auto", "device.mode_auto", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"auto", label:'Schedule', icon:"st.Office.office7"
    	}
        
        standardTile("mode_manual", "device.mode_manual", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"heat", label:'Manual', icon:"st.Weather.weather2"
   	 	}
        
        standardTile("mode_off", "device.mode_off", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"off", icon:"st.thermostat.heating-cooling-off"
   	 	}
        
        valueTile("boost", "device.boostLabel", inactiveLabel: false, decoration: "flat", width: 2, height: 2, wordwrap: true) {
			state("default", label:'${currentValue}', action:"boostButton")
		}
        
         standardTile("boostTimeUp", "device.boostLength", width: 1, height: 1, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointUp", label:'  ', action:"boostTimeUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#ffffff"
		}
		standardTile("boostTimeDown", "device.boostLength", width: 1, height: 1, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointDown", label:'  ', action:"boostTimeDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#ffffff"
		}
        
        main(["temperature"])
		details(["thermostat", "mode_auto", "mode_manual", "mode_off", "heatingSetpointUp", "heatingSetpoint", "boost", "boostTimeUp", "heatingSetpointDown", "boostTimeDown", "refresh"])
	}
}

def installed() {
	log.debug "Executing 'installed'"
    state.desiredHeatSetpoint = 7
	// execute handlerMethod every 1 minutes.
    runEvery1Minutes(poll)
    sendEvent(name: "checkInterval", value: 20 * 60 + 2 * 60, data: [protocol: "cloud"], displayed: false)
}

def updated() {
	log.debug "Executing 'updated'"
	// execute handlerMethod every 1 minutes.
    unschedule()
    runEvery1Minutes(poll)
    sendEvent(name: "checkInterval", value: 20 * 60 + 2 * 60, data: [protocol: "cloud"], displayed: false)
}

def uninstalled() {
	log.debug "Executing 'uninstalled'"
	unschedule()
}

// parse events into attributes
def parse(String description) {
}

// handle commands

def off() {
	setThermostatMode('off')
}

def on() {
	setThermostatMode('auto')
	
}

def heat() {
	setThermostatMode('heat')
}

def auto() {
	setThermostatMode('auto')
}

def setHeatingSetpoint(temp) {
	log.debug "Executing 'setHeatingSetpoint with temp $temp'"
	def latestThermostatMode = device.latestState('thermostatMode')
    
    if (temp < 5) {
		temp = 5
	}
	if (temp > 32) {
		temp = 32
	}
    def args
    if (settings.disableDevice == null || settings.disableDevice == false) {
    	//if thermostat is off, set to manual 
   		if (latestThermostatMode.stringValue == 'off') {
    		args = [
        		method: "setProgramme", roomId: device.deviceNetworkId, roomMode: "fixed"
        	]
			parent.apiPOSTByChild(args)
    	}
        args = [
        	method: "setProgramme", roomId: device.deviceNetworkId, roomMode: "fixed", fixed: [fixedTemp: "${(temp * 10) as Integer}"]
        ]
        parent.apiPOSTByChild(args)  
    	
    }
    runIn(3, refresh)
}

def setHeatingSetpointToDesired() {
	setHeatingSetpoint(state.newSetpoint)
}

def setNewSetPointValue(newSetPointValue) {
	log.debug "Executing 'setNewSetPointValue' with value $newSetPointValue"
    state.newSetpoint = newSetPointValue
    state.desiredHeatSetpoint = state.newSetpoint
	sendEvent("name":"desiredHeatSetpoint", "value": state.desiredHeatSetpoint, displayed: false)
	log.debug "Setting heat set point up to: ${state.newSetpoint}"
    setHeatingSetpointToDesired()
}

def heatingSetpointUp(){
	log.debug "Executing 'heatingSetpointUp'"
	setNewSetPointValue(getHeatTemp().toInteger() + 1)
}

def heatingSetpointDown(){
	log.debug "Executing 'heatingSetpointDown'"
	setNewSetPointValue(getHeatTemp().toInteger() - 1)
}

def setTemperature(value) {
	log.debug "Executing 'setTemperature with $value'"
    def currentTemp = device.currentState("temperature").doubleValue
	(value < currentTemp) ? (setNewSetPointValue(getHeatTemp().toInteger() - 1)) : (setNewSetPointValue(getHeatTemp().toInteger() + 1))
}

def setTemperatureForSlider(value) {
	log.debug "Executing 'setTemperatureForSlider with $value'"
	setNewSetPointValue(value)  
}

def getHeatTemp() { 
	return state.desiredHeatSetpoint == null ? device.currentValue("heatingSetpoint") : state.desiredHeatSetpoint
}

def emergencyHeat() {
	log.debug "Executing 'boost'"
	
    def latestThermostatMode = device.latestState('thermostatMode')
    
    //Don't do if already in BOOST mode.
	if (latestThermostatMode.stringValue != 'emergency heat') {
		setThermostatMode('emergency heat')
    }
    else {
    	log.debug "Already in boost mode."
    }
}


def setThermostatMode(mode) {
	if (settings.disableDevice == null || settings.disableDevice == false) {
		mode = mode == 'cool' ? 'heat' : mode
		log.debug "Executing 'setThermostatMode with mode $mode'"
    	def args
    	if (mode == 'off') {
        	//Sets whole location to off instead of individual thermostat. Awaiting Warmup API update.
            args = [
        		method: "setRunModeByRoomIdArray", roomIdArray: [device.deviceNetworkId as Integer], values: [runMode: "frost"]
        	]
        	parent.apiPOSTByChild(args)
        	
     		parent.setLocationToFrost()
    	} else if (mode == 'heat') {
    		args = [
        		method: "setProgramme", roomId: device.deviceNetworkId, roomMode: "fixed"
        	]	
            parent.apiPOSTByChild(args)
    	} else if (mode == 'emergency heat') {  
    		if (state.boostLength == null || state.boostLength == '')
        	{
        		state.boostLength = 60
            	sendEvent("name":"boostLength", "value": 60, displayed: true)
        	}
            args = [
        		method: "setOverride", rooms: ["$device.deviceNetworkId"], type: 3, temp: getBoostTempValue(), until: getBoostEndTime()
        	]
            parent.apiPOSTByChild(args)
   		} else {
        	args = [
        		method: "setProgramme", roomId: device.deviceNetworkId, roomMode: "prog"
        	]
            parent.apiPOSTByChild(args)
        }
		mode = mode == 'range' ? 'auto' : mode    	
    }
    runIn(3, refresh)
}

def refreshBoostLabel() {
	def boostLabel = "Start\n$state.boostLength Min Boost"
    def latestThermostatMode = device.latestState('thermostatMode')  
    if (latestThermostatMode.stringValue == 'emergency heat' ) {
    	boostLabel = "Restart\n$state.boostLength Min Boost"
    }
    if (latestThermostatMode.stringValue == 'off' ) {
    	boostLabel = "Boost not\navailable"
    }
    sendEvent("name":"boostLabel", "value": boostLabel, displayed: false)
}

def setBoostLength(minutes) {
	log.debug "Executing 'setBoostLength with length $minutes minutes'"
    if (minutes < 10) {
		minutes = 10
	}
	if (minutes > 240) {
		minutes = 240
	}
    state.boostLength = minutes
    sendEvent("name":"boostLength", "value": state.boostLength, displayed: true)
    refreshBoostLabel()  
}

def getBoostIntervalValue() {
	if (settings.boostInterval == null) {
    	return 10
    } 
    return settings.boostInterval.toInteger()
}

def getBoostTempValue() {
	log.debug "Executing 'getBoostTempValue'"
	def retVal 
	if (settings.boostTemp == null) {
    	retVal = "220"
    } 
    retVal = ((settings.boostTemp as Integer) * 10) as String
    return retVal
}

def getBoostEndTime() {
	log.debug "Executing 'getBoostEndTime'"
	use( TimeCategory ) {
    	def endDate = new Date() + state.boostLength.minutes
		return endDate.format("HH:mm")
	}
}

def boostTimeUp() {
	log.debug "Executing 'boostTimeUp'"
    //Round down results
    int boostIntervalValue = getBoostIntervalValue()
    def newBoostLength = (state.boostLength + boostIntervalValue) - (state.boostLength % boostIntervalValue)
	setBoostLength(newBoostLength)
}

def boostTimeDown() {
	log.debug "Executing 'boostTimeDown'"
    //Round down result
    int boostIntervalValue = getBoostIntervalValue()
    def newBoostLength = (state.boostLength - boostIntervalValue) - (state.boostLength % boostIntervalValue)
	setBoostLength(newBoostLength)
}

def boostButton() {
	log.debug "Executing 'boostButton'"
	setThermostatMode('emergency heat')
}

def poll() {
    log.debug "Executing 'poll'"
	def room = parent.getStatus(device.deviceNetworkId)
	if (room == []) {
		log.error("Unexpected result in parent.getStatus()")
		return []
	}
    log.debug room
    def modeMsg = ""
    def airTempMsg = ""
    def mode = room.runMode[6]
    if (mode == "fixed") mode = "heat"
    else if (mode == "off" || mode == "frost") mode = "off"
    else if (mode == "prog") mode = "auto"
    else if (mode == "override") mode = "emergency heat"
    sendEvent(name: 'thermostatMode', value: mode) 
    modeMsg = "Mode: " + mode.toUpperCase() + "."
    
 	//Boost button label
        if (state.boostLength == null || state.boostLength == '')
        {
        	state.boostLength = 60
            sendEvent("name":"boostLength", "value": 60, displayed: true)
        }
    	def boostLabel = "Start\n$state.boostLength Min Boost"
    
    //If Warmup heating device is set to disabled, then force off if not already off.
    if (settings.disableDevice != null && settings.disableDevice == true && activeHeatCoolMode != "OFF") {
    	//Sets whole location to off instead of individual thermostat. Awaiting Warmup API update.
        args = [
        		method: "setRunModeByRoomIdArray", roomIdArray: [device.deviceNetworkId as Integer], values: [runMode: "frost"]
        	]
        parent.apiPOSTByChild(args)
        
        parent.setLocationToFrost()
    	mode = 'off'
    } else if (mode == "emergency heat") {       
        def boostTime = room.overrideDur
        boostLabel = "Restart\n$state.boostLength Min Boost"
        sendEvent("name":"boostTimeRemaining", "value": boostTime + " mins")
    }
    
    if (settings.disableDevice != null && settings.disableDevice == true) {
    	modeMsg = "DISABLED"
    }
    
    def temperature = String.format("%2.1f",(room.currentTemp[6] as BigDecimal)/ 10)
    sendEvent(name: 'temperature', value: temperature, unit: "C", state: "heat")
    
    def heatingSetpoint = String.format("%2.1f",(room.targetTemp[6] as BigDecimal) / 10)
    sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: "C", state: "heat")
    sendEvent(name: 'coolingSetpoint', value: heatingSetpoint, unit: "C", state: "heat")
    sendEvent(name: 'thermostatSetpoint', value: heatingSetpoint, unit: "C", state: "heat", displayed: false)
    
    sendEvent(name: 'thermostatFanMode', value: "off", displayed: false)
    
    def averageAir = String.format("%2.1f",(room.airTemp[6] as BigDecimal) / 10)
    sendEvent("name": "averageAir", "value": averageAir, unit: "C")
    sendEvent("name":"statusMsg", "value": modeMsg + " " + airTempMsg, displayed: false)
    
    state.desiredHeatSetpoint = (int) Double.parseDouble(heatingSetpoint)
    sendEvent("name":"desiredHeatSetpoint", "value": state.desiredHeatSetpoint, unit: "C", displayed: false)   
    
    airTempMsg = "Air Temp: " + averageAir +"°C."
    sendEvent("name":"statusMsg", "value": modeMsg + " " + airTempMsg, displayed: false)
    sendEvent("name":"boostLabel", "value": boostLabel, displayed: false)
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}