package gaya.pe.kr.network.packet.global;


import gaya.pe.kr.network.packet.anno.CanBeNull;
import gaya.pe.kr.network.packet.anno.WaitingResult;
import gaya.pe.kr.network.packet.type.ResponseType;

import java.util.UUID;

public class WaitAndGetPacketResponse {

    boolean wait = false;
    UUID requestUUID;
    ResponseType responseType = ResponseType.NONE;

    @CanBeNull
    Object responseObject;

    public WaitAndGetPacketResponse(UUID requestUUID) {
        this.requestUUID = requestUUID;
    }

    @CanBeNull
    public Object getResponseObject() {
        return responseObject;
    }

    public void setResponseObject(Object responseObject) {
        this.responseObject = responseObject;
    }

    public <T> boolean isEqualResponseObjectType(T t) throws NullPointerException {
        if ( responseObject == null ) throw new NullPointerException("Response Object is null");
        return t.getClass().getTypeName().equals(this.getResponseObject().getClass().getTypeName());
    }

    @WaitingResult
    public synchronized ResponseType executeAndGetResult() {

        if ( responseType.equals(ResponseType.NONE) ) {
            try {
                wait();
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }
        }

        return responseType;

    }
    
    public synchronized void setResponse(ResponseType response) {
        
        if ( responseType.equals(ResponseType.NONE) ) {
            this.responseType = response;
            notifyAll();
        }
        
    }


}
