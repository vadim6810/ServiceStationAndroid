package il.co.tel_ran.carservice.fragments;

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

import il.co.tel_ran.carservice.InboxMessage;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.adapters.InboxMessagesAdapter;

/**
 * Created by Max on 29/11/2016.
 */

public class ProviderInboxFragment extends RefreshingFragment implements InboxMessagesAdapter.InboxMessageClickListener {

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
            mNoMessagesTextView.setVisibility(View.GONE);

            setupRecyclerView();
        }

        return mMainLayout;
    }

    @Override
    public void onRefreshStart() {
        super.onRefreshStart();

        // TODO: Implement refreshing when ServerConnection is implemented for inbox messages.
        onRefreshEnd();
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
            Toast.makeText(getContext(), "messageTitle=" + message.getTitle(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void setupRecyclerView() {
        mInboxMessagesRecyclerView = (RecyclerView) mMainLayout.findViewById(
                R.id.inbox_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mInboxMessagesRecyclerView.setLayoutManager(layoutManager);
        InboxMessagesAdapter searchResultAdapter = new InboxMessagesAdapter(
                getMockInboxMessages(), getContext(), this);
        mInboxMessagesRecyclerView.setAdapter(searchResultAdapter);
    }

    private ArrayList<InboxMessage> getMockInboxMessages() {
        ArrayList<InboxMessage> messages = new ArrayList<>();

        long now = System.currentTimeMillis();
        messages.add(new InboxMessage(1, "Message from customer Max", "", now - 86400000L, 1, InboxMessage.Source.USER));
        messages.add(new InboxMessage(2, "Message from customer Viktor", "", now, 2, InboxMessage.Source.USER));
        messages.add(new InboxMessage(3, "Message from customer Vadim", "", now - 3L * 86400000L, 3, InboxMessage.Source.USER));
        messages.add(new InboxMessage(4, "Message from customer Alex", "", now - 32L * 86400000L, 4, InboxMessage.Source.USER));
        messages.add(new InboxMessage(5, "Message from customer Elizabeta", "", now - 366L * 86400000L, 5, InboxMessage.Source.USER));
        return messages;
    }
}
