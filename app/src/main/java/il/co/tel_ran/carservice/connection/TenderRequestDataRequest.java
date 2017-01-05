package il.co.tel_ran.carservice.connection;

import com.android.volley.Request;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EnumSet;

import il.co.tel_ran.carservice.ServiceSubWorkType;
import il.co.tel_ran.carservice.TenderRequest;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;
import il.co.tel_ran.carservice.VehicleType;

import static il.co.tel_ran.carservice.connection.TenderRequestMaker.JSON_FIELD_IDUSER;
import static il.co.tel_ran.carservice.connection.TenderRequestMaker.JSON_FIELD_USER_NAME;

/**
 * Created by maxim on 05-Jan-17.
 */

public class TenderRequestDataRequest extends DataRequest {

    private final TenderRequest mTenderRequest;

    public TenderRequestDataRequest() {
        this(Request.Method.GET, ServerConnection.TENDERS_URL, null);
    }

    public TenderRequestDataRequest(TenderRequest tenderRequest) {
        this(Request.Method.POST, ServerConnection.TENDERS_URL, tenderRequest);
    }

    private TenderRequestDataRequest(int requestMethod, String url, TenderRequest tenderRequest) {
        super(requestMethod, url);

        mTenderRequest = tenderRequest;
    }

    @Override
    public String getRequestParameters() {
        return "";
    }

    @Override
    public JSONObject getRequestJSON() throws JSONException {
        if (getRequestMethod() == Request.Method.POST) {

            JSONObject jsonObject = new JSONObject();

            if (mTenderRequest != null) {
                try {
                    jsonObject.put(JSON_FIELD_IDUSER, Long.toString(mTenderRequest.getIdUser()));
                    jsonObject.put(JSON_FIELD_USER_NAME, mTenderRequest.getSender());

                    Place locationPlace = mTenderRequest.getPlace();
                    JSONObject chosenPlace = new JSONObject();
                    if (locationPlace != null) {
                        chosenPlace.put(TenderRequestMaker.JSON_FIELD_LATITUDE,
                                locationPlace.getLatLng().latitude);
                        chosenPlace.put(TenderRequestMaker.JSON_FIELD_LONGITUDE,
                                locationPlace.getLatLng().longitude);
                        chosenPlace.put(TenderRequestMaker.JSON_FIELD_FORMATTED_ADDRESS,
                                locationPlace.getAddress());
                        chosenPlace.put(TenderRequestMaker.JSON_FIELD_PLACE_ID,
                                locationPlace.getId());
                    } else {
                        // Set default location as Israel.
                        LatLng defaultLatLang = Utils.getDefaultPlaceLatLang();
                        String[] defaultAddressAndId = Utils.getDefaultPlaceAddressAndId();
                        chosenPlace.put(TenderRequestMaker.JSON_FIELD_LATITUDE,
                                defaultLatLang.latitude);
                        chosenPlace.put(TenderRequestMaker.JSON_FIELD_LONGITUDE,
                                defaultLatLang.longitude);
                        chosenPlace.put(TenderRequestMaker.JSON_FIELD_FORMATTED_ADDRESS,
                                defaultAddressAndId[0]);
                        chosenPlace.put(TenderRequestMaker.JSON_FIELD_PLACE_ID,
                                defaultAddressAndId[1]);
                    }
                    jsonObject.put(TenderRequestMaker.JSON_FIELD_CHOSEN_PLACE, chosenPlace);

                    EnumSet<VehicleType> vehicleTypes = mTenderRequest.getVehicleTypes();

                    jsonObject.put(TenderRequestMaker.JSON_FIELD_BICYCLE,
                            vehicleTypes.contains(VehicleType.MOTORCYCLE));
                    jsonObject.put(TenderRequestMaker.JSON_FIELD_PASSCAR,
                            vehicleTypes.contains(VehicleType.PRIVATE));
                    jsonObject.put(TenderRequestMaker.JSON_FIELD_LORRY,
                            vehicleTypes.contains(VehicleType.TRUCK));
                    jsonObject.put(TenderRequestMaker.JSON_FIELD_BUS,
                            vehicleTypes.contains(VehicleType.BUS));
                    jsonObject.put(TenderRequestMaker.JSON_FIELD_MOPED,
                            false); // TODO: add vehicle type.

                    VehicleData vehicleData = mTenderRequest.getVehicleData();

                    String vehicleDataString = "";
                    if (vehicleData != null){
                        vehicleDataString = vehicleData.toString();
                    }
                    jsonObject.put(TenderRequestMaker.JSON_FIELD_CAR, vehicleDataString);

                    ArrayList<String> subWorkTypeStrings = new ArrayList<>();
                    for (ServiceSubWorkType subWorkType : mTenderRequest.getSubWorkTypes()) {
                        subWorkTypeStrings.add(ServiceSubWorkType.getFieldForType(subWorkType));
                    }
                    jsonObject.put(TenderRequestMaker.JSON_FIELD_SERVICE,
                            new JSONArray(subWorkTypeStrings));

                    jsonObject.put(TenderRequestMaker.JSON_FIELD_SUM,
                            Float.toString(mTenderRequest.getPrice()));

                    jsonObject.put(TenderRequestMaker.JSON_FIELD_COMMENT,
                            mTenderRequest.getMessage());

                    jsonObject.put(TenderRequestMaker.JSON_FIELD_DATE,
                            Utils.convertDateToDateTime(mTenderRequest.getDeadlineDate()));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return jsonObject;
        }

        return null;
    }

    @Override
    public String getRequestString() {
        // Not used for this request.
        return null;
    }
}
