/**
 *  Fibaro Z-Wave FGK-101 Temperature & Door/Window Sensor Handler [v0.9.5.4, 3 December 2018]
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
 
/******************************************************************************************************************************
 *	Fibaro Z-Wave FGK-101 Marketing Description is at :
 *		http://www.fibaro.com/en/the-fibaro-system/door-window-sensor
 *
 *  Fibaro FGK-10x Operating Manual can be downloaded at :
 *		http://www.fibaro.com/files/instrukcje/eng/DoorWindowSensor%20FGK-101-107%20ENG_v21-v23.pdf
 *
 *	The current version of this Handler is parameterized to force Device's wakeup :
 *		- on any open<->closed state change
 *		- in case of Tampering Alarm triggering
 *		- every 60mn (wakeUpIntervalSet(seconds:60*60), hard coded)
 *		- whenever Temperature delta change since last report is greater than 0.31°C (Parameter#12, hard coded)
 *		also :
 *		- Temperature is natively reported by sensor in Celsius (SensorMultilevelReport[scale:0]);
 *		  convertion is needed for Fahrenheit display 
 *
 *  A few specificities of this device that are relevant to better understand some parts of this Handler :
 *		- it is a battery operated device, so Commands can only be sent to it whenever it wakes up
 *		- it is a multi-channel Device, and the multi-level temperature sensor reports only from EndPoint#2
 *		- specific configurable parameters are documented in the above Operating Manual
 *		- some of those parameters must be modified to activate the anti-Tampering Alarm
 *		- some of the "scaffolding" has been left in place as comments, since it may help other people to understand/modify this Handler
 *		- BEWARE : the optional DS18B20 sensor must be connected BEFORE the Device is activated (otherwise, reset the Device)
 *		- IMPORTANT : for debugging purpose, it is much better to change the wake-up period from the default 60mn to 1mn or so;
 *					but unless you force the early wake up of the sensor (forcing open/closed for instance), you will have to
 *					wait up to 60mn for the new value to become effective.
 *
 * Z-Wave Device Class: GENERIC_TYPE_SENSOR_BINARY / SPECIFIC_TYPE_ROUTING_SENSOR_BINARY
 * FGK-101 Raw Description [EndPoint:0] : "0 0 0x2001 0 0 0 c 0x30 0x9C 0x60 0x85 0x72 0x70 0x86 0x80 0x84 0x7A 0xEF 0x2B"
 * Command Classes supported according to Z-Wave Certificate ZC08-14070004 for FGK-101\US :
 * 	Used in Handler :
 *		- 0x20 - 32  : BASIC					V1
 *		  0x30 - 48  : SENSOR_BINARY			!V1! V2
 *		- 0x31 - 49  : SENSOR_MULTILEVEL		V1 !V2! V3 V4 V5
 *		- 0x56 - 86  : CRC_16_ENCAP				V1
 *		  0x60 - 96  : MULTI_CHANNEL			V3
 *		  0x70 - 112 : CONFIGURATION			V1 !V2!
 *		  0x72 - 114 : MANUFACTURER_SPECIFIC 	V1 !V2!
 *		  0x80 - 128 : BATTERY					V1
 *		  0x84 - 132 : WAKE_UP					V1 !V2!
 *		  0x85 - 133 : ASSOCIATION				V1 !V2!
 *		  0x86 - 134 : VERSION					V1
 *		  0x98 - 152 : SECURITY					V1 [only latest versions of FGK-101]
 *		  0x9C - 156 : SENSOR_ALARM				V1
 *	NOT used in Handler :
 *		  0x2B - 43  : SCENE_ACTIVATION			V1	
 *
 *	 also found in FGK-101 Raw Description, in addition to Z-Wave Certificate for FGK-101\US [?!!] :
 *		+ 0x7A - 122 : FIRMWARE_UPDATE_MD		V1 V2
 *		+ 0xEF - 239 : MARK  					V1
 ******************************************************************************************************************************/

