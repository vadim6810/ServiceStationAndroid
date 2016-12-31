package il.co.tel_ran.carservice;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by maxim on 26-Dec-16.
 */

public enum ServiceSubWorkType {
    AEROGRAPHY(ServiceWorkType.BODY_WORK),
    PAINTING(ServiceWorkType.BODY_WORK),
    POLISH(ServiceWorkType.BODY_WORK),
    BODY_REPAIR(ServiceWorkType.BODY_WORK),
    SELECTION_OF_COLORS(ServiceWorkType.BODY_WORK),
    BUMPER_REPAIR(ServiceWorkType.BODY_WORK),
    REMOVE_CHIPS(ServiceWorkType.BODY_WORK),
    ANTI_CORROSION_TREATMENT(ServiceWorkType.BODY_WORK),
    COMPUTER_DIAGNOSTICS(ServiceWorkType.DIAGNOSIS_REPAIR_ENGINE),
    ENGINE_REPAIR(ServiceWorkType.DIAGNOSIS_REPAIR_ENGINE),
    REPAIR_OF_DIESEL_ENGINE(ServiceWorkType.DIAGNOSIS_REPAIR_ENGINE),
    OVERHAUL(ServiceWorkType.DIAGNOSIS_REPAIR_ENGINE),
    REPAIR_CARBURETTORS(ServiceWorkType.DIAGNOSIS_REPAIR_ENGINE),
    REPAIR_INJECTORS(ServiceWorkType.DIAGNOSIS_REPAIR_ENGINE),
    REPAIR_COOLING_SYSTEM(ServiceWorkType.DIAGNOSIS_REPAIR_ENGINE),
    DIAGNOSING_UNDERCARRIAGE(ServiceWorkType.CHASSIS),
    REPAIR_UNDERCARRIAGE(ServiceWorkType.CHASSIS),
    ALIGNMENT(ServiceWorkType.CHASSIS),
    GEARBOX_DIAGNOSIS(ServiceWorkType.PPC),
    MANUAL_TRANSMISSION_REPAIR(ServiceWorkType.PPC),
    AUTOMATIC_TRANSMISSIONS_REPAIR(ServiceWorkType.PPC),
    CLUTCH_REPAIR(ServiceWorkType.PPC),
    ENGINE_OIL_REPLACEMENT(ServiceWorkType.PLANNED_TO),
    GEARBOX_OIL_REPLACEMENT(ServiceWorkType.PLANNED_TO),
    SPARK_REPLACEMENT(ServiceWorkType.PLANNED_TO),
    FILTERS_REPLACEMENT(ServiceWorkType.PLANNED_TO),
    FLUIDS_REPLACEMENT(ServiceWorkType.PLANNED_TO),
    ELECTRICAL_EQUIPMENT_REPAIR(ServiceWorkType.ELECTRICS),
    AIR_CONDITIONER_REPAIR(ServiceWorkType.ELECTRICS),
    AIR_CONDITIONER_REFILL(ServiceWorkType.ELECTRICS),
    HEATING_SYSTEM_REPAIR(ServiceWorkType.ELECTRICS),
    INJECTOR_FLUSHING(ServiceWorkType.ELECTRICS),
    NOZZLE_CHECK(ServiceWorkType.ELECTRICS),
    GENERATOR_REPAIR(ServiceWorkType.ELECTRICS),
    STARTER_REPAIR(ServiceWorkType.ELECTRICS),
    STEERING_REPAIR(ServiceWorkType.STEERING_GEAR),
    STEERING_RACK_REPAIR(ServiceWorkType.STEERING_GEAR),
    POWER_STEERING_REPAIR(ServiceWorkType.STEERING_GEAR),
    MUFFLERS_REPAIR(ServiceWorkType.EXHAUST_SYSTEM),
    CATALYSTS_REPAIR(ServiceWorkType.EXHAUST_SYSTEM),
    SILENCING_REPLACEMENT(ServiceWorkType.EXHAUST_SYSTEM),
    TIPS_INSTALLATION(ServiceWorkType.EXHAUST_SYSTEM),
    XENON_HEADLIGHTS_INSTALLATION(ServiceWorkType.OPTIONAL_EQUIPMENT),
    GLASS_TINTING(ServiceWorkType.OPTIONAL_EQUIPMENT),
    HEADLAMPS_TINTING(ServiceWorkType.OPTIONAL_EQUIPMENT),
    FILM_COVERING(ServiceWorkType.OPTIONAL_EQUIPMENT),
    MUSIC_EQUIPMENT_INSTALLATION(ServiceWorkType.OPTIONAL_EQUIPMENT),
    ALARMS_SETTING(ServiceWorkType.OPTIONAL_EQUIPMENT),
    PARKING_SENSOR_INSTALLATION(ServiceWorkType.OPTIONAL_EQUIPMENT),
    HBO_INSTALLATION(ServiceWorkType.OPTIONAL_EQUIPMENT),
    SOUNDPROOFING(ServiceWorkType.OPTIONAL_EQUIPMENT);

