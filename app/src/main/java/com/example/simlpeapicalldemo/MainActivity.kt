package com.example.simlpeapicalldemo

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //execute the task last thing
        CallAPILoginAsyncTask("karim","123456").execute()
    }
    //  for post request we should give parameter for CallAPILoginAsyncTask class
    private inner class CallAPILoginAsyncTask(val username:String, val password:String): AsyncTask<Any, Void, String>() {

        private lateinit var customProgressDialog:Dialog

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog()
            //display the result in logcat
            if (result != null) {
                Log.i("Json Response result",result)
            }
            //create json object
            val jsonObject=JSONObject(result)
            //now this json object will understand the structure of it
            //so now we can access element from this json object
            //this "value" is a key from json
            val message = jsonObject.optString("id")
            Log.i("id",message)

            //get a json inside the main json
            //this is the main
            val popup=jsonObject.optJSONObject("popup")
            // this is the second
            val menuitem=popup.optString("menuitem")
            Log.i("menuitem",menuitem)
            //accuses the list in json
            val dataList=jsonObject.optJSONArray("menuitem")
            Log.i("Data list size","${dataList.length()}")
            //to know everything value from list so we use for loop
            for (item in 0 until dataList.length()){
                Log.i("items","${dataList[item]}")
                // create object for every item
               val dataItemObject:JSONObject=dataList[item] as JSONObject
                //to access an item from one item { "id"}
                val id = dataItemObject.optInt("id")
                Log.i("ID","$id")
                val value = dataItemObject.optString("value")
                Log.i("value","$value")
            }

        }
        // method from Async
        override fun doInBackground(vararg params: Any?): String {
           var result:String

           //make a connection
           var connection:HttpURLConnection?=null

            try{
                val url=URL("https://run.mocky.io/v3/76131125-5e9b-4e67-9aa4-b30b6d78883f")
                //make connection with the link(website)
                connection=url.openConnection() as HttpURLConnection
                //get data
                connection.doInput=true
                //send data
                connection.doOutput=true

                //post requests
                connection.instanceFollowRedirects=false
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type","application/json")
                connection.setRequestProperty("charset","utf-8")
                connection.setRequestProperty("Accept","application/json")

                connection.useCaches=false

                val writeDataOutputStream=DataOutputStream(connection.outputStream)
                val jsonRequest=JSONObject()
                jsonRequest.put("username",username)
                jsonRequest.put("password",password)
                //write the json
                writeDataOutputStream.writeBytes(jsonRequest.toString())
                writeDataOutputStream.flush()
                writeDataOutputStream.close()
                //end of post request

                //receive data
                val httpResult:Int=connection.responseCode
                //check if the response code is 200
                if (httpResult==HttpURLConnection.HTTP_OK){
                    //read data from website
                    val inputStream=connection.inputStream
                    //create reader
                    val reader=BufferedReader(InputStreamReader(inputStream))
                    //now by the reader we can read every single line

                    //creating string builder
                    val stringBuilder=StringBuilder()
                    //create line to go to every line
                    var line:String?
                    try{
                        //check if reader still has something to read
                        while (reader.readLine().also { line=it }!=null){
                            stringBuilder.append(line+"\n")
                        }
                    }catch (e:IOException){
                        e.printStackTrace()
                    }finally {
                        //close the inputStream
                        try {
                            inputStream.close()
                        }catch (e:IOException){
                            e.printStackTrace()
                        }
                    }
                    result=stringBuilder.toString()
                    //if the connection was not okay
                }else{
                    //get the response method
                    result=connection.responseMessage
                }
            }catch (e:SocketTimeoutException){
                    result="Connection Timeout"
            }//general exception
            catch(e:Exception){
                result="Error: "+e.message
            }finally {
                //disconnect the connection
                connection?.disconnect()
            }
            //return string(result)
            return result
        }

        private fun showProgressDialog(){
            customProgressDialog= Dialog(this@MainActivity)
            customProgressDialog.setContentView(R.layout.dialog_custom_progress)
            customProgressDialog.show()
        }
        private fun cancelProgressDialog(){
            customProgressDialog.dismiss()
        }
    }
}