/******************************************************************************************************************************
 *	List of Known Bugs / Oddities / Missing Features :
 *		- valueTitle does not show displayNames on mobile Dashboard/Things page;
 *		  attempted workaround using : valueTile(){unit:'${displayName}') failed
 *		- valueTile behaves differently on mobile Dashboard (interpolated colors) from Simulator (step-wise colors)
 *		- using Preferences values instead of hard-coded values for some parameters would be nicer
 *****************************************************************************************************************************/

metadata {
	definition (name: "Fibaro Door/Window Senzor FGK-101", namespace: "mST", author: "Mihail Stanculescu") {
		capability "Contact Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Temperature Measurement"
		capability "Sensor"
		capability "Alarm"
        
        command "reportNext", ["string"]
        
        attribute "reportASAP", "number"
        attribute "deviceTime", "number"

        // FGK-101 Raw Description [EndPoint:0] : "0 0 0x2001 0 0 0 c 0x30 0x9C 0x60 0x85 0x72 0x70 0x86 0x80 0x84 0x7A 0xEF 0x2B"
		fingerprint deviceId: "0x2001", inClusters: "0x30, 0x60, 0x70, 0x72, 0x80, 0x84, 0x85, 0x9C"  // should include "0x20, 0x31" too ?!!
	}

	simulator {
		status "open":  "command: 2001, payload: FF"
		status "closed": "command: 2001, payload: 00"

        def T_values=[10,14,14.9,15,17,17.9,18,19,19.9,20,22,22.9,23,24,44,44.9,45,46,100]
        def float Ti
        for (int i = 0; i <= T_values.size()-1; i += 1) {
            Ti=T_values.get(i)
        	def theSensorValue = [(short)0, (short)0, (short)(Ti*100)/256, (short)(Ti*100)%256]
			// status "temperature ${Ti}°C":  zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2).encapsulate(zwave.sensorMultilevelV2.sensorMultilevelReport(scaledSensorValue: i, precision: 2, scale: 0, sensorType: 1, sensorValue: theSensorValue, size:4)).incomingMessage()
        }
	}

	tiles { 
    	valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
        	// label:'${name}', label:'${currentValue}', unit:"XXX" work, but NOT label:'${device.name}', label:'${displayName}', unit:'${unit}', ...
			state "temperature", label:'${currentValue}°\n', unit:"C", icon: "st.alarm.temperature.normal",
			// redondant lines added to avoid color interpolation on Dashboard (a feature or a bug ?!)
            backgroundColors:[							// ***on IDE Simulator***		// ***on iPad App***
				[value: 14, color: "#0033ff"],			//     °C <=14 : dark blue		//     °C <=14	: dark blue 
				    //[value: 14.1, color: "#00ccff"],	<- decimal value IGNORED by the Tile !!!
					//[value: 14.5],					// 15< °C <=19 : light blue		// 14< °C <15	: interpolated dark blue<-> light blue
                    [value: 15, color: "#00ccff"],		// 16< °C <=19 : light blue		// 15<=°C <=19	: light blue
                [value: 17, color: "#00ccff"],			// 16< °C <=19 : light blue		// 15<=°C <=19	: light blue
					//[value: 17.5],					// 15< °C <=19 : light blue		// 14< °C <15	: interpolated light blue<->blue-green
                	[value: 18, color: "#ccffcc"],		// 15< °C <=19 : light blue		// 18<=°C <=19	: blue-green
				[value: 19, color: "#ccffcc"],			// 15< °C <=19 : light blue		// 19°C			: blue-green
                    //[value: 19.5],					// 19< °C <=21 : blue-green		// 19< °C <20	: interpolated blue-green<->green
                	[value: 20, color: "#ccff00"],		// 19< °C <=21 : blue-green		// 20<=°C <=21	: green
				[value: 22, color: "#ccff00"],			// 21< °C <=23 : green			// 22°C			: green
					//[value: 22.5],					// 23< °C <=45 : orange			// 22< °C <23	: interpolated green<-> orange
					[value: 23, color: "#ffcc33"],		// 23< °C <=45 : orange  		// 23<=°C <=44	: orange
				[value: 43, color: "#ffcc33"],			// 23< °C <=45 : orange  		// 44°C			: orange
					//[value: 43.5],					// 45< °C      : red			// 44< °C <45	: interpolated orange <-> red
               		[value: 44, color: "#ff3300"]		// 45< °C      : red  			// 45<=°C		: red
			]
		}

        standardTile("contact", "device.contact") {
			state "open", label: 'open'/* in English :'${name}' */, icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
			state "closed", label: 'closed'/* in English :'${linkText}' */, icon: "st.contact.contact.closed", backgroundColor: "#79b821"
		}
        
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'batt. @ ${currentValue}%' /*battery*/, unit:""
		} 
        
        //Select temperatureF if Location temperature Scale is °F
        main(["temperature"])
		details(["temperature", "contact", "battery"])
        //main(["temperatureF"])
		//details(["temperatureF", "contact", "battery"])
	}
}

