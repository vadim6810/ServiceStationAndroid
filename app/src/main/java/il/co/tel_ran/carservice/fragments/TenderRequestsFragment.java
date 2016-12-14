package il.co.tel_ran.carservice.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.connection.ServerConnection;
import il.co.tel_ran.carservice.TenderRequest;
import il.co.tel_ran.carservice.activities.ProviderMainActivity;
import il.co.tel_ran.carservice.adapters.TenderRequestsAdapter;

/**
 * Created by Max on 24/11/2016.
 */

public class TenderRequestsFragment extends RefreshingFragment
        implements TenderRequestsAdapter.TenderRequestClickListener,
        ServerConnection.OnTenderRequestsRetrievedListener {

    private View mMainLayout;

    private ProgressBar mLoadRequestsProgressBar;
    private TextView mNoRequestsFoundTextView;

    private RecyclerView mRequestsRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mMainLayout == null) {
            mMainLayout = inflater.inflate(R.layout.fragment_tender_requests, container, false);

            mLoadRequestsProgressBar = (ProgressBar) mMainLayout.findViewById(R.id.tender_requests_progress_bar);
            mNoRequestsFoundTextView = (TextView) mMainLayout.findViewById(R.id.no_tender_requests_text_view);

            setupRecyclerView();
        }

        loadRequests();

        return mMainLayout;
    }

    private void setupRecyclerView() {
        mRequestsRecyclerView = (RecyclerView) mMainLayout.findViewById(R.id.tender_requests_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRequestsRecyclerView.setLayoutManager(layoutManager);

        TenderRequestsAdapter tenderRequestsAdapter = new TenderRequestsAdapter(
                new ArrayList<TenderRequest>(), getContext(), this);
        mRequestsRecyclerView.setAdapter(tenderRequestsAdapter);
    }

    @Override
    public void onSendReply(View view, String message, boolean isUpdate) {
        View parent = mRequestsRecyclerView.findContainingItemView(view);
        // Find the position in the adapter for this view.
        int itemPos = mRequestsRecyclerView.getChildAdapterPosition(parent);
        Toast.makeText(getContext(),
                "itemPos=" + itemPos + " message=" + message + " isUpdate=" + isUpdate,
                Toast.LENGTH_LONG)
                .show();
    }


    /*
     * ServerConnection.OnTenderRequestsRetrievedListener
     */

    @Override
    public void onTenderRequestRetrievingStarted() {
        TenderRequestsAdapter adapter = (TenderRequestsAdapter) mRequestsRecyclerView
                .getAdapter();

        // Check if we are showing any results currently.
        if (adapter.getItemCount() == 0) {
            mNoRequestsFoundTextView.setVisibility(View.GONE);
            mLoadRequestsProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTenderRequestRetrieved(List<TenderRequest> tenderRequests) {
        mLoadRequestsProgressBar.setVisibility(View.GONE);

        TenderRequestsAdapter adapter = (TenderRequestsAdapter) mRequestsRecyclerView
                .getAdapter();
        adapter.removeAllItems();
        if (tenderRequests != null && !tenderRequests.isEmpty()) {
            adapter.addItems(tenderRequests);
        } else {
            mNoRequestsFoundTextView.setVisibility(View.VISIBLE);
        }
    }

    public void loadRequests() {
        Activity containerActivity = getActivity();
        if (containerActivity != null) {
            try {
                ProviderMainActivity providerActivity = (ProviderMainActivity) containerActivity;

                ServerConnection serverConnection = providerActivity.getServerConnection();
                // Retrieve services from server
                if (serverConnection != null) {
                    serverConnection.getTenderRequests(this);
                }
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }

        onRefreshEnd();
    }

    @Override
    public void onRefreshStart() {
        super.onRefreshStart();
        loadRequests();
    }
}
