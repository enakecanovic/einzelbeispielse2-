package com.example.einzelbeispielse2

import android.annotation.SuppressLint
import android.app.Activity
import android.nfc.Tag
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.einzelbeispielse2.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.TimerTask

@DelicateCoroutinesApi
class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    private lateinit var main_binding: ActivityMainBinding
    private lateinit var my_socket: Socket

    private lateinit var my_dataOutputStream: DataOutputStream
    private lateinit var my_buffer_reader: BufferedReader

    lateinit var input_matr: String
    private var response_msg : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        main_binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(main_binding.root)

        main_binding.sendButton.setOnClickListener {
            input_matr = main_binding.inputfieldMatr.text.toString()

            if (TextUtils.isEmpty(input_matr)) {
                main_binding.serveranswer.text = ""
                main_binding.result.text = ""
                main_binding.matrInputLayout.error = "Matriculation number is missing"
                return@setOnClickListener
            }
            //send matriculation number from here //

            main_binding.inputfieldMatr.error = null

            GlobalScope.launch {
                Dispatchers.IO
                getResFromTcpConnection()
            }
        }

        main_binding.calculate.setOnClickListener {
            if (TextUtils.isEmpty(input_matr)) {
                main_binding.serveranswer.text = ""
                main_binding.result.text = ""
                main_binding.matrInputLayout.error = "Matriculation number is missing"
                return@setOnClickListener
            }
            //For my task, execute the code
            val task = 6
            executeRequiredTask(task)
        } }

    //Calculate the cross sum of the student number and then display it as binary number

    @SuppressLint("SetTextI18n")
    private fun executeRequiredTask(task: Int){
        GlobalScope.launch (Dispatchers.Default){
            val inputInList = getNumberListFromInput(input_matr)
            val crossSum = inputInList.sum()
            displayNumberAsBinary(crossSum)
        }
        Log.d(TAG, "executeRequiredTask: $task: executed")
    }

    suspend fun getNumberListFromInput (input: String) : MutableList<Int>{
        val mutableListNumbers = mutableListOf<Int>()

        for (i in input.indices){
            mutableListNumbers.add(Character.toString(input.get(i)).toInt())
        }
        return mutableListNumbers
    }

    private fun displayNumberAsBinary(number: Int) {
        val binaryRepresentation = number.toString(2)
        GlobalScope.launch(Dispatchers.Main){
            main_binding.result.text = "Binary representation of the cross sum of $input_matr is $binaryRepresentation"
        }
    }

    private suspend fun getResFromTcpConnection(){
        try {
            my_socket = withContext(Dispatchers.IO){
                Socket("se2-submission.aau.at", 20080)
            }

            my_socket.use { socket ->
                my_dataOutputStream = DataOutputStream(socket.getOutputStream())
                my_buffer_reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                my_dataOutputStream.writeBytes(input_matr + '\n')

                response_msg = my_buffer_reader.readLine()

                Log.d(TAG, "Response is: ${response_msg.toString()}")

                withContext(Dispatchers.Main){
                    main_binding.serveranswer.text = response_msg
                }
            }
        }
        catch (ex: IOException){
            ex.stackTrace
        }
    }
}