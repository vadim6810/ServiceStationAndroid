package il.co.tel_ran.carservice.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.VehicleExtendedData;
import il.co.tel_ran.carservice.adapters.VehicleMakesAdapter;
import il.co.tel_ran.carservice.connection.DataRequest;
import il.co.tel_ran.carservice.connection.DataResult;
import il.co.tel_ran.carservice.connection.RequestMaker;
import il.co.tel_ran.carservice.connection.RequestQueueSingleton;
import il.co.tel_ran.carservice.connection.ServerConnection;
import il.co.tel_ran.carservice.connection.ServerResponseError;
import il.co.tel_ran.carservice.connection.VehicleAPIDataRequest;
import il.co.tel_ran.carservice.connection.VehicleAPIRequestMaker;

/**
 * Created by maxim on 28-Dec-16.
 */

public class VehicleMakesFragment extends DialogFragment
        implements DialogInterface.OnClickListener, RequestMaker.OnDataRetrieveListener {

    private RecyclerView mVehicleMakesRecyclerView;
    private VehicleMakesAdapter mVehicleMakesAdapter;
    private boolean mIsSingleChoice;
    private ProgressBar mProgressBar;
    private AutoCompleteTextView mAutoCompleteVehicleMakesTextView;

    private SelectVehicleMakesDialogListener mListener;

    private ArrayList<String> mSelectedVehicleMakes;
    private ArrayList<VehicleExtendedData> mVehicleDataArrayList = new ArrayList<>();

    public interface SelectVehicleMakesDialogListener {
        void onVehicleMakesSelected(ArrayList<String> vehicleMakes);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = onCreateDialogView(getActivity().getLayoutInflater(), null, null);
        onViewCreated(view, null);

        // Build the AlertDialog.
        // We are using AlertDialog to add native support for buttons.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_vehicle_makes_title)
                .setView(view)
                .setPositiveButton(R.string.button_submit, this)
                .setNeutralButton(R.string.button_cancel, this);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    /*
     * RequestMaker.OnDataRetrieveListener
     */

    @Override
    public void onDataRetrieveSuccess(DataRequest dataRequest, DataResult result) {
        if (result.getDataType() == DataResult.Type.VEHICLE_API) {
            VehicleExtendedData[] vehicleExtendedDataArr = (VehicleExtendedData[]) result.getData();

            if (vehicleExtendedDataArr.length > 0) {
                ArrayList<String> carMakes = new ArrayList<>();
                for (VehicleExtendedData vehicleExtendedData : vehicleExtendedDataArr) {
                    carMakes.add(vehicleExtendedData.getVehicleMake());
                    mVehicleDataArrayList.add(vehicleExtendedData);
                }

                mVehicleMakesAdapter = new VehicleMakesAdapter(carMakes, mSelectedVehicleMakes,
                        mIsSingleChoice);
                mVehicleMakesRecyclerView.setAdapter(mVehicleMakesAdapter);

                String[] carMakesArr = new String[carMakes.size()];
                carMakes.toArray(carMakesArr);
                ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>
                        (getContext(), android.R.layout.simple_list_item_1, carMakesArr);
                mAutoCompleteVehicleMakesTextView.setAdapter(autoCompleteAdapter);

                toggleProgressBar(false);
            } else {
                // TODO: show some error
                dismiss();
            }
        }
    }

    @Override
    public void onDataRetrieveFailed(DataRequest dataRequest, DataResult.Type resultType,
                                     ServerResponseError error, @Nullable String message) {
        if (resultType == DataResult.Type.VEHICLE_API) {
            // TODO: show some error
            dismiss();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mListener != null && mVehicleMakesAdapter != null) {
                    mListener.onVehicleMakesSelected(mVehicleMakesAdapter.getSelectedCarMakes());
                }
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                break;
        }
    }

    public View onCreateDialogView(LayoutInflater inflater, @Nullable ViewGroup container,
                                   @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_vehicle_makes, container, false);

        mVehicleMakesRecyclerView = (RecyclerView) layout.findViewById(
                R.id.vehicle_makes_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mVehicleMakesRecyclerView.setLayoutManager(layoutManager);

        mProgressBar = (ProgressBar) layout.findViewById(R.id.vehicle_makes_progressbar);

        mAutoCompleteVehicleMakesTextView = (AutoCompleteTextView) layout
                .findViewById(R.id.vehicle_makes_auto_complete_text_view);
        mAutoCompleteVehicleMakesTextView.addTextChangedListener(mTextWatcher);
        loadVehicleApiData();

        return layout;
    }

    public void setOnVehicleMakesSelectedListener(SelectVehicleMakesDialogListener listener) {
        mListener = listener;
    }

    public static VehicleMakesFragment getInstance() {
        return getInstance(null, false);
    }

    public static VehicleMakesFragment getInstance(boolean isSingleChoice) {
        return getInstance(null, isSingleChoice);
    }

    public static VehicleMakesFragment getInstance(ArrayList<String> selectedVehicleMakes) {
        return getInstance(selectedVehicleMakes, false);
    }

    public static VehicleMakesFragment getInstance(ArrayList<String> selectedVehicleMakes,
                                                   boolean isSingleChoice) {
        VehicleMakesFragment vehicleMakesFragment = new VehicleMakesFragment();

        vehicleMakesFragment.setSelectedSubWorkTypes(selectedVehicleMakes);
        vehicleMakesFragment.setSingleChoice(isSingleChoice);

        return vehicleMakesFragment;
    }

    private void setSingleChoice(boolean isSingleChoice) {
        mIsSingleChoice = isSingleChoice;
    }

    private void setSelectedSubWorkTypes(ArrayList<String> selectedVehicleMakes) {
        mSelectedVehicleMakes = selectedVehicleMakes;
    }

    private void toggleProgressBar(boolean toggle) {
        if (toggle) {
            mVehicleMakesRecyclerView.setVisibility(View.GONE);
            mAutoCompleteVehicleMakesTextView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mAutoCompleteVehicleMakesTextView.setVisibility(View.VISIBLE);
            mVehicleMakesRecyclerView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

        }
    }

    private void loadVehicleApiData() {
        toggleProgressBar(true);

        // Build the request
        VehicleAPIDataRequest vehicleAPIDataRequest = new VehicleAPIDataRequest();
        // Add the request to queue.
        new VehicleAPIRequestMaker(this).makeRequest(getContext(), vehicleAPIDataRequest);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mVehicleMakesAdapter != null) {
                mVehicleMakesAdapter.filterByText(s.toString());
            }
        }
    };

    public ArrayList<VehicleExtendedData> getVehicleData() {
        return mVehicleDataArrayList;
    }
}
