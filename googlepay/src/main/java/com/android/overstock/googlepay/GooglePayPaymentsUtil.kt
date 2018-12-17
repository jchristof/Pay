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

import android.app.Activity

import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Contains helper static methods for dealing with the Payments API.
 *
 *
 * Many of the parameters used in the code are optional and are set here merely to call out their
 * existence. Please consult the documentation to learn more and feel free to remove ones not
 * relevant to your implementation.
 */
object GooglePayPaymentsUtil {
    private val MICROS = BigDecimal(1000000.0)

    /**
     * Create a Google Pay API base request object with properties used in all requests.
     *
     * @return Google Pay API base request object.
     * @throws JSONException
     */
    private val baseRequest: JSONObject
        @Throws(JSONException::class)
        get() = JSONObject().put("apiVersion", 2).put("apiVersionMinor", 0)

    /**
     * Gateway Integration: Identify your gateway and your app's gateway merchant identifier.
     *
     *
     * The Google Pay API response will return an encrypted payment method capable of being charged
     * by a supported gateway after payer authorization.
     *
     *
     * TODO: Check with your gateway on the parameters to pass and modify them in GooglePayConstants.java.
     *
     * @return Payment data tokenization for the CARD payment method.
     * @throws JSONException
     * @see [PaymentMethodTokenizationSpecification](https://developers.google.com/pay/api/android/reference/object.PaymentMethodTokenizationSpecification)
     */
    private val gatewayTokenizationSpecification: JSONObject
        @Throws(JSONException::class, RuntimeException::class)
        get() {
            if (GooglePayConstants.PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS.isEmpty()) {
                throw RuntimeException(
                        "Please edit the GooglePayConstants.java file to add gateway name and other parameters your " + "processor requires")
            }
            val tokenizationSpecification = JSONObject()

            tokenizationSpecification.put("type", "PAYMENT_GATEWAY")
            val parameters = JSONObject(GooglePayConstants.PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS)
            tokenizationSpecification.put("parameters", parameters)

            return tokenizationSpecification
        }

    /**
     * `DIRECT` Integration: Decrypt a response directly on your servers. This configuration has
     * additional data security requirements from Google and additional PCI DSS compliance complexity.
     *
     *
     * Please refer to the documentation for more information about `DIRECT` integration. The
     * type of integration you use depends on your payment processor.
     *
     * @return Payment data tokenization for the CARD payment method.
     * @throws JSONException
     * @see [PaymentMethodTokenizationSpecification](https://developers.google.com/pay/api/android/reference/object.PaymentMethodTokenizationSpecification)
     */
    private val directTokenizationSpecification: JSONObject
        @Throws(JSONException::class, RuntimeException::class)
        get() {
            if (GooglePayConstants.DIRECT_TOKENIZATION_PARAMETERS.isEmpty()
                    || GooglePayConstants.DIRECT_TOKENIZATION_PUBLIC_KEY.isEmpty()
                    || GooglePayConstants.DIRECT_TOKENIZATION_PUBLIC_KEY == null
                    || GooglePayConstants.DIRECT_TOKENIZATION_PUBLIC_KEY === "REPLACE_ME") {
                throw RuntimeException(
                        "Please edit the GooglePayConstants.java file to add protocol version & public key.")
            }
            val tokenizationSpecification = JSONObject()

            tokenizationSpecification.put("type", "DIRECT")
            val parameters = JSONObject(GooglePayConstants.DIRECT_TOKENIZATION_PARAMETERS)
            tokenizationSpecification.put("parameters", parameters)

            return tokenizationSpecification
        }

    /**
     * Card networks supported by your app and your gateway.
     *
     *
     * TODO: Confirm card networks supported by your app and gateway & update in GooglePayConstants.java.
     *
     * @return Allowed card networks
     * @see [CardParameters](https://developers.google.com/pay/api/android/reference/object.CardParameters)
     */
    private val allowedCardNetworks: JSONArray
        get() = JSONArray(GooglePayConstants.SUPPORTED_NETWORKS)

    /**
     * Card authentication methods supported by your app and your gateway.
     *
     *
     * TODO: Confirm your processor supports Android device tokens on your supported card networks
     * and make updates in GooglePayConstants.java.
     *
     * @return Allowed card authentication methods.
     * @see [CardParameters](https://developers.google.com/pay/api/android/reference/object.CardParameters)
     */
    private val allowedCardAuthMethods: JSONArray
        get() = JSONArray(GooglePayConstants.SUPPORTED_METHODS)

