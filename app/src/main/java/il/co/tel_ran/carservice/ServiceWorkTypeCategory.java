package il.co.tel_ran.carservice;

/**
 * Created by maxim on 28-Dec-16.
 */

import android.content.Context;

import java.util.ArrayList;
import java.util.EnumSet;

import static il.co.tel_ran.carservice.ServiceSubWorkType.AEROGRAPHY;
import static il.co.tel_ran.carservice.ServiceSubWorkType.AIR_CONDITIONER_REFILL;
import static il.co.tel_ran.carservice.ServiceSubWorkType.AIR_CONDITIONER_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.ALARMS_SETTING;
import static il.co.tel_ran.carservice.ServiceSubWorkType.ALIGNMENT;
import static il.co.tel_ran.carservice.ServiceSubWorkType.ANTI_CORROSION_TREATMENT;
import static il.co.tel_ran.carservice.ServiceSubWorkType.AUTOMATIC_TRANSMISSIONS_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.BODY_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.BUMPER_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.CATALYSTS_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.CLUTCH_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.COMPUTER_DIAGNOSTICS;
import static il.co.tel_ran.carservice.ServiceSubWorkType.DIAGNOSING_UNDERCARRIAGE;
import static il.co.tel_ran.carservice.ServiceSubWorkType.ELECTRICAL_EQUIPMENT_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.ENGINE_OIL_REPLACEMENT;
import static il.co.tel_ran.carservice.ServiceSubWorkType.ENGINE_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.FILM_COVERING;
import static il.co.tel_ran.carservice.ServiceSubWorkType.FILTERS_REPLACEMENT;
import static il.co.tel_ran.carservice.ServiceSubWorkType.FLUIDS_REPLACEMENT;
import static il.co.tel_ran.carservice.ServiceSubWorkType.GEARBOX_DIAGNOSIS;
import static il.co.tel_ran.carservice.ServiceSubWorkType.GEARBOX_OIL_REPLACEMENT;
import static il.co.tel_ran.carservice.ServiceSubWorkType.GENERATOR_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.GLASS_TINTING;
import static il.co.tel_ran.carservice.ServiceSubWorkType.HBO_INSTALLATION;
import static il.co.tel_ran.carservice.ServiceSubWorkType.HEADLAMPS_TINTING;
import static il.co.tel_ran.carservice.ServiceSubWorkType.HEATING_SYSTEM_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.INJECTOR_FLUSHING;
import static il.co.tel_ran.carservice.ServiceSubWorkType.MANUAL_TRANSMISSION_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.MUFFLERS_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.MUSIC_EQUIPMENT_INSTALLATION;
import static il.co.tel_ran.carservice.ServiceSubWorkType.NOZZLE_CHECK;
import static il.co.tel_ran.carservice.ServiceSubWorkType.OVERHAUL;
import static il.co.tel_ran.carservice.ServiceSubWorkType.PAINTING;
import static il.co.tel_ran.carservice.ServiceSubWorkType.PARKING_SENSOR_INSTALLATION;
import static il.co.tel_ran.carservice.ServiceSubWorkType.POLISH;
import static il.co.tel_ran.carservice.ServiceSubWorkType.POWER_STEERING_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.REMOVE_CHIPS;
import static il.co.tel_ran.carservice.ServiceSubWorkType.REPAIR_CARBURETTORS;
import static il.co.tel_ran.carservice.ServiceSubWorkType.REPAIR_COOLING_SYSTEM;
import static il.co.tel_ran.carservice.ServiceSubWorkType.REPAIR_INJECTORS;
import static il.co.tel_ran.carservice.ServiceSubWorkType.REPAIR_OF_DIESEL_ENGINE;
import static il.co.tel_ran.carservice.ServiceSubWorkType.REPAIR_UNDERCARRIAGE;
import static il.co.tel_ran.carservice.ServiceSubWorkType.SELECTION_OF_COLORS;
import static il.co.tel_ran.carservice.ServiceSubWorkType.SILENCING_REPLACEMENT;
import static il.co.tel_ran.carservice.ServiceSubWorkType.SOUNDPROOFING;
import static il.co.tel_ran.carservice.ServiceSubWorkType.SPARK_REPLACEMENT;
import static il.co.tel_ran.carservice.ServiceSubWorkType.STARTER_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.STEERING_RACK_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.STEERING_REPAIR;
import static il.co.tel_ran.carservice.ServiceSubWorkType.TIPS_INSTALLATION;
import static il.co.tel_ran.carservice.ServiceSubWorkType.XENON_HEADLIGHTS_INSTALLATION;

/**
 * Holds a main {@link ServiceWorkType} and an {@link java.util.EnumSet} of {@link ServiceSubWorkType}
 */
public class ServiceWorkTypeCategory {

    private ServiceWorkType mServiceWorkType;
    private ArrayList<ServiceSubWorkType> mSubWorkTypes;

