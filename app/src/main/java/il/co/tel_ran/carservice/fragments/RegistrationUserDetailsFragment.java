package il.co.tel_ran.carservice.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import il.co.tel_ran.carservice.HttpHandler;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.activities.SignUpActivity;

/**
 * Created by Max on 12/10/2016.
 */

public class RegistrationUserDetailsFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ProgressBar mVehicleApiDataProgressBar;

    private View mVehicleDetailsLayout;
    private AppCompatSpinner mVehicleMakeSpinner;
    private AppCompatSpinner mVehicleModelSpinner;
    private AppCompatSpinner mEngineSpinner;
    private AppCompatSpinner mModelYearSpinner;
    private ProgressBar mVehicleMakeProgressBar;
    private ProgressBar mVehicleModelProgressBar;
    private ProgressBar mEngineProgressBar;

    private static final String JSON_VEHICLE_API_BASE_URL =  "http://casco.cmios.ru/api";
    private static final String JSON_MAKE_API = "/cars/";

    private ArrayAdapter<Integer> mModelYearsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // Generate model years adapter.
        int minYear = 1980;
        int maxYear = 2017;
        Integer[] modelYears = new Integer[maxYear - minYear + 1];
        for (int j = 0, i = minYear; i <= maxYear; j++, i++) {
            modelYears[j] = i;
        }
        mModelYearsAdapter = new ArrayAdapter<>(getContext(),
                R.layout.support_simple_spinner_dropdown_item, modelYears);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_registration_step_userdetails, null);

        // Next step is finish.
        Button nextStep = (Button) layout.findViewById(R.id.userdetails_next_step);
        nextStep.setOnClickListener(this);

        Button previousStep = (Button) layout.findViewById(R.id.userdetails_previous_step);
        previousStep.setOnClickListener(this);

        // Arrow direction and placement relative to the button should be in the opposite direction.
        // RTL - to the left
        // LTR - to the right
        if (Utils.isLocaleRTL(Locale.getDefault())) {
            Drawable navigateRight = ContextCompat.getDrawable(getContext(), R.drawable.ic_navigate_next_accent_24dp);
            previousStep.setCompoundDrawablesWithIntrinsicBounds(null, null, navigateRight, null);
        } else {
            Drawable navigateLeft = ContextCompat.getDrawable(getContext(), R.drawable.ic_navigate_before_accent_24dp);
            previousStep.setCompoundDrawablesWithIntrinsicBounds(navigateLeft, null, null, null);
        }

        mVehicleDetailsLayout = layout.findViewById(R.id.vehicle_details_layout);

        mVehicleMakeSpinner = (AppCompatSpinner) layout.findViewById(R.id.vehicle_make_spinner);
        mVehicleMakeSpinner.setOnItemSelectedListener(this);
        mVehicleMakeProgressBar = (ProgressBar) layout.findViewById(R.id.vehicle_make_results_progress_bar);
        mVehicleModelSpinner = (AppCompatSpinner) layout.findViewById(R.id.vehicle_model_spinner);
        mVehicleModelSpinner.setOnItemSelectedListener(this);
        mVehicleModelProgressBar = (ProgressBar) layout.findViewById(R.id.vehicle_model_results_progress_bar);
        mModelYearSpinner = (AppCompatSpinner) layout.findViewById(R.id.vehicle_year_spinner);
        mModelYearSpinner.setAdapter(mModelYearsAdapter);
        mModelYearSpinner.setOnItemSelectedListener(this);
        mEngineSpinner = (AppCompatSpinner) layout.findViewById(R.id.engine_displacement_spinner);
        mEngineSpinner.setOnItemSelectedListener(this);
        mEngineProgressBar = (ProgressBar) layout.findViewById(R.id.vehicle_engine_displacement_results_progress_bar);

        // Load vehicle models from Vehicle API.
        new GetVehicleAPIDataTask(VehicleAPIRequestType.REQUEST_MAKE).execute(
                new VehicleAPIRequest(VehicleAPIRequestType.REQUEST_MAKE, JSON_VEHICLE_API_BASE_URL + JSON_MAKE_API));
        return layout;
    }

    @Override
    public void onClick(View v) {
        SignUpActivity containerActivity = null;
        try {
            containerActivity = (SignUpActivity) getActivity();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        switch (v.getId()) {
            case R.id.userdetails_next_step:
                // Finish registration process.
                break;
            case R.id.userdetails_previous_step:
                if (containerActivity != null) {
                    // Go back one page.
                    containerActivity.requestViewPagerPage(SignUpActivity.PAGE_LOGIN_DETAILS);
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        AppCompatSpinner spinner = null;
        VehicleAPIRequestType requestType = null;
        switch (parent.getId()) {
            case R.id.vehicle_make_spinner:
                // Different vehicle make was selected.
                spinner = mVehicleMakeSpinner;
                requestType = VehicleAPIRequestType.REQUEST_MODEL;
                break;
            case R.id.vehicle_model_spinner:
                // Different vehicle model was selected.
                spinner = mVehicleModelSpinner;
                requestType = VehicleAPIRequestType.REQUEST_MODIFICATION;
                break;
        }

        if (requestType != null && spinner != null) {
            // Get additional data for next spinner.
            ResultArrayAdapter adapter = (ResultArrayAdapter) spinner.getAdapter();
            VehicleAPIResult result = adapter.getItem(position);
            if (result != null) {
                VehicleAPIRequest request = new VehicleAPIRequest(requestType,
                        JSON_VEHICLE_API_BASE_URL + result.getExtraURL());
                new GetVehicleAPIDataTask(requestType).execute(request);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public class GetVehicleAPIDataTask extends AsyncTask<VehicleAPIRequest, Void, List<VehicleAPIResult>> {

        private VehicleAPIRequestType requestType;

        public GetVehicleAPIDataTask(VehicleAPIRequestType type) {
            requestType = type;
        }

        @Override
        protected List<VehicleAPIResult> doInBackground(VehicleAPIRequest... params) {

            // First (and only) parameter is the request.
            requestType = params[0].getRequestType();

            List<VehicleAPIResult> vehicleAPIResults = new ArrayList<>();
            try {
                JSONArray jsonArray = null;
                JSONObject jsonObject;
                switch (requestType) {
                    case REQUEST_MAKE:
                        // Get JSON Array of vehicle makes.
                        jsonArray = new JSONArray(parseURL(params[0].getURL()));
                        break;
                    case REQUEST_MODEL:
                        // Get JSON Object which contains models array.
                        jsonObject = new JSONObject(parseURL(params[0].getURL()));
                        jsonArray = jsonObject.getJSONArray("models");
                        break;
                    case REQUEST_MODIFICATION:
                        // Get JSON Object which contains modifications (engine dispalcement) array.
                        jsonObject = new JSONObject(parseURL(params[0].getURL()));
                        jsonArray = jsonObject.getJSONArray("modifications");
                        break;
                }

                String extraURL = "";
                for (int i = 0; i < jsonArray.length(); i++) {
                    // Periodically check if the task was canceled.
                    if (isCancelled())
                        break;

                    JSONObject object = jsonArray.getJSONObject(i);
                    // Get "title" which is a string representation of the data we require (make/model/modification)
                    String model = object.optString("title");
                    // Modification request doesn't contain url.
                    if (requestType != VehicleAPIRequestType.REQUEST_MODIFICATION)
                        // Get "url" which is a url for additional data for this make/model.
                        extraURL = object.getString("url");
                    vehicleAPIResults.add(new VehicleAPIResult(extraURL, model));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // TODO: handle errors.
            }

            return vehicleAPIResults;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            switch (requestType) {
                case REQUEST_MAKE:
                    // Set all spinners visibility to gone until they receive their results.
                    mVehicleMakeSpinner.setVisibility(View.GONE);
                    // The only visible view would be the Make Spinner progress bar.
                    mVehicleMakeProgressBar.setVisibility(View.VISIBLE);

                    mVehicleModelSpinner.setVisibility(View.GONE);
                    mVehicleModelProgressBar.setVisibility(View.GONE);
                    mModelYearSpinner.setVisibility(View.GONE);
                    mEngineSpinner.setVisibility(View.GONE);
                    mEngineProgressBar.setVisibility(View.GONE);
                    break;
                case REQUEST_MODEL:
                    // Set model, year and modification spinners visibility to gone.
                    mVehicleModelSpinner.setVisibility(View.GONE);
                    // Set the model progress bar to visible.
                    mVehicleModelProgressBar.setVisibility(View.VISIBLE);
                    mModelYearSpinner.setVisibility(View.GONE);
                    mEngineSpinner.setVisibility(View.GONE);
                    mEngineProgressBar.setVisibility(View.GONE);
                    break;
                case REQUEST_MODIFICATION:
                    // Set the modification spinner visibility to gone.
                    mEngineSpinner.setVisibility(View.GONE);
                    // Set the modification progress bar to visible.
                    mEngineProgressBar.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        protected void onPostExecute(List<VehicleAPIResult> vehicleAPIResults) {
            super.onPostExecute(vehicleAPIResults);
            switch (requestType) {
                case REQUEST_MAKE:
                    // Now that we received our results set the make spinner to visible again.
                    // Everything else (including progress bars) is still at gone.
                    // They will appear once they retrieve their individual results.
                    mVehicleMakeSpinner.setVisibility(View.VISIBLE);
                    mVehicleMakeProgressBar.setVisibility(View.GONE);

                    mVehicleModelSpinner.setVisibility(View.GONE);
                    mVehicleModelProgressBar.setVisibility(View.GONE);
                    mEngineSpinner.setVisibility(View.GONE);
                    mEngineProgressBar.setVisibility(View.GONE);
                    break;
                case REQUEST_MODEL:
                    // Set the spinner back to visible once we got the results.
                    mVehicleModelSpinner.setVisibility(View.VISIBLE);
                    mVehicleModelProgressBar.setVisibility(View.GONE);
                    // Year spinner can be visible since it doesn't require any asynchronous work.
                    mModelYearSpinner.setVisibility(View.VISIBLE);
                    mEngineSpinner.setVisibility(View.GONE);
                    mEngineProgressBar.setVisibility(View.GONE);
                    break;
                case REQUEST_MODIFICATION:
                    // Set the modification spinner back to visible.
                    mEngineSpinner.setVisibility(View.VISIBLE);
                    mEngineProgressBar.setVisibility(View.GONE);
                    break;
            }

            if (!vehicleAPIResults.isEmpty()) {
                // Convert the results ArrayList to an Array because we use an instance of ArrayAdapter.
                VehicleAPIResult[] resultsArray = new VehicleAPIResult[vehicleAPIResults.size()];
                vehicleAPIResults.toArray(resultsArray);
                // Apply the new results to an adapter.
                ResultArrayAdapter adapter = new ResultArrayAdapter(getContext(),
                        R.layout.support_simple_spinner_dropdown_item, resultsArray);
                // Set the adapter to the requesting spinner.
                switch (requestType) {
                    case REQUEST_MAKE:
                        mVehicleMakeSpinner.setAdapter(adapter);
                        break;
                    case REQUEST_MODEL:
                        mVehicleModelSpinner.setAdapter(adapter);
                        break;
                    case REQUEST_MODIFICATION:
                        mEngineSpinner.setAdapter(adapter);
                        break;
                }
            }
        }

        private String parseURL(String url) {
            // Parse vehicle data api url to get a JSON input.
            HttpHandler httpHandler = new HttpHandler();
            return httpHandler.makeServiceCall(url);
        }
    }

    // Vehicle data class, will be used when user finishes registration.
    public static class VehicleData {
        private String vehicleMake;
        private String vehicleModel;
        private String vehicleModifications;
        private int vehicleYear;

        public void setVehicleMake(String make) {
            vehicleMake = make;
        }

        public String getVehicleMake() {
            return vehicleMake;
        }

        public void setVehicleModel(String model) {
            vehicleModel = model;
        }

        public String getVehicleModel() {
            return vehicleModel;
        }

        public void setVehicleModifications(String modifications) {
            vehicleModifications = modifications;
        }

        public String getVehicleModifications() {
            return vehicleModifications;
        }

        public void setVehicleYear(int year) {
            vehicleYear = year;
        }

        public int getVehicleYear() {
            return vehicleYear;
        }
    }

    private static class VehicleAPIResult {
        private String mExtraURL;
        private String mResult;

        public VehicleAPIResult(String url, String result) {
            mExtraURL    = url;
            mResult      = result;
        }

        // Extra URL for additional information about this make/model.
        public String getExtraURL() {
            return mExtraURL;
        }

        // String describing the results (make name, model name or engine displacement).
        public String getResult() {
            return mResult;
        }
    }

    private static class VehicleAPIRequest {
        VehicleAPIRequestType mRequestType;
        String mURL;

        public VehicleAPIRequest(VehicleAPIRequestType requestType, String url) {
            mRequestType = requestType;
            mURL = url;
        }

        public VehicleAPIRequestType getRequestType() {
            return mRequestType;
        }

        public String getURL() {
            return mURL;
        }
    }

    private enum VehicleAPIRequestType {
        REQUEST_MAKE,
        REQUEST_MODEL,
        REQUEST_MODIFICATION
    }

    private class ResultArrayAdapter extends ArrayAdapter<VehicleAPIResult> {

        private ArrayList<VehicleAPIResult> mResults;

        public ResultArrayAdapter(Context context, int resource, VehicleAPIResult[] results) {
            super(context, resource, results);

            mResults = new ArrayList<>();

            for (VehicleAPIResult result : results) {
                mResults.add(result);
            }
        }

        @Nullable
        @Override
        public VehicleAPIResult getItem(int position) {
            return mResults.get(position);
        }

        @Override
        public void add(VehicleAPIResult result) {
            mResults.add(result);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            // Set the spinner item text to the relevant result.
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setText(mResults.get(position).getResult());
            }

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            // Set the drop down item text to the relevant result.
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setText(mResults.get(position).getResult());
            }

            return view;
        }

        @Override
        public int getCount() {
            return mResults.size();
        }
    }
}