////////////////////////////////
// parse events into attributes
////////////////////////////////

def parse(String description) {
		state.parseCount=state.parseCount+1
		settings.debugLevel = 2		// set to 1 or 2 when experimenting
		if (debugLevel>=1) {log.debug "--------------------------Parsing... ; state.parseCount: ${state.parseCount}--------------------------"}
		if (debugLevel>=2) {log.debug "Parsing... '${description}'"}
        def result = null
        def cmd = zwave.parse(description, [0x20:1, 0x30:1, 0x31:2, 0x56:1, 0x60:3, 0x70:2, 0x72:2, 0x80:1, 0x84:2, 0x85:2, 0x9C:1])
        if (cmd) {
                result = zwaveEvent(cmd)
                if (debugLevel>=1) {log.debug "Parsed ${cmd} to ${result.inspect()}"}
        } else {
                log.debug "Non-parsed event: ${description}"
        }
        return result
}

//SmartThings v2 Hub forces CRC16-encoded replies from FGK-101 Device (v1 Hub did not)
def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	log.debug "CRC16.......... cmd : ${cmd}"
    def versions = [0x20:1, 0x30: 1, 0x31: 2, 0x60: 3, 0x70: 2, 0x72: 2, 0x84: 2, 0x9C: 1]
	// def encapsulatedCommand = cmd.encapsulatedCommand(versions)
	def version = versions[cmd.commandClass as Integer]
    log.debug "commandClass : ${cmd.commandClass}"
    log.debug "version : ${version}"
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
    log.debug "ccObj : ${ccObj}"
    log.debug "cmd.command : ${cmd.command}"
    log.debug "cmd.data : ${cmd.data}"
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (!encapsulatedCommand) {
		log.debug "Could not extract command from ${cmd}"
	} else {
		zwaveEvent(encapsulatedCommand)
	}
}

// Devices that support the Security command class can send messages in an
// encrypted form; they arrive wrapped in a SecurityMessageEncapsulation
// command and must be unencapsulated
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
        def encapsulatedCommand = cmd.encapsulatedCommand([0x98: 1, 0x20: 1])
        // can specify command class versions here like in zwave.parse
        if (encapsulatedCommand) {
    			log.debug "encapsulatedCommand : ${encapsulatedCommand}"
                return zwaveEvent(encapsulatedCommand)
        }
}

def temperatureScaleFC(tempvalue) {
	//FGK-101 is natively °C; convert to °F if selected in settings
	def float tempFC = tempvalue
	if (location.temperatureScale == "F") {
		tempFC = tempvalue * 1.8 + 32
	}
	return tempFC
}

