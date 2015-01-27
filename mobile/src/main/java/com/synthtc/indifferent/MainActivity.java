package com.synthtc.indifferent;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.synthtc.indifferent.api.Deal;
import com.synthtc.indifferent.api.Meh;
import com.synthtc.indifferent.ui.ImageFragment;
import com.synthtc.indifferent.ui.NavigationDrawerFragment;
import com.synthtc.indifferent.ui.SettingsFragment;
import com.synthtc.indifferent.util.Alarm;
import com.synthtc.indifferent.util.Helper;
import com.synthtc.indifferent.util.MehCache;
import com.synthtc.indifferent.util.VolleySingleton;
import com.viewpagerindicator.CirclePageIndicator;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.json.JSONObject;

import java.util.ArrayList;

import in.uncod.android.bypass.Bypass;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final String LOGTAG = "Indifferent";
    private static Instant mToday;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private int mColor = R.color.primary_material_dark;
    private int mColorStatus = R.color.primary_dark_material_dark;
    private int mColorNav = R.color.primary_dark_material_dark;
    private boolean mIsPaused = false;
    private Fragment mFragmentToLoad = null;
    private boolean mInitialized = false;

    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPaused = false;
        loadFragment(mFragmentToLoad);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(Meh meh) {
        // update the main content by replacing fragments
        if (!mInitialized) {
            initialize();
        }

        if (meh == null) {
            loadFragment(new SettingsFragment());
            mTitle = getString(R.string.action_settings);
        } else {
            Instant instant = MehCache.getInstance(this).getInstant(meh);
            loadFragment(DealFragment.newInstance(instant, meh));
        }
        Helper.log(Log.DEBUG, "onNavigationDrawerItemSelected");
    }

    private void initialize() {
        if (mInitialized) {
            return;
        }

        mInitialized = true;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(SettingsFragment.KEY_ALARM_ENABLE, SettingsFragment.DEFAULT_ALARM_ENABLE)) {
            Alarm.set(this, false);
        }

        final MehCache mehCache = MehCache.getInstance(this);

        // Write some existing data to the storage
        ArrayList<String> samples = new ArrayList<String>();
        samples.add("{\"deal\":{\"features\":\"- Bluetooth 3.0 with 30-foot range\\r\\n- Built-in mic and call-answering functionality\\r\\n- Rechargeable lithium battery gets 8 hours to a charge\\r\\n- Smallish ear pads might be uncomfortable on your ears if you wear them all day, but fashion and health tip: you shouldn't wear headphones all day\\r\\n- Look like you spent stupid money on overpriced headphones without actually spending stupid money\",\"id\":\"a6ki000000000zoAAA\",\"items\":[{\"attributes\":[{\"key\":\"Color\",\"value\":\"White\"}],\"condition\":\"New\",\"id\":\"102086\",\"price\":24,\"photo\":\"https://res.cloudinary.com/mediocre/image/upload/v1420573190/ggrctd9olous1avgqjbr.png\"},{\"attributes\":[{\"key\":\"Color\",\"value\":\"Red\"}],\"condition\":\"New\",\"id\":\"102087\",\"price\":14,\"photo\":\"https://res.cloudinary.com/mediocre/image/upload/v1420573142/avfz0mds1lvwlpk2zesg.png\"},{\"attributes\":[{\"key\":\"Color\",\"value\":\"Black\"}],\"condition\":\"New\",\"id\":\"102088\",\"price\":14,\"photo\":\"https://res.cloudinary.com/mediocre/image/upload/v1420573220/rb4ymj9egoyrgqmlsgwm.png\"}],\"photos\":[\"https://res.cloudinary.com/mediocre/image/upload/v1420573142/avfz0mds1lvwlpk2zesg.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1420573163/ls03ro79tzkyyoamuqgk.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1420573190/ggrctd9olous1avgqjbr.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1420573220/rb4ymj9egoyrgqmlsgwm.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1420655408/im31xz3cmqhxiewa83mj.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1420655424/eejvqthksdmkifolzdfb.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1420573302/hzpbsoozk8ztihos6ysn.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1420674852/rb4rbl3q3hnyokq3dulm.png\"],\"title\":\"TOCCs Manhattan Bluetooth Headphones\",\"specifications\":\"Specs \\r\\n====\\r\\n- Model: TOCCS Manhattan\\r\\n- 30 ft Bluetooth 3.0 + [EDR transmission](http://en.wikipedia.org/wiki/Bluetooth#Bluetooth_v2.1_.2B_EDR)\\r\\n- 40mm stereo speaker\\r\\n- [CVC Noise cancellation](http://www.csr.com/products/22/cvc-5.0)\\r\\n- [A2DP audio distribution](http://en.wikipedia.org/wiki/List_of_Bluetooth_profiles#Advanced_Audio_Distribution_Profile_.28A2DP.29)\\r\\n- Rechargeable lithium battery with 8 hours life\\r\\n- Soft polyurethane leather with adjustable rubber headband\\r\\n- Includes 3.5mm audio cable for corded use\\r\\n\\r\\n**Condition** - New\\r\\n**Warranty** - 90 Day Replacement TOCCs\\r\\n**Ships Via** - FedEx SmartPost \\r\\n- $5 Shipping, Free with **[VMP](https://mediocre.com/vmp)** \\r\\n\\r\\nWhat's in the Box? \\r\\n====\\r\\n1x Bluetooth headphones\\r\\n1x 3.5mm audio cable\\r\\n1x Micro USB charging cable\\r\\n1x Quick start guide\\r\\n\\r\\nPictures\\r\\n====\\r\\n[What's in the box](https://res.cloudinary.com/mediocre/image/upload/v1420573163/ls03ro79tzkyyoamuqgk.png)\\r\\n[Red](https://res.cloudinary.com/mediocre/image/upload/v1420573142/avfz0mds1lvwlpk2zesg.png)\\r\\n[White](https://res.cloudinary.com/mediocre/image/upload/v1420573190/ggrctd9olous1avgqjbr.png)\\r\\n[Black](https://res.cloudinary.com/mediocre/image/upload/v1420573220/rb4ymj9egoyrgqmlsgwm.png)\\r\\n[Retail box](https://res.cloudinary.com/mediocre/image/upload/v1420573302/hzpbsoozk8ztihos6ysn.png)\\r\\n[Folds up](https://res.cloudinary.com/mediocre/image/upload/v1420655408/im31xz3cmqhxiewa83mj.png)\\r\\n[Detail of ear pad](https://res.cloudinary.com/mediocre/image/upload/v1420655424/eejvqthksdmkifolzdfb.png)\\r\\n\\r\\nPrice Check\\r\\n====\\r\\n[$119 List, $109 at TOCCs](http://www.toccs.com/headphones)\",\"story\":{\"title\":\"Beats spending 200 bucks.\",\"body\":\"Do you need \\\"studio headphones\\\"? Find out by taking this simple quiz:\\r\\n\\r\\n1. Do you think \\\"studio headphones\\\" are something you can find at Best Buy?\\r\\n\\r\\n2. Will you be using these headphones with Bluetooth audio?\\r\\n\\r\\n3. Have you ever considered buying Beats headphones, even for a second?\\r\\n\\r\\nIf you answered \\\"yes\\\" to any of the above questions, congratulations! You are not a professional music producer, recording engineer, or DJ. You don't need \\\"studio headphones\\\". And you especially don't need to pay extra for a pair of regular old headphones with a phony \\\"studio headphones\\\" label.\\r\\n\\r\\nReal talk: \\\"studio headphones\\\" now just means \\\"overpriced headphones with a plastic band that's all black and red and curvy and shit\\\". They have no more place in a professional recording studio than they do in a blacksmith's forge. \\r\\n\\r\\nIf you insist on \\\"studio headphones\\\", you might as well buy them cheap. This pair checks off all the boxes, from the aforementioned band to the ear pads made of \\\"PU leather\\\" (PU stands for \\\"polyurethane\\\" (so, not real leather) (also, note to polyurethane industry: find a better acronym)). The ear pads are a little on the little side, maybe. But those are all just matters of style. Down in them guts, these are just regular old half-decent Bluetooth headphones. \\r\\n\\r\\nSo today you can buy them for regular old headphone prices. Granted, that won't give you quite the same rush as paying way, way too much for them. If you want to play pretend record mogul, there are plenty of other places where you can go throw your money away.\\r\\n\\r\\nOn the other hand, if you answered \\\"no\\\" to all of the above questions and you *are* a professional music producer, let us know where we should send you a copy of our demo. It's like MIA meets *Chocolate Starfish*-era Limp Bizkit on a Jane's Addiction tip, with a little bit of James Brown thrown in there, only funkier. Please, serious industry requests only. You really need studio headphones to appreciate our sound.\"},\"theme\":{\"accentColor\":\"#be2b33\",\"backgroundColor\":\"#a0d0d7\",\"backgroundImage\":\"https://res.cloudinary.com/mediocre/image/upload/v1420573451/usnubcnzirjlyfofwunl.jpg\",\"foreground\":\"dark\"},\"url\":\"https://meh.com/deals/toccs-manhattan-bluetooth-headphones\",\"soldOutAt\":\"2015-01-08T07:02:57.055Z\",\"topic\":{\"commentCount\":78,\"createdAt\":\"2015-01-08T05:02:36.639Z\",\"id\":\"54ae0f6c6b977f4801a51348\",\"replyCount\":204,\"url\":\"https://meh.com/forum/topics/toccs-manhattan-bluetooth-headphones\",\"voteCount\":2}},\"poll\":{\"answers\":[{\"id\":\"a6li0000000PBj0AAG-1\",\"text\":\"under $20\",\"voteCount\":292},{\"id\":\"a6li0000000PBj0AAG-2\",\"text\":\"$20-$30\",\"voteCount\":210},{\"id\":\"a6li0000000PBj0AAG-3\",\"text\":\"$30-$50\",\"voteCount\":304},{\"id\":\"a6li0000000PBj0AAG-4\",\"text\":\"$50-$80\",\"voteCount\":278},{\"id\":\"a6li0000000PBj0AAG-5\",\"text\":\"$80-$120\",\"voteCount\":266},{\"id\":\"a6li0000000PBj0AAG-6\",\"text\":\"$120-$200\",\"voteCount\":151},{\"id\":\"a6li0000000PBj0AAG-7\",\"text\":\"more than $200\",\"voteCount\":106}],\"id\":\"a6li0000000PBj0AAG\",\"startDate\":\"2015-01-08T05:00:00.000Z\",\"title\":\"What is your gut feeling of the \\\"right\\\" price for a pair of good headphones?\",\"topic\":{\"commentCount\":17,\"createdAt\":\"2015-01-08T05:00:00.049Z\",\"id\":\"54ae0ed06b977f4801a51318\",\"replyCount\":15,\"url\":\"https://meh.com/forum/topics/what-is-your-gut-feeling-of-the-right-price-for-a-pair-of-good-headphones\",\"voteCount\":0}}}");
        samples.add("{\"deal\":{\"features\":\"- Free phone calls in the U.S.\\r\\n- No mobile dead zones to worry about\\r\\n- Like a landline phone without a landline\\r\\n- Included Bluetooth adapter can pair with up to 7 phones or handsets\\r\\n- Additional Ooma Premier service ($10/month) lets you do other stuff, like block anonymous calls, forward calls to your mobile, call Canada for free, and connect to your home's Nest system\",\"id\":\"a6ki000000000zjAAA\",\"items\":[{\"attributes\":[],\"condition\":\"Refurbished\",\"id\":\"102052\",\"price\":80,\"photo\":\"https://res.cloudinary.com/mediocre/image/upload/v1420755622/cyfyqdzmezixyglqw2o2.png\"}],\"photos\":[\"https://res.cloudinary.com/mediocre/image/upload/v1420755622/cyfyqdzmezixyglqw2o2.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1420759974/azmrvns1po2tzeohdpgw.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1420754992/rebxo1qjxohoeastmk8x.png\"],\"title\":\"Ooma Telo Bundle (Refurbished)\",\"specifications\":\"Specs \\r\\n====\\r\\n- Model: Ooma Telo BT\\r\\n- Plugs into your internet router\\r\\n- Includes bluetooth adapter which allows you to use your mobile phone with your Telo\\r\\n- Free US calling (taxes and fees may apply) and low-cost international calling\\r\\n- [Check here](http://www.ooma.com/products/taxes-fees) to see what your taxes may be \\r\\n- Features voicemail, caller-ID and call-waiting\\r\\n- Keep your current phone number for a one-time charge, or get a new number in any area code for free\\r\\n- Requires high-speed fixed-line internet connection, minimum 180 Kbps upstream, and a telephone\\r\\n\\r\\n**Condition** - Refurbished\\r\\n**Warranty** - [90 Days Mediocre](meh.com/warranty)\\r\\n**Estimated Delivery:** 1/19 - 1/21\\r\\n**Ships Via** - FedEx SmartPost \\r\\n- $5 Shipping, Free with **[VMP](https://mediocre.com/vmp)** \\r\\n\\r\\nWhat's in the Box? \\r\\n====\\r\\n1x Ooma Telo\\r\\n1x Bluetooth adapter\\r\\n1x Ethernet cable\\r\\n1x AC adapter\\r\\n1x Quick Start Guide.\\r\\n\\r\\nPictures\\r\\n====\\r\\n[Ooma Telo](https://res.cloudinary.com/mediocre/image/upload/v1420755622/cyfyqdzmezixyglqw2o2.png)\\r\\n[What's included](https://res.cloudinary.com/mediocre/image/upload/v1420759974/azmrvns1po2tzeohdpgw.png)\\r\\n[Oh no it's that damn sock again](https://res.cloudinary.com/mediocre/image/upload/v1420754992/rebxo1qjxohoeastmk8x.png)\\r\\n\\r\\nPrice Check\\r\\n====\\r\\n[$129.99 List, $109.99 at Amazon for the Telos (New)](http://www.amazon.com/Ooma-Telo-Phone-Service-Device/dp/B00I4XMEYA/ref=lh_ni_t?ie=UTF8&psc=1&smid=ATVPDKIKX0DER)\\r\\n[$29.99 List, $29.95 at Amazon for the Bluetooth adapter](http://www.amazon.com/Ooma-710-0114-100-Telo-Bluetooth-Adaptor/dp/B0045HE5DM/ref=sr_1_1?s=electronics&ie=UTF8&qid=1420759347&sr=1-1&keywords=ooma+bluetooth)\",\"story\":{\"title\":\"Can do what landlines couldn't: change with the times.\",\"body\":\"The Ooma Telo VoIP phone system was created to let you get rid of your landline without giving up all of the advantages of a landline. Service is certainly cheaper than a mobile phone - calls in the US are basically free. That mobile black hole in your house doesn't bother it. Even if your mobile phone works fine, it never hurts to have a redundant way to reach the outside world, just in case.\\r\\n\\r\\nAnd the Ooma Telo still does all that, if you need it. But the more people learned to live without landlines, the better mobile coverage got, the less reason there was for it to exist. Would the Telo phone follow the same decline as the telephone? Was Ooma doomed? \\r\\n\\r\\nNot necessarily. Because unlike traditional phones, which did only one thing, the Telo is adaptable. And Ooma's been adapting its ass off to the keep the Telo relevant.\\r\\n\\r\\nFirst there's the [Ooma Premier](http://ooma.com/products/premier) service. A $10 monthly subscription (not included) gives you things like three-way calling, a second line, call forwarding to your mobile phone, free calls to Canada, blocking anonymous and blacklisted calls, and a long menu of other little stuff. \\r\\n\\r\\nBut [as Ooma just announced at CES this week](http://ooma.com/blog/can-home-call-emergency/), if you have Nest in your house, Ooma Premier can talk to it in all kinds of potentially helpful ways. [Ooma Nest](http://ooma.com/nest/) can see when you leave your house and forward your calls while you're gone. If your kid doesn't get home from school on time, it can call you. When your Nest alarm goes off and you're far away from home, you can use Ooma to call your home 911 service from your mobile phone. [Here's a video about it](https://www.youtube.com/watch?v=4rsiRuqVNso), if that helps.\\r\\n\\r\\nIs any of this \\\"game-changing\\\"? No.\\r\\n\\r\\nBut it does mean that Ooma's still coming up with new ways to take advantage of the Telo's position at the junction of phone and Internet. This isn't some unloved orphan product that the manufacturer has dumped down the memory hole. Maybe this is as far as Ooma can take the Telo, or maybe they're working on something that'll blow our freaking minds, maaaan. Maybe it's just not something you have any use for. But unlike some of the crap we sell, it's a safe bet the Telo isn't going to be getting *less* useful.\"},\"theme\":{\"accentColor\":\"#33537f\",\"backgroundColor\":\"#d6dce4\",\"backgroundImage\":null,\"foreground\":\"dark\"},\"url\":\"https://meh.com/deals/ooma-telo-bundle--refurbished-\",\"topic\":{\"commentCount\":43,\"createdAt\":\"2015-01-09T05:00:17.349Z\",\"id\":\"54af6061ba5bee5c050b0baa\",\"replyCount\":42,\"url\":\"https://meh.com/forum/topics/ooma-telo-bundle-refurbished\",\"voteCount\":1}},\"poll\":{\"answers\":[{\"id\":\"a6li0000000PBjPAAW-1\",\"text\":\"Yes, I have Nest Protect and/or Nest Thermostat\",\"voteCount\":65},{\"id\":\"a6li0000000PBjPAAW-2\",\"text\":\"Yes, but I'm waiting for them to work out the bugs\",\"voteCount\":147},{\"id\":\"a6li0000000PBjPAAW-3\",\"text\":\"No, not now, but maybe someday if they get really awesome\",\"voteCount\":212},{\"id\":\"a6li0000000PBjPAAW-4\",\"text\":\"No, I don't see a need to replace regular old thermostats and smoke alarms\",\"voteCount\":79}],\"id\":\"a6li0000000PBjPAAW\",\"startDate\":\"2015-01-09T05:00:00.000Z\",\"title\":\"Do you have any interest in high-tech home-automation systems like Nest?\",\"topic\":{\"commentCount\":11,\"createdAt\":\"2015-01-09T05:00:00.271Z\",\"id\":\"54af6050ba5bee5c050b0ba8\",\"replyCount\":3,\"url\":\"https://meh.com/forum/topics/do-you-have-any-interest-in-high-tech-home-automation-systems-like-nest\",\"voteCount\":0}},\"video\":{\"id\":\"a6li0000000PBjPAAW\",\"startDate\":\"2015-01-09T05:00:00.000Z\",\"title\":\"It's Singin' Cowboy Time: If I Was Batman\",\"url\":\"https://www.youtube.com/watch?v=oe7RRJc-yLs\",\"topic\":{\"commentCount\":11,\"createdAt\":\"2015-01-09T04:59:59.911Z\",\"id\":\"54af604fba5bee5c050b0ba6\",\"replyCount\":5,\"url\":\"https://meh.com/forum/topics/its-singin-cowboy-time-if-i-was-batman\",\"voteCount\":7}}}");
        samples.add("{\"deal\":{\"features\":\"- Model # JBLONBEATPRCBLKAM \\r\\n- Each purchase includes two stupid speaker docks\\r\\n- No Bluetooth, it must enslave your phone or tablet to charge and play it\\r\\n- Rechargeable battery enables use as portable stereo system - OK, that's its one saving grace, kinda\\r\\n- Turns out it's compatible with the latest iPhones/iPads (and USB connected players), too, by accident\",\"id\":\"a6ki0000000TOOpAAO\",\"items\":[{\"attributes\":[],\"condition\":\"New\",\"id\":\"102109\",\"price\":44,\"photo\":\"https://res.cloudinary.com/mediocre/image/upload/v1421088512/uakbpaeyhyd3depds6wv.png\"}],\"photos\":[\"https://res.cloudinary.com/mediocre/image/upload/v1421088512/uakbpaeyhyd3depds6wv.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1421086899/rmvuioeqiq56a3e3gqov.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1415286369/ka8sfabrl5shnm8kcp4s.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1421086918/n1t2dl16s8ugnfodcyt7.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1421100765/h5f6niuisqjpko7pqy50.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1421112257/v2rqyluvhitifr5dhguc.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1421114105/gwjvt8tmrrwqk5trrktl.png\",\"https://res.cloudinary.com/mediocre/image/upload/v1421096961/kb7obf3sa8xogdfd79pe.png\"],\"title\":\"2-for-Tuesday: JBL OnBeat Mini with Lightning Connector\",\"specifications\":\"- Model # JBLONBEATPRCBLKAM \\r\\n- Speaker dock with Lightning connector \\r\\n- Dock fits iPad Mini, iPad Air, iPad 4th Gen, iPhone 5/5C/5S, iPod Touch 5th Gen, iPod Nano 7th Gen \\r\\n- Alternate USB or 3.5mm aux connections work with most mobile devices, PCs, and laptops \\r\\n- AC adapter charges docked devices \\r\\n- Portable use enabled by built-in rechargeable lithium-ion battery with up to eight-hour battery life \\r\\n- Transducers: 2 JBL full-range transducers â€¢ Power: 2 x 7 watts \\r\\n- Frequency response: 70Hz â€“ 20kHz \\r\\n- Signal-to-noise ratio: 73dB \\r\\n- Input impedance (aux): 3k ohms \\r\\n- Battery size/type: Built-in, rechargeable battery \\r\\n- Power requirement: 5.9V DC, 3.33A \\r\\n- Power consumption: 20W (maximum); 0.5W (standby) \\r\\n- Dimensions (H x W x D): 3.5 x 12 x 6.25\\\" \\r\\n\\r\\n[Manufacturer's Specs](http://www.jbl.com/images/media/ONBEATMINI_SS_EN.pdf) \\r\\n\\r\\n**Condition** - New \\r\\n**Warranty** - 1 Year JBL \\r\\n**Ships Via** - FedEx SmartPost \\r\\n- $5 Shipping, Free with **[VMP](http://mediocre.com/vmp)** \\r\\n\\r\\nWhat's in the Box? \\r\\n==== \\r\\n- 1 JBL OnBeat Mini \\r\\n- 1 AC adapter \\r\\n- Product guide \\r\\n\\r\\nPictures\\r\\n====\\r\\n[Retail boxes](https://res.cloudinary.com/mediocre/image/upload/v1421088512/uakbpaeyhyd3depds6wv.png)\\r\\n[Two units](https://res.cloudinary.com/mediocre/image/upload/v1421086899/rmvuioeqiq56a3e3gqov.png)\\r\\n[What's included](https://res.cloudinary.com/mediocre/image/upload/v1415286369/ka8sfabrl5shnm8kcp4s.png)\\r\\n[iPads!](https://res.cloudinary.com/mediocre/image/upload/v1421086918/n1t2dl16s8ugnfodcyt7.png)\\r\\n[Fits big and little](https://res.cloudinary.com/mediocre/image/upload/v1421100765/h5f6niuisqjpko7pqy50.png)\\r\\n[Or Android with an Aux cable (not included)](https://res.cloudinary.com/mediocre/image/upload/v1421112257/v2rqyluvhitifr5dhguc.png)\\r\\n[Portable!](https://res.cloudinary.com/mediocre/image/upload/v1421112241/sizhekqrnljmzdkvl2qt.png)\\r\\n[It's like the end of Raiders but just speaker docks](https://res.cloudinary.com/mediocre/image/upload/v1421096961/kb7obf3sa8xogdfd79pe.png)\\r\\nInset image from [Mark Hunter](https://www.flickr.com/photos/toolstop/4324416999/in/photolist-7iXUqB-7BuwsX-aeEnqh-qi7nx-6FoGYE-asoxaf-7A8LvM-jstH2i-755hWp-e1XEh7-egSbzN-6WUiyW-7AcxJd-cHW6oC-exi1ye-oqL7X4-6WQeSc-7AcwLQ-4ANVCF-5pTjhV-5RFxUY-34G2Fg-7Acx2m-6ELvxb-ahwGjA-qxog7-5LPx3H-5Vax5w-6Gg7T-a1324v-8g7dx9-6Jf1WP-7HMWZx-9bXNwL-iUcMyQ-dzh49g-7menrJ-hraHtP-8VPJCQ-iUasxZ-iBnzGB-iPUZMW-8U6RMc-iUbPcR-fDNabi-9xuabs-aLP3sR-8Cg4YG-aiQSch-ajFyTG) and [Nicolas Raymond](https://www.flickr.com/photos/82955120@N05/7615277728/in/photolist-h6KXhq-cAWhKS-cMbmo1-nXQfES-ptxhzY-eUwBwg-cHq7ZN-517Ywg-ftJpe4-98Tb7S-87bFoD-pgBKBe-bGd9C6-aetuN3-8mRCab-m8roVT-cMbcfm-gShvg8-dwBkrf-4xGSH5-6n51AK-bajyhZ-pWrmDq-bJETGv-jwpA35-b8Fpd8-gShkB7-cqm2Q7-gRYvav-gRXMXR-dWKdq8-gS8iHg-gSimk8-bajxxV-oWxde4-dyowjc-98Q5yZ-pogu8V-fB9e4m-55nne4-7rA9Sg-pNmipM-bajzQx-8jtY2f-9Q4F59-c1hCRh-4cLjGF-oBScSL-4GFNQr-5aqqxB) used under Creative Commons License\\r\\n\\r\\nPrice Check\\r\\n====\\r\\n[$399.98 List, $149.98 for 2 at Amazon](http://www.amazon.com/JBL-Mini-Performance-Lightning-Black/dp/B00CO07FIO)\",\"story\":{\"title\":\"We've solved the sellout problem.\",\"body\":\"Morning people and East Coast residents, we just want to say: we don't specifically *try* to pick deals that'll sell out in the middle of the night. Honest. When we put [that Cobra car charger](https://meh.com/deals/cobra-joyride-smart-charger) on the schedule for this past Saturday, we fully expected to see ample quantities thick on the ground by Sunday brunch. We were as surprised as anyone that they were all gone in half an hour.\\r\\n\\r\\nWell, we never claimed to be geniuses, but certainly we can't be *that* (happily) wrong two days in a row. Especially when Sunday's deal was the [Kershaw Snow White knife](https://meh.com/deals/kershaw-shuffle-snow-white-knife), which is distinguished from other compact EDC knives only by its white handle. Plenty of those for the late risers - oh, hey, sold out in an hour and a half. \\r\\n\\r\\nOK, OK, so for Monday, we were like, what do we have a *huge* quantity of? Like, so many that it could never possibly sell out? Ooh, look, [150,000 Built NY laptop and tablet sleeves](https://meh.com/deals/built-ny-assorted-bundle). Not a chance those'll sell out. Not even if we sell them in quantities of 10, 20, or 30. No way, right?\\r\\n\\r\\nYes way. The bundles of 30 were gone in a couple of hours. They were all gone by the time people got to work in their NY buildings.\\r\\n\\r\\nSo it's time to hit this sellout syndrome with everything we've got. Time for the nuclear option. Time to do - quite literally - our worst.\\r\\n\\r\\nIt's time for speaker docks. Longtime fans will know [how we feel about speaker docks](https://meh.com/deals/jbl-micro-speaker-dock), especially non-Bluetooth ones like this. It's just stoopid to plonk yer phone down in one place to listen to it. The JBL OnBeat Mini might be the best one, with its rechargeable battery, semi-decent speaker, and its compatibility with the latest iPads. But we can't say \\\"it's the only speaker dock you should buy\\\", because you shouldn't buy *any* speaker docks.\\r\\n\\r\\nBut a lame product is only one half of the equation. We've also got buttload after buttload of them. Low demand plus near-bottomless supply equals no sellout. And if you suspect we're working some kind of reverse psychology to get you to think there might be a sellout so you better buy one... no. We're not that sophisticated. We seriously have so many of these, they won't sell out.\\r\\n\\r\\nSo belly up to the buffet, Eastern Time Zone! The food stinks but there's plenty of it.\"},\"theme\":{\"accentColor\":\"#ef840f\",\"backgroundColor\":\"#170605\",\"backgroundImage\":\"https://res.cloudinary.com/mediocre/image/upload/v1421092115/wq3m8v1lhsym88voidb6.jpg\",\"foreground\":\"light\"},\"url\":\"https://meh.com/deals/2-for-tuesday--jbl-onbeat-mini\",\"topic\":{\"commentCount\":99,\"createdAt\":\"2015-01-13T05:00:03.128Z\",\"id\":\"54b4a6536cb77efc0ba69061\",\"replyCount\":72,\"url\":\"https://meh.com/forum/topics/2-for-tuesday-jbl-onbeat-mini-with-lightning-connector\",\"voteCount\":2}},\"poll\":{\"answers\":[{\"id\":\"a6li0000000PBk3AAG-1\",\"text\":\"Yes! And I blame Meh!\",\"voteCount\":222},{\"id\":\"a6li0000000PBk3AAG-2\",\"text\":\"Yes! And I blame time zones!\",\"voteCount\":84},{\"id\":\"a6li0000000PBk3AAG-3\",\"text\":\"Yes! And I blame myself!\",\"voteCount\":373},{\"id\":\"a6li0000000PBk3AAG-4\",\"text\":\"No, I check at launch every night\",\"voteCount\":126},{\"id\":\"a6li0000000PBk3AAG-5\",\"text\":\"No, the stuff I want has never sold out\",\"voteCount\":8},{\"id\":\"a6li0000000PBk3AAG-6\",\"text\":\"No, I never want anything sold here\",\"voteCount\":51}],\"id\":\"a6li0000000PBk3AAG\",\"startDate\":\"2015-01-13T05:00:00.000Z\",\"title\":\"Have you ever wanted something on Meh that sold out before you could buy it?\",\"topic\":{\"commentCount\":15,\"createdAt\":\"2015-01-13T05:00:01.095Z\",\"id\":\"54b4a6516cb77efc0ba69060\",\"replyCount\":8,\"url\":\"https://meh.com/forum/topics/have-you-ever-wanted-something-on-meh-that-sold-out-before-you-could-buy-it\",\"voteCount\":0}},\"video\":{\"id\":\"a6li0000000PBk3AAG\",\"startDate\":\"2015-01-13T05:00:00.000Z\",\"title\":\"Hey Everybody, It's Glen - Sports Small Talk\",\"url\":\"https://www.youtube.com/watch?v=mqpmUqmr9fs\",\"topic\":{\"commentCount\":5,\"createdAt\":\"2015-01-13T05:00:05.659Z\",\"id\":\"54b4a6556cb77efc0ba69064\",\"replyCount\":3,\"url\":\"https://meh.com/forum/topics/hey-everybody-its-glen---sports-small-talk\",\"voteCount\":1}}}");

        final Gson gson = new Gson();
        for (String json : samples) {
            Meh sample = gson.fromJson(json, Meh.class);
            DateTime dateTime = DateTime.parse(sample.getDeal().getTopic().getCreatedAt());
            Instant instant = dateTime.withZone(Helper.TIME_ZONE).withTimeAtStartOfDay().toInstant();
            mehCache.put(instant, json, false);
        }

        mToday = DateTime.now(Helper.TIME_ZONE).withTimeAtStartOfDay().toInstant();
        Helper.log(Log.DEBUG, "today is " + mToday);
        final Meh meh = mehCache.get(mToday);
        if (meh != null) {
            Instant instant = mehCache.getInstant(meh);
            if (instant != null) {
                Helper.log(Log.DEBUG, "initialize in cache");
                loadFragment(DealFragment.newInstance(instant, meh));
                restoreActionBar();
            } else {
                loadFragment(PlaceholderFragment.newInstance(PlaceholderFragment.ARG_SECTION_ERROR));
            }
        } else {
            Helper.log(Log.DEBUG, "initialize not in cache");
            final String url = getString(R.string.api_url, getString(R.string.api_key));
            final Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    Meh meh = gson.fromJson(jsonObject.toString(), Meh.class);
                    Instant instant = mehCache.getInstant(meh);
                    if (instant != null) {
                        Helper.log(Log.DEBUG, "initialize not in cache pulled down, updating sidebar and frag");
                        mehCache.put(instant, jsonObject, true);
                        mNavigationDrawerFragment.updateList();
                        loadFragment(DealFragment.newInstance(instant, meh));
                    } else {
                        loadFragment(PlaceholderFragment.newInstance(PlaceholderFragment.ARG_SECTION_ERROR));
                    }
                }
            };
            Response.ErrorListener responseErrorListener = new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    loadFragment(PlaceholderFragment.newInstance(PlaceholderFragment.ARG_SECTION_ERROR));
                    Helper.log(Log.ERROR, "VolleyError", volleyError);
                }
            };
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, responseListener, responseErrorListener);
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
        }
    }

    public void restoreActionBar() {
        Helper.log(Log.DEBUG, "restoreActionBar");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(mColor));
        actionBar.setTitle(mTitle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mColorStatus);
            getWindow().setNavigationBarColor(mColorNav);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            //getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            //return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            if (mIsPaused) {
                mFragmentToLoad = fragment;
            } else {
                mFragmentToLoad = null;
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
            }
        }
    }

    public static class DealFragment extends Fragment {
        public static final String ARG_INSTANT = "deal_instant";
        private static Meh mMeh;
        private static Instant mDealDate;
        private ViewPager mPager;
        private ImagePagerAdapter mAdapter;

        public DealFragment() {
        }

        public static DealFragment newInstance(Instant instant, Meh mehObject) {
            mDealDate = instant;
            mMeh = mehObject;

            DealFragment fragment = new DealFragment();
            Bundle args = new Bundle();
            args.putLong(ARG_INSTANT, instant.getMillis());
            fragment.setArguments(args);

            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Helper.log(Log.DEBUG, "DealFragment.onCreateView");
            Helper.cacheImages(getActivity(), mMeh);

            View rootView = inflater.inflate(R.layout.fragment_deal, container, false);

            final Deal deal = mMeh.getDeal();
            Deal.Theme theme = deal.getTheme();
            Deal.Story story = deal.getStory();

            int accentColor = Color.parseColor(theme.getAccentColor());
            int foregroundColor = Helper.getForegroundColor(getActivity(), accentColor);
            int backgroundColor = Color.parseColor(theme.getBackgroundColor());
            int textColor = Helper.getForegroundColor(getActivity(), backgroundColor);
            int highlightColor = Helper.getHighlightColor(accentColor, theme.getForeground());

            // Set up ViewPager and backing adapter
            mAdapter = new ImagePagerAdapter(getActivity().getSupportFragmentManager(), mMeh.getDeal().getPhotos().length);
            mPager = (ViewPager) rootView.findViewById(R.id.pager);
            mPager.setAdapter(mAdapter);
            mPager.setPageMargin((int) getResources().getDimension(R.dimen.horizontal_page_margin));
            mPager.setOffscreenPageLimit(2);

            CirclePageIndicator circleIndicator = (CirclePageIndicator) rootView.findViewById(R.id.pager_indicator);
            circleIndicator.setViewPager(mPager);
            circleIndicator.setFillColor(accentColor);
            circleIndicator.setStrokeColor(textColor);

            Button price = (Button) rootView.findViewById(R.id.price);
            GradientDrawable pill = (GradientDrawable) price.getBackground();
            TextView title = (TextView) rootView.findViewById(R.id.title);
            TextView features = (TextView) rootView.findViewById(R.id.features);
            TextView storyTitle = (TextView) rootView.findViewById(R.id.story_title);
            TextView storyBody = (TextView) rootView.findViewById(R.id.story);
            TextView specifications = (TextView) rootView.findViewById(R.id.specifications);
            FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_meh);

            fab.setColorNormal(accentColor);
            fab.setColorPressed(highlightColor);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deal.getUrl()));
                    getActivity().startActivity(browserIntent);
                }
            });

            String prices = deal.getPrices(getActivity());
            boolean tooLate = mDealDate.isBefore(mToday);
            if (deal.getSoldOutAt() != null || tooLate) {
                price.setTextColor(getResources().getColor(R.color.primary_material_light));
                price.setText(getString(deal.getSoldOutAt() != null ? R.string.deal_price_soldout : R.string.deal_price_toolate, prices));
                price.setEnabled(false);
                pill.setColor(Helper.getForegroundColor(getActivity(), backgroundColor, R.color.primary_dark_material_dark, R.color.primary_dark_material_light));
            } else {
                GradientDrawable pillPressed = (GradientDrawable) pill.getConstantState().newDrawable().mutate();
                pill.setColor(accentColor);
                pillPressed.setColor(highlightColor);

                StateListDrawable states = new StateListDrawable();
                states.addState(new int[]{android.R.attr.state_pressed}, pillPressed);
                states.addState(StateSet.WILD_CARD, pill);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    price.setBackground(states);
                } else {
                    price.setBackgroundDrawable(states);
                }

                price.setTextColor(foregroundColor);
                price.setText(getString(R.string.deal_price_buy, prices));
                price.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deal.getUrl().concat("/checkout")));
                        getActivity().startActivity(browserIntent);
                    }
                });
            }

            title.setText(deal.getTitle());
            title.setTextColor(textColor);

            Bypass bypass = new Bypass(getActivity());
            CharSequence markDown = bypass.markdownToSpannable(deal.getFeatures());
            features.setText(markDown);
            features.setTextColor(textColor);

            if (story != null) {
                storyTitle.setText(deal.getStory().getTitle());
                storyTitle.setTextColor(accentColor);
                markDown = bypass.markdownToSpannable(deal.getStory().getBody());
                storyBody.setText(markDown);
                storyBody.setTextColor(textColor);
                storyBody.setMovementMethod(LinkMovementMethod.getInstance());
                storyBody.setLinkTextColor(accentColor);
            } else {
                storyTitle.setVisibility(View.GONE);
                storyBody.setVisibility(View.GONE);
            }

            markDown = bypass.markdownToSpannable(deal.getSpecifications());
            specifications.setText(markDown);
            specifications.setTextColor(textColor);
            specifications.setMovementMethod(LinkMovementMethod.getInstance());
            specifications.setLinkTextColor(accentColor);

            rootView.setBackgroundColor(backgroundColor);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.mTitle = mMeh.getDeal().getTitle();
            Deal.Theme theme = mMeh.getDeal().getTheme();
            mainActivity.mColor = Color.parseColor(theme.getAccentColor());
            mainActivity.mColorStatus = Helper.getHighlightColor(mainActivity.mColor, theme.getForeground());
            mainActivity.mColorNav = Helper.getHighlightColor(Color.parseColor(theme.getBackgroundColor()), theme.getForeground());
            Helper.log(Log.DEBUG, "onAttach " + mainActivity.mTitle + " " + mainActivity.mColor + " " + mainActivity.mColorStatus);
        }

        private class ImagePagerAdapter extends FragmentStatePagerAdapter {
            private final int mSize;

            public ImagePagerAdapter(FragmentManager fm, int size) {
                super(fm);
                mSize = size;
            }

            @Override
            public int getCount() {
                return mSize;
            }

            @Override
            public Fragment getItem(int position) {
                return ImageFragment.newInstance(mMeh.getDeal().getPhotos()[position]);
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        public static final int ARG_SECTION_ERROR = -1;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static int mSectionNumber = ARG_SECTION_ERROR;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int layoutId = R.layout.fragment_main;
            if (mSectionNumber < 0) {
                layoutId = R.layout.fragment_error;
            }
            View rootView = inflater.inflate(layoutId, container, false);
            Helper.log(Log.DEBUG, "PlaceholderFragmentonCreateView " + mSectionNumber);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.mTitle = getString(R.string.error_oops);
            mainActivity.mColor = getResources().getColor(R.color.primary_material_dark);
            mainActivity.mColorStatus = getResources().getColor(R.color.primary_dark_material_dark);
            mainActivity.mColorNav = getResources().getColor(R.color.primary_dark_material_dark);
        }
    }
}