    private final ServiceWorkType mParentWorkType;

    ServiceSubWorkType(ServiceWorkType parentWorkType) {
        mParentWorkType = parentWorkType;
    }

    public ServiceWorkType getParentWorkType() {
        return mParentWorkType;
    }

    public static ArrayList<String> getFieldListForTypes(ArrayList<ServiceSubWorkType> subWorkTypes) {
        ArrayList<String> fields = new ArrayList<>();
        if (subWorkTypes != null && !subWorkTypes.isEmpty()) {
            for (ServiceSubWorkType subWorkType : subWorkTypes) {
                String field = getFieldForType(subWorkType);
                if (field != null)
                    fields.add(field);
            }
        }

        return fields;
    }

    public static String getFieldForType(ServiceSubWorkType subWorkType) {
        switch (subWorkType) {
            case AEROGRAPHY:
                return "Аэрография";
            case PAINTING:
                return "Покраска";
            case POLISH:
                return "Полировка";
            case BODY_REPAIR:
                return "Кузовной ремонт";
            case SELECTION_OF_COLORS:
                return "Подбор красок";
            case BUMPER_REPAIR:
                return "Ремонт бамперов";
            case REMOVE_CHIPS:
                return "Устранение сколов";
            case ANTI_CORROSION_TREATMENT:
                return "Антикоррозийная обработка";
            case COMPUTER_DIAGNOSTICS:
                return "Компьютерная диагностика";
            case ENGINE_REPAIR:
                return "Ремонт двигателя";
            case REPAIR_OF_DIESEL_ENGINE:
                return "Ремонт дизельных двигателей";
            case OVERHAUL:
                return "Капитальный ремонт";
            case REPAIR_CARBURETTORS:
                return "Ремонт карбюраторов";
            case REPAIR_INJECTORS:
                return "Ремонт инжекторов";
            case REPAIR_COOLING_SYSTEM:
                return "Ремонт систем охлаждения";
            case DIAGNOSING_UNDERCARRIAGE:
                return "Диагностика ходовой";
            case REPAIR_UNDERCARRIAGE:
                return "Ремонт ходовой";
            case ALIGNMENT:
                return "Развал-схождение";
            case GEARBOX_DIAGNOSIS:
                return "Диагностика КПП";
            case MANUAL_TRANSMISSION_REPAIR:
                return "Ремонт МКПП";
            case AUTOMATIC_TRANSMISSIONS_REPAIR:
                return "Ремонт АКПП";
            case CLUTCH_REPAIR:
                return "Ремонт сцепления";
            case ENGINE_OIL_REPLACEMENT:
                return "Замена масла в двигателе";
            case GEARBOX_OIL_REPLACEMENT:
                return "Замена масла в КПП";
            case SPARK_REPLACEMENT:
                return "Замена свечей";
            case FILTERS_REPLACEMENT:
                return "Замена фильтров";
            case FLUIDS_REPLACEMENT:
                return "Замена жидкостей";
            case ELECTRICAL_EQUIPMENT_REPAIR:
                return "Ремонт электрооборудования";
            case AIR_CONDITIONER_REPAIR:
                return "Ремонт кондиционеров";
            case AIR_CONDITIONER_REFILL:
                return "Заправка кондиционеров";
            case HEATING_SYSTEM_REPAIR:
                return "Ремонт отопительной системы";
            case INJECTOR_FLUSHING:
                return "Промывка инжектора";
            case NOZZLE_CHECK:
                return "Проверка форсунок на стенде";
            case GENERATOR_REPAIR:
                return "Ремонт генераторов";
            case STARTER_REPAIR:
                return "Ремонт стартеров";
            case STEERING_REPAIR:
                return "Ремонт рулевого механизма";
            case STEERING_RACK_REPAIR:
                return "Ремонт рулевой рейки";
            case POWER_STEERING_REPAIR:
                return "Ремонт гидроусилителя руля";
            case MUFFLERS_REPAIR:
                return "Ремонт глушителей";
            case CATALYSTS_REPAIR:
                return "Ремонт катализаторов";
            case SILENCING_REPLACEMENT:
                return "Замена глушителей";
            case TIPS_INSTALLATION:
                return "Установка насадок";
            case XENON_HEADLIGHTS_INSTALLATION:
                return "Установка ксеноновых фар";
            case GLASS_TINTING:
                return "Тонировка стекол";
            case HEADLAMPS_TINTING:
                return "Тонировка фар";
            case FILM_COVERING:
                return "Покрытие пленками";
            case MUSIC_EQUIPMENT_INSTALLATION:
                return "Установка музыкального оборудования";
            case ALARMS_SETTING:
                return "Установка сигнализаций";
            case PARKING_SENSOR_INSTALLATION:
                return "Установка датчиков парковки";
            case HBO_INSTALLATION:
                return "Установка ГБО";
            case SOUNDPROOFING:
                return "Шумоизоляция";
        }

        return null;
    }