def wakeUpResponse(cmdBlock) {
	//Initialization... (executed only once, when the Handler has been updated)
    //All untouched parameters are supposed to be DEFAULT (as factory-set)
    if (state.isInitialized == false) {
    	if (debugLevel>=2) {log.debug "state.isInitialized : ${state.isInitialized}"}
        cmdBlock << zwave.wakeUpV2.wakeUpIntervalSet(seconds:60*60, nodeid:zwaveHubNodeId).format() // NB : may have to wait 60mn for that value to be refreshed !
        cmdBlock << "delay 1200"
        // NOTE : any asynchronous temperature query thru SensorMultilevelGet() does NOT reset the delta-Temp base value (managed by DS18B20 hardware)
        // Adjust temperature report sensitivity for outside thermometers whose displayName starts with "*"
        def byte tempQuantumSixteenth
    	if (device.displayName.substring(0,1).equals("*")) {
        	tempQuantumSixteenth = 16	/* 16/16=1°C = 1.8°F */
        } else {
        	tempQuantumSixteenth = 5	/* 5/16=0.31°C = 0.56°F */
        }
        cmdBlock << zwave.configurationV2.configurationSet(parameterNumber: 12/*for FGK101*/, size: 1, configurationValue: [tempQuantumSixteenth]).format()
        cmdBlock << "delay 1200"     
        // inclusion of Device in Association#3 is needed to get delta-Temperature notification messages [cf Parameter#12 above]
        cmdBlock << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
        cmdBlock << "delay 1200"
        // inclusion of Device in Association#2 is needed to enable SensorAlarmReport() Command [anti-Tampering protection]
        cmdBlock << zwave.associationV2.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
        cmdBlock << "delay 1200"
        // inclusion of Device in Association#4 is needed for backward compatibility with non Z-Wave+ controlers
        cmdBlock << zwave.associationV2.associationSet(groupingIdentifier:4, nodeId:[zwaveHubNodeId]).format()
        cmdBlock << "delay 1200"
        // inclusion of Device in Association#5 is needed for backward compatibility with non Z-Wave+ controlers
        cmdBlock << zwave.associationV2.associationSet(groupingIdentifier:5, nodeId:[zwaveHubNodeId]).format()
        cmdBlock << "delay 1200"
        state.isInitialized = true
        if (debugLevel>=2) {log.debug "state.isInitialized : ${state.isInitialized}"}
    }
    
	//Regular Commands...
    def long nowTime = new Date().getTime()
    // Next line needed because "update()" does not seem to work anymore
    state.batteryInterval = (long) (24*60-45)*60*1000  // 1 day
    if (nowTime-state.lastReportBattery > state.batteryInterval) {
		cmdBlock << zwave.batteryV1.batteryGet().format()
        cmdBlock << "delay 1200"
        //next 2 lines redondant since any open/closed status change is asynchronously notified... but useful in case of missing basicSet notification
    	cmdBlock << zwave.basicV1.basicGet().format()
    	cmdBlock << "delay 1200"
    }

    //next 2 lines redondant too : SensorBinaryReport(EndPoint: 1) == BasicReport

    cmdBlock << zwave.wakeUpV2.wakeUpIntervalGet().format() // NB : may have to wait 60mn for that value to be refreshed !
    cmdBlock << "delay 1200"
    cmdBlock << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint: 2, destinationEndPoint: 2, commandClass:0x31/*Sensor Multilevel*/, command:4/*Get*/).format()
    cmdBlock << "delay 1200"
    cmdBlock << zwave.wakeUpV2.wakeUpNoMoreInformation().format()
    cmdBlock << "delay 2000"
    if (debugLevel>=2) {
        log.debug "wakeUpNoMoreInformation()"
        log.debug "cmdBlock : ${cmdBlock}"
    }
    return cmdBlock
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
		// IMPORTANT NOTE : when the batteryLevel becomes too low, Device reports become erratic, all periodic wakeUpNotifications stop
        // and consequently BATTERYLEVEL IS NOT UPDATED ANYMORE every 24 hours, continuing to display the last (and obsolete) reported value.
        // Curiously, asynchronous sensorMultilevelReports continue to arrive, for some time, making the Device look (partially) "alive"
    	if (debugLevel>=2) {log.debug "wakeupv2.WakeUpNotification $cmd"}
        def event = createEvent(descriptionText: "${device.displayName} woke up", isStateChange: true, displayed: false)
        def cmdBlock = []
        cmdBlock=wakeUpResponse(cmdBlock)
        return [event, response(cmdBlock)]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd) {
	// IMPORTANT NOTE : when the batteryLevel becomes too low, Device reports become erratic, all periodic wakeUpNotifications stop
	// and consequently BATTERYLEVEL IS NOT UPDATED ANYMORE every 24 hours, continuing to display the last (and obsolete) reported value.
	// Curiously, asynchronous sensorMultilevelReports continue to arrive, for some time, making the Device look (partially) "alive"
	// This section resets the displayed battery level to 1% when the battery level is obsolete by more than 48h.
    state.batteryInterval = (long) (24*60-45)*60*1000  // 1 day
    def long nowTime = new Date().getTime()
    if (nowTime-state.lastReportBattery > 3*state.batteryInterval) {  // reset batteryLevel to 1% if no update for 48-72 hours
    	log.debug "obsolete (likely low) battery value : ${((nowTime-state.lastReportBattery)/3600000)} hours old"
        sendEvent(name: "battery", displayed: true, isStateChange:true, unit: "%", value: 1, descriptionText: "${device.displayName} has a low battery")
	    state.lastReportBattery = nowTime
	}

        def float scaledSensorValue = cmd.scaledSensorValue
        // Adjust measured temperature based on previous manual calibration; FGK-101 is natively °C
        switch (device.name) {
            case 'T005' :										//JJG	
            	scaledSensorValue = scaledSensorValue + 0.0554
    			log.debug "Temp Adjust for : ${device.name}"
                break;
            case 'T006' :										//MLE
            	scaledSensorValue = scaledSensorValue + 0.0297
    			log.debug "Temp Adjust for : ${device.name}"
                break;
            case 'T003' :										//MPT
            	scaledSensorValue = scaledSensorValue - 0.0603
    			log.debug "Temp Adjust for : ${device.name}"
                break;
            case 'T002' :										//NBN	
            	scaledSensorValue = scaledSensorValue - 0.0758
    			log.debug "Temp Adjust for : ${device.name}"
                break;
            case 'T004' :										//SCU
            	scaledSensorValue = scaledSensorValue + 0.0011
    			log.debug "Temp Adjust for : ${device.name}"
                break;
            case 'T007' :										//FSU
            	scaledSensorValue = scaledSensorValue + 0.0025
    			log.debug "Temp Adjust for : ${device.name}"
                break;
            case 'T008' :										
            	scaledSensorValue = scaledSensorValue - 0.0146
    			log.debug "Temp Adjust for : ${device.name}"
                break;
            case 'T009' :										
            	scaledSensorValue = scaledSensorValue + 0.0383
    			log.debug "Temp Adjust for : ${device.name}"
                break;
            case 'T010' :										
            	scaledSensorValue = scaledSensorValue + 0.0383
    			log.debug "Temp Adjust for : ${device.name}"
                break;
            case 'T011' :										
            	scaledSensorValue = scaledSensorValue - 0.0889
    			log.debug "Temp Adjust for : ${device.name}"
                break;
            case 'T012' :										
            	scaledSensorValue = scaledSensorValue - 0.0532
    			log.debug "Temp Adjust for : ${device.name}"
                break;
            case 'T013' :										
            	scaledSensorValue = scaledSensorValue + 0.0383
    			log.debug "Temp Adjust for : ${device.name}"
                break;
            case 'T014' :										//*ext*//
            	scaledSensorValue = scaledSensorValue - 0.0160
    			log.debug "Temp Adjust for : ${device.name}"
                break;
        }
        //Round to nearest 1 decimal temperature value; convert to °F if needed
        def float ftempSign = temperatureScaleFC(scaledSensorValue) < 0 ? -1 : +1
		def float ftemp = ftempSign * ((((temperatureScaleFC(scaledSensorValue).abs()*100+5)/10).intValue()*1.0)/10)
        if (debugLevel>=2) {
        	log.debug "ftempSign : ${ftempSign}"
        	log.debug "ftemp : ${ftemp}"
        }
        // Next line needed because "update()" does not seem to work anymore
    	state.maxEventInterval = (long) (4*60-10)*60*1000  // at least 1 Temperature Report event every 4 hours
        nowTime = new Date().getTime()
        if (debugLevel>=2) {
        	log.debug "cmd.scaledSensorValue : ${cmd.scaledSensorValue}"
        	log.debug "correction : ${scaledSensorValue-cmd.scaledSensorValue}"
    		log.debug "device.displayName : ${device.displayName}"
    		log.debug "'Date().getTime()' : ${new Date().getTime()}"
            log.debug "state.forcedWakeUp : ${state.forcedWakeUp}"
            log.debug "state.maxEventInterval : ${state.maxEventInterval}"
    		log.debug "state.lastReportTime : ${state.lastReportTime}"
    		log.debug "nowTime : ${nowTime}"
    		log.debug "(nowTime-state.lastReportTime > state.maxEventInterval) : ${(nowTime-state.lastReportTime > state.maxEventInterval)}"
    		log.debug "ftemp : ${ftemp}"
            log.debug "state.lastReportedTemp: ${state.lastReportedTemp}"
        }
        // Adjust temperature report sensitivity for outside thermometers whose displayName starts with "*"
        def float tempQuantum
    	if (device.displayName.substring(0,1).equals("*")) {
        	tempQuantum = temperatureScaleFC(0.9999)-temperatureScaleFC(0)
        } else {
        	tempQuantum = temperatureScaleFC(0.2999)-temperatureScaleFC(0)
        }
        log.debug "((ftemp-state.lastReportedTemp).abs()>${tempQuantum}): ${(ftemp-state.lastReportedTemp).abs()>tempQuantum}"
        if (((ftemp-state.lastReportedTemp).abs()>tempQuantum) || ((nowTime-state.lastReportTime) > state.maxEventInterval) || state.forcedWakeUp) {
        	def map = [ displayed: true, value: ftemp.toString(), isStateChange:true, linkText:"${device.displayName}" ]
        	switch (cmd.sensorType) {
                case 1:
                        map.name = "temperature"
                        map.unit = cmd.scale == 1 ? "F" : "C"
                        //ignores Device's native temperature scale, ftemp already converted to °F if settings as such
                        map.unit = location.temperatureScale
                        log.debug "map.value : ${map.value}"
                        log.debug "map.unit : ${map.unit}"
                        break;
        	}
			if (debugLevel>=2) {
        		log.debug "temperature Command : ${map.inspect()}"
        	}
        	state.lastReportedTemp = ftemp
            state.lastReportTime = nowTime
            state.forcedWakeUp = false
            // For Test purpose; redondant with reportNext() => state.forcedWakeUp=1
            if (device.currentValue('reportASAP')==1) {sendEvent(name: "reportASAP", value: 0, isStateChange: true)}
        	return createEvent(map)
        }
}