    public ServiceWorkTypeCategory(ServiceWorkType serviceWorkType) {
        this(serviceWorkType, new ArrayList<ServiceSubWorkType>());
    }

    public ServiceWorkTypeCategory(ServiceWorkType serviceWorkType,
                                   ArrayList<ServiceSubWorkType> serviceSubWorkTypes) {
        mServiceWorkType    = serviceWorkType;
        mSubWorkTypes       = serviceSubWorkTypes;
    }

    public void setServiceWorkType(ServiceWorkType workType) {
        mServiceWorkType = workType;
    }

    public ServiceWorkType getServiceWorkType() {
        return mServiceWorkType;
    }

    public ServiceWorkTypeCategory addSubWorkType(ServiceSubWorkType subWorkType) {
        if (!mSubWorkTypes.contains(subWorkType)) {
            mSubWorkTypes.add(subWorkType);
        }

        return this;
    }

    public ArrayList<ServiceSubWorkType> getSubWorkTypes() {
        return mSubWorkTypes;
    }

    /**
     * Get a ready preset of a category by work type.
     * @param workType Main work type for this category
     * @return a ready instance {@link ServiceWorkTypeCategory} containing main work type and sub work types.
     */
    public static ServiceWorkTypeCategory getPreBuiltCategory(ServiceWorkType workType,
                                                              Context context) {
        ServiceWorkTypeCategory category = new ServiceWorkTypeCategory(workType);

        String workTypeString = null;
        switch (workType) {
            case BODY_WORK:
                workTypeString = context.getString(R.string.work_type_body_work);

                category.addSubWorkType(AEROGRAPHY.setDescription(context, R.string.sub_work_type_aerography))
                        .addSubWorkType(PAINTING.setDescription(context, R.string.sub_work_type_painting))
                        .addSubWorkType(POLISH.setDescription(context, R.string.sub_work_type_polish))
                        .addSubWorkType(BODY_REPAIR.setDescription(context, R.string.sub_work_type_body_repair))
                        .addSubWorkType(SELECTION_OF_COLORS.setDescription(context, R.string.sub_work_type_color_selection))
                        .addSubWorkType(BUMPER_REPAIR.setDescription(context, R.string.sub_work_type_bumper_repair))
                        .addSubWorkType(REMOVE_CHIPS.setDescription(context, R.string.sub_work_type_remove_chips))
                        .addSubWorkType(ANTI_CORROSION_TREATMENT.setDescription(context, R.string.sub_work_type_anti_corrosion_treatment));
                break;
            case DIAGNOSIS_REPAIR_ENGINE:
                workTypeString = context.getString(R.string.work_type_engine_diagnosis_repair);

                category.addSubWorkType(ENGINE_REPAIR.setDescription(context, R.string.sub_work_type_diesel_engine_repair))
                        .addSubWorkType(REPAIR_OF_DIESEL_ENGINE.setDescription(context, R.string.sub_work_type_diesel_engine_repair))
                        .addSubWorkType(OVERHAUL.setDescription(context, R.string.sub_work_type_overhaul))
                        .addSubWorkType(REPAIR_CARBURETTORS.setDescription(context, R.string.sub_work_type_carburettors_repair))
                        .addSubWorkType(REPAIR_INJECTORS.setDescription(context, R.string.sub_work_type_injectors_repair))
                        .addSubWorkType(REPAIR_COOLING_SYSTEM.setDescription(context, R.string.sub_work_type_cooling_system_repair));
                break;
            case CHASSIS:
                workTypeString = context.getString(R.string.work_type_chassis);

                category.addSubWorkType(DIAGNOSING_UNDERCARRIAGE.setDescription(context, R.string.sub_work_type_undercarriage_diagnostics))
                        .addSubWorkType(REPAIR_UNDERCARRIAGE.setDescription(context, R.string.sub_work_type_undercarriage_repair))
                        .addSubWorkType(ALIGNMENT.setDescription(context, R.string.sub_work_type_alignment));
                break;
            case PPC:
                workTypeString = context.getString(R.string.work_type_ppc);

                category.addSubWorkType(GEARBOX_DIAGNOSIS.setDescription(context, R.string.sub_work_type_gearbox_diagnosis))
                        .addSubWorkType(MANUAL_TRANSMISSION_REPAIR.setDescription(context, R.string.sub_work_type_manual_transmission_repair))
                        .addSubWorkType(AUTOMATIC_TRANSMISSIONS_REPAIR.setDescription(context, R.string.sub_work_type_automatic_transmission_repair))
                        .addSubWorkType(CLUTCH_REPAIR.setDescription(context, R.string.sub_work_type_clutch_repair));
                break;
            case PLANNED_TO:
                workTypeString = context.getString(R.string.work_type_planned_to);

                category.addSubWorkType(ENGINE_OIL_REPLACEMENT.setDescription(context, R.string.sub_work_type_engine_oil_replacement))
                        .addSubWorkType(GEARBOX_OIL_REPLACEMENT.setDescription(context, R.string.sub_work_type_gearbox_oil_replacement))
                        .addSubWorkType(SPARK_REPLACEMENT.setDescription(context, R.string.sub_work_type_spark_replacement))
                        .addSubWorkType(FILTERS_REPLACEMENT.setDescription(context, R.string.sub_work_type_filters_replacement))
                        .addSubWorkType(FLUIDS_REPLACEMENT.setDescription(context, R.string.sub_work_type_fluids_replacement));
                break;
            case ELECTRICS:
                workTypeString = context.getString(R.string.work_type_electrics);

                category.addSubWorkType(COMPUTER_DIAGNOSTICS.setDescription(context, R.string.sub_work_type_computer_diagnostics))
                        .addSubWorkType(ELECTRICAL_EQUIPMENT_REPAIR.setDescription(context, R.string.sub_work_type_electrical_equipment_repair))
                        .addSubWorkType(AIR_CONDITIONER_REPAIR.setDescription(context, R.string.sub_work_type_ac_repair))
                        .addSubWorkType(AIR_CONDITIONER_REFILL.setDescription(context, R.string.sub_work_type_ac_refill))
                        .addSubWorkType(HEATING_SYSTEM_REPAIR.setDescription(context, R.string.sub_work_type_heating_system_repair))
                        .addSubWorkType(INJECTOR_FLUSHING.setDescription(context, R.string.sub_work_type_injector_flushing))
                        .addSubWorkType(NOZZLE_CHECK.setDescription(context, R.string.sub_work_type_nozzle_check))
                        .addSubWorkType(GENERATOR_REPAIR.setDescription(context, R.string.sub_work_type_generator_repair))
                        .addSubWorkType(STARTER_REPAIR.setDescription(context, R.string.sub_work_type_starter_repair));
                break;
            case STEERING_GEAR:
                workTypeString = context.getString(R.string.work_type_steering_gear);

                category.addSubWorkType(STEERING_REPAIR.setDescription(context, R.string.work_type_steering_gear))
                        .addSubWorkType(STEERING_RACK_REPAIR.setDescription(context, R.string.sub_work_type_steering_rack_repair))
                        .addSubWorkType(POWER_STEERING_REPAIR.setDescription(context, R.string.sub_work_type_power_steering_repair));
                break;
            case EXHAUST_SYSTEM:
                workTypeString = context.getString(R.string.work_type_exhaust_system);

                category.addSubWorkType(MUFFLERS_REPAIR.setDescription(context, R.string.sub_work_type_mufflers_repair))
                        .addSubWorkType(CATALYSTS_REPAIR.setDescription(context, R.string.sub_work_type_catalysts_repair))
                        .addSubWorkType(SILENCING_REPLACEMENT.setDescription(context, R.string.sub_work_type_silencing_replacement))
                        .addSubWorkType(TIPS_INSTALLATION.setDescription(context, R.string.sub_work_type_tips_installation));
                break;
            case OPTIONAL_EQUIPMENT:
                workTypeString = context.getString(R.string.work_type_optional_equipment);

                category.addSubWorkType(XENON_HEADLIGHTS_INSTALLATION.setDescription(context, R.string.sub_work_type_xenon_headlights_installation))
                        .addSubWorkType(GLASS_TINTING.setDescription(context, R.string.sub_work_type_glass_tinting))
                        .addSubWorkType(HEADLAMPS_TINTING.setDescription(context, R.string.sub_work_type_headlamps_tinting))
                        .addSubWorkType(FILM_COVERING.setDescription(context, R.string.sub_work_type_film_covering))
                        .addSubWorkType(MUSIC_EQUIPMENT_INSTALLATION.setDescription(context, R.string.sub_work_type_music_equipment_installation))
                        .addSubWorkType(ALARMS_SETTING.setDescription(context, R.string.sub_work_type_alarm_setting))
                        .addSubWorkType(PARKING_SENSOR_INSTALLATION.setDescription(context, R.string.sub_work_type_parking_sensor_installation))
                        .addSubWorkType(HBO_INSTALLATION.setDescription(context, R.string.sub_work_type_hbo_installation))
                        .addSubWorkType(SOUNDPROOFING.setDescription(context, R.string.sub_work_type_soundproofing));
                break;
        }

        workType.setDescription(workTypeString);

        return category;
    }

    public static ArrayList<ServiceWorkTypeCategory> generateWorkTypeCategories(Context context) {
        ArrayList<ServiceWorkTypeCategory> serviceWorkTypeCategories = new ArrayList<>();
        // Generate categories for all work types.
        for (ServiceWorkType workType : EnumSet.allOf(ServiceWorkType.class)) {
            serviceWorkTypeCategories.add(ServiceWorkTypeCategory.getPreBuiltCategory(workType,
                    context));
        }

        return serviceWorkTypeCategories;
    }
}
