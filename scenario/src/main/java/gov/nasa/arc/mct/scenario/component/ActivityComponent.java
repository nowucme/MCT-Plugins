package gov.nasa.arc.mct.scenario.component;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.JAXBModelStatePersistence;
import gov.nasa.arc.mct.components.ModelStatePersistence;
import gov.nasa.arc.mct.components.PropertyDescriptor;
import gov.nasa.arc.mct.components.PropertyDescriptor.VisualControlDescriptor;
import gov.nasa.arc.mct.scenario.component.TimePropertyEditor.TimeProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents an Activity user object.
 * 
 * Activities have start and end times, associated costs, and plain-text types. 
 * Activities may contain "sub-activities" (other activities); note that these 
 * sub-activities must always be contained entirely within their parent's 
 * time span.
 *
 */
public class ActivityComponent extends CostFunctionComponent implements DurationCapability {
	private final AtomicReference<ActivityModelRole> model = new AtomicReference<ActivityModelRole>(new ActivityModelRole());
	
	/**
	 * Get the underlying data about this Activity (start time, end time, costs, type...)
	 * @return underlying activity data
	 */
	public ActivityData getData() {
		return getModel().getData();
	}
		
	@Override
	public Set<AbstractComponent> getAllModifiedObjects() {
		// Note that this is necessary to support Save All
		// ("all modified objects" is the All in Save All;
		//  typically, this should be all dirty children.)
		
		// TODO: What about cycles?
		Set<AbstractComponent> modified = new HashSet<AbstractComponent>();
		for (AbstractComponent child : getComponents()) {
			if (child.isDirty()) {
				modified.add(child);
			}
			modified.addAll(child.getAllModifiedObjects());
		}
		return modified;
	}

	@Override
	protected <T> T handleGetCapability(Class<T> capability) {
		// Note: Don't report self as capability until initialized.
		if (capability.isAssignableFrom(getClass())) {
			return capability.cast(this);
		}
		if (capability.isAssignableFrom(ModelStatePersistence.class)) {
		    JAXBModelStatePersistence<ActivityModelRole> persistence = new JAXBModelStatePersistence<ActivityModelRole>() {

				@Override
				protected ActivityModelRole getStateToPersist() {
					return model.get();
				}

				@Override
				protected void setPersistentState(ActivityModelRole modelState) {
					model.set(modelState);
				}

				@Override
				protected Class<ActivityModelRole> getJAXBClass() {
					return ActivityModelRole.class;
				}
		        
			};
			
			return capability.cast(persistence);
		}
		return null;
	}
	
	@Override
	public List<CostFunctionCapability> getInternalCostFunctions() {
		return Arrays.<CostFunctionCapability>asList(new CostFunctionStub(true), new CostFunctionStub(false));
	}

	/**
	 * Get the container for underlying Activity data
	 * @return the container for underlying Activity data (its model)
	 */
	public ActivityModelRole getModel() {
		return model.get();
	}

	@Override
	public List<PropertyDescriptor> getFieldDescriptors()  {

		// Provide an ordered list of fields to be included in the MCT Platform's InfoView.
		List<PropertyDescriptor> fields = new ArrayList<PropertyDescriptor>();


		// We specify a mutable text field.  The control display's values are maintained in the business model
		// via the PropertyEditor object.  When a new value is to be set, the editor also validates the prospective value.
		PropertyDescriptor type = new PropertyDescriptor("Activity Type",
				new TypePropertyEditor(this), VisualControlDescriptor.TextField);
		type.setFieldMutable(true);
		PropertyDescriptor startTime = new PropertyDescriptor("Start Time", 
				new TimePropertyEditor(this, TimeProperty.START),  VisualControlDescriptor.TextField);
		startTime.setFieldMutable(true);
		PropertyDescriptor endTime = new PropertyDescriptor("End Time", 
				new TimePropertyEditor(this, TimeProperty.END),  VisualControlDescriptor.TextField);
		endTime.setFieldMutable(true);
		PropertyDescriptor duration = new PropertyDescriptor("Duration",
				new TimePropertyEditor(this, TimeProperty.DURATION), VisualControlDescriptor.TextField);
		duration.setFieldMutable(true);
		PropertyDescriptor power = new PropertyDescriptor("Power (W)", 
				new PowerPropertyEditor(this),  VisualControlDescriptor.TextField);
		power.setFieldMutable(true);
		PropertyDescriptor comm = new PropertyDescriptor("Comm (Kb/s)", 
				new CommPropertyEditor(this),  VisualControlDescriptor.TextField);
		comm.setFieldMutable(true);
		PropertyDescriptor notes = new PropertyDescriptor("Notes", 
				new NotesPropertyEditor(this),  VisualControlDescriptor.TextArea);
		notes.setFieldMutable(true);

		fields.add(type);
		fields.add(startTime);
		fields.add(endTime);
		fields.add(duration);
		fields.add(power);
		fields.add(comm);
		fields.add(notes);

		return fields;
	}
	
	/**
	 * Get the named type associated with this activity.
	 * @return
	 */
	public String getType() {
		return getData().getActivityType();
	}

	// Maintain set of components being constrained to avoid cycles
	private static ThreadLocal<Set<String>> ignoreSet = new ThreadLocal<Set<String>>() {
		@Override
		protected Set<String> initialValue() {
			return new HashSet<String>();
		}		
	};
	
	@Override
	public long getStart() {
		long start = getData().getStartTime().getTime();
		ignoreSet.get().add(getComponentId());
		for (AbstractComponent child : getComponents()) {
			if (!ignoreSet.get().contains(child.getComponentId())) {
				DurationCapability dc = child.getCapability(DurationCapability.class);
				if (dc != null) {			
					start = Math.min(start, dc.getStart());
				}
			}
		}
		ignoreSet.get().remove(getComponentId());
		return start;
	}

	@Override
	public long getEnd() {
		long end = getData().getEndTime().getTime();
		ignoreSet.get().add(getComponentId());
		for (AbstractComponent child : getComponents()) {
			if (!ignoreSet.get().contains(child.getComponentId())) {
				DurationCapability dc = child.getCapability(DurationCapability.class);
				if (dc != null) {			
					end = Math.max(end, dc.getEnd());
				}
			}
		}
		ignoreSet.get().remove(getComponentId());
		return end;
	}	
	
	public void setType(String type) {
		getData().setActivityType(type);
	}

	@Override
	public void setStart(long start) {

		getData().setStartDate(new Date(start > 0 ? start : 0));
	}

	@Override
	public void setEnd(long end) {
		getData().setEndDate(new Date(end > getStart() ? end : getStart()));
	}

	
	/**
	 * Stub implementation of cost functions for activity components.
	 * In the future, this should be generalizable to include more than 
	 * just Comms and Power
	 * 
	 * @author vwoeltje
	 *
	 */
	private class CostFunctionStub implements CostFunctionCapability {
		private boolean isComm; //Otherwise, is power
		
		public CostFunctionStub(boolean isComm) {
			super();
			this.isComm = isComm;
		}

		@Override
		public String getName() {
			return isComm ? "Comms" : "Power";
		}

		@Override
		public String getUnits() {
			return isComm ? "KB/s" : "Watts";
		}

		@Override
		public double getValue(long time) {
			if (time < getStart() || time >= getEnd()) {
				return 0;
			} else {
				return isComm ? getData().getComm() : getData().getPower();
			}
		}

		@Override
		public Collection<Long> getChangeTimes() {
			return Arrays.asList(getStart(), getEnd());
		}
		
	}


}