    public static ServiceSubWorkType getTypeFromField(String field) {
        if (field.equals("Аэрография"))
            return AEROGRAPHY;
        if (field.equals("Покраска"))
            return PAINTING;
        if (field.equals("Полировка"))
            return POLISH;
        if (field.equals("Кузовной ремонт"))
            return BODY_REPAIR;
        if (field.equals("Подбор красок"))
            return SELECTION_OF_COLORS;
        if (field.equals("Ремонт бамперов"))
            return BUMPER_REPAIR;
        if (field.equals("Устранение сколов"))
            return REMOVE_CHIPS;
        if (field.equals("Антикоррозийная обработка"))
            return ANTI_CORROSION_TREATMENT;
        if (field.equals("Компьютерная диагностика"))
            return COMPUTER_DIAGNOSTICS;
        if (field.equals("Ремонт двигателя"))
            return ENGINE_REPAIR;
        if (field.equals("Ремонт дизельных двигателей"))
            return REPAIR_OF_DIESEL_ENGINE;
        if (field.equals("Капитальный ремонт"))
            return OVERHAUL;
        if (field.equals("Ремонт карбюраторов"))
            return REPAIR_CARBURETTORS;
        if (field.equals("Ремонт инжекторов"))
            return REPAIR_INJECTORS;
        if (field.equals("Ремонт систем охлаждения"))
            return REPAIR_COOLING_SYSTEM;
        if (field.equals("Диагностика ходовой"))
            return DIAGNOSING_UNDERCARRIAGE;
        if (field.equals("Ремонт ходовой"))
            return REPAIR_UNDERCARRIAGE;
        if (field.equals("Развал-схождение"))
            return ALIGNMENT;
        if (field.equals("Диагностика КПП"))
            return GEARBOX_DIAGNOSIS;
        if (field.equals("Ремонт МКПП"))
            return MANUAL_TRANSMISSION_REPAIR;
        if (field.equals("Ремонт АКПП"))
            return AUTOMATIC_TRANSMISSIONS_REPAIR;
        if (field.equals("Ремонт сцепления"))
            return CLUTCH_REPAIR;
        if (field.equals("Замена масла в двигателе"))
            return ENGINE_OIL_REPLACEMENT;
        if (field.equals("Замена масла в КПП"))
            return GEARBOX_OIL_REPLACEMENT;
        if (field.equals("Замена свечей"))
            return SPARK_REPLACEMENT;
        if (field.equals("Замена фильтров"))
            return FILTERS_REPLACEMENT;
        if (field.equals("Замена жидкостей"))
            return FLUIDS_REPLACEMENT;
        if (field.equals("Ремонт электрооборудования"))
            return ELECTRICAL_EQUIPMENT_REPAIR;
        if (field.equals("Ремонт кондиционеров"))
            return AIR_CONDITIONER_REPAIR;
        if (field.equals("Заправка кондиционеров"))
            return AIR_CONDITIONER_REFILL;
        if (field.equals("Ремонт отопительной системы"))
            return HEATING_SYSTEM_REPAIR;
        if (field.equals("Промывка инжектора"))
            return INJECTOR_FLUSHING;
        if (field.equals("Проверка форсунок на стенде"))
            return NOZZLE_CHECK;
        if (field.equals("Ремонт генераторов"))
            return GENERATOR_REPAIR;
        if (field.equals("Ремонт стартеров"))
            return STARTER_REPAIR;
        if (field.equals("Ремонт рулевого механизма"))
            return STEERING_REPAIR;
        if (field.equals("Ремонт рулевой рейки"))
            return STEERING_RACK_REPAIR;
        if (field.equals("Ремонт гидроусилителя руля"))
            return POWER_STEERING_REPAIR;
        if (field.equals("Ремонт глушителей"))
            return MUFFLERS_REPAIR;
        if (field.equals("Ремонт катализаторов"))
            return CATALYSTS_REPAIR;
        if (field.equals("Замена глушителей"))
            return SILENCING_REPLACEMENT;
        if (field.equals("Установка насадок"))
            return TIPS_INSTALLATION;
        if (field.equals("Установка ксеноновых фар"))
            return XENON_HEADLIGHTS_INSTALLATION;
        if (field.equals("Тонировка стекол"))
            return GLASS_TINTING;
        if (field.equals("Тонировка фар"))
            return HEADLAMPS_TINTING;
        if (field.equals("Покрытие пленками"))
            return FILM_COVERING;
        if (field.equals("Установка музыкального оборудования"))
            return MUSIC_EQUIPMENT_INSTALLATION;
        if (field.equals("Установка сигнализаций"))
            return ALARMS_SETTING;
        if (field.equals("Установка датчиков парковки"))
            return PARKING_SENSOR_INSTALLATION;
        if (field.equals("Установка ГБО"))
            return HBO_INSTALLATION;
        if (field.equals("Шумоизоляция"))
            return SOUNDPROOFING;

        return null;
    }

    public ServiceSubWorkType setDescription(String string) {
        mReadableValue = string;

        return this;
    }

    public ServiceSubWorkType setDescription(Context context, int resId) {
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