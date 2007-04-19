package org.codehaus.groovy.grails.web.taglib;

import groovy.lang.Closure;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.xpath.DefaultXPath;

import org.springframework.web.servlet.support.RequestContextUtils as RCU;

/**
 * Tests for the FormTagLib.groovy file which contains tags to help with the
 * creation of HTML forms
 *
 * @author Graeme
 *
 */
public class FormTagLib2Tests extends AbstractGrailsTagTests {

    /** The name used for the datePicker tags created in the test cases. */
    private static final String DATE_PICKER_TAG_NAME = "testDatePicker";
    private static final def SELECT_TAG_NAME = "testSelect";

    private static final Collection DATE_PRECISIONS_INCLUDING_MINUTE = Collections.unmodifiableCollection(Arrays.asList( ["minute", null] as String[] ))
    private static final Collection DATE_PRECISIONS_INCLUDING_HOUR = Collections.unmodifiableCollection(Arrays.asList(["hour", "minute",null] as String[] ))
    private static final Collection DATE_PRECISIONS_INCLUDING_DAY = Collections.unmodifiableCollection(Arrays.asList(["day", "hour", "minute", null] as String[] ))
    private static final Collection DATE_PRECISIONS_INCLUDING_MONTH = Collections.unmodifiableCollection(Arrays.asList(["month", "day", "hour", "minute", null] as String[] ))

    

   public void testDatePickerTagWithDefaultDateAndPrecision() throws Exception {
        testDatePickerTag(null, null);
    }

    public void testDatePickerTagWithYearPrecision() throws Exception {
        testDatePickerTag(null, "year");
    }

    public void testDatePickerTagWithMonthPrecision() throws Exception {
        testDatePickerTag(null, "month");
    }

    public void testDatePickerTagWithDayPrecision() throws Exception {
        testDatePickerTag(null, "day");
    }

    public void testDatePickerTagWithHourPrecision() throws Exception {
        testDatePickerTag(null, "hour");
    }

    public void testDatePickerTagWithMinutePrecision() throws Exception {
        testDatePickerTag(null, "minute");
    }

    public void testDatePickerTagWithCustomDate() throws Exception {
        testDatePickerTag(new Date(0), null);
    }

    public void testDatePickerTagWithCustomDateAndPrecision() throws Exception {
        testDatePickerTag(new Date(0), "day");
    }

    private void testDatePickerTag(Date date, String precision) throws Exception {
        Document document = getDatePickerOutput(date, precision);
        assertNotNull(document);

        // validate presence and structure of hidden date picker form field
        XPath xpath = new DefaultXPath("//input[@name='" + DATE_PICKER_TAG_NAME + "' and @type='hidden' and @value='struct']");
        assertTrue(xpath.booleanValueOf(document));

        // if no date was given, default to the current date
        Calendar calendar = new GregorianCalendar();
        if (date != null) {
                calendar.setTime(date);
        }

        // validate id attributes
        String xp
        if (['day', 'hour', 'minute'].contains(precision)) {
            xp = "//select[@name='" + DATE_PICKER_TAG_NAME + "_day' and @id='"+DATE_PICKER_TAG_NAME+"_day']";
            xpath = new DefaultXPath(xp);
            assertTrue(xpath.booleanValueOf(document));
        }

        if (['month', 'day', 'hour', 'minute'].contains(precision)) {
            xpath = new DefaultXPath("//select[@name='" + DATE_PICKER_TAG_NAME + "_month' and @id='"+DATE_PICKER_TAG_NAME+"_month']");
            assertTrue(xpath.booleanValueOf(document));
        }

        if (['minute', 'hour', 'day', 'month', 'year'].contains(precision)) {
            xpath = new DefaultXPath("//select[@name='" + DATE_PICKER_TAG_NAME + "_year' and @id='"+DATE_PICKER_TAG_NAME+"_year']");
            assertTrue(xpath.booleanValueOf(document));
        }

        if (['hour', 'minute'].contains(precision)) {
            xpath = new DefaultXPath("//select[@name='" + DATE_PICKER_TAG_NAME + "_hour' and @id='"+DATE_PICKER_TAG_NAME+"_hour']");
            assertTrue(xpath.booleanValueOf(document));
        }

        if ('minute' == precision) {
            xpath = new DefaultXPath("//select[@name='" + DATE_PICKER_TAG_NAME + "_minute' and @id='"+DATE_PICKER_TAG_NAME+"_minute']");
            assertTrue(xpath.booleanValueOf(document));
        }

        // validate presence and value of selected date fields
        validateSelectedYearValue(document, calendar);
        validateSelectedMonthValue(document, calendar, precision);
        validateSelectedDayValue(document, calendar, precision);
        validateSelectedHourValue(document, calendar, precision);
        validateSelectedMinuteValue(document, calendar, precision);
    }

