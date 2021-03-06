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

package com.android.overstock.googlepay

import com.google.android.gms.wallet.WalletConstants

import java.util.Arrays
import java.util.HashMap

/**
 * This file contains several constants you must edit before proceeding.
 * Please take a look at GooglePayPaymentsUtil.java to see where the constants are used and to potentially
 * remove ones not relevant to your integration.
 *
 *
 * Required changes:
 *
 *  1.  Update SUPPORTED_NETWORKS and SUPPORTED_METHODS if required (consult your processor if
 * unsure)
 *  1.  Update CURRENCY_CODE to the currency you use.
 *  1.  Update SHIPPING_SUPPORTED_COUNTRIES to list the countries where you currently ship. If this
 * is not applicable to your app, remove the relevant bits from GooglePayPaymentsUtil.java.
 *  1.  If you're integrating with your `PAYMENT_GATEWAY`, update
 * PAYMENT_GATEWAY_TOKENIZATION_NAME and PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS per the
 * instructions they provided. You don't need to update DIRECT_TOKENIZATION_PUBLIC_KEY.
 *  1.  If you're using `DIRECT` integration, please edit protocol version and public key as
 * per the instructions.
 */

//result code for initiating a google pay payment
const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991

object GooglePayConstants {

    //Google Pay conversion factor
    val MICROS_PER_DOLLAR = 1000000;
    /**
     * Changing this to ENVIRONMENT_PRODUCTION will make the API return chargeable card information.
     * Please refer to the documentation to read about the required steps needed to enable
     * ENVIRONMENT_PRODUCTION.
     *
     * @value #PAYMENTS_ENVIRONMENT
     */

    val PAYMENTS_ENVIRONMENT = if(BuildConfig.DEBUG) WalletConstants.ENVIRONMENT_TEST else WalletConstants.ENVIRONMENT_PRODUCTION

    /**
     * The allowed networks to be requested from the API. If the user has cards from networks not
     * specified here in their account, these will not be offered for them to choose in the popup.
     *
     * @value #SUPPORTED_NETWORKS
     */
    val SUPPORTED_NETWORKS = Arrays.asList(
            "AMEX",
            "DISCOVER",
            //"JCB",
            "MASTERCARD",
            "VISA")

    /**
     * The Google Pay API may return cards on file on Google.com (PAN_ONLY) and/or a device token on
     * an Android device authenticated with a 3-D Secure cryptogram (CRYPTOGRAM_3DS).
     *
     * @value #SUPPORTED_METHODS
     */
    val SUPPORTED_METHODS = Arrays.asList(
            "PAN_ONLY",
            "CRYPTOGRAM_3DS")

    /**
     * Required by the API, but not visible to the user.
     *
     * @value #CURRENCY_CODE Your local currency
     */
    val CURRENCY_CODE = "USD"

    /**
     * Supported countries for shipping (use ISO 3166-1 alpha-2 country codes). Relevant only when
     * requesting a shipping address.
     *
     * @value #SHIPPING_SUPPORTED_COUNTRIES
     */
    val SHIPPING_SUPPORTED_COUNTRIES = Arrays.asList("US")

    /**
     * The name of your payment processor/gateway. Please refer to their documentation for more
     * information.
     *
     * @value #PAYMENT_GATEWAY_TOKENIZATION_NAME
     */
    val PAYMENT_GATEWAY_TOKENIZATION_NAME = "example"

    /**
     * Custom parameters required by the processor/gateway.
     * In many cases, your processor / gateway will only require a gatewayMerchantId.
     * Please refer to your processor's documentation for more information. The number of parameters
     * required and their names vary depending on the processor.
     *
     * @value #PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS
     */
    val PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS: HashMap<String, String> = object : HashMap<String, String>() {
        init {
            put("gateway", PAYMENT_GATEWAY_TOKENIZATION_NAME)
            put("gatewayMerchantId", "exampleGatewayMerchantId")
            // Your processor may require additional parameters.
        }
    }

    /**
     * Only used for `DIRECT` tokenization. Can be removed when using `PAYMENT_GATEWAY`
     * tokenization.
     *
     * @value #DIRECT_TOKENIZATION_PUBLIC_KEY
     */
    val DIRECT_TOKENIZATION_PUBLIC_KEY = "BDd7hKPmIavM8arhHZ4/kj996VK/4fIYHBPTuljZSB1lhvJS5SrmVHILqUaEnvMYdKTliL0Ajow8bEcgck/WnYM="

    /**
     * Parameters required for `DIRECT` tokenization.
     * Only used for `DIRECT` tokenization. Can be removed when using `PAYMENT_GATEWAY`
     * tokenization.
     *
     * @value #DIRECT_TOKENIZATION_PARAMETERS
     */
    val DIRECT_TOKENIZATION_PARAMETERS: HashMap<String, String> = object : HashMap<String, String>() {
        init {
            put("protocolVersion", "ECv1")
            put("publicKey", DIRECT_TOKENIZATION_PUBLIC_KEY)
        }
    }
}
