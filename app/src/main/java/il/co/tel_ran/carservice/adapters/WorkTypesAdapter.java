package il.co.tel_ran.carservice.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import il.co.tel_ran.carservice.ServiceSubWorkType;
import il.co.tel_ran.carservice.ServiceWorkType;
import il.co.tel_ran.carservice.ServiceWorkTypeCategory;

/**
 * Created by maxim on 20-Dec-16.
 */

public class WorkTypesAdapter extends RecyclerView.Adapter<WorkTypesAdapter.ViewHolder> {

    public static final int VIEW_TYPE_TITLE_MULTI_CHOICE   = 0;
    public static final int VIEW_TYPE_TITLE_SINGLE_CHOICE  = 1;
    public static final int VIEW_TYPE_ITEM_MULTI_CHOICE    = 2;
    public static final int VIEW_TYPE_ITEM_SINGLE_CHOICE   = 3;

    private SparseArray<ServiceWorkType> mWorkTypes = new SparseArray<>();
    // Used for multi-choice items (the other will be empty).
    private SparseArray<ServiceSubWorkType> mSubWorkTypes;
    // Used for single-choice items (the other will be empty).
    private SparseArray<ArrayList<ServiceSubWorkType>> mSubWorkTypeArrays;

    private boolean mIsSingleChoice;

    private SparseBooleanArray mCheckedItems = new SparseBooleanArray();

    private CompoundButton mPreviousCheckedButton;

    public WorkTypesAdapter(List<ServiceWorkTypeCategory> categories, Context context,
                            boolean isSingleChoice,
                            ArrayList<ServiceSubWorkType> selectedSubWorkTypes) {
        mIsSingleChoice = isSingleChoice;

        int position = 0;
        for (ServiceWorkTypeCategory category : categories) {
            mWorkTypes.append(position, category.getServiceWorkType());

            ArrayList<ServiceSubWorkType> subWorkTypes = category.getSubWorkTypes();

            // The positioning logic is different for single choice items.
            // When single choice, the items in the RecyclerView are actually RadioGroup views, therefore
            // the entire sub work types are considered as one item.
            // For multi-choice items, each item has a corresponding CheckBox view, making it independent.
            if (isSingleChoice) {
                if (mSubWorkTypeArrays == null) {
                    mSubWorkTypeArrays = new SparseArray<>();
                }
                mSubWorkTypeArrays.append(++position, subWorkTypes);

                /*for (ServiceSubWorkType subWorkType : subWorkTypes) {
                    // Update checked items (if any).
                    if (selectedSubWorkTypes != null
                            && selectedSubWorkTypes.contains(subWorkType)) {
                        int checkedKey = getKeyForPosition(position,
                                subWorkTypes.indexOf(subWorkType));

                        mCheckedItems.append(checkedKey, true);
                    }
                }*/
            } else {
                if (mSubWorkTypes == null) {
                    mSubWorkTypes = new SparseArray<>();
                }

                /*int titleKey = getKeyForPosition(position);
                int checkedSubWorkTypesCount = 0;
                for (ServiceSubWorkType subWorkType : subWorkTypes) {
                    mSubWorkTypes.append(++position, subWorkType);

                    // Update checked items (if any).
                    if (selectedSubWorkTypes != null
                            && selectedSubWorkTypes.contains(subWorkType)) {
                        int checkedKey = getKeyForPosition(position);
                        mCheckedItems.append(checkedKey, true);

                        checkedSubWorkTypesCount++;
                    }
                }

                // Contains entire category
                if (checkedSubWorkTypesCount == subWorkTypes.size()) {
                    mCheckedItems.append(titleKey, true);
                }*/
            }

            int titleKey = getKeyForPosition(position);
            int checkedSubWorkTypesCount = 0;
            int checkedKey;
            for (ServiceSubWorkType subWorkType : subWorkTypes) {
                if (!isSingleChoice) {
                    mSubWorkTypes.append(++position, subWorkType);
                    checkedKey = getKeyForPosition(position);
                } else {
                    checkedKey = getKeyForPosition(position,
                            subWorkTypes.indexOf(subWorkType));
                }

                // Update checked items (if any).
                if (selectedSubWorkTypes != null
                        && selectedSubWorkTypes.contains(subWorkType)) {
                    mCheckedItems.append(checkedKey, true);

                    checkedSubWorkTypesCount++;
                }
            }

            // Contains entire category
            if (checkedSubWorkTypesCount == subWorkTypes.size()) {
                mCheckedItems.append(titleKey, true);
            }

            position++;
        }
    }