    private void validateSelectedYearValue(Document document, Calendar calendar) {
        assertSelectFieldPresentWithSelectedValue(document, DATE_PICKER_TAG_NAME + "_year", Integer.toString(calendar.get(Calendar.YEAR)));
    }

    private void validateSelectedMonthValue(Document document, Calendar calendar, String precision) {
        final String FIELD_NAME = DATE_PICKER_TAG_NAME + "_month";

        String expectedMonthValue = Integer.toString(1); // January
        
        if (DATE_PRECISIONS_INCLUDING_MONTH.contains(precision)) {
            expectedMonthValue = Integer.toString(calendar.get(Calendar.MONTH)+1);
            assertSelectFieldPresentWithSelectedValue(document, FIELD_NAME, expectedMonthValue);
        }
        else {
        	
            assertSelectFieldNotPresent(document, FIELD_NAME);
        }
    }

    private void validateSelectedDayValue(Document document, Calendar calendar, String precision) {
        final String FIELD_NAME = DATE_PICKER_TAG_NAME + "_day";

        String expectedDayValue = Integer.toString(1); // 1st day of the month
        if (DATE_PRECISIONS_INCLUDING_DAY.contains(precision)) {
            expectedDayValue = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
            assertSelectFieldPresentWithSelectedValue(document, FIELD_NAME, expectedDayValue);
        }
        else {
            assertSelectFieldNotPresent(document, FIELD_NAME);
        }
    }

    private void validateSelectedHourValue(Document document, Calendar calendar, String precision) {
        final String FIELD_NAME = DATE_PICKER_TAG_NAME + "_hour";

        String expectedHourValue = "00";
        if (DATE_PRECISIONS_INCLUDING_HOUR.contains(precision)) {
            int rawHourValue = calendar.get(Calendar.HOUR_OF_DAY);
            expectedHourValue = (rawHourValue < 10) ? ("0" + rawHourValue) : Integer.toString(rawHourValue);
            assertSelectFieldPresentWithSelectedValue(document, FIELD_NAME, expectedHourValue);
        }
        else {
            assertSelectFieldNotPresent(document, FIELD_NAME);
        }
    }

    private void validateSelectedMinuteValue(Document document, Calendar calendar, String precision) {
        final String FIELD_NAME = DATE_PICKER_TAG_NAME + "_minute";

        String expectedMinuteValue = "00";
        if (DATE_PRECISIONS_INCLUDING_MINUTE.contains(precision)) {
            int rawMinuteValue = calendar.get(Calendar.MINUTE);
            expectedMinuteValue = (rawMinuteValue < 10) ? ("0" + rawMinuteValue) : Integer.toString(rawMinuteValue);
            assertSelectFieldPresentWithSelectedValue(document, FIELD_NAME, expectedMinuteValue);
        }
        else {
            assertSelectFieldNotPresent(document, FIELD_NAME);
        }
    }

    private void assertSelectFieldPresentWithSelectedValue(Document document, String fieldName, String value) {
        XPath xpath = new DefaultXPath("//select[@name='" + fieldName + "']/option[@selected='selected' and @value='" + value + "']");
        assertTrue(xpath.booleanValueOf(document));
    }

    private void assertSelectFieldPresentWithValue(Document document, String fieldName, String value) {
        XPath xpath = new DefaultXPath("//select[@name='" + fieldName + "']/option[@value='" + value + "']");
        assertTrue(xpath.booleanValueOf(document));
    }

    private void assertSelectFieldPresentWithValueAndText(Document document, String fieldName, String value, String label) {
        XPath xpath = new DefaultXPath("//select[@name='" + fieldName + "']/option[@value='" + value + "' and text()='"+label+"']");
        assertTrue(xpath.booleanValueOf(document));
    }

    private void assertSelectFieldNotPresent(Document document, String fieldName) {
        XPath xpath = new DefaultXPath("//select[@name='" + fieldName + "']");
        assertFalse(xpath.booleanValueOf(document));
    }

    private Document getDatePickerOutput(Date date, String precision) throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        def document = withTag("datePicker", pw) { tag ->

	        assertNotNull(tag);
	
	        Map attrs = new HashMap();
	        attrs.put("name", DATE_PICKER_TAG_NAME);
	
	        if (date != null) {
	            attrs.put("value", date);
	        }
	
	        if (precision != null) {
	            attrs.put("precision", precision);
	        }
	
	        tag.call(attrs);
	
	        String enclosed = "<test>" + sw.toString() + "</test>";

	        return DocumentHelper.parseText(enclosed);
        }
        return document
    }
}

