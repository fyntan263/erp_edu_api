package com.innoverse.erp_edu_api.features.income.income_stream;

import lombok.Getter;

@Getter
public enum IncomeSourceType {
    TUITION("Tuition", "Core fees for academic instruction, courses, and educational programs"),
    APPLICATION_AND_REGISTRATION("Application & Registration", "Fees related to student application, enrollment, and registration processes"),
    EXAMINATION_AND_ASSESSMENT("Examination & Assessment", "Fees for tests, exams, assessments, and certification programs"),
    BOARDING_AND_HOSTEL("Boarding & Hostel", "Fees for residential accommodation, lodging, and meal services"),
    TRANSPORTATION("Transportation", "Fees for student transportation services"),
    EXTRACURRICULAR_ACTIVITY("Extracurricular Activity", "Fees for sports, clubs, arts, and other non-academic activities"),
    MATERIALS_AND_SUPPLIES("Materials & Supplies", "Fees for educational materials, books, supplies, and equipment"),
    TECHNOLOGY("Technology", "Fees for technology-related resources and services"),
    FACILITY_AND_SERVICES("Facility & Services", "Fees for use and maintenance of school facilities and services"),
    EVENT("Event", "Fees for specific events, trips, and special occasions"),
    FINE_AND_PENALTY("Fine & Penalty", "Charges for penalties, late payments, and damages"),
    DONATION_AND_FUNDRAISING("Donation & Fundraising", "Voluntary contributions and fundraising income"),
    OTHER("Other", "For any income source that does not fit into the predefined categories"),
    CUSTOM("Custom", "For school-defined categories not covered by standard types");

    private final String displayName;
    private final String description;

    IncomeSourceType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
