package org.telcomp.sbb;

import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.Address;
import javax.slee.RolledBackContext;
import javax.slee.SbbContext;
import javax.slee.nullactivity.NullActivity;
import javax.slee.nullactivity.NullActivityContextInterfaceFactory;
import javax.slee.nullactivity.NullActivityFactory;

import net.java.slee.resource.sip.SleeSipProvider;

import org.telcomp.events.EndGetDataTelcoServiceEvent;
import org.telcomp.events.EndReceiveIMTelcoServiceEvent;
import org.telcomp.events.StartGetDataTelcoServiceEvent;
import org.telcomp.events.StartSendIMTelcoServiceEvent;

public abstract class ReceiveIMSbb implements javax.slee.Sbb {
	
	private SleeSipProvider sipFactoryProvider;
	private MessageFactory messageFactory;
	private NullActivityFactory nullActivityFactory;
	private NullActivityContextInterfaceFactory nullACIFactory;

	public void onMessage(RequestEvent event, ActivityContextInterface aci) {
		Request request = event.getRequest();
		ContentTypeHeader cont = (ContentTypeHeader) request.getHeader(ContentTypeHeader.NAME);
		System.out.println("*******************************************");
		System.out.println("ReceiveIMTelcoService Invoked");
		
		if(cont.getContentType().equals("text")){
			CallIdHeader callId = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
			String CallID = callId.getCallId();
			this.setCallID(CallID);
			ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
			String toName = this.getName(toHeader.getAddress().toString());
			FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
			this.setFromUser(fromHeader.getAddress().toString());
			String message = new String(request.getRawContent());
			this.setMessage(message);
			ServerTransaction st = (ServerTransaction) aci.getActivity();
			ActivityContextInterface nullAci = this.createNullActivityACI();
			try {
				Response response = messageFactory.createResponse(Response.OK, request);
				st.sendResponse(response);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(toName.equals("TelcompIM")){
				HashMap<String, Object> operationInputs = new HashMap<String, Object>();
				operationInputs.put("fromUserUri", (String) fromHeader.getAddress().toString());
				operationInputs.put("message", (String) message);
				EndReceiveIMTelcoServiceEvent EndReceiveIMTelcoServiceEvent = new EndReceiveIMTelcoServiceEvent(operationInputs);
				this.fireEndReceiveIMTelcoServiceEvent(EndReceiveIMTelcoServiceEvent, nullAci, null);
				System.out.println("Output FromUserUri = "+fromHeader.getAddress().toString());
				System.out.println("Output Message = "+message);
				System.out.println("*******************************************");
			} else{
				HashMap<String, Object> operationInputs = new HashMap<String, Object>();
				operationInputs.put("parameter", (String) "sipuri");
				operationInputs.put("identification", (String) toName);
				StartGetDataTelcoServiceEvent dataAccess = new StartGetDataTelcoServiceEvent(operationInputs);
				nullAci.attach(this.sbbContext.getSbbLocalObject());
				this.fireStartGetDataTelcoServiceEvent(dataAccess, nullAci, null);
			}
		} else{
			ServerTransaction st = (ServerTransaction) aci.getActivity();
			try {
				Response response = messageFactory.createResponse(Response.OK, request);
				st.sendResponse(response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		aci.detach(this.sbbContext.getSbbLocalObject());
	}
	
	public void onEndGetDataTelcoServiceEvent(EndGetDataTelcoServiceEvent event, ActivityContextInterface aci) {
		aci.detach(this.sbbContext.getSbbLocalObject());
		ActivityContextInterface nullAci = this.createNullActivityACI();
		System.out.println("*****************ToUri: "+event.getValue()+"**************FromUri: "+this.getFromUser());
		HashMap<String, Object> operationInputs = new HashMap<String, Object>();
		operationInputs.put("toUserUri", (String) event.getValue());
		operationInputs.put("fromUserUri", (String) this.getFromUser());
		operationInputs.put("message", (String) this.getMessage());
		operationInputs.put("callId", (String) this.getCallID());
		StartSendIMTelcoServiceEvent sendIM = new StartSendIMTelcoServiceEvent(operationInputs);
		this.fireStartSendIMTelcoServiceEvent(sendIM, nullAci, null);
	}
	
	private ActivityContextInterface createNullActivityACI() {
		NullActivity nullActivity = this.nullActivityFactory.createNullActivity();
		ActivityContextInterface nullActivityACI = null;
		nullActivityACI = this.nullACIFactory.getActivityContextInterface(nullActivity);
		return nullActivityACI;
	}
	
	private String getName(String prevName){
		return prevName.substring(prevName.indexOf(':')+1, prevName.indexOf('@'));
	}
	
	public abstract void setCallID(String callId);
	public abstract String getCallID();
	public abstract void setMessage(String message);
	public abstract String getMessage();
	public abstract void setFromUser(String fromUser);
	public abstract String getFromUser();


	
	// TODO: Perform further operations if required in these methods.
	public void setSbbContext(SbbContext context) { 
		this.sbbContext = context;
		try {
			Context ctx = (Context) new InitialContext().lookup("java:comp/env");
			sipFactoryProvider = (SleeSipProvider) ctx.lookup("slee/resources/jainsip/1.2/provider");
			messageFactory = sipFactoryProvider.getMessageFactory();
			nullActivityFactory = (NullActivityFactory) ctx.lookup("slee/nullactivity/factory");
			nullACIFactory = (NullActivityContextInterfaceFactory) ctx.lookup("slee/nullactivity/activitycontextinterfacefactory");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
    public void unsetSbbContext() { this.sbbContext = null; }
    
    // TODO: Implement the lifecycle methods if required
    public void sbbCreate() throws javax.slee.CreateException {}
    public void sbbPostCreate() throws javax.slee.CreateException {}
    public void sbbActivate() {}
    public void sbbPassivate() {}
    public void sbbRemove() {}
    public void sbbLoad() {}
    public void sbbStore() {}
    public void sbbExceptionThrown(Exception exception, Object event, ActivityContextInterface activity) {}
    public void sbbRolledBack(RolledBackContext context) {}
	
	public abstract void fireEndReceiveIMTelcoServiceEvent (EndReceiveIMTelcoServiceEvent event, ActivityContextInterface aci, Address address);
	public abstract void fireStartSendIMTelcoServiceEvent (StartSendIMTelcoServiceEvent event, ActivityContextInterface aci, Address address);
	public abstract void fireStartGetDataTelcoServiceEvent (StartGetDataTelcoServiceEvent event, ActivityContextInterface aci, Address address);

	
	/**
	 * Convenience method to retrieve the SbbContext object stored in setSbbContext.
	 * 
	 * TODO: If your SBB doesn't require the SbbContext object you may remove this 
	 * method, the sbbContext variable and the variable assignment in setSbbContext().
	 *
	 * @return this SBB's SbbContext object
	 */
	
	protected SbbContext getSbbContext() {
		return sbbContext;
	}

	private SbbContext sbbContext; // This SBB's SbbContext

}
