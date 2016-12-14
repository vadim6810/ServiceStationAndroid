package il.co.tel_ran.carservice.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import il.co.tel_ran.carservice.InboxMessage;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.connection.ServerConnection;
import il.co.tel_ran.carservice.activities.ProviderMainActivity;
import il.co.tel_ran.carservice.adapters.InboxMessagesAdapter;

/**
 * Created by Max on 29/11/2016.
 */

public class ProviderInboxFragment extends RefreshingFragment implements InboxMessagesAdapter.InboxMessageClickListener, ServerConnection.OnProviderInboxMessagesRetrievedListener {

    private View mMainLayout;

    private ProgressBar mLoadInboxProgressBar;
    private TextView mNoMessagesTextView;

    private RecyclerView mInboxMessagesRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mMainLayout == null) {
            mMainLayout = inflater.inflate(R.layout.fragment_provider_inbox, container, false);

            mLoadInboxProgressBar = (ProgressBar) mMainLayout.findViewById(R.id.inbox_progress_bar);
            mNoMessagesTextView = (TextView) mMainLayout.findViewById(R.id.no_inbox_messages_text_view);

            setupRecyclerView();
        }

        List<InboxMessage> inboxMessages = null;
        Activity containerActivity = getActivity();
        if (containerActivity != null) {
            try {
                ProviderMainActivity providerActivity = (ProviderMainActivity) containerActivity;

                inboxMessages = providerActivity.getRetrievedMessages();
                if (inboxMessages != null && !inboxMessages.isEmpty()) {
                    onProviderInboxMessagesRetrieved(inboxMessages);
                }

                // Release the resource from activity
                providerActivity.clearRetrievedMessages();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }

        if (inboxMessages == null)
            loadMessages();

        return mMainLayout;
    }

    @Override
    public void onRefreshStart() {
        super.onRefreshStart();

        loadMessages();
    }

    /*
     * InboxMessagesAdapter.InboxMessageClickListener
     */

    @Override
    public void onClickMessage(View view) {
        // Find the position in the adapter for this view.
        int itemPos = mInboxMessagesRecyclerView.getChildAdapterPosition(view);
        InboxMessagesAdapter adapter = (InboxMessagesAdapter) mInboxMessagesRecyclerView
                .getAdapter();

        // Get the message object for this position.
        final InboxMessage message = adapter.getItem(itemPos);

        if (message != null) {
            showMessageDialog(message);
        }
    }

    /*
     * ServerConnection.OnProviderInboxMessagesRetrievedListener
     */

    @Override
    public void onProviderInboxMessagesRetrievingStarted() {
        InboxMessagesAdapter adapter = (InboxMessagesAdapter) mInboxMessagesRecyclerView
                .getAdapter();

        // Check if we are showing any messages currently.
        if (adapter.getItemCount() == 0) {
            mNoMessagesTextView.setVisibility(View.GONE);
            mLoadInboxProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onProviderInboxMessagesRetrieved(List<InboxMessage> inboxMessages) {
        mLoadInboxProgressBar.setVisibility(View.GONE);

        InboxMessagesAdapter adapter = (InboxMessagesAdapter) mInboxMessagesRecyclerView
                .getAdapter();
        adapter.removeAllItems();
        if (inboxMessages != null && !inboxMessages.isEmpty()) {
            adapter.addItems(inboxMessages);
            mNoMessagesTextView.setVisibility(View.GONE);
        } else {
            mNoMessagesTextView.setVisibility(View.VISIBLE);
        }

        onRefreshEnd();
    }

    private void setupRecyclerView() {
        mInboxMessagesRecyclerView = (RecyclerView) mMainLayout.findViewById(
                R.id.inbox_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mInboxMessagesRecyclerView.setLayoutManager(layoutManager);
        InboxMessagesAdapter searchResultAdapter = new InboxMessagesAdapter(
                new ArrayList<InboxMessage>(), getContext(), this);
        mInboxMessagesRecyclerView.setAdapter(searchResultAdapter);
    }

    public void loadMessages() {
        Activity containerActivity = getActivity();
        if (containerActivity != null) {
            try {
                ProviderMainActivity providerActivity = (ProviderMainActivity) containerActivity;

                ServerConnection serverConnection = providerActivity.getServerConnection();
                // Retrieve services from server
                if (serverConnection != null) {
                    serverConnection.getProviderInboxMessages(this);
                }
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    private void showMessageDialog(InboxMessage inboxMessage) {
        new AlertDialog.Builder(getContext())
                .setTitle(inboxMessage.getTitle())
                .setMessage(inboxMessage.getMessage())
                .setNeutralButton(R.string.dismiss, null)
                .show();
    }
}
