/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.samples.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.overstock.googlepay.GooglePlayDelegate;
import com.android.overstock.googlepay.GooglePayPaymentsUtil;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static com.android.overstock.googlepay.GooglePayConstantsKt.LOAD_PAYMENT_DATA_REQUEST_CODE;

/**
 * Checkout implementation for the app
 */
public class CheckoutActivity extends Activity {

  private GooglePlayDelegate googlePlayDelegate;

  /**
   * A Google Pay payment button presented to the viewer for interaction.
   *
   * @see <a href="https://developers.google.com/pay/api/android/guides/brand-guidelines">Google Pay
   *     payment button brand guidelines</a>
   */
  private View mGooglePayButton;

  /**
   * Arbitrarily-picked constant integer you define to track a request for payment data activity.
   *
   * @value #LOAD_PAYMENT_DATA_REQUEST_CODE
   */
 // private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

  private TextView mGooglePayStatusText;

  private ItemInfo mBikeItem = new ItemInfo("Simple Bike", 300 * 1000000, R.drawable.bike);
  private long mShippingCost = 90 * 1000000;
  /**
   * Initialize the Google Pay API on creation of the activity
   *
   * @see Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_checkout);

    // Set up the mock information for our item in the UI.
    initItemUI();

    mGooglePayButton = findViewById(R.id.googlepay_button);
    mGooglePayStatusText = findViewById(R.id.googlepay_status);

    // Initialize a Google Pay API client for an environment suitable for testing.
    // It's recommended to create the PaymentsClient object inside of the onCreate method.
    googlePlayDelegate = new GooglePlayDelegate(this);
    googlePlayDelegate.whenGooglePayIsReady(new Function0<Unit>() {
      @Override
      public Unit invoke() {
        setGooglePayAvailable();
        return null;
      }
    });

    mGooglePayButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            requestPayment(view);
          }
        });
  }

  /**
   * If isReadyToPay returned {@code true}, show the button and hide the "checking" text. Otherwise,
   * notify the user that Google Pay is not available. Please adjust to fit in with your current
   * user flow. You are not required to explicitly let the user know if isReadyToPay returns {@code
   * false}.
   */
  private void setGooglePayAvailable() {
      mGooglePayStatusText.setVisibility(View.GONE);
      mGooglePayButton.setVisibility(View.VISIBLE);
  }

  /**
   * Handle a resolved activity from the Google Pay payment sheet.
   *
   * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
   * @param resultCode Result code returned by the Google Pay API.
   * @param data Intent from the Google Pay API containing payment or error data.
   * @see <a href="https://developer.android.com/training/basics/intents/result">Getting a result
   *     from an Activity</a>
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
        // value passed in AutoResolveHelper
      case LOAD_PAYMENT_DATA_REQUEST_CODE:
        googlePlayDelegate.onRequestGooglePayResult(resultCode, data);
        // Re-enables the Google Pay payment button.
        mGooglePayButton.setClickable(true);
        break;
    }
  }

  // This method is called when the Pay with Google button is clicked.
  public void requestPayment(View view) {
    // Disables the button to prevent multiple clicks.
    mGooglePayButton.setClickable(false);

    googlePlayDelegate.requestPayment(1);
  }

  private void initItemUI() {
    TextView itemName = findViewById(R.id.text_item_name);
    ImageView itemImage = findViewById(R.id.image_item_image);
    TextView itemPrice = findViewById(R.id.text_item_price);

    itemName.setText(mBikeItem.getName());
    itemImage.setImageResource(mBikeItem.getImageResourceId());
    itemPrice.setText(GooglePayPaymentsUtil.microsToString(mBikeItem.getPriceMicros()));
  }
}