def sensorValueEvent(value) {
	if (value) {
		createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
	} else {
		createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
	}
}

// BasicReport should not be necessary since all status change notifications are asynchronous via BasicSet
// But useful as defensive programming, in case of missed notifications, to make sure latest change has been properly reported and registered
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	sensorValueEvent(cmd.value)
    if (debugLevel>=2) {log.debug "basicv1.BasicReport $cmd.value"}
    def cmdValue = cmd.value
	return openClosed(cmd, cmdValue)
}

// To check that WakeUpInterval does not revert to 1mn instead of 1h
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
    if (debugLevel>=2) {log.debug "WakeUpIntervalReport $cmd"}
    if (cmd.seconds!=60*60) {
    	def result = createEvent(name:"WakeUpIntervalReport", descriptionText:"${device.displayName} had ${cmd.seconds} seconds wakeUp period", isStateChange:true, displayed:true, linkText:"${device.displayName}")
   		configure()
    }
    return result
}

def openClosed(cmd, cmdValue) {
    def theState = cmdValue == 0 ? "closed" : "open"
    if (debugLevel>=2) {log.debug "openClosed $cmd"}
    // Use closed/open sensor notification to trigger push of updated Temperature value and immediate setting of updated device parameters
    // Sometimes, Temperature forced refresh stops working : SensorMultilevelGet() Commands are stacked but not executed immediately;
    // will restart after some time, and stacked Commands will be executed !
    def event = createEvent(name:"contact", value:"${theState}", descriptionText:"${device.displayName} is ${theState}", isStateChange:true, displayed:true, linkText:"${device.displayName}")
    state.forcedWakeUp = true
    def cmdBlock = []
    cmdBlock=wakeUpResponse(cmdBlock)
    return [event, response(cmdBlock)]
}
    
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    if (debugLevel>=2) {log.debug "basicv1.BasicSet $cmd"}
    def cmdValue = cmd.value
	return openClosed(cmd, cmdValue)
}

