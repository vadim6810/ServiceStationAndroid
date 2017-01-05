package il.co.tel_ran.carservice.connection;

import il.co.tel_ran.carservice.TenderRequest;

/**
 * Created by maxim on 05-Jan-17.
 */

public class TenderRequestDataResult extends DataResult<TenderRequest> {

    public TenderRequestDataResult(TenderRequest[] data) {
        super(Type.TENDER_REQUEST, data);
    }
}
