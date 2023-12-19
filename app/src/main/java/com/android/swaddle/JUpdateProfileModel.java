package com.android.swaddle;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.ArrayList;

public class JUpdateProfileModel {

    @SerializedName("child_id")
    String child_id;

    @SerializedName("profile_picture")
    File profile_picture;

    @SerializedName("first_name")
    String first_name;

    @SerializedName("middle_name")
    String middle_name;


    @SerializedName("last_name")
    String last_name;

    @SerializedName("nick_name")
    String nick_name;


    @SerializedName("dob")
    String dob;


    @SerializedName("child_call_you")
    String child_call_you;


    @SerializedName("gender")
    String gender;


    @SerializedName("primary_address")
    String primary_address;


    @SerializedName("secondary_address")
    String secondary_address;


    @SerializedName("custody")
    String custody;

    @SerializedName("iep")
    String iep;


    @SerializedName("comment")
    String comment;

    @SerializedName("iep_diagnosed_by")
    String iep_diagnosed_by;


    @SerializedName("does_have_any_allergies")
    String does_have_any_allergies;


    @SerializedName("food_allergies")
    ArrayList<String> food_allergies;


    @SerializedName("environmental_allergies")
    ArrayList<String> environmental_allergies;

    @SerializedName("does_child_require_epi_pin")
    String does_child_require_epi_pin;


    @SerializedName("will_you_provide_epi_pin")
    String will_you_provide_epi_pin;

    @SerializedName("epi_pen_expiration_date")
    String epi_pen_expiration_date;


    @SerializedName("monday")
    ArrayList<String> monday;

    @SerializedName("tuesday")
    ArrayList<String> tuesday;

    @SerializedName("wednesday")
    ArrayList<String> wednesday;

    @SerializedName("thursday")
    ArrayList<String> thursday;

    @SerializedName("friday")
    ArrayList<String> friday;


    @SerializedName("custody_consideration")
    String custody_consideration;


    @SerializedName("custody_description")
    String custody_description;

    public JUpdateProfileModel(String child_id,File file, String first_name, String middle_name, String last_name, String nick_name, String dob,
                               String child_call_you, String gender, String primary_address, String secondary_address, String custody, String iep,
                               String comment, String iep_diagnosed_by, String does_have_any_allergies, ArrayList<String> food_allergies,
                               ArrayList<String> environmental_allergies, String does_child_require_epi_pin, String will_you_provide_epi_pin,
                               String epi_pen_expiration_date,
                               ArrayList<String> monday, ArrayList<String> tuesday, ArrayList<String> wednesday,
                               ArrayList<String> thursday, ArrayList<String> friday,String custody_consideration,String custody_description) {
        this.child_id = child_id;
        this.profile_picture = file;
        this.first_name = first_name;
        this.middle_name = middle_name;
        this.last_name = last_name;
        this.nick_name = nick_name;
        this.dob = dob;
        this.child_call_you = child_call_you;
        this.gender = gender;
        this.primary_address = primary_address;
        this.secondary_address = secondary_address;
        this.custody = custody;
        this.iep = iep;
        this.comment = comment;
        this.iep_diagnosed_by = iep_diagnosed_by;
        this.does_have_any_allergies = does_have_any_allergies;
        this.food_allergies = food_allergies;
        this.environmental_allergies = environmental_allergies;
        this.does_child_require_epi_pin = does_child_require_epi_pin;
        this.will_you_provide_epi_pin = will_you_provide_epi_pin;
        this.epi_pen_expiration_date = epi_pen_expiration_date;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.custody_consideration = custody_consideration;
        this.custody_description = custody_description;
    }
}