// SensorBinaryReport should never occur since all status change notifications are asynchronous via BasicSet
def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
    if (debugLevel>=2) {log.debug "sensorbinaryv1.SensorBinaryReport $cmd"}
    def cmdValue = cmd.sensorValue
	return openClosed(cmd, cmdValue)
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd) {
	//def event = sensorValueEvent(cmd.sensorState)
    if (debugLevel>=2) {log.debug "sensoralarmv1.SensorAlarmReport $cmd.sensorState"}
    def event = createEvent(name:"alarm", descriptionText:"${device.displayName} is tampered with !", isStateChange:true, displayed:true, linkText:"${device.displayName}")
    def cmdBlock = []
    state.forcedWakeUp = true
    cmdBlock=wakeUpResponse(cmdBlock)
    return [event, response(cmdBlock)]
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    // Next line needed because "update()" does not seem to work anymore
    state.batteryInterval = (long) (24*60-45)*60*1000  // 1 day
    def long nowTime = new Date().getTime()
    if (debugLevel>=2) {
    	log.debug "batteryv1.BatteryReport ${cmd.batteryLevel}"
    	log.debug "nowTime : ${nowTime}"
    	log.debug "state.lastReportBattery : ${state.lastReportBattery}"
    	log.debug "state.batteryInterval : ${state.batteryInterval}"
        log.debug "state.forcedWakeUp : ${state.forcedWakeUp}"
    }
    if ((nowTime-state.lastReportBattery > state.batteryInterval) || state.forcedWakeUp) {
		def map = [ name: "battery", displayed: true, isStateChange:true, unit: "%" ]
		if (cmd.batteryLevel == 0xFF) {
			map.value = 1
			map.descriptionText = "${device.displayName} has a low battery"
			map.isStateChange = true
		} else {
			map.value = cmd.batteryLevel
		}
    	state.lastReportBattery = nowTime
        log.debug "battery map : ${map}"
    	return [createEvent(map)]
    }
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    if (debugLevel>=2) {log.debug "ConfigurationReport - Parameter#${cmd.parameterNumber}: ${cmd.configurationValue}"}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd) {
    if (debugLevel>=2) {log.debug "multichannelv3.MultiChannelCapabilityReport: ${cmd}"}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) {
    if (debugLevel>=2) {log.debug "multichannelv3.MultiChannelCapabilityReport: ${cmd}"}
}
 
