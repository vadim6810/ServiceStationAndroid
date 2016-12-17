package il.co.tel_ran.carservice.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.ServiceType;

/**
 * Created by maxim on 9/29/2016.
 */

public class ServiceSearchResultAdapter
        extends RecyclerView.Adapter<ServiceSearchResultAdapter.ViewHolder> implements View.OnClickListener {

    private final String mServiceCarWashStr;
    private final String mServiceTuningStr;
    private final String mServiceACRepairRefillStr;
    private final String mServiceTyreRepairStr;

    private final boolean mIsRecentServices;

    private List<ServiceStation> mSearchResults = new ArrayList<>();

    private ServiceSearchResultClickListener mListener;

    public interface ServiceSearchResultClickListener {
        void onClickSearchResult(View view);
        void onClickDeleteResult(View view);
    }

    public ServiceSearchResultAdapter(List<ServiceStation> services, Context context,
                                      ServiceSearchResultClickListener listener, boolean isRecentServices) {
        mSearchResults = services;

        mServiceCarWashStr = context.getString(R.string.required_service_car_wash);
        mServiceTuningStr = context.getString(R.string.required_service_tuning);
        mServiceTyreRepairStr = context.getString(R.string.required_service_tyre_repair);
        mServiceACRepairRefillStr = context.getString(R.string.required_service_air_cond_refill);

        mListener = listener;

        mIsRecentServices = isRecentServices;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.remove_recent_service_button) {
            mListener.onClickDeleteResult(v);
        } else {
            mListener.onClickSearchResult(v);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();

        View serviceSearchResult = LayoutInflater.from(parentContext)
                .inflate(R.layout.service_search_result_layout, parent, false);

        serviceSearchResult.setOnClickListener(this);

        ViewHolder holder =  new ViewHolder(serviceSearchResult);

        holder.deleteServiceButton.setOnClickListener(this);

        if (mIsRecentServices) {
            holder.deleteServiceButton.setVisibility(View.VISIBLE);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ServiceStation serviceStation = mSearchResults.get(position);

        String servicesText = "";
        int count = 0;
        for (ServiceType serviceType : serviceStation.getAvailableServices()) {
            switch (serviceType) {
                case CAR_WASH:
                    servicesText = servicesText + mServiceCarWashStr;
                    break;
                case TUNING:
                    servicesText = servicesText + mServiceTuningStr;
                    break;
                case TYRE_REPAIR:
                    servicesText = servicesText + mServiceTyreRepairStr;
                    break;
                case AC_REPAIR_REFILL:
                    servicesText = servicesText + mServiceACRepairRefillStr;
                    break;
            }

            // Add a separating comma as long as this is not the last serviceStation.
            if ((count++ + 1) < serviceStation.getAvailableServices().size())
                servicesText = servicesText + ", ";
        }

        holder.serviceNameTextView.setText(serviceStation.getName());
        holder.availableServicesTextView.setText(servicesText);
        holder.locationTextView.setText(serviceStation.getCityName());
    }

    @Override
    public int getItemCount() {
        return mSearchResults.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView serviceNameTextView;
        private final TextView availableServicesTextView;
        private final TextView locationTextView;

        private final ImageButton deleteServiceButton;

        public ViewHolder(View layout) {
            super(layout);

            serviceNameTextView = (TextView) layout.findViewById(
                    R.id.result_service_name_text_view);
            availableServicesTextView = (TextView) layout.findViewById(
                    R.id.result_available_services_text_view);
            locationTextView = (TextView) layout.findViewById(
                    R.id.result_location_text_view);
            deleteServiceButton = (ImageButton) layout.findViewById(
                    R.id.remove_recent_service_button);
        }
    }

    public void addItem(ServiceStation service) {
        addItem(service, true);
    }

    public void addItems(ServiceStation... services) {
        for (ServiceStation service : services) {
            addItem(service, false);
        }

        notifyDataSetChanged();
    }

    public void addItems(Collection<ServiceStation> services) {
        for (ServiceStation service : services) {
            addItem(service, false);
        }

        notifyDataSetChanged();
    }

    public void removeAllItems() {
        mSearchResults.clear();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < mSearchResults.size()) {
            mSearchResults.remove(position);

            notifyDataSetChanged();
        }
    }

    public ServiceStation getItem(int position) {
        if (position >= 0 && position < mSearchResults.size()) {
            return mSearchResults.get(position);
        }

        return null;
    }

    private void addItem(ServiceStation service, boolean notify) {
        if (service != null) {
            mSearchResults.add(service);

            if (notify) {
                notifyDataSetChanged();
            }
        }
    }
}
