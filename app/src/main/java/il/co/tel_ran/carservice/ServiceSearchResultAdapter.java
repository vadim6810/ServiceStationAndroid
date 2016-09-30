package il.co.tel_ran.carservice;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by maxim on 9/29/2016.
 */

public class ServiceSearchResultAdapter
        extends RecyclerView.Adapter<ServiceSearchResultAdapter.ViewHolder> implements View.OnClickListener {

    private final String serviceCarWashStr;
    private final String serviceTuningStr;
    private final String serviceACRepairRefillStr;
    private final String serviceTyreRepairStr;

    private List<ServiceSearchResult> mSearchResults = new ArrayList<ServiceSearchResult>();

    private ServiceSearchResultClickListener mListener;

    public interface ServiceSearchResultClickListener {
        void onClickSearchResult(View view);
    }

    public ServiceSearchResultAdapter(List<ServiceSearchResult> searchResults, Context context,
                                      ServiceSearchResultClickListener listener) {
        mSearchResults = searchResults;

        serviceCarWashStr        = context.getString(R.string.required_service_car_wash);
        serviceTuningStr         = context.getString(R.string.required_service_tuning);
        serviceTyreRepairStr     = context.getString(R.string.required_service_tyre_repair);
        serviceACRepairRefillStr = context.getString(R.string.required_service_air_cond_refill);

        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        mListener.onClickSearchResult(v);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();

        View serviceSearchResult = LayoutInflater.from(parentContext)
                .inflate(R.layout.service_search_result_layout, parent, false);

        serviceSearchResult.setOnClickListener(this);

        return new ViewHolder(serviceSearchResult);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ServiceSearchResult searchResult = mSearchResults.get(position);

        String servicesText = "";
        int count = 0;
        for (ServiceType service : searchResult.getAvailableServices()) {
            switch (service) {
                case SERVICE_TYPE_CAR_WASH:
                    servicesText = servicesText + serviceCarWashStr;
                    break;
                case SERVICE_TYPE_TUNING:
                    servicesText = servicesText + serviceTuningStr;
                    break;
                case SERVICE_TYPE_TYRE_REPAIR:
                    servicesText = servicesText + serviceTyreRepairStr;
                    break;
                case SERVICE_TYPE_AC_REPAIR_REFILL:
                    servicesText = servicesText + serviceACRepairRefillStr;
                    break;
            }

            // Add a separating comma as long as this is not the last service.
            if ((count++ + 1) < searchResult.getAvailableServices().size())
                servicesText = servicesText + ", ";
        }

        holder.serviceNameTextView.setText(searchResult.getName());
        holder.availableServicesTextView.setText(servicesText);
        holder.locationTextView.setText(searchResult.getCityName());
    }

    @Override
    public int getItemCount() {
        return mSearchResults.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView serviceNameTextView;
        private final TextView availableServicesTextView;
        private final TextView locationTextView;

        public ViewHolder(View layout) {
            super(layout);

            serviceNameTextView = (TextView) layout.findViewById(
                    R.id.result_service_name_text_view);
            availableServicesTextView = (TextView) layout.findViewById(
                    R.id.result_available_services_text_view);
            locationTextView = (TextView) layout.findViewById(
                    R.id.result_location_text_view);
        }
    }

    public void addItem(ServiceSearchResult searchResult) {
        addItem(searchResult, true);
    }

    public void addItems(ServiceSearchResult... searchResults) {
        for (ServiceSearchResult result : searchResults) {
            addItem(result, false);
        }

        notifyDataSetChanged();
    }

    public void addItems(Collection<ServiceSearchResult> searchResults) {
        for (ServiceSearchResult result : searchResults) {
            addItem(result, false);
        }

        notifyDataSetChanged();
    }

    public void removeAllItems() {
        mSearchResults.clear();
    }

    public ServiceSearchResult getItem(int position) {
        if (position >= 0 && position < mSearchResults.size()) {
            return mSearchResults.get(position);
        }

        return null;
    }

    private void addItem(ServiceSearchResult searchResult, boolean notify) {
        if (searchResult != null) {
            mSearchResults.add(searchResult);

            if (notify) {
                notifyDataSetChanged();
            }
        }
    }
}
