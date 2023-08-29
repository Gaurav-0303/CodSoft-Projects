package com.gb.currencyconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.gb.currencyconverter.databinding.ActivityMainBinding
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mpp: HashMap<String, Double> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardViewLast.visibility = View.GONE
        getApiData()
        binding.buttonRefresh.setOnClickListener {
            binding.cardViewLast.visibility = View.GONE
            binding.textViewResult.text = ""
            binding.editTextValue.text = null
            getApiData()
        }
    }

    private fun getApiData() {

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://data.fixer.io/api/latest?access_key=b609860cfc6325eb98f7f831a3d8d207")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Network error occurred",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                val responseBody = response.body?.string()

                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    // Parse the JSON response
                    val jsonObject = JSONObject(responseBody)
                    val ratesObject = jsonObject.getJSONObject("rates")

                    // Extract all currency names and their rates and store them in separate arrays
                    val currencyNames = ArrayList<String>()
                    val currencyRates = ArrayList<Double>()

                    // Base currency, which is always included in the rates
                    currencyNames.add("EUR")
                    currencyRates.add(1.0)

                    val iterator: Iterator<String> = ratesObject.keys()
                    while (iterator.hasNext()) {
                        val currency = iterator.next()
                        val rate = ratesObject.getDouble(currency)
                        currencyNames.add(currency)
                        currencyRates.add(rate)
                    }

                    // Use the exchange rate data as needed
                    runOnUiThread {

                        //store values in map
                        for (i in currencyNames.indices) {
                            mpp[currencyNames[i]] = currencyRates[i]
                        }

                        // Update UI or display the data
                        val namesAdapter = ArrayAdapter(this@MainActivity, R.layout.drop_down_item, currencyNames)
                        val ratesAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, currencyRates)
                        binding.spinnerFrom.adapter = namesAdapter
                        binding.spinnerTo.adapter = namesAdapter
                        binding.buttonSubmit.setOnClickListener {
                            if(binding.editTextValue.text.toString() == ""){
                                Toast.makeText(this@MainActivity, "Enter Value", Toast.LENGTH_LONG).show()
                            }
                            else{
                                convertCurrency()
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "API request failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    private fun convertCurrency(){

        binding.cardViewLast.visibility = View.VISIBLE

        val fromName = binding.spinnerFrom.selectedItem?.toString()
        val toName = binding.spinnerTo.selectedItem?.toString()
        val fromVal = mpp[fromName]
        val toVal = mpp[toName]
        var result: Double? = null

        val valueEnteredString = binding.editTextValue.text.toString()
        if(valueEnteredString == ""){
            Toast.makeText(this, "Please enter value", Toast.LENGTH_LONG).show()
        }
        else{
            val valueEntered = valueEnteredString.toDouble()

            result = (valueEntered * toVal!!) / fromVal!!
            val finalResult = String.format("%.3f", result)

            binding.textViewResult.text = "Result : $finalResult"
        }

        //card view updates👇
        binding.textViewFromBTC.text = "$fromName "
        binding.textViewToUSD.text = " $toName"
        binding.valueEnteredByUser.text = valueEnteredString
        binding.buttonMinus.setOnClickListener {
            var doubleValue = binding.valueEnteredByUser.text.toString().toDouble()
            doubleValue--
            binding.valueEnteredByUser.text = doubleValue.toString()
            val editableValue: Editable = SpannableStringBuilder.valueOf(doubleValue.toString())
            binding.editTextValue.text = editableValue
            convertCurrency()
        }
        binding.buttonPlus.setOnClickListener {
            var doubleValue = binding.valueEnteredByUser.text.toString().toDouble()
            doubleValue++
            binding.valueEnteredByUser.text = doubleValue.toString()
            val editableValue: Editable = SpannableStringBuilder.valueOf(doubleValue.toString())
            binding.editTextValue.text = editableValue
            convertCurrency()
        }
        binding.textViewGreyFrom.text = "$fromName "
        binding.textViewGreyTo.text = " $toName"
        if (result != null) {
            val doubleValue = result / binding.valueEnteredByUser.text.toString().toString().toDouble()
            val finalResult = String.format("%.3f", doubleValue)
            binding.textViewValueOfUSD.text = "$finalResult "
        }
    }

}