def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    if (debugLevel>=2) {log.debug "versionv1.VersionReport: ${cmd}"}
}
 
// MultiChannelCmdEncap and MultiInstanceCmdEncap are ways that devices can indicate that a message
// is coming from one of multiple subdevices or "endpoints" that would otherwise be indistinguishable
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
        def encapsulatedCommand = cmd.encapsulatedCommand([0x30: 1, 0x31: 2]) // can specify command class versions here like in zwave.parse
        if (debugLevel>=2) {log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")}
        if (encapsulatedCommand) {
                return zwaveEvent(encapsulatedCommand)
        }
}

// Catch All command Handler in case of unexpected message
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "!!! $device.displayName: ${cmd}", displayed: false)
}

// When a Temperature Event got lost in transit, the Watchdog requests a forced report at next wake up
// The "reportNext()" alarm command is used to signal back from the Watchdog SmartApp to the sleepy Device Handler
def reportNext(commandMsg) {
	log.debug "reportNext !"
    log.debug "commandMsg : ${commandMsg}"
    state.forcedWakeUp = true
		// IMPORTANT NOTE : when the batteryLevel becomes too low, Device reports become erratic, all periodic wakeUpNotifications stop
        // and consequently BATTERYLEVEL IS NOT UPDATED ANYMORE every 24 hours, continuing to display the last (and obsolete) reported value.
        // Curiously, asynchronous sensorMultilevelReports continue to arrive, for some time, making the Device look (partially) "alive"
    	// This section resets the displayed battery level to 1% when the battery level is obsolete by more than 48h.
	// Next line may be needed because "update()" does not seem to work reliably anymore
    state.batteryInterval = (long) (24*60-45)*60*1000  // 1 day
    def long nowTime = new Date().getTime()
    if (nowTime-state.lastReportBattery > 3*state.batteryInterval) {  // reset batteryLevel to 1% if no update for 48-72 hours
    	log.debug "obsolete (likely low) battery value : ${((nowTime-state.lastReportBattery)/3600000)} hours old"
        sendEvent(name: "battery", displayed: true, isStateChange:true, unit: "%", value: 1, descriptionText: "${device.displayName} has a low battery")
	    state.lastReportBattery = nowTime
	}
	return []
}

