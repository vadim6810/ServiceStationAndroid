package il.co.tel_ran.carservice;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by maxim on 17-Dec-16.
 */

public enum ServiceWorkType {
    BODY_WORK,
    DIAGNOSIS_REPAIR_ENGINE,
    CHASSIS,
    PPC,
    PLANNED_TO,
    ELECTRICS,
    STEERING_GEAR,
    EXHAUST_SYSTEM,
    OPTIONAL_EQUIPMENT;

    public static ArrayList<String> getFieldListForTypes(ArrayList<ServiceWorkType> workTypes) {
        ArrayList<String> fields = new ArrayList<>();
        if (workTypes != null && !workTypes.isEmpty()) {
            for (ServiceWorkType workType : workTypes) {
                String field = getFieldForType(workType);
                if (field != null)
                    fields.add(field);
            }
        }

        return fields;
    }

    public static String getFieldForType(ServiceWorkType workType) {
        switch (workType) {
            case BODY_WORK:
                return "Кузовные работы";
            case DIAGNOSIS_REPAIR_ENGINE:
                return "Диагностика и ремонт двигателя";
            case CHASSIS:
                return "Ходовая";
            case PPC:
                return "КПП";
            case PLANNED_TO:
                return "Плановое ТО";
            case ELECTRICS:
                return "Электрооборудование";
            case STEERING_GEAR:
                return "Рулевой механизм";
            case EXHAUST_SYSTEM:
                return "Выхлопная система";
            case OPTIONAL_EQUIPMENT:
                return "Дополнительное оборудование";
        }

        return null;
    }

    public static ServiceWorkType getTypeFromField(String field) {
        if (field.equals("Кузовные работы"))
            return BODY_WORK;
        if (field.equals("Диагностика и ремонт двигателя"))
            return DIAGNOSIS_REPAIR_ENGINE;
        if (field.equals("Ходовая"))
            return CHASSIS;
        if (field.equals("КПП"))
            return PPC;
        if (field.equals("Плановое ТО"))
            return PLANNED_TO;
        if (field.equals("Электрооборудование"))
            return ELECTRICS;
        if (field.equals("Рулевой механизм"))
            return STEERING_GEAR;
        if (field.equals("Выхлопная система"))
            return EXHAUST_SYSTEM;
        if (field.equals("Дополнительное оборудование"))
            return OPTIONAL_EQUIPMENT;

        return null;
    }

    public ServiceWorkType setDescription(String string) {
        mReadableValue = string;

        return this;
    }

    public ServiceWorkType setDescription(Context context, int resId) {
        mReadableValue = context.getString(resId);

        return this;
    }

    private String mReadableValue;

    @Override
    public String toString() {
        if (mReadableValue == null || mReadableValue.isEmpty()) {
            return super.toString();
        }
        return mReadableValue;
    }
}
