package il.co.tel_ran.carservice.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.EnumSet;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServiceSubWorkType;
import il.co.tel_ran.carservice.ServiceWorkType;
import il.co.tel_ran.carservice.ServiceWorkTypeCategory;
import il.co.tel_ran.carservice.WorkTypesItemDecoration;
import il.co.tel_ran.carservice.adapters.WorkTypesAdapter;

/**
 * Created by maxim on 28-Dec-16.
 */

public class WorkTypesFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    private boolean mIsSingleChoice = true;

    private RecyclerView mWorkTypesRecyclerView;

    private SelectWorkTypesDialogListener mListener;

    private ArrayList<ServiceSubWorkType> mSelectedSubWorkTypes;

    public interface SelectWorkTypesDialogListener {
        void onWorkTypeSelected(ServiceWorkType[] workTypes, ServiceSubWorkType[] subWorkTypes);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();
        if (extras != null && !extras.isEmpty()) {
            mIsSingleChoice = extras.getBoolean("is_single_choice");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = onCreateDialogView(getActivity().getLayoutInflater(), null, null);
        onViewCreated(view, null);

        // Build the AlertDialog.
        // We are using AlertDialog to add native support for buttons.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Select Work Types")
                .setView(view)
                .setPositiveButton(R.string.button_submit, this)
                .setNeutralButton(R.string.button_cancel, this);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mListener != null) {
                    WorkTypesAdapter workTypesAdapter =
                            (WorkTypesAdapter) mWorkTypesRecyclerView.getAdapter();
                    if (workTypesAdapter != null) {
                        ArrayList<ServiceWorkType> selectedWorkTypes = workTypesAdapter
                                .getSelectedWorkTypes();
                        ArrayList<ServiceSubWorkType> selectedSubWorkTypes = workTypesAdapter
                                .getSelectedSubWorkTypes();

                        ServiceWorkType[] serviceWorkTypesArr =
                                new ServiceWorkType[selectedWorkTypes.size()];
                        ServiceSubWorkType[] serviceSubWorkTypesArr =
                                new ServiceSubWorkType[selectedSubWorkTypes.size()];

                        selectedWorkTypes.toArray(serviceWorkTypesArr);
                        selectedSubWorkTypes.toArray(serviceSubWorkTypesArr);

                        mListener.onWorkTypeSelected(serviceWorkTypesArr, serviceSubWorkTypesArr);
                    }
                }
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                break;
        }
    }

    public View onCreateDialogView(LayoutInflater inflater, @Nullable ViewGroup container,
                                   @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_work_types_selection, container, false);

        Context context = getContext();

        mWorkTypesRecyclerView = (RecyclerView) layout.findViewById(
                R.id.work_types_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        mWorkTypesRecyclerView.setLayoutManager(layoutManager);
        WorkTypesAdapter workTypesAdapter = new WorkTypesAdapter(
                ServiceWorkTypeCategory.generateWorkTypeCategories(context),
                context, mIsSingleChoice, mSelectedSubWorkTypes);
        mWorkTypesRecyclerView.setAdapter(workTypesAdapter);

        int itemSpacingMargin = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        int[] margins = {itemSpacingMargin, 0, itemSpacingMargin, 0};

        WorkTypesItemDecoration itemDecoration = new WorkTypesItemDecoration(margins);
        mWorkTypesRecyclerView.addItemDecoration(itemDecoration);

        return layout;
    }

    public void setOnWorkTypesSelectedListener(SelectWorkTypesDialogListener listener) {
        mListener = listener;
    }

    public static WorkTypesFragment getInstance(boolean isSingleChoice) {
        return getInstance(isSingleChoice, null);
    }

    public static WorkTypesFragment getInstance(boolean isSingleChoice,
                                                ArrayList<ServiceSubWorkType> selectedSubWorkTypes) {
        WorkTypesFragment workTypesFragment = new WorkTypesFragment();

        Bundle args = new Bundle();
        args.putBoolean("is_single_choice", isSingleChoice);
        workTypesFragment.setArguments(args);

        workTypesFragment.setSelectedSubWorkTypes(selectedSubWorkTypes);

        return workTypesFragment;
    }

    private void setSelectedSubWorkTypes(ArrayList<ServiceSubWorkType> selectedSubWorkTypes) {
        mSelectedSubWorkTypes = selectedSubWorkTypes;
    }
}