    /**
     * Describe your app's support for the CARD payment method.
     *
     *
     * The provided properties are applicable to both an IsReadyToPayRequest and a
     * PaymentDataRequest.
     *
     * @return A CARD PaymentMethod object describing accepted cards.
     * @throws JSONException
     * @see [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
     */
    private// Optionally, you can add billing address/phone number associated with a CARD payment method.
    val baseCardPaymentMethod: JSONObject
        @Throws(JSONException::class)
        get() {
            val cardPaymentMethod = JSONObject()
            cardPaymentMethod.put("type", "CARD")

            val parameters = JSONObject()
            parameters.put("allowedAuthMethods", allowedCardAuthMethods)
            parameters.put("allowedCardNetworks", allowedCardNetworks)
            parameters.put("billingAddressRequired", true)

            val billingAddressParameters = JSONObject()
            billingAddressParameters.put("format", "FULL")

            parameters.put("billingAddressParameters", billingAddressParameters)

            cardPaymentMethod.put("parameters", parameters)

            return cardPaymentMethod
        }

    /**
     * Describe the expected returned payment data for the CARD payment method
     *
     * @return A CARD PaymentMethod describing accepted cards and optional fields.
     * @throws JSONException
     * @see [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
     */
    private val cardPaymentMethod: JSONObject
        @Throws(JSONException::class)
        get() {
            val cardPaymentMethod = baseCardPaymentMethod
            //cardPaymentMethod.put("tokenizationSpecification", gatewayTokenizationSpecification)
            cardPaymentMethod.put("tokenizationSpecification", directTokenizationSpecification)
            return cardPaymentMethod
        }

    /**
     * An object describing accepted forms of payment by your app, used to determine a viewer's
     * readiness to pay.
     *
     * @return API version and payment methods supported by the app.
     * @see [IsReadyToPayRequest](https://developers.google.com/pay/api/android/reference/object.IsReadyToPayRequest)
     */
    @JvmStatic
    val isReadyToPayRequest: JSONObject?
        get() {
            try {
                val isReadyToPayRequest = baseRequest
                isReadyToPayRequest.put(
                        "allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod))

                return isReadyToPayRequest
            } catch (e: JSONException) {
                return null
            }

        }

    /**
     * Information about the merchant requesting payment information
     *
     * @return Information about the merchant.
     * @throws JSONException
     * @see [MerchantInfo](https://developers.google.com/pay/api/android/reference/object.MerchantInfo)
     */
    private val merchantInfo: JSONObject
        @Throws(JSONException::class)
        get() = JSONObject().put("merchantName", "OSTK")

    /**
     * Creates an instance of [PaymentsClient] for use in an [Activity] using the
     * environment and theme set in [GooglePayConstants].
     *
     * @param activity is the caller's activity.
     */
    @JvmStatic
    fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder().setEnvironment(GooglePayConstants.PAYMENTS_ENVIRONMENT).build()
        return Wallet.getPaymentsClient(activity, walletOptions)
    }

    /**
     * Provide Google Pay API with a payment amount, currency, and amount status.
     *
     * @return information about the requested payment.
     * @throws JSONException
     * @see [TransactionInfo](https://developers.google.com/pay/api/android/reference/object.TransactionInfo)
     */
    @Throws(JSONException::class)
    private fun getTransactionInfo(price: String): JSONObject {
        val transactionInfo = JSONObject()
        transactionInfo.put("totalPrice", price)
        transactionInfo.put("totalPriceStatus", "FINAL")
        transactionInfo.put("currencyCode", GooglePayConstants.CURRENCY_CODE)

        return transactionInfo
    }

    /**
     * An object describing information requested in a Google Pay payment sheet
     *
     * @return Payment data expected by your app.
     * @see [PaymentDataRequest](https://developers.google.com/pay/api/android/reference/object.PaymentDataRequest)
     */
    @JvmStatic
    fun getPaymentDataRequest(price: String): JSONObject? {
        try {
            val paymentDataRequest = GooglePayPaymentsUtil.baseRequest
            paymentDataRequest.put(
                    "allowedPaymentMethods", JSONArray().put(GooglePayPaymentsUtil.cardPaymentMethod))
            paymentDataRequest.put("transactionInfo", GooglePayPaymentsUtil.getTransactionInfo(price))
            paymentDataRequest.put("merchantInfo", GooglePayPaymentsUtil.merchantInfo)

            /* An optional shipping address requirement is a top-level property of the PaymentDataRequest
      JSON object. */
            paymentDataRequest.put("shippingAddressRequired", true)

            val shippingAddressParameters = JSONObject()
            shippingAddressParameters.put("phoneNumberRequired", false)

            val allowedCountryCodes = JSONArray(GooglePayConstants.SHIPPING_SUPPORTED_COUNTRIES)

            shippingAddressParameters.put("allowedCountryCodes", allowedCountryCodes)
            paymentDataRequest.put("shippingAddressParameters", shippingAddressParameters)
            return paymentDataRequest
        } catch (e: JSONException) {
            return null
        }

    }

    /**
     * Converts micros to a string format accepted by [GooglePayPaymentsUtil.getPaymentDataRequest].
     *
     * @param micros value of the price.
     */
    @JvmStatic
    fun microsToString(micros: Long): String {
        return BigDecimal(micros).divide(MICROS).setScale(2, RoundingMode.HALF_EVEN).toString()
    }
}
