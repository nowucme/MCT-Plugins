/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
 *
 * The MCT platform is licensed under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 *
 * MCT includes source code licensed under additional open source licenses. See 
 * the MCT Open Source Licenses file included with this distribution or the About 
 * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
 * information. 
 *******************************************************************************/
package gov.nasa.arc.mct.scenario.component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Underlying model data for Activity components.
 * These includes costs (power/data), time/duration, and type.
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ActivityData {

	private Map<String, String> properties = new HashMap<String, String> ();
	
	public ActivityData() {
		init();
	}
	
	private void init() {

		properties.put("COMM", "0.0");
		properties.put("POWER", "0.0");
		properties.put("type", "");
		properties.put("notes", "");
		properties.put("startTime", "");
		properties.put("endTime", "");
		properties.put("url", "");
		properties.put("procedureUrl", "");
	}
	
	public String getValue(String key) {
		String value = "";
		if (properties.containsKey(key)) {
			value = properties.get(key);
		}
		return value;
	}
	
	public void setValue(String key, String value) {
		if (properties.containsKey(key)) {
			properties.put(key, value);
		}
	}

	public long getDurationTime()
	{
		long endTime = Long.parseLong(properties.get("endTime"));
		long startTime = Long.parseLong(properties.get("startTime"));
		return endTime - startTime;
	}
	
	public void setDurationTime(long duration)
	{		
		long startTime = Long.parseLong(properties.get("startTime"));
		properties.put("endTime", String.valueOf(startTime + duration));
	}
	
	public double getPower() {
		return Double.valueOf(getValue("POWER"));
	}

	public void setPower(double power) {
		setValue("POWER", String.valueOf(power));
	}

	public double getComm() {
		return Double.valueOf(getValue("COMM"));
	}

	public void setComm(double comm) {
		setValue("COMM", String.valueOf(comm));
	}
	
	public String getActivityType() {
		return getValue("type");
	}
	
	public void setActivityType(String type) {
		setValue("type", type);
	}
	
	public String getNotes() {
		return getValue("notes");
	}
	
	public void setNotes(String notes) {
		setValue("notes", notes);
	}

	public Date getStartTime() {
		return new Date(Long.valueOf(getValue("startTime")));
	}

	public void setStartDate(Date startDate) {
		setValue("startTime", String.valueOf(startDate.getTime()));
	}

	public Date getEndTime() {
		return new Date(Long.valueOf(getValue("endTime")));
	}

	public void setEndDate(Date endDate) {
		setValue("endTime", String.valueOf(endDate.getTime()));
	}
	
	public String getUrl() {
		return getValue("url");
	}
	
	public void setUrl(String url) {
		setValue("url", url);
	}

	public String getProcedureUrl() {
		return getValue("procedureUrl");
	}

	public void setProcedureUrl(String procedureUrl) {
		setValue("procedureUrl", procedureUrl);
	}
	
}
