package il.co.tel_ran.carservice;

/**
 * Created by maxim on 12-Nov-16.
 */

public class TenderReply {

    private ServiceStation mReplyingService;

    private String mReplyMessage;

    public TenderReply(ServiceStation replyingStation, String message) {
        mReplyingService = replyingStation;
        mReplyMessage = message;
    }

    public void setReplyingService(ServiceStation service) {
        mReplyingService = service;
    }

    public ServiceStation getReplyingService() {
        return mReplyingService;
    }

    public void setReplyMessage(String message) {
        mReplyMessage = message;
    }

    public String getReplyMessage() {
        return mReplyMessage;
    }
}