///////////////////
// For Tests Purpose
///////////////////

// Executed each time the Handler is updated
def updated() {
	log.debug "Updated !"
    // All state.xxx attributes are Device-local, NOT Location-wide
    state.isInitialized = false
    state.lastReportedTemp = (float) -1000
    state.lastReportTime = (long) 0
    state.lastReportBattery = (long) 0
	// Real-time clock of sensors (ceramic resonator) is up to 3% inaccurate
    state.batteryInterval = (long) (24*60-45)*60*1000  // 1 Battery Report event every 24 hours, rounded up to the nearest hourly wakeup
    state.maxEventInterval = (long) (4*60-10)*60*1000  // at least 1 Temperature Report event every 3:50 hours
    state.parseCount=(int) 0
    state.forcedWakeUp = true
    if (!(state.deviceID)) {state.deviceID = device.name}
    log.debug "state.deviceID: ${state.deviceID}"
    log.debug "state.batteryInterval : ${state.batteryInterval}"
    log.debug "state.maxEventInterval : ${state.maxEventInterval}"
    // For Test purpose; redondant with reportNext() => state.forcedWakeUp=1
    sendEvent(name: "reportASAP", value: 1, isStateChange: true)
    log.debug "device.currentValue('reportASAP') : ${device.currentValue('reportASAP')}"

    infos()
}


// If you add the Configuration capability to your device type, this command will be called right
// after the device joins to set device-specific configuration commands.
def configure() {
	log.debug "Configuring..."
    // Adjust temperature report sensitivity for outside thermometers whose displayName starts with "*"
    def byte tempQuantumSixteenth
    log.debug "device.displayName.substring(0,1) : ${device.displayName.substring(0,1)}"
    if (device.displayName.substring(0,1).equals("*")) {
    	tempQuantumSixteenth = 16	/* 16/16=1°C = 1.8°F */
    } else {
    	tempQuantumSixteenth = 5	/* 5/16=0.31°C = 0.56°F */
    }
    log.debug "tempQuantumSixteenth : ${tempQuantumSixteenth}"
	delayBetween([
		// Make sure sleepy battery-powered sensors send their WakeUpNotifications to the hub
		zwave.wakeUpV2.wakeUpIntervalSet(seconds:60*60, nodeid:zwaveHubNodeId).format(),
		// NOTE : any asynchronous temperature query thru SensorMultilevelGet() does NOT reset the delta-Temp base value (managed by DS18B20 hardware)
		zwave.configurationV2.configurationSet(parameterNumber: 12/*for FGK101*/, size: 1, configurationValue: [tempQuantumSixteenth]).format(),
        // inclusion of Device in Association#3 is needed to get delta-Temperature notification messages [cf Parameter#12 above]
        zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format(),
        // inclusion of Device in Association#2 is needed to enable SensorAlarmReport() Command [anti-Tampering protection]
        zwave.associationV2.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format(),
        // get zwave version information
        zwave.versionV1.versionGet().format()
	],1200)
}

def infos() {
	if (!state.devices) { state.devices = [:] }
    log.debug "zwaveHubNodeId: ${zwaveHubNodeId}"					// -> "1"
    log.debug "device.displayName: ${device.displayName}"			// -> "JJG"
    log.debug "device.id: ${device.id}"							// -> "75841488-ae76-4cac-b523-a2694e72c25a"
    log.debug "device.name: ${device.name}"						// -> "T001"
    log.debug "device.label: ${device.label}"						// -> "JJG"
    log.debug "device.data: ${device.data}"   					// -> "[endpointId:0, version: 2.1, MSR:010F-0700-2000]"
    //log.debug "'device.rawDescription': ${device.rawDescription}"	// -> "0 0 0x2001 0 0 0 c 0x30 0x9C 0x60 0x85 0x72 0x70 0x86 0x80 0x84 0x7A 0xEF 0x2B"
}