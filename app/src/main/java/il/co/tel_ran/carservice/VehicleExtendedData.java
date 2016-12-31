package il.co.tel_ran.carservice;

import java.util.ArrayList;

/**
 * Created by maxim on 30-Dec-16.
 */

public class VehicleExtendedData extends VehicleData {

    private ArrayList<String> mExtraModels;

    public void setExtraModels(ArrayList<String> extraModels) {
        mExtraModels = extraModels;
    }

    public ArrayList<String> getExtraModels() {
        return mExtraModels;
    }
}