    @Override
    public WorkTypesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();

        View view = null;
        switch (viewType) {
            case VIEW_TYPE_TITLE_MULTI_CHOICE:
                view = new CheckBox(parentContext);
                break;
            case VIEW_TYPE_TITLE_SINGLE_CHOICE:
                view = new TextView(parentContext);
                break;
            case VIEW_TYPE_ITEM_MULTI_CHOICE:
                view = new CheckBox(parentContext);
                break;
            case VIEW_TYPE_ITEM_SINGLE_CHOICE:
                view = new RadioGroup(parentContext);
                break;
        }


        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(final WorkTypesAdapter.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_TITLE_MULTI_CHOICE:
                if (holder.itemOrTitleCheckBox != null) {
                    holder.itemOrTitleCheckBox.setText(
                            mWorkTypes.get(position).toString());

                    final int key = getKeyForPosition(position);
                    holder.itemOrTitleCheckBox.setChecked(mCheckedItems.get(key));

                    int currentWorkTypeIndex = mWorkTypes.indexOfKey(position);
                    final int firstItemPosition = position + 1;
                    final int lastItemPosition;

                    // Check if this is the last item
                    if (mWorkTypes.indexOfKey(position) == mWorkTypes.size() - 1) {
                        lastItemPosition = mSubWorkTypes.keyAt(mSubWorkTypes.size() - 1);
                    } else {
                        lastItemPosition = mWorkTypes.keyAt(currentWorkTypeIndex + 1) - 1;
                    }

                    // We use OnClickListener because the adapter re-uses the CheckBox views and
                    // triggers check change listener (without user interaction).
                    holder.itemOrTitleCheckBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean newState = holder.itemOrTitleCheckBox.isChecked();

                            // Set this CheckBox checked.
                            mCheckedItems.append(key, newState);
                            // Set all sub work types checked
                            for (int i = firstItemPosition; i <= lastItemPosition; i++) {
                                int itemKey = getKeyForPosition(i);
                                boolean prevState = mCheckedItems.get(itemKey);
                                mCheckedItems.append(itemKey, newState);

                                if (newState != prevState) {
                                    notifyItemChanged(i);
                                }
                            }
                        }
                    });
                }
                break;
            case VIEW_TYPE_TITLE_SINGLE_CHOICE:
                if (holder.titleTextView != null) {
                    holder.titleTextView.setText(
                            mWorkTypes.get(position).toString());
                }
                break;
            case VIEW_TYPE_ITEM_MULTI_CHOICE:
                if (holder.itemOrTitleCheckBox != null) {
                    holder.itemOrTitleCheckBox.setText(
                            mSubWorkTypes.get(position).toString());

                    final int key = getKeyForPosition(position);
                    holder.itemOrTitleCheckBox.setChecked(mCheckedItems.get(key));

                    final int finalCurrentPos = position;

                    // We use OnClickListener because the adapter re-uses the CheckBox views and
                    // triggers check change listener (without user interaction).
                    holder.itemOrTitleCheckBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean newState = holder.itemOrTitleCheckBox.isChecked();

                            // Set this CheckBox checked.
                            mCheckedItems.append(key, newState);

                            if (!newState) {
                                int size = mWorkTypes.size();
                                int titlePos = -1;
                                // Remove the title's check (if any)
                                for (int i = 0; i < size; i++ ) {
                                    int workTypePos = mWorkTypes.keyAt(i);
                                    if (workTypePos > finalCurrentPos) {
                                        titlePos = mWorkTypes.keyAt(i - 1);
                                        break;
                                    }
                                }

                                // If we haven't found anything yet that's because this title is the last one.
                                if (titlePos == -1) {
                                    titlePos = mWorkTypes.keyAt(size - 1);
                                }

                                mCheckedItems.append(getKeyForPosition(titlePos), false);
                                // Update the item visually.
                                notifyItemChanged(titlePos);
                            }
                        }
                    });
                }
                break;
            case VIEW_TYPE_ITEM_SINGLE_CHOICE:
                if (holder.itemRadioGroup != null) {
                    // Remove any previous items to make sure we get the right items for the relevant category
                    if (holder.itemRadioGroup.getChildCount() != 0) {
                        holder.itemRadioGroup.removeAllViews();
                    }

                    ArrayList<ServiceSubWorkType> subWorkTypes = mSubWorkTypeArrays.get(position);
                    if (subWorkTypes != null && !subWorkTypes.isEmpty()) {
                        for (ServiceSubWorkType subWorkType : subWorkTypes) {
                            final int key = getKeyForPosition(position,
                                    subWorkTypes.indexOf(subWorkType));
                            // Create new RadioButton for this position.
                            RadioButton radioButton = new RadioButton(
                                    holder.itemRadioGroup.getContext());

                            if (mCheckedItems.get(key)) {
                                radioButton.setChecked(true);
                                mPreviousCheckedButton = radioButton;
                            }

                            // Append the text.
                            radioButton.setText(
                                    subWorkType.toString());
                            // Set listener
                            radioButton.setOnCheckedChangeListener(
                                    new CompoundButton.OnCheckedChangeListener() {

                                        @Override
                                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                            if (mPreviousCheckedButton != null) {
                                                mPreviousCheckedButton.setChecked(false);
                                            }

                                            mPreviousCheckedButton = buttonView;
                                            mCheckedItems.clear();
                                            mCheckedItems.append(key, isChecked);
                                        }
                                    });
                            // Add to group.
                            holder.itemRadioGroup.addView(radioButton);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (mIsSingleChoice) {
            // For each work type we have a title and RadioGroup.
            return mWorkTypes.size() * 2;
        } else {
            // For each work type and for each of their children there's a CheckBox.
            int subWorkTypesSize = mSubWorkTypes.size();
            // Sub work types will contain the higher index if it there are any work types.
            if (subWorkTypesSize > 0) {
                return mSubWorkTypes.keyAt(subWorkTypesSize - 1);
            } else {
                return mWorkTypes.keyAt(mWorkTypes.size() - 1);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mIsSingleChoice) {
            if (position % 2 == 0) {
                return VIEW_TYPE_TITLE_SINGLE_CHOICE;
            } else {
                return VIEW_TYPE_ITEM_SINGLE_CHOICE;
            }
        } else {
            // Check if the position is contained in the work types SparseArray.
            int keyIndex = mWorkTypes.indexOfKey(position);
            if (keyIndex > -1) {
                // It's a title.
                return VIEW_TYPE_TITLE_MULTI_CHOICE;
            }

            // Otherwise this is an item.
            return VIEW_TYPE_ITEM_MULTI_CHOICE ;
        }
    }

    public ArrayList<ServiceSubWorkType> getSelectedSubWorkTypes() {
        ArrayList<ServiceSubWorkType> selectedSubWorkTypes = new ArrayList<>();
        if (mIsSingleChoice) {
            int size = mSubWorkTypeArrays.size();
            for (int i = 0; i < size; i++) {
                // Get position for this index.
                int position = mSubWorkTypeArrays.keyAt(i);
                // Get the array containing ServiceSubWorkType objects
                ArrayList<ServiceSubWorkType> subWorkTypes = mSubWorkTypeArrays.valueAt(i);
                for (ServiceSubWorkType subWorkType : subWorkTypes) {
                    // Calculate the key for the given position.
                    int key = getKeyForPosition(position, subWorkTypes.indexOf(subWorkType));
                    // Check if there's any boolean value corresponding to the key
                    if (mCheckedItems.get(key)) {
                        selectedSubWorkTypes.add(subWorkType);
                    }
                }
            }
        } else {
            int size = mSubWorkTypes.size();
            for (int i = 0; i < size; i++) {
                // Get position for this index.
                int position = mSubWorkTypes.keyAt(i);
                // Calculate the key for the given position.
                int key = getKeyForPosition(position);
                // Check if there's any boolean value corresponding to the key
                if (mCheckedItems.get(key)) {
                    selectedSubWorkTypes.add(mSubWorkTypes.valueAt(i));
                }
            }
        }

        return selectedSubWorkTypes;
    }

    public ArrayList<ServiceWorkType> getSelectedWorkTypes() {
        ArrayList<ServiceWorkType> selectedWorkTypes = new ArrayList<>();

        ArrayList<ServiceSubWorkType> selectedSubWorkTypes = getSelectedSubWorkTypes();

        if (mIsSingleChoice) {
            if (!selectedSubWorkTypes.isEmpty()) {
                int size = mSubWorkTypeArrays.size();
                for (int i = 0; i < size; i++) {
                    // Get the array containing ServiceSubWorkType objects
                    ArrayList<ServiceSubWorkType> subWorkTypes = mSubWorkTypeArrays.valueAt(i);
                    // Check if any of the selected sub work types are contained in this work types array.
                    for (ServiceSubWorkType selectedSubWorkType : selectedSubWorkTypes) {
                        if (subWorkTypes.contains(selectedSubWorkType)) {
                            int workTypesArrayPosition = mSubWorkTypeArrays.keyAt(i);
                            // The selected work type is one position lower.
                            selectedWorkTypes.add(mWorkTypes.get(workTypesArrayPosition - 1));
                            break;
                        }
                    }
                }
            }
        } else {
            int size = mWorkTypes.size();
            for (int i = 0; i < size; i++) {
                ServiceWorkType workType = mWorkTypes.valueAt(i);
                // Get position for this index.
                int position = mWorkTypes.keyAt(i);
                // Calculate the key for the given position.
                int key = getKeyForPosition(position);
                // Check if there's any boolean value corresponding to the key
                if (mCheckedItems.get(key)) {
                    selectedWorkTypes.add(workType);
                    // Skip to the next item to make sure we don't add the same work type twice.
                    continue;
                }

                int nextIndex = i + 1;
                int nextWorkTypePosition = -1;
                if (nextIndex < size) {
                    nextWorkTypePosition = mWorkTypes.keyAt(nextIndex);
                }

                int subWorkTypesSize = mSubWorkTypes.size();
                for (int j = 0; j < subWorkTypesSize; j++) {
                    // Check if any of the selected sub work types contains the sub work type at the given index.
                    if (selectedSubWorkTypes.contains(mSubWorkTypes.valueAt(j))) {
                        // Get position for this sub work type.
                        int subWorkTypePosition = mSubWorkTypes.keyAt(j);
                        if (position < subWorkTypePosition) {
                            if (nextWorkTypePosition == -1) {
                                // If this is true that means this work type is the last one.
                                // Since we did find a sub work type with a greater position, this work type should be added.
                                selectedWorkTypes.add(workType);
                                break;
                            }
                            // Check if this position is between this work type and the next one.
                            if (subWorkTypePosition < nextWorkTypePosition) {
                                // If it is, add it.
                                selectedWorkTypes.add(workType);
                            }
                            break;
                        }
                    }
                }
            }
        }

        return selectedWorkTypes;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleTextView;
        private final CheckBox itemOrTitleCheckBox;
        private final RadioGroup itemRadioGroup;

        public ViewHolder(View layout, int viewType) {
            super(layout);

            switch (viewType) {
                case VIEW_TYPE_TITLE_MULTI_CHOICE:
                    // FALLTHROUGH
                case VIEW_TYPE_ITEM_MULTI_CHOICE:
                    itemOrTitleCheckBox = (CheckBox) layout;

                    titleTextView = null;
                    itemRadioGroup = null;
                    break;
                case VIEW_TYPE_TITLE_SINGLE_CHOICE:
                    titleTextView = (TextView) layout;

                    itemOrTitleCheckBox = null;
                    itemRadioGroup = null;
                    break;
                case VIEW_TYPE_ITEM_SINGLE_CHOICE:
                    itemRadioGroup = (RadioGroup) layout;

                    titleTextView = null;
                    itemOrTitleCheckBox = null;
                    break;
                default:
                    titleTextView = null;
                    itemOrTitleCheckBox = null;
                    itemRadioGroup = null;
                    break;
            }
        }
    }

    private int getKeyForPosition(int position, int index) {
        return getKeyForPosition(position) + index;
    }

    private int getKeyForPosition(int position) {
        return (position + 1) * 100;
    }
}
