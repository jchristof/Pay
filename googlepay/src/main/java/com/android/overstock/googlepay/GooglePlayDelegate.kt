package com.android.overstock.googlepay

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import org.json.JSONException
import org.json.JSONObject

class GooglePlayDelegate(private val activity: Activity) {
    // Initialize a Google Pay API client for an environment suitable for testing.
    // It's recommended to create the PaymentsClient object inside of the onCreate method.
    private val paymentClient = GooglePayPaymentsUtil.createPaymentsClient(activity)

    //Determine the viewer's ability to pay with a payment method
    fun whenGooglePayIsReady(onGooglePayReady: () -> Unit) {
        val isReadyToPayJson = GooglePayPaymentsUtil.isReadyToPayRequest ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task = paymentClient.isReadyToPay(request)
        task.addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)?.let {
                    if(it) onGooglePayReady()
                }

            } catch (exception: ApiException) {
                // Process error
                Log.w("isReadyToPay failed", exception)
            }

        }
    }

    fun requestPayment(amount:Long){
        val price = GooglePayPaymentsUtil.microsToString(amount * GooglePayConstants.MICROS_PER_DOLLAR)

        // TransactionInfo transaction = GooglePayPaymentsUtil.createTransaction(price);
        val paymentDataRequestJson = GooglePayPaymentsUtil.getPaymentDataRequest(price) ?: return

        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        if (request != null) {
            AutoResolveHelper.resolveTask(
                    paymentClient.loadPaymentData(request), activity, LOAD_PAYMENT_DATA_REQUEST_CODE)
        }
    }

    fun onRequestGooglePayResult(resultCode:Int, data: Intent){
        when (resultCode) {
            Activity.RESULT_OK -> {
                val paymentData = PaymentData.getFromIntent(data)
                handlePaymentSuccess(paymentData!!)
            }
            Activity.RESULT_CANCELED -> {}
            AutoResolveHelper.RESULT_ERROR -> {
                AutoResolveHelper.getStatusFromIntent(data)?.let {status ->
                    Log.w("loadPaymentData failed", String.format("Error code: %d", status.statusCode))
                }

            }
        }
    }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see <a
     *     href="https://developers.google.com/pay/api/android/reference/object#PaymentData">Payment
     *     Data</a>
     */
    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson() ?: return

        // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
        val paymentMethodData: JSONObject

        try {
            paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".
            if (paymentMethodData
                            .getJSONObject("tokenizationData")
                            .getString("type") == "PAYMENT_GATEWAY" && paymentMethodData
                            .getJSONObject("tokenizationData")
                            .getString("token") == "examplePaymentMethodToken") {
                val alertDialog = AlertDialog.Builder(activity)
                        .setTitle("Warning")
                        .setMessage(
                                "Gateway name set to \"example\" - please modify " + "GooglePayConstants.java and replace it with your own gateway.")
                        .setPositiveButton("OK", null)
                        .create()
                alertDialog.show()
            }

            val billingName = paymentMethodData.getJSONObject("info").getJSONObject("billingAddress").getString("name")
            Log.d("BillingName", billingName)
            Toast.makeText(activity, "Payment success for  $billingName", Toast.LENGTH_LONG).show()

            // Logging token string.
            Log.d("GooglePaymentToken", paymentMethodData.getJSONObject("tokenizationData").getString("token"))
        } catch (e: JSONException) {
            Log.e("handlePaymentSuccess", "Error: " + e.toString())
            return
        }

    }
}