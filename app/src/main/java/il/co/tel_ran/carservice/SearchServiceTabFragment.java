package il.co.tel_ran.carservice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Max on 16/09/2016.
 */
public class SearchServiceTabFragment extends Fragment
    implements View.OnClickListener {

    private final static int[] DISTRICT_CHECKBOX_IDS = {
            R.id.district_checkbox_south,
            R.id.district_checkbox_center,
            R.id.district_checkbox_north
    };
    private final static int[] SERVICE_CHECKBOX_IDS = {
            R.id.service_checkbox_car_wash,
            R.id.service_checkbox_tuning,
            R.id.service_checkbox_tyre_repair,
            R.id.service_checkbox_air_cond
    };

    private View searchFieldsLayout;
    private Button expandFieldsButton;

    private AppCompatCheckBox[] districtCheckBoxes = new AppCompatCheckBox[DISTRICT_CHECKBOX_IDS.length];
    private AppCompatCheckBox[] servicesCheckBoxes = new AppCompatCheckBox[SERVICE_CHECKBOX_IDS.length];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_tab_search_service, container, false);

        searchFieldsLayout = layout.findViewById(R.id.find_service_expandable_layout);

        expandFieldsButton = (Button) layout.findViewById(R.id.expand_search_fields_button);
        expandFieldsButton.setOnClickListener(this);
        AppCompatImageButton collapseFieldsButton = (AppCompatImageButton) layout.findViewById(R.id.collapse_search_fields_button);
        collapseFieldsButton.setOnClickListener(this);

        Button findServicesButton = (Button) layout.findViewById(R.id.find_services_button);
        findServicesButton.setOnClickListener(this);

        Button clearFieldsButton = (Button) layout.findViewById(R.id.clear_search_fields_button);
        clearFieldsButton.setOnClickListener(this);

        for (int i = 0; i < districtCheckBoxes.length; i++) {
            districtCheckBoxes[i] = (AppCompatCheckBox) layout.findViewById(DISTRICT_CHECKBOX_IDS[i]);
        }
        for (int i = 0; i < servicesCheckBoxes.length; i++) {
            servicesCheckBoxes[i] = (AppCompatCheckBox) layout.findViewById(SERVICE_CHECKBOX_IDS[i]);
        }

        return layout;
    }

    @Override
    public void onClick(View view) {
        int EXPAND_COLLAPSE_DURATION = 350;
        switch (view.getId()) {
            case R.id.expand_search_fields_button:
                Utils.collapseView(expandFieldsButton, EXPAND_COLLAPSE_DURATION);
                Utils.expandView(searchFieldsLayout, EXPAND_COLLAPSE_DURATION);
                break;
            case R.id.collapse_search_fields_button:
                Utils.collapseView(searchFieldsLayout, EXPAND_COLLAPSE_DURATION);
                Utils.expandView(expandFieldsButton, EXPAND_COLLAPSE_DURATION);
                break;
            case R.id.clear_search_fields_button:
                for (AppCompatCheckBox districtCheckbox : districtCheckBoxes) {
                    districtCheckbox.setChecked(false);
                }
                for (AppCompatCheckBox serviceCheckbox : servicesCheckBoxes) {
                    serviceCheckbox.setChecked(false);
                }
                break;
        }
    }

}
