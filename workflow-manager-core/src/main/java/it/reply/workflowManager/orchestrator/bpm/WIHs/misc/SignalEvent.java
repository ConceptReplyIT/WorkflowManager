package it.reply.workflowManager.orchestrator.bpm.WIHs.misc;


public class SignalEvent<T> {

	public enum SignalEventType {
		ERROR("Error");
		
		private String name;
		
		SignalEventType(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public enum SignalEventSubType {
		GENERIC("Generic"), PERSISTENCE("Persistence");
		
		private String name;
		
		SignalEventSubType(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}

	private SignalEventType type;
	private SignalEventSubType subType;
	private T payload;

	public SignalEvent(SignalEventType type, SignalEventSubType subType) {
		this(type, subType, null);
	}

	public SignalEvent(SignalEventType type, SignalEventSubType subType, T payload) {
		this.type = type;
		this.subType = subType;
		this.setPayload(payload);
	}

	public SignalEventType getType() {
		return type;
	}

	public void setType(SignalEventType type) {
		this.type = type;
	}
	
	public SignalEventSubType getSubType() {
		return subType;
	}

	public void setSubType(SignalEventSubType subType) {
		this.subType = subType;
	}
	
	public String getName() {
		return new StringBuilder(type.toString()).append("-").append(subType.toString()).append(type.toString()).toString();
	}

	public T getPayload() {
		return payload;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}

}
