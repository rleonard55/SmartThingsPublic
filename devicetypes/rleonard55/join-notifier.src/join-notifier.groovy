/**
 *  Join Notifier
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
 
metadata {
	definition (name: "Join Notifier", namespace: "rleonard55", author: "Rob Leonard", iconUrl: "https://joaoapps.com/wp-content/uploads/2015/11/com.joaomgcd.join_-270x250.png") {
		command "push", ["string", "string", "string"]
        command "push", ["string", "string"]
        command "push", ["string"]
        command "pushUrl", ["string"]
        command "pushClipboard", ["string"]
        command "pushFile", ["string"]
        command "test"
        capability "Notification"
	}

	simulator {
		// TODO: define status and reply messages here
	}
    
	preferences {
        input "deviceId", "string", title: "Device Id", displayDuringSetup: true, required: true
        //input "apiKey", "text", title: "API Key", required: false
	}
    
	tiles {
		// TODO: define your main and details tiles here
        standardTile("name", "device.name", width: 2, height: 2, canChangeIcon: true, icon: "http://cdn.device-icons.smartthings.com/Office/office19-icn.png") {
        	state "name", label: "Test", action: "test"//, backgroundColor: "#59ab46"
        }
	}
}

def test() {
	log.debug "Executing 'test'"
    push("Hello $location")
}
def parse(String description) {
	log.debug "Parsing '${description}'"
    push(description)
}

def deviceNotification() {
	log.debug "Executing 'deviceNotification'"
	// TODO: handle 'deviceNotification' command
}
def deviceNotification(text) {
	push(text)
}

def push(text) {
	def Map = [:]
	Map.Text=text

    join_push(getRequestUrl(Map))
}
def push(text, title) {
	def Map = [:]
    Map.Text=text
    Map.Title=title
    
    join_push(getRequestUrl(Map))
}
def push(text, title, iconUrl) {
	def Map = [:]
    Map.Text=text
    Map.Title=title
    Map.Icon = iconUrl
    join_push(getRequestUrl(Map))
}
def pushUrl(url) {
	def Map = [:]
	Map.URL=url

    join_push(getRequestUrl(Map))
}
def pushClipboard(text){
	def Map = [:]
	Map.Clipboard=text

    join_push(getRequestUrl(Map))
}
def pushFile(Url) { 
	def Map = [:]
	Map.File=Url

    join_push(getRequestUrl(Map))
}

private getDeviceId() {	
	return getDevicePreferenceByName(device, "deviceId") 
}
private getServerUrl() { 
return "https://joinjoaomgcd.appspot.com/_ah/api/messaging/v1/sendPush?" }

private getRequestUrl(JoinMsg) {
	def ReturnUrl="https://joinjoaomgcd.appspot.com/_ah/api/messaging/v1/sendPush?"
	def encoding = "UTF-8"
    
    if(JoinMsg.Text!=null)
    	ReturnUrl+="text="+URLEncoder.encode(JoinMsg.Text.toString(),encoding)+"&"
    
    if(JoinMsg.Title!=null)
    	ReturnUrl+="title="+URLEncoder.encode(JoinMsg.Title.toString(), encoding)+"&"    
    
    if(JoinMsg.Icon!=null)
    	ReturnUrl+="icon="+URLEncoder.encode(JoinMsg.Icon.toString(), encoding)+"&" 
        
    if(JoinMsg.URL!=null)
    	ReturnUrl+="url="+URLEncoder.encode(JoinMsg.URL.toString(), encoding)+"&" 
    
    if(JoinMsg.Clipboard!=null)
    	ReturnUrl+="clipboard="+URLEncoder.encode(JoinMsg.Clipboard.toString(), encoding)+"&" 
    
    if(JoinMsg.File!=null)
    	ReturnUrl+="file="+JoinMsg.File+"&" 
    
    ReturnUrl+="deviceId="+getDeviceId().toString()
    
    log.debug "URL: "+ReturnUrl
    return ReturnUrl
}
private join_push(url) {
	
    def Response
    httpGet(url) { resp -> Response = resp.data}

    if(Response.success == true)
		log.info "Success"
    else
        log.error ("Failed to send "+Response)
}