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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.services.component.ComponentRegistry;
import gov.nasa.arc.mct.services.component.CreateWizardUI;

import javax.swing.JButton;
import javax.swing.JComponent;

/**
 * 
 * Decorates an object creation wizard such that, when the object is 
 * created, it is added as a child to some other object (specified 
 * at construction time by component ID.)
 *
 */
public class RepositoryWizardDecorator extends CreateWizardUI {
	private CreateWizardUI wizard;
	private String repositoryID;
	
	public RepositoryWizardDecorator(CreateWizardUI wizard, String repositoryID) {
		super();
		this.wizard = wizard;
		this.repositoryID = repositoryID;
	}

	@Override
	public AbstractComponent createComp(ComponentRegistry comp,
			AbstractComponent parentComp) {
		AbstractComponent created = wizard.createComp(comp, parentComp);
		
		// Also persist the created component to the designated repository,
		// unless the component was created in a repository.
		if (parentComp.getCapability(RepositoryCapability.class) == null) {
			AbstractComponent repository = comp.getComponent(repositoryID);		
			repository.addDelegateComponent(created);
			repository.save();
		}
		
		return created;
	}

	@Override
	public JComponent getUI(JButton create) {
		return wizard.getUI(create);
	}

	@Override
	public String toString() {
		return wizard.toString();
	}
}
