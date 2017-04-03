/**
 *  Arrival Motion Lights
 *
 *  Copyright 2017 Rob Leonard
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
    name: "Arrival Motion Lights",
    namespace: "rleonard55",
    author: "Rob Leonard",
    description: "Toggles the motion light to ensure they are on when someone arrives. Waits for a few seconds after that person has entered the door and toggles then again slower to reset them back to motion mode. ",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn@2x.png")

preferences {
	page(name: "settings")
}
def settings() {
    dynamicPage(name: "settings", title: "Turn switches off after some minutes", uninstall: true, install: true) {
        section("When the following arrives..."){
            input "myPresence", "capability.presenceSensor", title: "Presence Sensor?", multiple: true, required: true
        }
        section("Quickly toggle this switch...") {
            input "mySwitches", "capability.switch", title: "Switch?", multiple: true, required: true
        }
        section("Wait for ...") {
            input "myContact", "capability.contactSensor", title: "Contact Sensor?", required: false
        }
        section("Timeout after/if...") {
            input "myTimeoutMinutes", "number", title: "Timeout Minutes?", defaultValue: 5, required: true
        }
        section("Between these times...") {
            input "startingX", "enum", title: "Starting at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
            if(startingX in [null, "A specific time"]) 
            	input "starting", "time", title: "Start time", required: false
            else if(startingX == "Sunrise") 
            	input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: true, defaultValue: 0
            else input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: true, defaultValue: 0
                }
        section() {
            input "endingX", "enum", title: "Ending at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
            if(endingX in [null, "A specific time"]) 
            	input "ending", "time", title: "End time", required: false
            else if(endingX == "Sunrise") 
            	input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, defaultValue: 0
            else input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, defaultValue: 0
                } 
        section("Additional Settings") {
        	input "modes", "mode", title: "Only when mode is", multiple: true, required: false
        	input "myForceSeconds", "number", title: "Force wait Seconds?", defaultValue: 1, required: true
            input "myResetSeconds", "number", title: "Reset wait Seconds?", defaultValue: 3, required: true
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
	log.debug "Running Initalized"
	subscribe(myPresence, "presence.present", forceOn)
    subscribe(myContact, "contact.closed", reset)
	// TODO: subscribe to attributes, devices, locations, etc.
}

def forceOn(evt) {
	log.debug "Running force"
    
    if(state.LastForceTime!= null) {
    	runIn(myTimeoutMinutes*60,timeout)
    	log.debug "Multiple recent attempts, extending timeout"
    	return }
        
    if(!timeToRun()) {
    	log.debug "Not time to run, skipping."
    	return }
    
    if(!modeOk()) {
    	log.debug "Mode mismatch, skipping."
    	return }
        
	log.debug "Running 'Force On'"
    state.LastForceTime = now()
    state.LastResetTime = null
    
    def delay = 1000*myForceSeconds
	def initialActionOn = mySwitches.collect{it.currentSwitch == "on"}
        
    mySwitches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                	log.debug "Turning off "+s
					s.off() }} 
    pause(delay)
    
    mySwitches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                	log.debug "Turning on "+s
					s.on() }}
    
    runIn(myTimeoutMinutes*60,timeout)
}
def reset(evt) {
	log.debug "Running 'Reset'"
    state.LastForceTime = null
    state.LastResetTime = now()
    
    def delay= 1000*myResetSeconds
	def initialActionOn = mySwitches.collect{it.currentSwitch == "on"}
   
    mySwitches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                	log.debug "Turning off "+s
					s.off() }}
    pause(delay)
    
    mySwitches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                	log.debug "Turning on "+s
					s.on() }}
}
def timeout() {
	log.debug "Running 'Timeout'"
    if(state.LastResetTime == null)
    	reset(null)
}

private timeToRun() {

	log.debug "Running timeToRun"
	def start = null
	def stop = null
    def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)

    if(starting!= null)
    	start = timeToday(starting,location.timeZone)
    if(ending!= null)
        stop = timeToday(ending,location.timeZone)
        
    if(startingX == "Sunrise")
    	start = s.sunrise
     if(startingX == "Sunset")
    	start = s.sunset
     if(endingX == "Sunrise")  
      	stop = s.sunrise
     if(endingX == "Sunset")
     	stop = s.sunset
	
     if(stop < start) 
     	stop = stop + 1
    
    log.debug "start: ${start} | stop: ${stop}"
    return timeOfDayIsBetween(start, stop, (new Date()), location.timeZone)
}

private timeToRun2(){

    def currTime = now()
	def start = null
	def stop = null
    def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)

    if(starting!= null)
    	start = timeToday(starting,location.timeZone)
    if(ending!= null)
        stop = timeToday(ending,location.timeZone)
        
        log.debug "sunrise: ${s.sunrise.time} | sunset: ${s.sunset.time}"   
        
	if(startingX == "Sunrise")
    	start = timeToday(s.sunrise.time,location.timeZone)
     if(startingX == "Sunset")
    	start = timeToday(s.sunset.time,location.timeZone)
     if(endingX == "Sunrise")  
      	stop = timeToday(s.sunrise.time,location.timeZone)
     if(endingX == "Sunset")
     	stop = timeToday(s.sunset.time,location.timeZone)
     
     if(stop < start) 
     	stop = stop + 1
        
     log.debug "Start: ${start} | Stop: ${stop} | current: ${currTime}"   
     return (currTime >= start && currTime <= stop)
}
private modeOk() {
	log.debug "Running modeOk"
	if(modes == null) return true
	def result = !modes || modes.contains(location.mode)
	return result
}