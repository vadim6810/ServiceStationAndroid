package il.co.tel_ran.carservice.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import java.util.ArrayList;

/**
 * Created by maxim on 29-Dec-16.
 */

public class VehicleMakesAdapter extends RecyclerView.Adapter<VehicleMakesAdapter.ViewHolder>
        implements CompoundButton.OnCheckedChangeListener {

    private ArrayList<String> mCarMakes;
    private ArrayList<String> mVisibleCarMakes;
    private ArrayList<Integer> mSelectedCarMakeIndexes;

    private boolean mIsSingleChoice;
    // Only used when using single choice.
    private CompoundButton mPreviousSelectedButton;

    public VehicleMakesAdapter(ArrayList<String> carMakes, ArrayList<String> selectedCarMakes) {
        this(carMakes, selectedCarMakes, false);
    }

    public VehicleMakesAdapter(ArrayList<String> carMakes, ArrayList<String> selectedCarMakes,
                               boolean isSingleChoice) {
        mCarMakes = carMakes;
        mSelectedCarMakeIndexes = new ArrayList<>();

        mVisibleCarMakes = new ArrayList<>();
        mVisibleCarMakes.addAll(carMakes);

        if (selectedCarMakes != null && !selectedCarMakes.isEmpty()) {
            // Iterate through all car makes
            for (String selectedCarMake : selectedCarMakes) {
                // Check if this car make is contained in the car makes array.
                if (mCarMakes.contains(selectedCarMake)) {
                    // Add the index of this car make string to the selected indexes array.
                    mSelectedCarMakeIndexes.add(mCarMakes.indexOf(selectedCarMake));
                }
            }
        }

        mIsSingleChoice = isSingleChoice;
    }

    @Override
    public VehicleMakesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();

        CompoundButton compoundButton;
        if (mIsSingleChoice) {
            compoundButton = new RadioButton(parentContext);
            compoundButton.setOnCheckedChangeListener(this);
        } else {
            compoundButton = new CheckBox(parentContext);
            compoundButton.setOnCheckedChangeListener(this);
        }

        return new ViewHolder(compoundButton);
    }

    @Override
    public void onBindViewHolder(VehicleMakesAdapter.ViewHolder holder, int position) {
        String carMakeString = mVisibleCarMakes.get(position);

        holder.itemCheckBox.setText(carMakeString);
        holder.itemCheckBox.setTag(carMakeString);

        int index = mCarMakes.indexOf(carMakeString);
        // Update the checked status if this index is considered selected.
        holder.itemCheckBox.setChecked(mSelectedCarMakeIndexes.contains(index));
    }

    @Override
    public int getItemCount() {
        return mVisibleCarMakes.size();
    }

    /*
     * CompoundButton.OnCheckedChangeListener
     */

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Object tag = buttonView.getTag();
        if (tag != null) {
            String carMake = (String) tag;

            int index = mCarMakes.indexOf(carMake);

            if (isChecked) {
                if (mIsSingleChoice) {
                    mSelectedCarMakeIndexes.clear();
                    if (mPreviousSelectedButton != null) {
                        mPreviousSelectedButton.setChecked(false);
                    }
                }
                // Add the index to the array.
                if (!mSelectedCarMakeIndexes.contains(index)) {
                    mSelectedCarMakeIndexes.add(index);
                }

                mPreviousSelectedButton = buttonView;
            } else {
                // Remove the index from the array
                if (mSelectedCarMakeIndexes.contains(index)) {
                    // The use of indexOf is to make sure the array removes by the index of the car index and not by it's own index.
                    mSelectedCarMakeIndexes.remove(mSelectedCarMakeIndexes.indexOf(index));
                }
            }
        }
    }

    public void filterByText(String text) {
        if (text != null && !text.isEmpty()) {
            // Iterate through all car makes and look for the ones matching the filter.
            for (String vehicleMakeStr : mCarMakes) {
                // Convert to lower case for higher match chance.
                if (vehicleMakeStr.toLowerCase().contains(text.toLowerCase())) {
                    // Add the vehicle to the array if it's not there already
                    if (!mVisibleCarMakes.contains(vehicleMakeStr))
                        mVisibleCarMakes.add(vehicleMakeStr);
                } else {
                    // Remove the vehicle from the array if it's there (but not matching filter).
                    if (mVisibleCarMakes.contains(vehicleMakeStr))
                        mVisibleCarMakes.remove(vehicleMakeStr);

                }
            }
        } else {
            // If there's no text to filter by, show all results.
            mVisibleCarMakes.clear();
            mVisibleCarMakes.addAll(mCarMakes);
        }

        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedCarMakes() {
        ArrayList<String> selectedCarMakes = new ArrayList<>();
        for (int selectedCarMakeIndex : mSelectedCarMakeIndexes) {
            selectedCarMakes.add(mCarMakes.get(selectedCarMakeIndex));
        }

        return selectedCarMakes;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final CompoundButton itemCheckBox;

        public ViewHolder(View checkBox) {
            super(checkBox);

            itemCheckBox = (CompoundButton) checkBox;
        }
    }
}
