package com.example.user.coinz;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.filters.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)

//check bank in and give coins functions and limitations
//all coins(including other players' spare change) can only be given when 25 coins were banked in already
//only 25 coins can be banked in a day
//and check achievement values corresponding to giving and getting coins
public class BankInGiveCoinsTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule .grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    public void resetDatabase(){
        HashMap<String, Object> walletData = new HashMap<>();
        for(int i = 1;i<61;i++){
            HashMap<String, Object> coinData = new HashMap<>();
            if(i<14){
                coinData.put("id", "dolr");
                coinData.put("value", i);
                coinData.put("currency", "DOLR");
                coinData.put("bankedIn", false);
                coinData.put("coinGivenToOthers", false);
                coinData.put("coinGivenByOthers", false);
                coinData.put("coinGiverName", "");
            }else if(i<26){
                coinData.put("id", "peny");
                coinData.put("value", i);
                coinData.put("currency", "PENY");
                coinData.put("bankedIn", false);
                coinData.put("coinGivenToOthers", false);
                coinData.put("coinGivenByOthers", false);
                coinData.put("coinGiverName", "");

            }else if(i<39){
                coinData.put("id", "quid");
                coinData.put("value", i);
                coinData.put("currency", "QUID");
                coinData.put("bankedIn", false);
                coinData.put("coinGivenToOthers", false);
                coinData.put("coinGivenByOthers", false);
                coinData.put("coinGiverName", "");
            }else if(i<51){
                coinData.put("id", "shil");
                coinData.put("value", i);
                coinData.put("currency", "SHIL");
                coinData.put("bankedIn", false);
                coinData.put("coinGivenToOthers", false);
                coinData.put("coinGivenByOthers", false);
                coinData.put("coinGiverName", "");
            }else {
                coinData.put("id", "dolr");
                coinData.put("value", i);
                coinData.put("currency", "DOLR");
                coinData.put("bankedIn", false);
                coinData.put("coinGivenToOthers", false);
                coinData.put("coinGivenByOthers", true);
                coinData.put("coinGiverName", "test2");
            }


            String name = "coin"+i;
            walletData.put(name,coinData);
        }
        FirebaseFirestore.getInstance().collection("Users").document("TEST").set(walletData);
        HashMap<String, Object> achievementData = new HashMap<>();
        achievementData.put("Coin getter",0);
        achievementData.put("Coin giver",0);
        FirebaseFirestore.getInstance().collection("Users").document("6qfxDOYkppTy9FPcuRaTbH16U322").update(achievementData);
    }
    @Test
    public void bankInGiveCoinsTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.email_edittext_login),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText2.perform(click());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.email_edittext_login),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("test@gmail.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.password_edittext_login),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText4.perform(replaceText("aaaa1111"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.login_button_login), withText("Login"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        resetDatabase();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.bank_button), withText("Bank"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton2.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView2.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction recyclerView3 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView3.perform(actionOnItemAtPosition(2, click()));

        ViewInteraction recyclerView4 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView4.perform(actionOnItemAtPosition(3, click()));

        ViewInteraction recyclerView5 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView5.perform(actionOnItemAtPosition(4, click()));

        ViewInteraction recyclerView6 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView6.perform(actionOnItemAtPosition(5, click()));

        ViewInteraction recyclerView7 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView7.perform(actionOnItemAtPosition(6, click()));

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.bank_in_button), withText("Bank in"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton3.perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton4 = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton4.perform(scrollTo(), click());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction recyclerView8 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView8.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction recyclerView9 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView9.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction recyclerView10 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView10.perform(actionOnItemAtPosition(2, click()));

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.give_coin_button), withText("Give coin(s)"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton5.perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction textView = onView(withId(R.id.number_of_coins_text));
        textView.check(matches(withText("0 coin(s)\nbanked in")));

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(R.id.bank_in_button), withText("Bank in"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton6.perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton7 = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton7.perform(scrollTo(), click());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction recyclerView11 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView11.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction recyclerView12 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView12.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction recyclerView13 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView13.perform(actionOnItemAtPosition(2, click()));

        ViewInteraction recyclerView14 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView14.perform(actionOnItemAtPosition(3, click()));

        ViewInteraction recyclerView15 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView15.perform(actionOnItemAtPosition(4, click()));

        ViewInteraction recyclerView16 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView16.perform(actionOnItemAtPosition(5, click()));

        ViewInteraction recyclerView17 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView17.perform(actionOnItemAtPosition(6, click()));

        ViewInteraction recyclerView18 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView18.perform(actionOnItemAtPosition(7, click()));

        ViewInteraction recyclerView19 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView19.perform(actionOnItemAtPosition(8, click()));

        ViewInteraction recyclerView20 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView20.perform(actionOnItemAtPosition(9, click()));

        ViewInteraction appCompatButton8 = onView(
                allOf(withId(R.id.give_coin_button), withText("Give coin(s)"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton8.perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton9 = onView(
                allOf(withId(R.id.bank_in_button), withText("Bank in"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton9.perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton10 = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton10.perform(scrollTo(), click());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction textView2 = onView(withId(R.id.number_of_coins_text));
        textView2.check(matches(withText("10 coin(s)\nbanked in")));

        ViewInteraction recyclerView21 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView21.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction recyclerView22 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView22.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction recyclerView23 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView23.perform(actionOnItemAtPosition(3, click()));

        ViewInteraction recyclerView24 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView24.perform(actionOnItemAtPosition(2, click()));

        ViewInteraction recyclerView25 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView25.perform(actionOnItemAtPosition(4, click()));

        ViewInteraction recyclerView26 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView26.perform(actionOnItemAtPosition(5, click()));

        ViewInteraction recyclerView27 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView27.perform(actionOnItemAtPosition(6, click()));

        ViewInteraction recyclerView28 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView28.perform(actionOnItemAtPosition(7, click()));

        ViewInteraction recyclerView29 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView29.perform(actionOnItemAtPosition(8, click()));

        ViewInteraction recyclerView30 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView30.perform(actionOnItemAtPosition(9, click()));

        ViewInteraction appCompatButton11 = onView(
                allOf(withId(R.id.bank_in_button), withText("Bank in"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton11.perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton12 = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton12.perform(scrollTo(), click());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction textView3 = onView(withId(R.id.number_of_coins_text));
        textView3.check(matches(withText("20 coin(s)\nbanked in")));

        ViewInteraction recyclerView31 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView31.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction recyclerView32 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView32.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction recyclerView33 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView33.perform(actionOnItemAtPosition(2, click()));

        ViewInteraction recyclerView34 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView34.perform(actionOnItemAtPosition(3, click()));

        ViewInteraction recyclerView35 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView35.perform(actionOnItemAtPosition(4, click()));

        ViewInteraction recyclerView36 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView36.perform(actionOnItemAtPosition(5, click()));

        ViewInteraction appCompatButton13 = onView(
                allOf(withId(R.id.give_coin_button), withText("Give coin(s)"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton13.perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton14 = onView(
                allOf(withId(R.id.bank_in_button), withText("Bank in"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton14.perform(click());

        ViewInteraction recyclerView37 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView37.perform(actionOnItemAtPosition(5, click()));

        ViewInteraction appCompatButton15 = onView(
                allOf(withId(R.id.bank_in_button), withText("Bank in"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton15.perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton16 = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton16.perform(scrollTo(), click());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction textView4 = onView(withId(R.id.number_of_coins_text));
        textView4.check(matches(withText("25 coin(s)\nbanked in")));

        ViewInteraction recyclerView38 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView38.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction recyclerView39 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView39.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction recyclerView40 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView40.perform(actionOnItemAtPosition(2, click()));

        ViewInteraction recyclerView41 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView41.perform(actionOnItemAtPosition(3, click()));

        ViewInteraction recyclerView42 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView42.perform(actionOnItemAtPosition(4, click()));

        ViewInteraction recyclerView43 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView43.perform(actionOnItemAtPosition(5, click()));

        ViewInteraction recyclerView44 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView44.perform(actionOnItemAtPosition(6, click()));

        ViewInteraction recyclerView45 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView45.perform(actionOnItemAtPosition(7, click()));

        ViewInteraction recyclerView46 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView46.perform(actionOnItemAtPosition(8, click()));

        ViewInteraction recyclerView47 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView47.perform(actionOnItemAtPosition(9, click()));

        ViewInteraction appCompatButton17 = onView(
                allOf(withId(R.id.bank_in_button), withText("Bank in"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton17.perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton18 = onView(
                allOf(withId(R.id.give_coin_button), withText("Give coin(s)"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton18.perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction editText = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.custom),
                                childAtPosition(
                                        withId(R.id.customPanel),
                                        0)),
                        0),
                        isDisplayed()));
        editText.perform(click());

        ViewInteraction editText2 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.custom),
                                childAtPosition(
                                        withId(R.id.customPanel),
                                        0)),
                        0),
                        isDisplayed()));
        editText2.perform(replaceText("TEST2"), closeSoftKeyboard());

        ViewInteraction appCompatButton19 = onView(
                allOf(withId(android.R.id.button1), withText("Confirm"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton19.perform(scrollTo(), click());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction recyclerView48 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView48.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction recyclerView49 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView49.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction recyclerView50 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView50.perform(actionOnItemAtPosition(2, click()));

        ViewInteraction recyclerView51 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView51.perform(actionOnItemAtPosition(3, click()));

        ViewInteraction recyclerView52 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView52.perform(actionOnItemAtPosition(4, click()));

        ViewInteraction recyclerView53 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView53.perform(actionOnItemAtPosition(5, click()));

        ViewInteraction recyclerView54 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView54.perform(actionOnItemAtPosition(6, click()));

        ViewInteraction recyclerView55 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView55.perform(actionOnItemAtPosition(7, click()));

        ViewInteraction recyclerView56 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView56.perform(actionOnItemAtPosition(8, click()));

        ViewInteraction recyclerView57 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView57.perform(actionOnItemAtPosition(9, click()));

        ViewInteraction appCompatButton20 = onView(
                allOf(withId(R.id.give_coin_button), withText("Give coin(s)"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton20.perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction editText3 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.custom),
                                childAtPosition(
                                        withId(R.id.customPanel),
                                        0)),
                        0),
                        isDisplayed()));
        editText3.perform(click());

        ViewInteraction editText4 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.custom),
                                childAtPosition(
                                        withId(R.id.customPanel),
                                        0)),
                        0),
                        isDisplayed()));
        editText4.perform(replaceText("TEST"), closeSoftKeyboard());

        ViewInteraction appCompatButton21 = onView(
                allOf(withId(android.R.id.button1), withText("Confirm"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton21.perform(scrollTo(), click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton22 = onView(
                allOf(withId(R.id.give_coin_button), withText("Give coin(s)"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton22.perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction editText5 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.custom),
                                childAtPosition(
                                        withId(R.id.customPanel),
                                        0)),
                        0),
                        isDisplayed()));
        editText5.perform(click());

        ViewInteraction editText6 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.custom),
                                childAtPosition(
                                        withId(R.id.customPanel),
                                        0)),
                        0),
                        isDisplayed()));
        editText6.perform(replaceText("TEST2"), closeSoftKeyboard());

        ViewInteraction appCompatButton23 = onView(
                allOf(withId(android.R.id.button1), withText("Confirm"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton23.perform(scrollTo(), click());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction recyclerView58 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView58.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction recyclerView59 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView59.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction recyclerView60 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView60.perform(actionOnItemAtPosition(2, click()));

        ViewInteraction recyclerView61 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView61.perform(actionOnItemAtPosition(3, click()));

        ViewInteraction recyclerView62 = onView(
                allOf(withId(R.id.bank_recycler_view),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                0)));
        recyclerView62.perform(actionOnItemAtPosition(4, click()));

        ViewInteraction appCompatButton24 = onView(
                allOf(withId(R.id.give_coin_button), withText("Give coin(s)"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton24.perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton25 = onView(
                allOf(withId(android.R.id.button2), withText("Cancel"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                2)));
        appCompatButton25.perform(scrollTo(), click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton26 = onView(
                allOf(withId(R.id.bank_in_button), withText("Bank in"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton26.perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton27 = onView(
                allOf(withId(R.id.give_coin_button), withText("Give coin(s)"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton27.perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction editText7 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.custom),
                                childAtPosition(
                                        withId(R.id.customPanel),
                                        0)),
                        0),
                        isDisplayed()));
        editText7.perform(click());

        ViewInteraction editText8 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.custom),
                                childAtPosition(
                                        withId(R.id.customPanel),
                                        0)),
                        0),
                        isDisplayed()));
        editText8.perform(replaceText("TEST2"), closeSoftKeyboard());

        ViewInteraction appCompatButton28 = onView(
                allOf(withId(android.R.id.button1), withText("Confirm"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton28.perform(scrollTo(), click());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pressBack();

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatButton29 = onView(
                allOf(withId(R.id.my_profile_button), withText("My profile"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton29.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView5 = onView(withId(R.id.coin_giver_text));
        textView5.check(matches(withText("25/100")));

        ViewInteraction textView6 = onView(withId(R.id.coin_getter_text));
        textView6.check(matches(withText("10/100")));

        pressBack();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatButton30 = onView(
                allOf(withId(R.id.log_out_button), withText("Log out"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        appCompatButton30.perform(click());

        ViewInteraction appCompatButton31 = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton31.perform(scrollTo(), click());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView7 = onView(withId(R.id.Title));
        textView7.check(matches(withText("Welcome to Coinz!")));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
