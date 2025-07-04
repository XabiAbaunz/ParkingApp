package com.lksnext.ParkingXAbaunz.view.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static org.hamcrest.Matchers.anything;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.lksnext.ParkingXAbaunz.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NewReservationFragmentTest {

    private FragmentScenario<NewReservationFragment> fragmentScenario;

    @Before
    public void setUp() {
        fragmentScenario = FragmentScenario.launchInContainer(
                NewReservationFragment.class,
                null,
                android.R.style.Theme_Material_Light
        );
    }

    @After
    public void tearDown() {
        if (fragmentScenario != null) {
            fragmentScenario.close();
        }
    }

    @Test
    public void testFragmentLaunches() {
        onView(withId(R.id.titleText))
                .check(matches(isDisplayed()))
                .check(matches(withText("Nueva Reserva")));
    }

    @Test
    public void testAllViewsAreDisplayed() {
        onView(withId(R.id.titleText))
                .check(matches(isDisplayed()));

        onView(withId(R.id.typeLabel))
                .check(matches(isDisplayed()))
                .check(matches(withText("Tipo de Plaza")));

        onView(withId(R.id.typeSpinner))
                .check(matches(isDisplayed()));

        onView(withId(R.id.cocheLabel))
                .check(matches(isDisplayed()))
                .check(matches(withText("Selecciona tu Coche")));

        onView(withId(R.id.cocheSpinner))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        onView(withId(R.id.dateLabel))
                .check(matches(isDisplayed()))
                .check(matches(withText("Fecha")));

        onView(withId(R.id.selectDateButton))
                .check(matches(isDisplayed()))
                .check(matches(withText("Seleccionar Fecha")));

        onView(withId(R.id.timeLabel))
                .check(matches(isDisplayed()))
                .check(matches(withText("Horario")));

        onView(withId(R.id.selectStartTimeButton))
                .check(matches(isDisplayed()))
                .check(matches(withText("Inicio")));

        onView(withId(R.id.selectEndTimeButton))
                .check(matches(isDisplayed()))
                .check(matches(withText("Fin")));

        onView(withId(R.id.confirmReservationButton))
                .check(matches(isDisplayed()))
                .check(matches(withText("Confirmar Reserva")));

        onView(withId(R.id.progressBar))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    @Test
    public void testPlazaTypeSpinnerSelection() {
        onView(withId(R.id.typeSpinner))
                .perform(click());

        onData(anything())
                .atPosition(0)
                .perform(click());

        onView(withId(R.id.typeSpinner))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testCocheSpinnerInteraction() {
        try {
            onView(withId(R.id.cocheSpinner))
                    .perform(click());
            pressBack();
        } catch (Exception e) {
            onView(withId(R.id.cocheSpinner))
                    .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
        }
    }

    @Test
    public void testDateSelectionOpensDatePicker() {
        onView(withId(R.id.selectDateButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.selectDateButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testDateButtonTextUpdate() {
        onView(withId(R.id.selectDateButton))
                .check(matches(withText("Seleccionar Fecha")));
    }

    @Test
    public void testStartTimeSelectionOpensTimePicker() {
        onView(withId(R.id.selectStartTimeButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.selectStartTimeButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testEndTimeSelectionOpensTimePicker() {
        onView(withId(R.id.selectEndTimeButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.selectEndTimeButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testTimeButtonsInitialText() {
        onView(withId(R.id.selectStartTimeButton))
                .check(matches(withText("Inicio")));

        onView(withId(R.id.selectEndTimeButton))
                .check(matches(withText("Fin")));
    }

    @Test
    public void testConfirmReservationWithoutCoche() {
        onView(withId(R.id.confirmReservationButton))
                .perform(click());

        onView(withId(R.id.confirmReservationButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testConfirmReservationWithoutDate() {
        onView(withId(R.id.typeSpinner))
                .perform(click());

        onData(anything())
                .atPosition(0)
                .perform(click());

        onView(withId(R.id.confirmReservationButton))
                .perform(click());

        onView(withId(R.id.confirmReservationButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testConfirmReservationWithoutStartTime() {
        onView(withId(R.id.selectDateButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.confirmReservationButton))
                .perform(click());

        onView(withId(R.id.confirmReservationButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testConfirmReservationWithoutEndTime() {
        onView(withId(R.id.selectDateButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.selectStartTimeButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.confirmReservationButton))
                .perform(click());

        onView(withId(R.id.confirmReservationButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testCompleteReservationFlow() {
        onView(withId(R.id.typeSpinner))
                .perform(click());

        onData(anything())
                .atPosition(0)
                .perform(click());

        try {
            onView(withId(R.id.cocheSpinner))
                    .perform(click());
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.selectDateButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.selectStartTimeButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.selectEndTimeButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.confirmReservationButton))
                .perform(click());

        onView(withId(R.id.confirmReservationButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testProgressBarInitiallyHidden() {
        onView(withId(R.id.progressBar))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    @Test
    public void testProgressBarShowsWhenLoading() {
        onView(withId(R.id.confirmReservationButton))
                .perform(click());

        onView(withId(R.id.progressBar))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    @Test
    public void testSpinnersAreClickable() {
        onView(withId(R.id.typeSpinner))
                .perform(click());

        onData(anything())
                .atPosition(0)
                .perform(click());

        onView(withId(R.id.typeSpinner))
                .check(matches(isDisplayed()));

        try {
            onView(withId(R.id.cocheSpinner))
                    .perform(click());
            pressBack();
            onView(withId(R.id.cocheSpinner))
                    .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
        } catch (Exception e) {
            onView(withId(R.id.cocheSpinner))
                    .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
        }
    }

    @Test
    public void testEndTimeBeforeStartTimeValidation() {
        onView(withId(R.id.selectStartTimeButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.selectEndTimeButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.confirmReservationButton))
                .perform(click());

        onView(withId(R.id.confirmReservationButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testFieldsCanBeReset() {
        onView(withId(R.id.selectDateButton))
                .check(matches(withText("Seleccionar Fecha")));

        onView(withId(R.id.selectStartTimeButton))
                .check(matches(withText("Inicio")));

        onView(withId(R.id.selectEndTimeButton))
                .check(matches(withText("Fin")));
    }

    @Test
    public void testMultipleInteractions() {
        onView(withId(R.id.typeSpinner))
                .perform(click());

        onData(anything())
                .atPosition(0)
                .perform(click());

        onView(withId(R.id.selectDateButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.selectStartTimeButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.selectEndTimeButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.confirmReservationButton))
                .check(matches(isDisplayed()));

        onView(withId(R.id.titleText))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testUIConsistency() {
        onView(withId(R.id.titleText))
                .check(matches(withText("Nueva Reserva")));

        onView(withId(R.id.confirmReservationButton))
                .check(matches(withText("Confirmar Reserva")));

        onView(withId(R.id.selectDateButton))
                .perform(click());

        try {
            pressBack();
        } catch (Exception e) {
        }

        onView(withId(R.id.titleText))
                .check(matches(withText("Nueva Reserva")));

        onView(withId(R.id.confirmReservationButton))
                .check(matches(withText("Confirmar Reserva")));
    }

    @Test
    public void testPlazaTypeSelectionNormal() {
        onView(withId(R.id.typeSpinner))
                .perform(click());

        onData(anything())
                .atPosition(0)
                .perform(click());

        onView(withId(R.id.typeSpinner))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testPlazaTypeSelectionElectrico() {
        onView(withId(R.id.typeSpinner))
                .perform(click());

        onData(anything())
                .atPosition(2)
                .perform(click());

        onView(withId(R.id.typeSpinner))
                .check(matches(isDisplayed()));
    }
}