/* 
*  
*/ 
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field
metadata 
{
	definition(
		name: "Smart Home Monitor2",
		author: "rleonard55",
		description: "Control Smart Home Monitor",
		category: "Security",
		iconUrl: "http://cdn.device-icons.smartthings.com/Home/home2-icn.png",
		iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home2-icn@2x.png",
		iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png")
		{
			capability "Switch"
			capability "Refresh"        
		}
	
	// UI tile definitions
	tiles 
	{
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) 
		{
			state "off", label: 'Disarmed', action: "switch.on", icon: "st.Kids.kid10", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Armed', action: "switch.off", icon: "st.Kids.kid10", backgroundColor: "#79b821", nextState: "off"
		}
	}
}

def on() 
{
	initialize()
	log.debug "Turning On"
 	sendLocationEvent(name: "alarmSystemStatus", value: "stay")
	sendEvent(name: "switch", value: "on")
}

def off() 
{
	log.debug "Turning Off"
	sendLocationEvent(name: "alarmSystemStatus", value: "off")
	sendEvent(name: "switch", value: "off")
}

def inputHandler(evt) 
{
	log.debug "This event name is ${evt.name}"
    log.debug "Input: ${evt}"
    
    def value
    switch(evt.value) 
	{
        case "stay":
            value = "armed_home"
			on()
        	break
        case "away":
            value = "armed_away"
			on()
        	break
        case "off":
            value = "disarmed"
			off()
       		break
        default:
            value = false
        	break
    }
	
    if ( value == false ) 
	{
       log.debug "Unknown event: ${evt.value}"
       return
    }
}

//preferences {}

def installed() 
{
    log.debug "Installed with settings: ${settings}"
    //runEvery15Minutes(initialize)
    initialize()
}

def updated() 
{
	log.debug "Updated with settings: ${settings}"
	//unsubscribe()
	initialize()
}

def initialize() 
{
	//unsubscribe()
	log.debug "Initializing"
    // Subscribe to events from SmartThings 
    subscribe(location, "alarmSystemStatus", inputHandler)
    subscribe(location, "Security", inputHandler)
}