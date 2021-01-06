package com.androdevlinux.bitk

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androdevlinux.bitk.databinding.ActivityMainBinding
import com.msgilligan.bitcoinj.rpc.BitcoinClient
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Sha256Hash
import java.net.URI
import java.util.logging.Logger

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val logger = Logger.getLogger(this.javaClass.name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.successBtn.setOnClickListener {
            val rpcUserName = binding.edtRpcUsername.text.toString()
            val rpcPassword = binding.edtRpcPassword.text.toString()
            val port = binding.edtRpcPort.text.toString()
            val ipAddress = binding.edtIpAddress.text.toString()
            val transactionRawHex = binding.edtRawTxHex.text.toString()
            logger.info("$ipAddress:$port/")

            if (rpcUserName.isEmpty() || rpcPassword.isEmpty() || port.isEmpty() || ipAddress.isEmpty() || transactionRawHex.isEmpty()) {
                Toast.makeText(this@MainActivity, "Empty value", Toast.LENGTH_SHORT).show()
            } else {
                val client = BitcoinClient(
                    NetworkParameters.fromID(NetworkParameters.PAYMENT_PROTOCOL_ID_TESTNET),
                    URI("http://$ipAddress:$port/"),
                    rpcUserName,
                    rpcPassword
                )
                Single.fromCallable { client.sendRawTransaction(transactionRawHex) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<Sha256Hash> {
                        override fun onSuccess(response: Sha256Hash) {
                            logger.info("response $response")
                            binding.responseArea.text = response.toString()
                        }

                        override fun onSubscribe(d: Disposable) {
                            logger.info("onSubscribe")
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            binding.responseArea.text = e.message
                            logger.info("Error:  ${e.message}")
                        }
                    })
            }
        }
    }
}