package il.co.tel_ran.carservice.connection;

import il.co.tel_ran.carservice.ServiceReview;

/**
 * Created by maxim on 24-Dec-16.
 */

public class ReviewDataResult extends DataResult<ServiceReview> {

    public ReviewDataResult(ServiceReview[] data) {
        super(Type.REVIEW, data);
    }
}
